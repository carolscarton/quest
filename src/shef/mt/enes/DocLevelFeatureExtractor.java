package shef.mt.enes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import shef.mt.features.util.Sentence;
import shef.mt.features.util.Doc;
import shef.mt.features.util.Paragraph;
import shef.mt.features.util.DocLevelFeatureManager;
import shef.mt.tools.DocLevelMissingResourceGenerator;
import shef.mt.tools.ResourceProcessor;
import shef.mt.tools.DocLevelProcessorFactory;
import shef.mt.util.PropertiesManager;
import shef.mt.tools.Tokenizer;


/**
 * Main class for the doc-level feature extraction pipeline.
 *
 * @author GustavoH
 */
public class DocLevelFeatureExtractor {

    

    private String workDir;
    private String input;
    private String output;
    private String features;

    private ArrayList<String> listSourceFiles = new ArrayList<>();
    private ArrayList<String> listTargetFiles = new ArrayList<>();

    private String sourceFile;
    private String targetFile;
    private String sourceLang;
    private String targetLang;

    private PropertiesManager resourceManager;
    private DocLevelFeatureManager featureManager;
    private String configPath;
    private String mod;

    private static boolean forceRun = false;


    public DocLevelFeatureExtractor(String[] args) {
        //Parse command line arguments:
        this.parseArguments(args);

        //Setup main folders:
        workDir = System.getProperty("user.dir");
        input = workDir + File.separator + resourceManager.getString("input");
        output = workDir + File.separator + resourceManager.getString("output");
        System.out.println("Work dir: " + workDir);
        System.out.println("Input folder: " + input);
        System.out.println("Output folder: " + output);
    }

    public static void main(String[] args) {
        //Measure initial time:
        long start = System.currentTimeMillis();

        //Run doc-level feature extractor:
        DocLevelFeatureExtractor dfe = new DocLevelFeatureExtractor(args);
        dfe.run();

        //Measure ending time:
        long end = System.currentTimeMillis();
        System.out.println("Processing completed in " + (end - start) / 1000 + " seconds.");
    }


    public void processListFiles(){
        try {
            BufferedReader sourceBR = new BufferedReader(new FileReader(this.getSourceFile()));
            BufferedReader targetBR = new BufferedReader(new FileReader(this.getTargetFile()));
            while (sourceBR.ready() && targetBR.ready()) {
                listSourceFiles.add(sourceBR.readLine().trim());
                listTargetFiles.add(targetBR.readLine().trim());              
            }
            
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    public void run() {
        //Set output writer for feature values:
        String outputPath = this.output + File.separator + "output.txt";
        BufferedWriter outWriter = null;
        try {
            outWriter = new BufferedWriter(new FileWriter(outputPath));
        } catch (IOException ex) {
            Logger.getLogger(DocLevelFeatureExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Build input and output folders:
        System.out.println("\n********** Creating necessary folders **********");
        this.constructFolders();

        //Produce missing resources:
        System.out.println("\n********** Producing missing resources **********");
        DocLevelMissingResourceGenerator missingGenerator = new DocLevelMissingResourceGenerator(this);
        missingGenerator.produceMissingResources();
        
	this.processListFiles();
        try{
	
	        //Get next doc from sgm file (Doc class)
		//Process sentences and calculate features:
	        System.out.println("\n********** Producing output **********");
		int documentCounter = 0;

		while (this.getNextSourceDoc(documentCounter)!=null && this.getNextTargetDoc(documentCounter)!=null){
                    sourceFile = this.getNextSourceDoc(documentCounter);
                    targetFile = this.getNextTargetDoc(documentCounter);
                    
                    //Pre-processing (tokenizer + truecase):
	            System.out.println("\n********** Pre-processing **********");                    
                    this.preProcess();
                    
	            //Create processor factory:
	            System.out.println("\n********** Creating processors **********");
	            DocLevelProcessorFactory processorFactory = new DocLevelProcessorFactory(this);

		    //Get required resource processors:
		    ResourceProcessor[][] resourceProcessors = processorFactory.getResourceProcessors();
		    ResourceProcessor[] resourceProcessorsSource = resourceProcessors[0];
		    ResourceProcessor[] resourceProcessorsTarget = resourceProcessors[1];
		
		    //Get readers of source and target files input:
	            BufferedReader sourceBR = new BufferedReader(new FileReader(this.getSourceFile()));
        	    BufferedReader targetBR = new BufferedReader(new FileReader(this.getTargetFile()));


		    ArrayList<Sentence> targetSentences = new ArrayList<>();
		    ArrayList<Sentence> sourceSentences = new ArrayList<>();
		    int sentenceCounter = 0;
            	    while (sourceBR.ready() && targetBR.ready()) {
                         //Create source and target sentence objects: 
                         Sentence sourceSentence = new Sentence(sourceBR.readLine().trim(), sentenceCounter);
                         Sentence targetSentence = new Sentence(targetBR.readLine().trim(), sentenceCounter);
			 sourceSentences.add(sourceSentence);
			 targetSentences.add(targetSentence);
                         sentenceCounter++;

	            }
		    
                    //Create source and target document objects:
		    Doc targetDocument = new Doc(targetSentences, documentCounter);
		    Doc sourceDocument = new Doc(sourceSentences, documentCounter);
                    
                    
                    //Run processors over source document:
		    for (ResourceProcessor processor : resourceProcessorsSource) {
		       processor.processNextDocument(sourceDocument);
		    }

		    //Run processors over target document:
		    for (ResourceProcessor processor : resourceProcessorsTarget) {
		       processor.processNextDocument(targetDocument);
		    }


			
		    documentCounter++;	
		    
                    //Run features for document pair:
		    String featureValues = getFeatureManager().runFeatures(sourceDocument, targetDocument).trim();
		    outWriter.write(featureValues);
		    outWriter.newLine();
	
                        
		}
                
                //save output file:
                outWriter.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(WordLevelFeatureExtractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WordLevelFeatureExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
	
    }

    public String getNextSourceDoc(int documentCounter) {
        if (documentCounter<listSourceFiles.size()){
            return listSourceFiles.get(documentCounter);
        }
        return null;
        
    }
    
    public String getNextTargetDoc(int documentCounter) {
	if (documentCounter<listSourceFiles.size()){
            return listTargetFiles.get(documentCounter);
        }
        return null;
        
    }

    public void constructFolders() {
	//Create input folders:
	File f = new File(input);
	if (!f.exists()) {
	    f.mkdirs();
	}
	System.out.println("Input folder created " + f.getPath());
	
	f = new File(input + File.separator + getSourceLang());
	if (!f.exists()) {
	    f.mkdirs();
	}
	System.out.println("Input folder created " + f.getPath());
	
	f = new File(input + File.separator + getTargetLang());
	if (!f.exists()) {
	    f.mkdirs();
	}
	System.out.println("Input folder created " + f.getPath());
	
	f = new File(input + File.separator + getTargetLang() + File.separator + "temp");
	if (!f.exists()) {
	    f.mkdirs();
	}
	System.out.println("Input folder created " + f.getPath());

	//Create output folders:
	String output = getResourceManager().getString("output");
	f = new File(output);
	if (!f.exists()) {
	    f.mkdirs();
	}
	System.out.println("Output folder created " + f.getPath());
    }

    private void preProcess() {
	//Create input and output paths:
	String sourceInputFolder = input + File.separator + getSourceLang();
	String targetInputFolder = input + File.separator + getTargetLang();
        System.out.println(sourceInputFolder);
	File origSourceFile = new File(getSourceFile());
        //System.out.println(getSourceFile());
	File inputSourceFile = new File(sourceInputFolder + File.separator + origSourceFile.getName());
	File origTargetFile = new File(getTargetFile());
	File inputTargetFile = new File(targetInputFolder + File.separator + origTargetFile.getName());

	//Create copy of original input files to input folder:
	try {
	    System.out.println("Copying source input to: " + inputSourceFile.getPath());
	    System.out.println("Copying target input to: " + inputTargetFile.getPath());
	    this.copyFile(origSourceFile, inputSourceFile);
	    this.copyFile(origTargetFile, inputTargetFile);
	} catch (IOException e) {
            System.out.println(e);
	    return;
	}


	//run tokenizer for source
        System.out.println("Running tokenizer for source file...");
        
	//verify language support
        String src_abbr = ""; 
        if (getSourceLang().equalsIgnoreCase ("english"))
            src_abbr = "en";
        else if (getSourceLang().equalsIgnoreCase ("spanish"))
            src_abbr = "es";
        else if (getSourceLang().equalsIgnoreCase ("french"))
            src_abbr = "fr";
        else if (getSourceLang().equalsIgnoreCase ("german"))
            src_abbr = "de";
        else if (getSourceLang().equalsIgnoreCase("dutch"))
            src_abbr = "nl";
        else if (getSourceLang().equalsIgnoreCase("portuguese"))
            src_abbr = "pt";
        else
            System.out.println("Don't recognise the source language");
        
	String truecasePath = "";
        if (null != resourceManager.getProperty(getSourceLang() + ".lowercase")) {
            truecasePath = resourceManager.getProperty(getSourceLang() + ".lowercase")  + " -q ";
        } else {
            truecasePath = resourceManager.getString(getSourceLang() + ".truecase") + " --model " + resourceManager.getString(getSourceLang() + ".truecase.model");
        }
        Tokenizer enTok = new Tokenizer(inputSourceFile.getPath(), inputSourceFile.getPath() + ".tok", truecasePath, resourceManager.getString(getSourceLang() + ".tokenizer"), src_abbr, forceRun);
        
        enTok.run();
        //Update input paths:
	this.sourceFile = enTok.getTok();
        System.out.println("New source file: "+sourceFile);

        //run tokenizer for target
        System.out.println("Running tokenizer for source file...");

	//verify language support
        String tgt_abbr = ""; 
        if (getTargetLang().equalsIgnoreCase ("english"))
            tgt_abbr = "en";
        else if (getTargetLang().equalsIgnoreCase ("spanish"))
            tgt_abbr = "es";
        else if (getTargetLang().equalsIgnoreCase ("french"))
            tgt_abbr = "fr";
        else if (getTargetLang().equalsIgnoreCase ("german"))
            tgt_abbr = "de";
        else if (getTargetLang().equalsIgnoreCase("dutch"))
            tgt_abbr = "nl";
        else if (getTargetLang().equalsIgnoreCase("portuguese"))
            tgt_abbr = "pt";
        else
            System.out.println("Don't recognise the target language");
        

        if (null != resourceManager.getProperty(targetLang + ".lowercase")) {
            truecasePath = resourceManager.getProperty(getTargetLang() + ".lowercase")  + " -q ";
        } else {
            truecasePath = resourceManager.getString(getTargetLang() + ".truecase") + " --model " + resourceManager.getString(getTargetLang() + ".truecase.model");
        }
        Tokenizer esTok = new Tokenizer(inputTargetFile.getPath(),inputTargetFile.getPath() + ".tok", truecasePath, resourceManager.getString(getTargetLang() + ".tokenizer"), tgt_abbr, forceRun);
        
        esTok.run();
	//Update input paths:
        targetFile = esTok.getTok();
        System.out.println("New target file: "+targetFile);
    }

    public void parseArguments(String[] args) {

	Option help = OptionBuilder.withArgName("help").hasArg()
	        .withDescription("print project help information")
	        .isRequired(false).create("help");

	Option input = OptionBuilder.withArgName("input").hasArgs(3)
	        .isRequired(true).create("input");

	Option lang = OptionBuilder.withArgName("lang").hasArgs(2)
	        .isRequired(false).create("lang");

	Option feat = OptionBuilder.withArgName("feat").hasArgs(1)
	        .isRequired(false).create("feat");

	Option gb = OptionBuilder.withArgName("gb")
	        .withDescription("GlassBox input files").hasOptionalArgs(2)
	        .hasArgs(3).create("gb");

	Option mode = OptionBuilder
	        .withArgName("mode")
	        .withDescription("blackbox features, glassbox features or both")
	        .hasArgs(1).isRequired(true).create("mode");

	Option config = OptionBuilder
	        .withArgName("config")
	        .withDescription("cofiguration file")
	        .hasArgs(1).isRequired(false).create("config");

	CommandLineParser parser = new PosixParser();
	Options options = new Options();
	options.addOption(help);
	options.addOption(input);
	options.addOption(mode);
	options.addOption(lang);
	options.addOption(feat);
	options.addOption(gb);
	options.addOption(config);

	try {
	    CommandLine line = parser.parse(options, args);

	    if (line.hasOption("config")) {
	        resourceManager = new PropertiesManager(line.getOptionValue("config"));
	    } else {
	        resourceManager = new PropertiesManager();
	    }

	    if (line.hasOption("input")) {
	        String[] files = line.getOptionValues("input");
	        sourceFile = files[0];
	        targetFile = files[1];
	    }

	    if (line.hasOption("lang")) {
	        String[] langs = line.getOptionValues("lang");
	        sourceLang = langs[0];
	        targetLang = langs[1];
	    } else {
	        sourceLang = getResourceManager().getString("sourceLang.default");
	        targetLang = getResourceManager().getString("targetLang.default");
	    }

	    if (line.hasOption("mode")) {
	        String[] modeOpt = line.getOptionValues("mode");
	        setMod(modeOpt[0].trim());
	        configPath = getResourceManager().getString("featureConfig." + getMod());
	        featureManager = new DocLevelFeatureManager(configPath);
	    }

	    if (line.hasOption("feat")) {
	        // print the value of block-size
	        features = line.getOptionValue("feat");
	        getFeatureManager().setFeatureList(features);
	    } else {
	        getFeatureManager().setFeatureList("all");
	    }

	} catch (ParseException exp) {
	    System.out.println("Unexpected exception:" + exp.getMessage());
	}
    }

    private void copyFile(File sourceFile, File destFile) throws IOException {
	if (sourceFile.equals(destFile)) {
	    return;
	}

	if (!destFile.exists()) {
	    destFile.createNewFile();
	}

	java.nio.channels.FileChannel source = null;
	java.nio.channels.FileChannel destination = null;
	try {
	    source = new FileInputStream(sourceFile).getChannel();
	    destination = new FileOutputStream(destFile).getChannel();
	    destination.transferFrom(source, 0, source.size());
	} finally {
	    if (source != null) {
	        source.close();
	    }
	    if (destination != null) {
	        destination.close();
	    }
	}
    }

    /**
     * @return the sourceFile
     */
    public String getSourceFile() {
	return sourceFile;
    }

    /**
     * @return the targetFile
     */
    public String getTargetFile() {
	return targetFile;
    }

    /**
     * @return the sourceLang
     */
    public String getSourceLang() {
	return sourceLang;
    }

    /**
     * @return the targetLang
     */
    public String getTargetLang() {
	return targetLang;
    }

    /**
     * @return the resourceManager
     */
    public PropertiesManager getResourceManager() {
	return resourceManager;
    }

    /**
     * @return the featureManager
     */
    public DocLevelFeatureManager getFeatureManager() {
	return featureManager;
    }

    /**
     * @return the mod
     */
    public String getMod() {
	return mod;
    }

    /**
     * @param mod the mod to set
     */
    public void setMod(String mod) {
	this.mod = mod;
    }
}
