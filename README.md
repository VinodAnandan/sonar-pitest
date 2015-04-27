Sonar Pitest Plugin
===================

Description / Features
----------------------
PIT is a mutation testing tool for java. You can check out the official pitest web site for more details on mutation testing and PIT. 
Long story short, mutation testing is a very smart way to check the relevance of unit tests. The main idea is to alter the tested code and check that at least one unit test fails. An alteration of the code is called a "mutant". A mutant has "survived" the tests if there is no test failure. 
The goal of this plugin is to bring PIT results to SonarQube. Right now the integration of these results is quite simple, "survived mutants" on code covered by tests are seen as SonarQube issues.  
Even if PIT detects "survived mutants" on uncovered lines of code, these mutants are simply ignored by the plugin. 

Usage
-----
### Limitations of mutation testing
This section is not specific to PIT but since mutation testing is not yet a mainstream method... Here are a couple of general advises and warnings:
Mutation testing is very CPU time expensive. It is really important to control the scope of mutation testing in order to keep acceptable SonarQube analysis times. See below for tips on analysis time.
Mutation testing works on true unit tests. Do not try to use it on integration tests, you might mess up your database, file system, whatever external system used by your integration tests.  


Henri Coles, creator of PIT, gave very useful tips on the PIT mailing list to help reduce PIT execution time:
* Target only specific portions of your codebase (using the class filters)
* Limit the mutation operators used
* Limit the number of mutations per class
* Tweak the number of threads 
* Check out the "Advanced configuration properties" at the bottom of this page to address the above points.

### Configuration
Since mutation testing is not (yet) officially supported by SonarQube, this plugin acts as a "single rule" rule engine... This rule, named "Survived mutant", is disabled by default and hence needs to be activated when pitest is used. 

### Project build setup
**PIT needs to be launched before SonarQube**
You can launch PIT using the PIT maven plugin or the command line runner. PIT execution must be done before SonarQube analysis. You also need to specify the "reuseReport" mode of the PIT SonarQube plugin. 
Pit needs to be configured in order to generate XML reports. Be aware that PIT default behavior is to generate HTML reports.  Below a simple configuration example for maven :

    <!-- inside the build/plugins section -->
    <plugin>
      <groupId>org.pitest</groupId>
      <artifactId>pitest-maven</artifactId>
      <version>LATEST</version>
      <configuration>
        <inScopeClasses>
          <param>com.acme.tools.commons*</param>
        </inScopeClasses>
        <targetClasses>
          <param>com.acme.tools.commons*</param>
        </targetClasses>
        <outputFormats>
          <outputFormat>XML</outputFormat>	
        </outputFormats>
      </configuration>
    </plugin>
    
inScopeClasses and targetClasses parameters indicated the classes of the system under test where mutations can be performed. In the example above, all the classes from the com.acme.tools.commons package and sub packages may be altered.  
Once configured in the maven pom file, you need to run PITusing the following command "mvn org.pitest:pitest-maven:mutationCoverage". 
Note : Of course, all the configuration options are clearly explained in the official pitest documentation. 
Last but not least, you need to run a SonarQube analysis with the PIT plugin activated in "reuseReport" mode. The following command would do the job:

    "mvn sonar:sonar -Dsonar.pitest.mode=reuseReport"

By default SonarQube will search the latest PIT report in "target/pit-reports". You can specify another location using property "sonar.pitest.reportsDirectory". 
You will find below the list of all the available configuration parameters. 

### Basic configuration properties
Below the exhaustive list of configuration properties of the SonarQube pitest plugin:

| Name | Key | Default value | Description |
|------|-----|---------------|-------------|
| Pitest activation mode | sonar.pitest.mode | skip | Possible values : 'skip' and 'reuseReport' |
| Path to the pitest reports | sonar.pitest.reportsDirectory | target/pit-reports |Path used to locate pitest xml reports. Pitest creates a new subfolder "timestamp" at each shot. The SonarQube plugin will explore these subfolders and find the newest xml reports generated. |

You can check out the quickstart section of the official pitest web site for detailed instructions.