package com.bodastage.boda_nokiacmdataparser;

/**
 * Bodastage Solutions
 *
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class NokiaCMDataParser 
{
    
    /**
     * Current version
     * 
     * @since  1.1.0
     */
    final static public String VERSION = "2.2.0";
    
    /**
     * Tracks Managed Object attributes to write to file. This is dictated by 
     * the first instance of the MO found. 
     * @TODO: Handle this better.
     *
     * @since 1.0.0
     */
    private Map<String, Stack> moColumns = new LinkedHashMap<String, Stack>();
    
    /**
     * This holds a map of the Managed Object Instances (MOIs) to the respective
     * csv print writers.
     * 
     * @since 1.0.0
     */
    private Map<String, PrintWriter> moiPrintWriters 
            = new LinkedHashMap<String, PrintWriter>();
    
    /**
     * Tag data.
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    private String tagData = "";
    
    /**
     * Output directory.
     *
     * @since 1.0.0
     */
    private String outputDirectory = "/tmp";
    
    /**
     * Parser start time. 
     * 
     * @since 1.0.4
     * @version 1.0.0
     */
    final long startTime = System.currentTimeMillis();
    
    /**
     * The base file name of the file being parsed.
     * 
     * @since 1.0.0
     * @version 1.0.0
     */
    private String baseFileName = "";
    
    /**
     * The file to be parsed.
     * 
     * @since 1.0.0
     * @version 1.0.0
     */
    private String dataFile;
    
    /**
     * The holds the parameters and corresponding values for the moi tag  
     * currently being processed.
     * 
     * @since 1.0.0
     * @version 1.0.0
     */
    private Map<String,String> moiParameterValueMap 
            = new LinkedHashMap<String, String>();
    
    
    
    /**
     * List of parameters and their values in the managedObject>list>item location
     * 
     * @since 1.0.0
     * @version 1.0.0
     */
    private Map<String,String> itemParamValueMap 
            = new LinkedHashMap<String, String>();
    
    /**
     * Current className MO attribute.
     * 
     * @since 1.0.0
     * @version 1.0.0
    */
    private String moClassName = null;
    private String moDistName = null;
    private String moVersion = null;
    private String moId = null;
    
    private boolean inItem = false;
    private boolean inHead = false;
    
    private String listName = null;
    private String dateTime = null;
    private String parameterName = null;
    
    /**
     * Value of the name atttribute of the p XML tag
     * 
     * @since 1.0.0
     */
    private String pAttrName = null;


    /**
     * The file/directory to be parsed.
     *
     * @since 1.1.0
     */
    private String dataSource;
    
    /**
     * Parser states. Currently there are only 2: extraction and parsing
     * 
     * @since 1.1.0
     */
    private int parserState = ParserStates.EXTRACTING_PARAMETERS;
    
    /**
     * File containing a list of parameters to export
     * 
     */
    private String parameterFile = null;
    
    /**
     * Extract managed objects and their parameters
     */
    private Boolean extractParametersOnly = false;

    /**
     * Set the parameter file name 
     * 
     * @param filename 
     */
    public void setParameterFile(String filename){
        parameterFile = filename;
    }
    
    
    public void setExtractParametersOnly(Boolean bool){
        extractParametersOnly = bool;
    }
     
    public static void main( String[] args )
    {
        
       //Define
       Options options = new Options();
       CommandLine cmd = null;
       String outputDirectory = null;   
       String inputFile = null;
       String parameterConfigFile = null;
       Boolean onlyExtractParameters = false;
       Boolean showHelpMessage = false;
       Boolean showVersion = false;
       
      try{ 
            options.addOption( "p", "extract-parameters", false, "extract only the managed objects and parameters" );
            options.addOption( "v", "version", false, "display version" );
            options.addOption( Option.builder("i")
                    .longOpt( "input-file" )
                    .desc( "input file or directory name")
                    .hasArg()
                    .argName( "INPUT_FILE" ).build());
            options.addOption(Option.builder("o")
                    .longOpt( "output-directory" )
                    .desc( "output directory name")
                    .hasArg()
                    .argName( "OUTPUT_DIRECTORY" ).build());
            options.addOption(Option.builder("c")
                    .longOpt( "parameter-config" )
                    .desc( "parameter configuration file")
                    .hasArg()
                    .argName( "PARAMETER_CONFIG" ).build() );
            options.addOption( "h", "help", false, "show help" );
            
            //Parse command line arguments
            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse( options, args);

            if( cmd.hasOption("h")){
                showHelpMessage = true;
            }

            if( cmd.hasOption("v")){
                showVersion = true;
            }
            
            if(cmd.hasOption('o')){
                outputDirectory = cmd.getOptionValue("o"); 
            }
            
            if(cmd.hasOption('i')){
                inputFile = cmd.getOptionValue("i"); 
            }
            
            if(cmd.hasOption('c')){
                parameterConfigFile = cmd.getOptionValue("c"); 
            }
            
            if(cmd.hasOption('p')){
                onlyExtractParameters  = true;
            }
            
       }catch(IllegalArgumentException e){
           
       } catch (ParseException ex) {
//            java.util.logging.Logger.getLogger(HuaweiCMObjectParser.class.getName()).log(Level.SEVERE, null, ex);
        }
       
      
      

        try{
            
            if(showVersion == true ){
                System.out.println(VERSION);
                System.out.println("Copyright (c) 2019 Bodastage Solutions(http://www.bodastage.com)");
                System.exit(0);
            }
            
            //show help
            if( showHelpMessage == true || 
                inputFile == null || 
                ( outputDirectory == null && onlyExtractParameters == false) ){
                     HelpFormatter formatter = new HelpFormatter();
                     String header = "Parses Nokia RAML20 configuration management XML data files to csv.\n\n";
                     String footer = "\n";
                     footer += "Examples: \n";
                     footer += "java -jar boda-nokiacmdataparser.jar -i raml20_dump.xml -o out_folder\n";
                     footer += "java -jar boda-nokiacmdataparser.jar -i input_folder -o out_folder\n";
                     footer += "java -jar boda-nokiacmdataparser.jar -i input_folder -p\n";
                     footer += "java -jar boda-nokiacmdataparser.jar -i input_folder -p -m\n";
                     footer += "\nCopyright (c) 2019 Bodastage Solutions(http://www.bodastage.com)";
                     formatter.printHelp( "java -jar boda-nokiacmdataparser.jar", header, options, footer );
                     System.exit(0);
            }
        
            //Confirm that the output directory is a directory and has write 
            //privileges
            if(outputDirectory != null ){
                File fOutputDir = new File(outputDirectory);
                if (!fOutputDir.isDirectory()) {
                    System.err.println("ERROR: The specified output directory is not a directory!.");
                    System.exit(1);
                }

                if (!fOutputDir.canWrite()) {
                    System.err.println("ERROR: Cannot write to output directory!");
                    System.exit(1);
                }
            }
            
            //Get parser instance
            NokiaCMDataParser cmParser = new NokiaCMDataParser();

            
            if(onlyExtractParameters == true ){
                cmParser.setExtractParametersOnly(true);
            }
            
            if(  parameterConfigFile != null ){
                File f = new File(parameterConfigFile);
                if(f.isFile()){
                    cmParser.setParameterFile(parameterConfigFile);
                    cmParser.getParametersToExtract(parameterConfigFile);
                    cmParser.parserState = ParserStates.EXTRACTING_VALUES;
                }
            }
            
            cmParser.setDataSource(inputFile);
            if(outputDirectory != null ) cmParser.setOutputDirectory(outputDirectory);
            
            cmParser.parse();
        }catch(Exception e){
            System.out.println(e.getMessage());
            System.exit(1);
        }
        
      
    }
    
  /**
     * Extract parameter list from  parameter file
     * 
     * @param filename 
     */
    public  void getParametersToExtract(String filename) throws FileNotFoundException, IOException{
        BufferedReader br = new BufferedReader(new FileReader(filename));
        for(String line; (line = br.readLine()) != null; ) {
           String [] moAndParameters =  line.split(":");
           String mo = moAndParameters[0];
           String [] parameters = moAndParameters[1].split(",");
           
           Stack parameterStack = new Stack();
           for(int i =0; i < parameters.length; i++){
               parameterStack.push(parameters[i]);
           }
           
           moColumns.put(mo, parameterStack);

        }
        
        //Move to the parameter value extraction stage
        //parserState = ParserStates.EXTRACTING_VALUES;
    }
    
    /**
     * Show parser help.
     * 
     * @since 1.0.0
     * @version 1.0.0
     */
    public static void showHelp(){
        System.out.println("boda-nokiacmdataparser " + VERSION + ". Copyright (c) 2017 Bodastage(http://www.bodastage.com)");
        System.out.println("Parses Nokia configuration management XML data files to csv.");
        System.out.println("Usage: java -jar boda-nokiacmdataparser.jar <fileToParse.xml|Directory> <outputDirectory>");
    }
    
    
    /**
     * Determines if the source data file is a regular file or a directory and 
     * parses it accordingly
     * 
     * @since 1.1.0
     * @version 1.0.0
     * @throws XMLStreamException
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    public void processFileOrDirectory() throws IOException, XMLStreamException {
        //this.dataFILe;
        Path file = Paths.get(this.dataSource);
        boolean isRegularExecutableFile = Files.isRegularFile(file)
                & Files.isReadable(file);

        boolean isReadableDirectory = Files.isDirectory(file)
                & Files.isReadable(file);

        if (isRegularExecutableFile) {
            this.setFileName(this.dataSource);
            baseFileName =  getFileBasename(this.dataFile);
            
            if( parserState == ParserStates.EXTRACTING_PARAMETERS){
                System.out.print("Extracting parameters from " + this.baseFileName + "...");
            }else{
                System.out.print("Parsing " + this.baseFileName + "...");
            }
                    
            this.parseFile(this.dataSource);
            
            if( parserState == ParserStates.EXTRACTING_PARAMETERS){
                 System.out.println("Done.");
            }else{
                System.out.println("Done.");
                //System.out.println(this.baseFileName + " successfully parsed.\n");
            }
        }

        if (isReadableDirectory) {

            File directory = new File(this.dataSource);

            //get all the files from a directory
            File[] fList = directory.listFiles();

            for (File f : fList) {
                this.setFileName(f.getAbsolutePath());
                try {
                    baseFileName =  getFileBasename(this.dataFile);
                    if( parserState == ParserStates.EXTRACTING_PARAMETERS){
                        System.out.print("Extracting parameters from " + this.baseFileName + "...");
                    }else{
                        System.out.print("Parsing " + this.baseFileName + "...");
                    }
                    
                    //Parse
                    this.parseFile(f.getAbsolutePath());
                    if( parserState == ParserStates.EXTRACTING_PARAMETERS){
                         System.out.println("Done.");
                    }else{
                        System.out.println("Done.");
                        //System.out.println(this.baseFileName + " successfully parsed.\n");
                    }
                   
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("Skipping file: " + this.baseFileName + "\n");
                }
            }
        }

    }
    

    /**
     * Reset parser variables before next file
     */
    public void resetVariables(){
        //Reset variables
            tagData = "";
            baseFileName = "";
            moClassName = null;
            moDistName = null;
            moVersion = null;
            moId = null;
            inItem = false;
            inHead = false;
            listName = null;
            dateTime = null;
            parameterName = null;
            pAttrName = null;
    }
    
    /**
     * Parser entry point 
     * 
     * @throws XMLStreamException
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException 
     * 
     * @since 1.1.1
     */
    public void parse() throws IOException, XMLStreamException {
        //Extract parameters
        if (parserState == ParserStates.EXTRACTING_PARAMETERS) {
            processFileOrDirectory();

            parserState = ParserStates.EXTRACTING_VALUES;
        }
        
        //Reset variables
        resetVariables();
        
        //Extracting values
        if (parserState == ParserStates.EXTRACTING_VALUES) {
            processFileOrDirectory();
            parserState = ParserStates.EXTRACTING_DONE;
        }
        
        closeMOPWMap();
    }
    
    /**
     * Parses the CM XML file.
     * 
     * @since 1.1.0
     * @version 1.0.0
     * 
     */
    public void parseFile( String inputFilename ) 
    throws XMLStreamException, FileNotFoundException, UnsupportedEncodingException
    {
            XMLInputFactory factory = XMLInputFactory.newInstance();

            XMLEventReader eventReader = factory.createXMLEventReader(
                    new FileReader(inputFilename));
            baseFileName = getFileBasename(inputFilename);

            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();
                
                switch (event.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT:
                        startElementEvent(event);
                        break;
                    case XMLStreamConstants.SPACE:
                    case XMLStreamConstants.CHARACTERS:
                        characterEvent(event);
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        endELementEvent(event);
                        break;
                    case XMLStreamConstants.COMMENT:
                        break;
                }
            }

    }
    
    /**
     * Handle start element event.
     *
     * @param xmlEvent
     *
     * @since 1.0.0
     * @version 1.0.0
     *
     */
    public void startElementEvent(XMLEvent xmlEvent) throws FileNotFoundException {
        
        StartElement startElement = xmlEvent.asStartElement();
        String qName = startElement.getName().getLocalPart();
        String prefix = startElement.getName().getPrefix();
        
        Iterator<Attribute> attributes = startElement.getAttributes();
        //CHeck beginning of the head tag
        if(qName.equals("header")){
            inHead = true;
        }
        
        //Extract the dateTime from the log tag
        if(qName.equals("log") && inHead == true ){
            while (attributes.hasNext()) {
                Attribute attribute = attributes.next();
                String attrName = attribute.getName().getLocalPart();
                String attrValue =  attribute.getValue();
                if (attrName.equals("dateTime")) {
                    dateTime = attrValue;
                }
            }
        }
        
        //Extract managedObject
        if(qName.equals("managedObject") ){
            while (attributes.hasNext()) {
                Attribute attribute = attributes.next();
                String attrName = attribute.getName().getLocalPart();
                String attrValue =  attribute.getValue();
                if (attrName.equals("class")) {
                    moClassName = attrValue;
                }
                if (attrName.equals("version")) {
                    moVersion = attrValue;
                }
                if (attrName.equals("distName")) {
                    moDistName = attrValue;
                }
                if (attrName.equals("id")) {
                    moId = attrValue;
                }
            }
        }
        
        //list tag
        if(qName.equals("list") ){
            while (attributes.hasNext()) {
                Attribute attribute = attributes.next();
                String attrName = attribute.getName().getLocalPart();
                String attrValue =  attribute.getValue();
                if (attrName.equals("name")) {
                    listName = attrValue;
                }
            }
        }
        
        //list item tag
        if(qName.equals("item")){
            inItem = true;
        }
        
        //parameter tag
        if(qName.equals("p") ){
            while (attributes.hasNext()) {
                parameterName = null;
                Attribute attribute = attributes.next();
                String attrName = attribute.getName().getLocalPart();
                String attrValue =  attribute.getValue();
                if (attrName.equals("name")) {
                    parameterName = attrValue;
                }
            }
        }
    }
    
    /**
     * Handle character events.
     *
     * @param xmlEvent
     * 
     * @version 1.0.0
     * @since 1.0.0
     */
    public void characterEvent(XMLEvent xmlEvent) {
        Characters characters = xmlEvent.asCharacters();
        if(!characters.isWhiteSpace()){
            tagData = characters.getData(); 
        }
    }  
    
    /**
     * Get file base name.
     * 
     * @param filename String The base name of the input data file.
     * 
     * @since 1.0.0
     * @version 1.0.0
     */
     public String getFileBasename(String filename){
        try{
            return new File(filename).getName();
        }catch(Exception e ){
            return filename;
        }
    }
     
     
    /**
     * Processes the end tags.
     * 
     * @param xmlEvent
     * 
     * @since 1.0.0
     * @version 1.0.0
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException 
     */
    public void endELementEvent(XMLEvent xmlEvent)
            throws FileNotFoundException, UnsupportedEncodingException {
        EndElement endElement = xmlEvent.asEndElement();
        String prefix = endElement.getName().getPrefix();
        String qName = endElement.getName().getLocalPart();
        
        if(qName.equals("head")){
            inHead = false;
        }
        
        if(qName.equals("p") && listName == null){
            moiParameterValueMap.put(parameterName, tagData );
            parameterName = null;
        }
        
        if(qName.equals("p") && listName != null && parameterName == null && 
                inItem == false){
            if( moiParameterValueMap.containsKey(listName)){
                String prevValue = moiParameterValueMap.get(listName);
                moiParameterValueMap.put(listName, prevValue + ";" + tagData);
            }else{
                moiParameterValueMap.put(listName, tagData);
            }
            
            parameterName = null;
        }
        
        if(qName.equals("p") && listName != null && inItem == true && 
                parameterName != null ){
            if(itemParamValueMap.containsKey(parameterName)){
                String prevValue = itemParamValueMap.get(parameterName);
                itemParamValueMap.put(parameterName, prevValue + ";" + tagData );
            }else{
                itemParamValueMap.put(parameterName, tagData);
            }
            
            parameterName = null;
        }
        
        //Collect the managed object parameter values
        if(qName.equals("managedObject")){
            //System.out.println("managedObject:" + moClassName);
            String paramNames = "FILENAME,DATETIME,VERSION,DISTNAME,MOID";
            String paramValues = baseFileName+ "," + dateTime + ","+moVersion+","+moDistName+","+moId;
            
            if(ParserStates.EXTRACTING_PARAMETERS == parserState){

                if(!moColumns.containsKey(moClassName)){
                    moColumns.put(moClassName, new Stack());
                }
                
                Stack columns = moColumns.get(moClassName);
                
                Iterator<Map.Entry<String, String>> iter = 
                        moiParameterValueMap.entrySet().iterator();
                
                //Iterate through the columns and add the missing columns
                while(iter.hasNext()){
                    Map.Entry<String, String> me = iter.next();
                    if(!columns.contains(me.getKey())){
                        columns.add(me.getKey());
                    }
                }
            }
            
            
            if(ParserStates.EXTRACTING_VALUES == parserState){
                Stack columns  = moColumns.get(moClassName);
                
                //Create print writer and write the file header to it
                if( !moiPrintWriters.containsKey(moClassName)){

                    String moiFile = outputDirectory + File.separatorChar + moClassName +  ".csv";
                    moiPrintWriters.put(moClassName, new PrintWriter(moiFile));

                    
                    
                    for(int i =0; i < columns.size(); i++){
                        
                        String pName = columns.get(i).toString();
                        if(pName.equals("FILENAME") || pName.equals("DATETIME") 
                            || pName.equals("VERSION") || pName.equals("DISTNAME") 
                            || pName.equals("MOID")){
                            continue;
                        }
                        paramNames += "," + columns.get(i);
                    }
                    moiPrintWriters.get(moClassName).println(paramNames);
                }
                
                //Extract parameter values
                for(int i=0; i < columns.size(); i++){
                    String pName = columns.get(i).toString();
                    
                    if(pName.equals("FILENAME") || pName.equals("DATETIME") 
                        || pName.equals("VERSION") || pName.equals("DISTNAME") 
                        || pName.equals("MOID")){
                        continue;
                    }
                    
                    if( moiParameterValueMap.containsKey(pName)){
                        paramValues += "," + toCSVFormat(moiParameterValueMap.get(pName));
                    }else{
                        paramValues += ",";
                    }
                }
                
                //Write values to file
                PrintWriter pw = moiPrintWriters.get(moClassName);
                pw.println(paramValues);
            }

               
            moiParameterValueMap.clear();
            moClassName = null;
        }
        
        if(qName.equals("item")){
            Iterator<Map.Entry<String, String>> iter = itemParamValueMap.entrySet().iterator();
            String pName = listName + "_";
            String pValue = "";
            while(iter.hasNext()){
                Map.Entry<String, String> me = iter.next();
                pName += me.getKey() + "_";
                pValue+= me.getValue() + "&";
            }
            
            pValue = pValue.replaceAll("&$", "");
            pName = pName.replaceAll("_$", "");
            
            if( moiParameterValueMap.containsKey(pName)){
                String prevValue = moiParameterValueMap.get(pName);
                moiParameterValueMap.put(pName, prevValue + ";" + pValue);
            }else{
                moiParameterValueMap.put(pName, pValue);
            }
            inItem = false;
            itemParamValueMap.clear();
        }
        
        if(qName.equals("list")){
            listName = null; 
        }
                
    }
    
    
    /**
     * Print program's execution time.
     * 
     * @since 1.0.0
     */
    public void printExecutionTime(){
        float runningTime = System.currentTimeMillis() - startTime;
        
        String s = "Parsing completed. ";
        s = s + "Total time:";
        
        //Get hours
        if( runningTime > 1000*60*60 ){
            int hrs = (int) Math.floor(runningTime/(1000*60*60));
            s = s + hrs + " hours ";
            runningTime = runningTime - (hrs*1000*60*60);
        }
        
        //Get minutes
        if(runningTime > 1000*60){
            int mins = (int) Math.floor(runningTime/(1000*60));
            s = s + mins + " minutes ";
            runningTime = runningTime - (mins*1000*60);
        }
        
        //Get seconds
        if(runningTime > 1000){
            int secs = (int) Math.floor(runningTime/(1000));
            s = s + secs + " seconds ";
            runningTime = runningTime - (secs/1000);
        }
        
        //Get milliseconds
        if(runningTime > 0 ){
            int msecs = (int) Math.floor(runningTime/(1000));
            s = s + msecs + " milliseconds ";
            runningTime = runningTime - (msecs/1000);
        }

        
        System.out.println(s);
    }
    
    /**
     * Close file print writers.
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    public void closeMOPWMap() {
        Iterator<Map.Entry<String, PrintWriter>> iter
                = moiPrintWriters.entrySet().iterator();
        while (iter.hasNext()) {
            iter.next().getValue().close();
        }
        moiPrintWriters.clear();
    }
    
    /**
     * Process given string into a format acceptable for CSV format.
     *
     * @since 1.0.0
     * @param s String
     * @return String Formated version of input string
     */
    public String toCSVFormat(String s) {
        String csvValue = s;

        //Check if value contains comma
        if (s.contains(",")) {
            csvValue = "\"" + s + "\"";
        }

        if (s.contains("\"")) {
            csvValue = "\"" + s.replace("\"", "\"\"") + "\"";
        }

        return csvValue;
    }
    
    /**
     * Set the output directory.
     * 
     * @since 1.0.0
     * @version 1.0.0
     * @param String directoryName 
     */
    public void setOutputDirectory(String directoryName ){
        this.outputDirectory = directoryName;
    }
     
    /**
     * Set name of file to parser.
     * 
     * @since 1.0.0
     * @version 1.0.0
     * @param String filename
     */
    public void setFileName(String filename ){
        this.dataFile = filename;
    }
    
    /**
     * Set name of file/directory to parser.
     * 
     * @since 1.1.0
     * @version 1.1.0
     * @param dataSource 
     */
    public void setDataSource(String dataSource ){
        this.dataSource = dataSource;
    }
    
}
