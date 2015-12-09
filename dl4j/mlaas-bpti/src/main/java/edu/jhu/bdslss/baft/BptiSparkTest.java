package edu.jhu.bdslss.baft;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.io.FileUtils;
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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * Created by Aileme on 12/6/15.
 */
public class BptiSparkTest {
    private static Logger log = LoggerFactory.getLogger(BptiExample.class);
    static public LinkedList<Option> options = new LinkedList<Option>();

    public static void main(String[] args) throws Exception {
        // To run:
        // BptiUsedExample -input_file [input file] -input_model_conf_file [input conf file]
        //              -input_model_weights_file [input weights file] -output_model_conf_file [output conf file]
        //              -output_model_weights_file [output weights file] -output_stats_file [stats file]
        // Parse the command line.
        String[] mandatory_args = { "input_file","input_model_conf_file","input_model_weights_file", "output_stats_file"};
        createCommandLineOptions();
        CommandLineUtilities.initCommandLineParameters(args, BptiExample.options, mandatory_args);

        String inputFile = CommandLineUtilities.getOptionValue("input_file");
        String inputModelConf = CommandLineUtilities.getOptionValue("input_model_conf_file");
        String inputModelWeights = CommandLineUtilities.getOptionValue("input_model_weights_file");
        //String outputModelConf = CommandLineUtilities.getOptionValue("output_model_conf_file");
        //String outputModelWeights = CommandLineUtilities.getOptionValue("output_model_weights_file");
        String outputStats = CommandLineUtilities.getOptionValue("output_stats_file");

        Nd4j.ENFORCE_NUMERICAL_STABILITY = true;
        final int numFeat = 15;//970;
        int outputNum = 5;

        //int splitTrainNum = (int) (batchSize*.8);
        //DataSet mnist;
        //SplitTestAndTrain trainTest;

        //Load data..
        RecordReader reader = new CSVRecordReader(0, ",");
        //reader.initialize(new FileSplit(new ClassPathResource("pca_features4000.txt").getFile()));
        reader.initialize(new FileSplit(new File(inputFile)));

        log.info("Build model....");
        MultiLayerConfiguration confFromJson = MultiLayerConfiguration.fromJson(FileUtils.readFileToString(new File(inputModelConf)));
        DataInputStream dis = new DataInputStream(new FileInputStream(inputModelWeights));
        INDArray newParams = Nd4j.read(dis);
        dis.close();
        MultiLayerNetwork model = new MultiLayerNetwork(confFromJson);
        model.init();
        model.setParameters(newParams);

        int iterations = confFromJson.getConf(0).getNumIterations();
        int listenerFreq = iterations/5;
        model.setListeners(Arrays.asList((IterationListener) new ScoreIterationListener(listenerFreq)));

        log.info("READING...");
        DataSetIterator iter = new RecordReaderDataSetIterator(reader, 4000,numFeat,outputNum);
        DataSet next = iter.next();
        next.normalizeZeroMeanZeroUnitVariance();
        //trainTest = next.splitTestAndTrain(0.7);
        //Train
        //log.info("Train model....");
        //model.fit(trainTest.getTrain());

        log.info("Evaluate model....");
        Evaluation eval = new Evaluation(outputNum);

        log.info("Predictions---");


        INDArray predict2 = model.output(next.getFeatureMatrix());
        eval.eval(next.getLabels(), predict2);
        String stats = eval.stats();
        log.info(stats);
        //Save stats to file
        //PrintStream stream = new PrintStream(outputStats);
        //stream.println(stats);
        FileUtils.write(new File(outputStats), stats);

        /*
        //Do not save model to file
        OutputStream fos = Files.newOutputStream(Paths.get(outputModelWeights));
        DataOutputStream dos = new DataOutputStream(fos);
        Nd4j.write(model.params(), dos);
        dos.flush();
        dos.close();
        FileUtils.write(new File(outputModelConf), model.getLayerWiseConfigurations().toJson());
        */
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
        //registerOption("output_model_conf_file", "String", true, "The path to save the computed model conf to.");
        registerOption("input_model_conf_file", "String", true, "The path to load the previous model conf from.");
        //registerOption("output_model_weights_file", "String", true, "The path to save the computed model weights to.");
        registerOption("input_model_weights_file", "String", true, "The path to load the previous model weights from.");
        registerOption("output_stats_file", "String", true, "The path to save the model stats to.");

        // Other options will be added here.
    }
}
