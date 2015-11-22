package edu.jhu.bdslss.baft;

import org.canova.api.records.reader.RecordReader;
import org.canova.api.records.reader.impl.CSVRecordReader;
import org.canova.api.split.FileSplit;
import org.deeplearning4j.datasets.canova.RecordReaderDataSetIterator;
import org.deeplearning4j.datasets.iterator.DataSetIterator;
import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.RBM;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.api.IterationListener;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Aileme on 11/13/15.
 */
public class BptiExample {
    private static Logger log = LoggerFactory.getLogger(BptiExample.class);

    public static void main(String[] args) throws Exception {

        Nd4j.ENFORCE_NUMERICAL_STABILITY = true;
        final int numFeat = 970;
        int outputNum = 5;
        //int numSamples =1000;
        //int batchSize = 20;
        int iterations = 1000;
        int seed = 123;
        int listenerFreq = iterations/5;
        //int splitTrainNum = (int) (batchSize*.8);
        //DataSet mnist;
        SplitTestAndTrain trainTest;
        //DataSet trainInput;
        //List<INDArray> testInput = new ArrayList<>();
        //List<INDArray> testLabels = new ArrayList<>();

        //Load data..
        RecordReader reader = new CSVRecordReader(0, ",");
        reader.initialize(new FileSplit(new ClassPathResource("avg_features20.txt").getFile()));

        log.info("Build model....");
        /*MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                //.constrainGradientToUnitNorm(true)
                .iterations(iterations)
                //.momentum(0.5)
                //.momentumAfter(Collections.singletonMap(3, 0.9))
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .list(2)
                .layer(0, new RBM.Builder().nIn(numFeat).nOut(100).lossFunction(LossFunctions.LossFunction.RMSE_XENT).build())
                //.layer(1, new RBM.Builder().nIn(100).nOut(50).lossFunction(LossFunctions.LossFunction.RMSE_XENT).build())
                //.layer(2, new RBM.Builder().nIn(50).nOut(25).lossFunction(LossFunctions.LossFunction.RMSE_XENT).build())
                .layer(1, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD).activation("softmax")
                        .nIn(100).nOut(outputNum).build())
                .pretrain(true).backprop(true)
                .build(); */
        String activ = "tanh";
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .iterations(iterations)
                .constrainGradientToUnitNorm(true).useDropConnect(true)
                //.learningRate(1e-1)
                .l1(0.3).regularization(true).l2(1e-3)
                .constrainGradientToUnitNorm(true).optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .list(3)
                .layer(0, new DenseLayer.Builder().nIn(numFeat).nOut(300)
                        .activation(activ).dropOut(0.5)
                        .weightInit(WeightInit.XAVIER)
                        .build())
                .layer(1, new DenseLayer.Builder().nIn(300).nOut(200)
                        .activation(activ)
                        .weightInit(WeightInit.XAVIER)
                        .build())
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .weightInit(WeightInit.XAVIER)
                        .activation("softmax")
                        .nIn(200).nOut(outputNum).build())
                .backprop(true).pretrain(false)
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(Arrays.asList((IterationListener) new ScoreIterationListener(listenerFreq)));

        DataSetIterator iter = new RecordReaderDataSetIterator(reader, 20,970,5);
        DataSet next = iter.next();
        next.normalizeZeroMeanZeroUnitVariance();
        next.shuffle();
        //log.info("Num of examples: " + String.valueOf(next.numExamples()));
        trainTest = next.splitTestAndTrain(0.7);

        //Train
        log.info("Train model....");
        //System.out.println(trainTest.getTrain().getFeatureMatrix().shape()[0]);
        model.fit(trainTest.getTrain());

        log.info("Evaluate model....");
        Evaluation eval = new Evaluation(outputNum);

        log.info("Predictions---");
        INDArray predict2 = model.output(trainTest.getTest().getFeatureMatrix());
        /*
        int[] shape = trainTest.getTest().getLabels().shape();
        //System.out.println(System.out.format("%d,%d", shape[0], shape[1]));
        log.info(predict2.toString());
        log.info("Labels---");
        //shape = trainTest.getTest().getLabels().shape();
        //log.info(System.out.format("%d,%d", shape[0], shape[1]));
        INDArray maxElements = predict2.max(1);
        //log.info(maxElements.toString());
        //log.info(maxElements.getScalar(0).toString());
        //log.info(predict2.getScalar(0, 0).toString());
        INDArray labels = Nd4j.create(new float[shape[0]*shape[1]], new int[]{shape[0], shape[1]}) ;
        for(int i=0;i<shape[0];i++){
            for(int j=0;j<shape[1];j++){
                if(maxElements.getFloat(i) == predict2.getFloat(i, j)) {
                    //log.info("I'm inside the if!!!");
                    labels.put(i, j, 1);
                }
            }
        }
        //log.info(labels.toString());
        //log.info(trainTest.getTest().getLabels().toString());
        //log.info(trainTest.getTrain().getLabels().toString());
        */
        eval.eval(trainTest.getTest().getLabels(), predict2);

        log.info(eval.stats());
        log.info("****************Example finished********************");
    }

}
