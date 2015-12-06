package edu.jhu.bdslss.baft;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
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
import org.deeplearning4j.nn.conf.layers.DenseLayer;
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
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Created by Aileme on 11/16/15.
 */
public class BptiSparkExample {
    private static Logger log = LoggerFactory.getLogger(BptiSparkExample.class);
    static public LinkedList<Option> options = new LinkedList<Option>();

    public static void main(String[] args) throws Exception {

        // set to test mode
        SparkConf sparkConf = new SparkConf().set(SparkDl4jMultiLayer.AVERAGE_EACH_ITERATION, "false")
                .setAppName("sparktest");

        // To run:
        // BptiSparkExample -input_file [input file] -output_model_conf_file [output conf file]
        //              -output_model_weights_file [output weights file -output_stats_file [stats file]
        // Parse the command line.
        String[] mandatory_args = { "input_file", "output_model_conf_file",
                "output_model_weights_file", "output_stats_file"};
        createCommandLineOptions();
        CommandLineUtilities.initCommandLineParameters(args, BptiExample.options, mandatory_args);

        String inputFile = CommandLineUtilities.getOptionValue("input_file");
        if(StringUtils.isBlank(inputFile)) throw new Exception("Please specify input file");

        String outputModelConf = CommandLineUtilities.getOptionValue("output_model_conf_file");
        if(StringUtils.isBlank(outputModelConf)) throw new Exception("Please specify model config output file");

        String outputModelWeights = CommandLineUtilities.getOptionValue("output_model_weights_file");
        if(StringUtils.isBlank(outputModelWeights)) throw new Exception("Please specify model weights output file");

        String outputStats = CommandLineUtilities.getOptionValue("output_stats_file");
        if(StringUtils.isBlank(outputStats)) throw new Exception("Please specify stats output file");

        Nd4j.ENFORCE_NUMERICAL_STABILITY = true;
        final int numFeat = 15; //970;
        int outputNum = 5;
        int numSamples = 4000;
        int iterations = 50;
        int seed = 123;
        int listenerFreq = iterations/25;
        int batchSize = 500; //10;
        SplitTestAndTrain trainTest;

        //Load data..
        RecordReader reader = new CSVRecordReader(0, ",");


        reader.initialize(new FileSplit(new File(inputFile)));

        //log.info("Build model....");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);
        String activation = "tanh";

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed).batchSize(batchSize)
                .iterations(iterations)
                .constrainGradientToUnitNorm(true).useDropConnect(true)
                .learningRate((1e-1) * 5)
                .l1(0.3).regularization(false).l2(1e-3)
                .constrainGradientToUnitNorm(true).optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .list(5)
                .layer(0, new DenseLayer.Builder().nIn(numFeat).nOut(750)
                        .activation(activation).dropOut(0.5)
                        .weightInit(WeightInit.XAVIER)
                        .build())
                .layer(1, new DenseLayer.Builder().nIn(750).nOut(500)
                        .activation(activation).dropOut(0.5)
                        .weightInit(WeightInit.XAVIER)
                        .build())
                .layer(2, new DenseLayer.Builder().nIn(500).nOut(300)
                        .activation(activation).dropOut(0.5)
                        .weightInit(WeightInit.XAVIER)
                        .build())
                .layer(3, new DenseLayer.Builder().nIn(300).nOut(200)
                        .activation(activation)
                        .weightInit(WeightInit.XAVIER)
                        .build())
                .layer(4, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .weightInit(WeightInit.XAVIER)
                        .activation("softmax")
                        .nIn(200).nOut(outputNum).build())
                .backprop(true).pretrain(false)
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(Arrays.asList((IterationListener) new ScoreIterationListener(listenerFreq)));

        System.out.println("Initializing network");

        DataSetIterator iter = new RecordReaderDataSetIterator(reader, numSamples,numFeat,outputNum);
        DataSet next = iter.next();
        //next.normalizeZeroMeanZeroUnitVariance();
        //next.shuffle();
        //log.info("Num of examples: " + String.valueOf(next.numExamples()));
        double split = 0.8;
        trainTest = next.splitTestAndTrain(split);

        SparkDl4jMultiLayer master = new SparkDl4jMultiLayer(sc,conf);
        //number of partitions should be partitioned by batch size
        //JavaRDD<String> lines = sc.textFile("/Users/Aileme/git/x-bpti/src/main/resources/avg_features20.txt",60000 / conf.getConf(0).getBatchSize());
        JavaRDD<DataSet> data = sc.parallelize(trainTest.getTrain().asList(), trainTest.getTrain().numExamples() / conf.getConf(0).getBatchSize());

        //Train
        log.info("Train model....");
        // for checking shuffle
        MultiLayerNetwork network2 = master.fitDataSet(data);

        log.info("Evaluate model....");
        Evaluation eval = new Evaluation(outputNum);

        log.info("Predictions---");
        INDArray predict2 = network2.output(trainTest.getTest().getFeatureMatrix());

        eval.eval(trainTest.getTest().getLabels(), predict2);

        System.out.println(eval.stats());

        String stats = eval.stats();
        log.info(stats);
        //Save stats to file
        //PrintStream stream = new PrintStream(outputStats);
        //stream.println(stats);
        FileUtils.write(new File(outputStats), stats);

        //Save model to file
        OutputStream fos = Files.newOutputStream(Paths.get(outputModelWeights));
        DataOutputStream dos = new DataOutputStream(fos);
        Nd4j.write(model.params(), dos);
        dos.flush();
        dos.close();
        FileUtils.write(new File(outputModelConf), model.getLayerWiseConfigurations().toJson());

        log.info("****************Example finished********************");
    }

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
}
