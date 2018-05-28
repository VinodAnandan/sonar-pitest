Sonar Pitest Plugin
===================

[![Build Status](https://travis-ci.org/VinodAnandan/sonar-pitest.svg?branch=master)](https://travis-ci.org/VinodAnandan/sonar-pitest)  
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/quality_gate?project=org.sonarsource.pitest%3Asonar-pitest-plugin)](https://sonarcloud.io/api/project_badges/quality_gate?project=org.sonarsource.pitest%3Asonar-pitest-plugin)


Compatibility Matrix
--------------------
| Sonarqube version | Pitest plugin version |
|-------------------|-----------------------|
| Sonarqube 7.1  | sonar pitest 1.0-SNAPSHOT ([download](https://github.com/SonarQubeCommunity/sonar-pitest/releases/tag/1.0-SNAPSHOT)) |
| Sonarqube 6.7 (LTS)  | sonar pitest 0.9 ([download](https://github.com/SonarQubeCommunity/sonar-pitest/releases/tag/0.9)) |
| Sonarqube 6.5  | sonar pitest 0.8 ([download](https://github.com/SonarQubeCommunity/sonar-pitest/releases/tag/0.8)) |
| Sonarqube 5.6.X  | sonar pitest 0.7 ([download](https://github.com/SonarQubeCommunity/sonar-pitest/releases/tag/0.7)) |



Contributing
------------
### Pull Request (PR)

To submit a contribution, create a pull request for this repository. Please make sure that you follow the SonarQube Developer Guidelines [code style](https://github.com/SonarSource/sonar-developer-toolset#code-style) and all tests are passing.

Description / Features
----------------------
PIT is a mutation testing tool for java. You can check out the official pitest web site for more details on mutation testing and PIT.
Long story short, mutation testing is a very smart way to check the relevance of unit tests. The main idea is to alter the tested code and check that at least one unit test fails. An alteration of the code is called a "mutant". A mutant has "survived" the tests if there is no test failure. A mutant is "killed" if there is a test failure when the tests are executed on the mutated code.

The goal of this plugin is to bring PIT results to SonarQube. "Survived mutants" are seen as SonarQube issues. "Killed mutants" show as a coverage measure in the class containing the mutant. If code is not covered by any test, that code will not be mutated; prior test coverage is a precondition for mutation testing. Finally, if the percentage of "Survived mutants" in a source file exceeds a configurable threshold, the plugin creates a SonarQube issue on the source file.


Usage
-----
### Notes on mutation testing
Mutation testing should only be executed on true unit tests. Do not try to use it on tests accessing resources such as a database or filesystem, as the mutation may can unexpected consequences.   
Mutation testing can be computationally expensive.  Henry Coles, creator of PIT, provides the following tips to manage PIT execution time:
* Target only specific portions of your codebase (using the class filters)
* Limit the mutation operators used
* Limit the number of mutations per class
* Tweak the number of threads

### Configuration
The sonar-pitest plugin exposes two rules:
* "Survived mutant", which creates an issue (of TYPE BUG and SEVERITY MAJOR) whenever Mutated code does not result in a test failure
* "Insufficient Mutation Coverage", which creates an an issue (of TYPE BUG and SEVERITY MAJOR) whenever the percentage of Survived mutants exceeds a configurable threshold (default: 65%)

Both rules are inactive by default 

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
Once configured in the maven pom file, you need to run PITusing the following command:  

    mvn org.pitest:pitest-maven:mutationCoverage

Note : Of course, all the configuration options are clearly explained in the official pitest documentation.
Last but not least, you need to run a SonarQube analysis with the PIT plugin activated in "reuseReport" mode (the default). The following command would do the job:

    mvn sonar:sonar

By default SonarQube will search the latest PIT report in "target/pit-reports". You can specify another location using property "sonar.pitest.reportsDirectory".
You will find below the list of all the available configuration parameters.

### Basic configuration properties
Below the exhaustive list of configuration properties of the SonarQube pitest plugin:

| Name | Key | Default value | Description |
|------|-----|---------------|-------------|
| Pitest activation mode | sonar.pitest.mode | reuseReport | Possible values : 'skip' and 'reuseReport' |
| Path to the pitest reports | sonar.pitest.reportsDirectory | target/pit-reports |Path used to locate pitest xml reports. Pitest creates a new subfolder "timestamp" at each shot. The SonarQube plugin will explore these subfolders and find the newest xml reports generated. |

You can check out the quickstart section of the official pitest web site for detailed instructions.
