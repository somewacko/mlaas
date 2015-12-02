package edu.jhu.bdslss.baft;

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
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.util.Arrays;

/**
 * Created by Aileme on 11/22/15.
 */
public class FictitiousExample {
    private static Logger log = LoggerFactory.getLogger(FictitiousExample.class);

    public static void main(String[] args) throws Exception {

        Nd4j.ENFORCE_NUMERICAL_STABILITY = true;
        final int numFeat = 2;
        int outputNum = 2;
        //int numSamples = 1000;
        //int batchSize = 20;
        int iterations = 100 ; //1000;
        int seed = 123;
        int listenerFreq = iterations/10;
        //int splitTrainNum = (int) (batchSize*.8);
        //DataSet mnist;
        SplitTestAndTrain trainTest;
        //DataSet trainInput;
        //List<INDArray> testInput = new ArrayList<>();
        //List<INDArray> testLabels = new ArrayList<>();

        //Load data..
        RecordReader reader = new CSVRecordReader(0, ",");
        reader.initialize(new FileSplit(new ClassPathResource("fictitious.txt").getFile()));

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
        String activation = "tanh";
        /*MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .iterations(iterations)
                .constrainGradientToUnitNorm(true).useDropConnect(true)
                .learningRate(1e-1)
                .l1(0.3).regularization(true).l2(1e-3)
                .constrainGradientToUnitNorm(true)
                .list(3)
                .layer(0, new DenseLayer.Builder().nIn(numFeat).nOut(4)
                        .activation(activation)//.dropOut(0.5)
                        .weightInit(WeightInit.XAVIER)
                        .build())
                .layer(1, new DenseLayer.Builder().nIn(4).nOut(4)
                        .activation(activation)
                        .weightInit(WeightInit.XAVIER)
                        .build())
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .weightInit(WeightInit.XAVIER)
                        .activation("softmax")
                        .nIn(4).nOut(outputNum).build())
                .backprop(true).pretrain(false)
                .build();*/
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed) // Seed to lock in weight initialization for tuning
                .iterations(iterations) // # training iterations predict/classify & backprop
                .learningRate(1e-1f) // Optimization step size
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT) // Backprop method (calculate the gradients)
                .constrainGradientToUnitNorm(false).l2(2e-4).regularization(false)
                .list(3) // # NN layers (does not count input layer)
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

        DataSetIterator iter = new RecordReaderDataSetIterator(reader, null, 12,numFeat,outputNum, false);
        DataSet next = iter.next();
        next.normalizeZeroMeanZeroUnitVariance();
        next.shuffle();
        trainTest = next.splitTestAndTrain(0.7);

        //Train
        log.info("Train model....");
        //System.out.println(trainTest.getTrain().getFeatureMatrix().shape()[0]);
        model.fit(trainTest.getTrain());

        log.info("Evaluate model....");
        Evaluation eval = new Evaluation(outputNum);

        log.info("Predictions---");
        INDArray predict2 = model.output(trainTest.getTest().getFeatureMatrix());
        log.info(predict2.toString());
        eval.eval(trainTest.getTest().getLabels(), predict2);

        log.info(eval.stats());
        log.info("****************Example finished********************");
    }

}
