package edu.jhu.bdslss.baft;

import org.apache.commons.io.FileUtils;
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

import java.io.DataOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

/**
 * Created by Aileme on 11/13/15.
 */
public class BptiExample {
    private static Logger log = LoggerFactory.getLogger(BptiExample.class);
    static public LinkedList<Option> options = new LinkedList<Option>();

    public static void main(String[] args) throws Exception {
        // To run:
        // BptiExample -input_file [input file] -output_model_conf_file [output conf file]
        //              -output_model_weights_file [output weights file -output_stats_file [stats file]
        // Parse the command line.
        /*String[] mandatory_args = { "input_file", "output_model_conf_file",
                "output_model_weights_file", "output_stats_file"};
        createCommandLineOptions();
        CommandLineUtilities.initCommandLineParameters(args, BptiExample.options, mandatory_args);

        String inputFile = CommandLineUtilities.getOptionValue("input_file");
        String outputModelConf = CommandLineUtilities.getOptionValue("output_model_conf_file");
        String outputModelWeights = CommandLineUtilities.getOptionValue("output_model_weights_file");
        String outputStats = CommandLineUtilities.getOptionValue("output_stats_file");
*/
        Nd4j.ENFORCE_NUMERICAL_STABILITY = true;
        final int numFeat = 78;//15;//970;
        int outputNum = 5;
        //int numSamples =1000;
        //int batchSize = 20;
        int iterations =40;
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

        //reader.initialize(new FileSplit(new File("src/main/resources/mid_noh_features4000.txt")));
        reader.initialize(new FileSplit(new File("src/main/resources/window_features4000.txt")));


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
                .constrainGradientToUnitNorm(true)
                // .useDropConnect(true)
                .learningRate(1e-1*5)
                .l1(0.3)
                .regularization(true).l2(1e-1)
                 //.momentum(0.5)
                 //.momentumAfter(Collections.singletonMap(3, 0.9))
                 .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .list(5)
                .layer(0, new DenseLayer.Builder().nIn(numFeat).nOut(100)
                        .activation(activ).dropOut(0.5)
                        .weightInit(WeightInit.VI)
                        .build())
                .layer(1, new DenseLayer.Builder().nIn(100).nOut(75)       //300 200
                        .activation(activ)
                        .weightInit(WeightInit.VI)
                        .build())
                 .layer(2, new DenseLayer.Builder().nIn(75).nOut(75)       //300 200
                         .activation(activ)
                         .weightInit(WeightInit.VI)
                         .build())
                 .layer(3, new DenseLayer.Builder().nIn(75).nOut(50)       //300 200
                         .activation(activ)
                         .weightInit(WeightInit.VI)
                         .build())
                .layer(4, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .weightInit(WeightInit.VI)
                        .activation("softmax")
                        .nIn(50).nOut(outputNum).build())
                .backprop(true).pretrain(true)
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(Arrays.asList((IterationListener) new ScoreIterationListener(listenerFreq)));

        log.info("READING...");
        DataSetIterator iter = new RecordReaderDataSetIterator(reader, 76000,78,5);
        log.info(" bring from iterator");
        DataSet next = iter.next();
        log.info("Done reading");
        next.normalizeZeroMeanZeroUnitVariance();
        log.info("Pre shuffle " + next.get(next.numExamples()-1));
        log.info("Pre shuffle " + next.get(0));
        //next.shuffle();
        log.info("Shuffle's last entry: " + next.get(next.numExamples()-1));
        log.info("Num of examples: " + String.valueOf(next.numExamples()));
        trainTest = next.splitTestAndTrain(0.8);
        /////log.info("check iterate function: "+ trainTest.getTrain().iterateWithMiniBatches().totalExamples());
        //Train
        log.info("Train model....");
        //System.out.println(trainTest.getTrain().getFeatureMatrix().shape()[0]);
        ////model.fit(trainTest.getTrain().iterateWithMiniBatches());
        model.fit(trainTest.getTrain());
            /****
        List<INDArray> testInput = new ArrayList<>();
        List<INDArray> testLabels = new ArrayList<>();
        while(iter.hasNext()){
            DataSet next = iter.next();
            next.normalizeZeroMeanZeroUnitVariance();
            log.info("Num of examples: " + String.valueOf(next.numExamples()));
            trainTest = next.splitTestAndTrain(0.7);
            testInput.add(trainTest.getTest().getFeatureMatrix());
            testLabels.add(trainTest.getTest().getLabels());
            /////log.info("check iterate function: "+ trainTest.getTrain().iterateWithMiniBatches().totalExamples());
            //Train
            log.info("Train model....");
            //System.out.println(trainTest.getTrain().getFeatureMatrix().shape()[0]);
            ////model.fit(trainTest.getTrain().iterateWithMiniBatches());
            model.fit(trainTest.getTrain());
        }****/

        log.info("Evaluate model....");
        Evaluation eval = new Evaluation(outputNum);

        log.info("Predictions---");

        /*****for(int i = 0; i < testInput.size(); i++) {
            INDArray output = model.output(testInput.get(i));
            eval.eval(testLabels.get(i), output);
        }****/

        //int[] shape = trainTest.getTest().getLabels().shape();
        //System.out.println(System.out.format("%d,%d", shape[0], shape[1]));
        //log.info(predict2.toString());
        //log.info("Labels---");
        //shape = trainTest.getTest().getLabels().shape();
        //log.info(System.out.format("%d,%d", shape[0], shape[1]));
        //INDArray maxElements = predict2.max(1);
        //log.info(maxElements.toString());
        //log.info(maxElements.getScalar(0).toString());
        //log.info(predict2.getScalar(0, 0).toString());
        //INDArray labels = Nd4j.create(new float[shape[0]*shape[1]], new int[]{shape[0], shape[1]}) ;
        //for(int i=0;i<shape[0];i++){
         //   for(int j=0;j<shape[1];j++){
         //       if(maxElements.getFloat(i) == predict2.getFloat(i, j)) {
                    //log.info("I'm inside the if!!!");
        //            labels.put(i, j, 1);
        //        }
        //    }
        //}
        //log.info(labels.toString());
        //log.info(trainTest.getTest().getLabels().toString());
        //log.info(trainTest.getTrain().getLabels().toString());

        INDArray predict2 = model.output(trainTest.getTest().getFeatureMatrix());
        eval.eval(trainTest.getTest().getLabels(), predict2);
        log.info(eval.stats());

        log.info("********Test on Train************");
        predict2 = model.output(trainTest.getTrain().getFeatureMatrix());
        eval.eval(trainTest.getTrain().getLabels(), predict2);
        log.info(eval.stats());
        //Save stats to file
        //PrintStream stream = new PrintStream(outputStats);
        //stream.println(stats);
        //FileUtils.write(new File(outputStats), stats);
/*
        //Save model to file
        OutputStream fos = Files.newOutputStream(Paths.get(outputModelWeights));
        DataOutputStream dos = new DataOutputStream(fos);
        Nd4j.write(model.params(), dos);
        dos.flush();
        dos.close();
        FileUtils.write(new File(outputModelConf), model.getLayerWiseConfigurations().toJson());
*/
        log.info("****************Example finished********************");
    }
/*
    public static void registerOption(String option_name, String arg_name, boolean has_arg, String description) {
        OptionBuilder.withArgName(arg_name);
        OptionBuilder.hasArg(has_arg);
        OptionBuilder.withDescription(description);
        Option option = OptionBuilder.create(option_name);

        BptiExample.options.add(option);
    }

    private static void createCommandLineOptions() {
        registerOption("input_file", "String", true, "The path to the input file.");
        registerOption("output_model_conf_file", "String", true, "The path to save the computed model conf to.");
        registerOption("output_model_weights_file", "String", true, "The path to save the computed model weights to.");
        registerOption("output_stats_file", "String", true, "The path to save the model stats to.");

        // Other options will be added here.
    }
*/
}
