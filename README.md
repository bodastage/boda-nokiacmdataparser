![Build status](https://travis-ci.org/bodastage/boda-huaweicmobjectparser.svg?branch=master)

# boda-nokiacmdataparser
Parses Nokia RAN configuration data XML files to csv. It parses 2G, 3G, and 4G configuration management XML files.

Below is the expected format of the input file:

```XML
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE raml SYSTEM 'raml20.dtd'>
<raml version="2.0" xmlns="raml20.xsd">
  <cmData type="actual">
    <header>
      <log dateTime="2015-11-04T17:41:27" action="created" appInfo="ActualExporter">UIValues are used</log>
    </header>
    <managedObject class="XXX" version="XXX" distName="XXXX" id="XXXXX">
      <p name="name">XXXX</p>
      ...
    </managedObject>
    <managedObject class="XXX" version="XXX" distName="XXXX" id="XXXXX">
      <p name="XXX">XXXX</p>
      <list name="XXX">
        <item>
          <p name="XXX">XXX</p>
          <p name="XXX">XXX</p>
        </item>
        <item>
          <p name="XXX">XXX</p>
          <p name="XXX">XXX</p>
        </item>
      </list>
      ...
    </managedObject>
   </cmData>
</raml>
```
# Usage
```
usage: java -jar boda-nokiacmdataparser.jar
Parses Nokia RAML20 configuration management XML data files to csv.

 -c,--parameter-config <PARAMETER_CONFIG>   parameter configuration file
 -h,--help                                  show help
 -i,--input-file <INPUT_FILE>               input file or directory name
 -o,--output-directory <OUTPUT_DIRECTORY>   output directory name
 -p,--extract-parameters                    extract only the managed
                                            objects and parameters
 -v,--version                               display version

Examples:
java -jar boda-nokiacmdataparser.jar -i raml20_dump.xml -o out_folder
java -jar boda-nokiacmdataparser.jar -i input_folder -o out_folder
```

# Download and installation
The lastest compiled jar file is availabled in the dist directory. Alternatively, download it directly from [here](https://github.com/bodastage/boda-nokiacmdataparser/raw/master/dist/boda-nokiacmdataparser.jar).

# Requirements
To run the jar file, you need Java version 1.8 and above.

# Getting help
To report issues with the application or request new features use the issue [tracker](https://github.com/bodastage/boda-nokiacmdataparser/issues). For help and customizations send an email to info@bodastage.com.

# Credits
[Bodastage](http://www.bodastage.com) - info@bodastage.com

# Contact
For any other concerns apart from issues and feature requests, send an email to info@bodastage.com.

# Licence
This project is licensed under the Apache 2.0 licence.  See LICENCE file for details.
