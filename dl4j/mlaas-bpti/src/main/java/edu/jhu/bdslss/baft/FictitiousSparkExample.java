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
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
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

/**
 * Created by bimangujral on 11/30/15.
 */
public class FictitiousSparkExample {
    private static Logger log = LoggerFactory.getLogger(BptiSparkExample.class);

    public static void main(String[] args) throws Exception {

        // set to test mode
        SparkConf sparkConf = new SparkConf().set(SparkDl4jMultiLayer.AVERAGE_EACH_ITERATION, "false")
                .setAppName("sparktest");

        Nd4j.ENFORCE_NUMERICAL_STABILITY = true;
        final int numFeat = 2;
        int outputNum = 2;
        int numSamples = 12;
        int batchSize = 3;
        int iterations = 100 ; //1000;
        int seed = 123;
        int listenerFreq = iterations/10;
        SplitTestAndTrain trainTest;

        //Load data..
        RecordReader reader = new CSVRecordReader(0, ",");

        reader.initialize(new FileSplit(new File("/local/bdslss15-baft/resources/fictitious.txt")));

        //log.info("Build model....");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);
        String activation = "tanh";
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed).batchSize(batchSize) // Seed to lock in weight initialization for tuning
                .iterations(iterations) // # training iterations predict/classify & backprop
                .learningRate(1e-1f) // Optimization step size
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT) // Backprop method (calculate the gradients)
                .constrainGradientToUnitNorm(false).l2(2e-4).regularization(false)
                .list(3) // # NN layers (does not count input layer)
                /*.seed(seed).batchSize(batchSize)
                .iterations(iterations)
                .constrainGradientToUnitNorm(true).useDropConnect(true)
                .learningRate(1e-1)
                //.l1(0.3).regularization(false).l2(1e-3)
                .constrainGradientToUnitNorm(true).optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .list(3)
                .layer(0, new DenseLayer.Builder().nIn(numFeat).nOut(9)
                        .activation(activation).dropOut(0.5)
                        .weightInit(WeightInit.XAVIER)
                        .build())
                .layer(1, new DenseLayer.Builder().nIn(9).nOut(7)
                        .activation(activation).dropOut(0.5)
                        .weightInit(WeightInit.XAVIER)
                        .build())
                .layer(2, new DenseLayer.Builder().nIn(7).nOut(5)
                        .activation(activation).dropOut(0.5)
                        .weightInit(WeightInit.XAVIER)
                        .build())
                .layer(3, new DenseLayer.Builder().nIn(5).nOut(4)
                        .activation(activation)
                        .weightInit(WeightInit.XAVIER)
                        .build())
                .layer(4, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .weightInit(WeightInit.XAVIER)
                        .activation("softmax")
                        .nIn(4).nOut(outputNum).build())
                .backprop(true).pretrain(false)
                .build();*/
                .layer(0, new DenseLayer.Builder()
                        .nIn(numFeat) // # input nodes
                        .nOut(4) // # output nodes
                        .activation(activation)
                        .weightInit(WeightInit.XAVIER)
                        .build()
                ).layer(1, new DenseLayer.Builder()
                        .nIn(4) // # input nodes
                        .nOut(4) // # output nodes
                        .activation(activation)
                        .weightInit(WeightInit.XAVIER)
                        .build()
                )
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nIn(4) // # input nodes
                        .nOut(outputNum) // # output nodes
                        .activation("softmax")
                        .weightInit(WeightInit.XAVIER)
                        .build()
                )
                .backprop(true).pretrain(false).build();

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(Arrays.asList((IterationListener) new ScoreIterationListener(listenerFreq)));

        System.out.println("Initializing network");

        DataSetIterator iter = new RecordReaderDataSetIterator(reader, numSamples,numFeat,outputNum);
        DataSet next = iter.next();
        //next.normalizeZeroMeanZeroUnitVariance();
        next.shuffle();
        //log.info("Num of examples: " + String.valueOf(next.numExamples()));
        trainTest = next.splitTestAndTrain(0.8);

        SparkDl4jMultiLayer master = new SparkDl4jMultiLayer(sc,conf);
        //number of partitions should be partitioned by batch size
        //JavaRDD<String> lines = sc.textFile("/Users/Aileme/git/x-bpti/src/main/resources/avg_features20.txt",60000 / conf.getConf(0).getBatchSize());
        JavaRDD<DataSet> data = sc.parallelize(trainTest.getTrain().asList(), numSamples / conf.getConf(0).getBatchSize());

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