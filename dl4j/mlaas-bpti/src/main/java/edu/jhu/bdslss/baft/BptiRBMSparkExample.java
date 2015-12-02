package edu.jhu.bdslss.baft;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.canova.api.records.reader.RecordReader;
import org.canova.api.records.reader.impl.CSVRecordReader;
import org.canova.api.split.FileSplit;
import org.deeplearning4j.datasets.canova.RecordReaderDataSetIterator;
import org.deeplearning4j.datasets.iterator.DataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.RBM;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.api.IterationListener;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.spark.impl.multilayer.SparkDl4jMultiLayer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by Aileme on 12/2/15.
 */
public class BptiRBMSparkExample {
    private static Logger log = LoggerFactory.getLogger(BptiSparkExample.class);

    public static void main(String[] args) throws Exception {

        // set to test mode
        SparkConf sparkConf = new SparkConf().set(SparkDl4jMultiLayer.AVERAGE_EACH_ITERATION, "false")
                .setAppName("sparktest");

        Nd4j.ENFORCE_NUMERICAL_STABILITY = true;
        final int numFeat = 15; //970;
        int outputNum = 5;
        int numSamples = 4000;
        int iterations = 40;
        int seed = 123;
        int listenerFreq = iterations/25;
        int batchSize = 10; //10;
        SplitTestAndTrain trainTest;

        //Load data..
        RecordReader reader = new CSVRecordReader(0, ",");

        reader.initialize(new FileSplit(new File("/local/bdslss15-baft/resources/pca_features4000.txt")));

        //log.info("Build model....");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);
        String activation = "tanh";
        RBM.VisibleUnit vu = RBM.VisibleUnit.GAUSSIAN;
        RBM.HiddenUnit hu = RBM.HiddenUnit.RECTIFIED;
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed).learningRate(1e-6f).batchSize(batchSize)
                .constrainGradientToUnitNorm(true)
                .iterations(iterations)
                .momentum(0.5)
                .momentumAfter(Collections.singletonMap(3, 0.9))
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .list(4)
                .layer(0, new RBM.Builder(RBM.HiddenUnit.RECTIFIED, RBM.VisibleUnit.GAUSSIAN)
                        .nIn(numFeat) // # input nodes
                        .nOut(100) // # fully connected hidden layer nodes. Add list if multiple layers.
                        .weightInit(WeightInit.XAVIER) // Weight initialization
                        .k(1) // # contrastive divergence iterations
                        .activation(activation) // Activation function type
                        .lossFunction(LossFunctions.LossFunction.RMSE_XENT) // Loss function type
                        .updater(Updater.ADAGRAD)
                        .dropOut(0.5)
                        .build())
                .layer(1, new RBM.Builder(RBM.HiddenUnit.RECTIFIED, RBM.VisibleUnit.GAUSSIAN)
                        .nIn(100) // # input nodes
                        .nOut(50) // # fully connected hidden layer nodes. Add list if multiple layers.
                        .weightInit(WeightInit.XAVIER) // Weight initialization
                        .k(1) // # contrastive divergence iterations
                        .activation(activation) // Activation function type
                        .lossFunction(LossFunctions.LossFunction.RMSE_XENT) // Loss function type
                        .updater(Updater.ADAGRAD)
                        .dropOut(0.5)
                        .build())
                .layer(2, new RBM.Builder(RBM.HiddenUnit.RECTIFIED, RBM.VisibleUnit.GAUSSIAN)
                        .nIn(50) // # input nodes
                        .nOut(20) // # fully connected hidden layer nodes. Add list if multiple layers.
                        .weightInit(WeightInit.XAVIER) // Weight initialization
                        .k(1) // # contrastive divergence iterations
                        .activation(activation) // Activation function type
                        .lossFunction(LossFunctions.LossFunction.RMSE_XENT) // Loss function type
                        .updater(Updater.ADAGRAD)
                        .dropOut(0.5)
                        .build())
                .layer(3, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD).activation("softmax")
                        .nIn(20).nOut(outputNum).build())
                .pretrain(true)
                .backprop(true)
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(Arrays.asList((IterationListener) new ScoreIterationListener(listenerFreq)));

        System.out.println("Initializing network");

        DataSetIterator iter = new RecordReaderDataSetIterator(reader, numSamples,numFeat,outputNum);
        DataSet next = iter.next();
        //next.normalizeZeroMeanZeroUnitVariance();
        next.shuffle();
        //log.info("Num of examples: " + String.valueOf(next.numExamples()));
        double split = 0.8;
        trainTest = next.splitTestAndTrain(split);

        SparkDl4jMultiLayer master = new SparkDl4jMultiLayer(sc,conf);
        //number of partitions should be partitioned by batch size
        //JavaRDD<String> lines = sc.textFile("/Users/Aileme/git/x-bpti/src/main/resources/avg_features20.txt",60000 / conf.getConf(0).getBatchSize());
        JavaRDD<DataSet> data = sc.parallelize(trainTest.getTrain().asList(), trainTest.getTrain().numExamples() / conf.getConf(0).getBatchSize());

        //Train
        log.info("Train model....");
        MultiLayerNetwork network2 = master.fitDataSet(data);
        FileOutputStream fos  = new FileOutputStream("params.txt");
        DataOutputStream dos = new DataOutputStream(fos);
        Nd4j.write(dos, network2.params());
        dos.flush();
        dos.close();

        log.info("Evaluate model....");
        Evaluation eval = new Evaluation(outputNum);

        log.info("Predictions---");
        INDArray predict2 = network2.output(trainTest.getTest().getFeatureMatrix());

        eval.eval(trainTest.getTest().getLabels(), predict2);

        System.out.println(eval.stats());
        System.out.println("****************Example finished********************");
    }
}
