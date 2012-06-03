/*
 * Sonar Pitest Plugin
 * Copyright (C) 2009 Alexandre Victoor
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.pitest;

import static org.sonar.plugins.pitest.PitestConstants.*;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.predicate.Predicate;
import org.pitest.internal.ClassPathByteArraySource;
import org.pitest.mutationtest.Mutator;
import org.pitest.mutationtest.ReportOptions;
import org.pitest.mutationtest.config.ConfigurationFactory;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.report.OutputFormat;
import org.pitest.testng.TestGroupConfig;
import org.pitest.util.Functions;
import org.pitest.util.Glob;

import org.apache.commons.configuration.Configuration;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.BatchExtension;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.utils.SonarException;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;


public class ReportOptionsBuilder implements BatchExtension {
  
  private static final Logger LOG = LoggerFactory.getLogger(ReportOptionsBuilder.class);

  private final Configuration configuration;
  private final ProjectFileSystem fileSystem;
  private final MavenProject mvnProject;
  private final PitestConfigurationBuilder configurationBuilder;

  public ReportOptionsBuilder(ProjectFileSystem fileSystem, MavenProject mvnProject, Configuration configuration, PitestConfigurationBuilder configurationBuilder) {
    this.fileSystem = fileSystem;
    this.mvnProject = mvnProject;
    this.configuration = configuration;
    this.configurationBuilder = configurationBuilder;
  }
  
  
  public ReportOptions build() {
    final ReportOptions data = new ReportOptions();
    
    String codePath = fileSystem.getBuildOutputDir().getAbsolutePath();
    LOG.info("Mutating from {}", codePath);
    data.setCodePaths(Collections.singleton(codePath));

    Collection<String> classPathElements;
    if (mvnProject==null) {
      classPathElements = Arrays.asList(configuration.getStringArray(CLASSPATH));
    } else {
      classPathElements = Lists.newArrayList();
      try {
        classPathElements.addAll(mvnProject.getTestClasspathElements());
        classPathElements.addAll(mvnProject.getCompileClasspathElements());
        classPathElements.addAll(mvnProject.getRuntimeClasspathElements());
        classPathElements.addAll(mvnProject.getSystemClasspathElements());
      } catch (DependencyResolutionRequiredException e) {
        throw new SonarException(e); // should not happen
      }
      addOwnDependenciesToClassPath(classPathElements);
    }
    data.setClassPathElements(classPathElements);
    
    data.setDependencyAnalysisMaxDistance(configuration.getInt(MAX_DEPENDENCY_DISTANCE, -1));  //this.mojo.getMaxDependencyDistance());
    data.setFailWhenNoMutations(configuration.getBoolean(FAIL_WHEN_NO_MUTATIONS, true)); // this.mojo.isFailWhenNoMutations());

    data.setTargetClasses(determineTargetClasses());
    data.setTargetTests(determineTargetTests());
   
    data.setMutateStaticInitializers(configuration.getBoolean(MUTATE_STATIC_INITIALIZERS, false));
    data.setExcludedMethods(globStringsToPredicates(getConfigurationValues(EXCLUDED_METHODS)));
    data.setExcludedClasses(globStringsToPredicates(getConfigurationValues(EXCLUDED_CLASSES)));
    data.setNumberOfThreads(configuration.getInt(THREADS, 1));
    data.setMaxMutationsPerClass(configuration.getInt(MAX_MUTATIONS_PER_CLASS, -1));

    final File reportDirectory;
    String reportDirectoryPath =  configuration.getString(REPORT_DIRECTORY_KEY); 
    if (Strings.isNullOrEmpty(reportDirectoryPath)) {
      // ${project.build.directory}/pit-reports
      reportDirectory = new File(fileSystem.getBuildDir(), "pit-reports");
    } else {
      reportDirectory = new File(reportDirectoryPath);
    }
    
    // TODO do we need to check the existence of this directory?
    data.setReportDir(reportDirectory.getAbsolutePath());
    
    if (LOG.isDebugEnabled()) {
      data.setVerbose(true);
    }
    
    List<String> jvmArgs = getConfigurationValues(JVM_ARGS);
    if (!jvmArgs.isEmpty()) {
      data.addChildJVMArgs(jvmArgs);
    }

    data.setMutators(determineMutators());
    
    data.setTimeoutConstant(configuration.getLong(TIMEOUT_CONSTANT, 3000));
    data.setTimeoutFactor(configuration.getFloat(TIMEOUT_FACTOR, 1.25f));
    
    List<String> avoidCallsTo = getConfigurationValues(AVOID_CALLS_TO);
    if (!avoidCallsTo.isEmpty()) {
      data.setLoggingClasses(avoidCallsTo);
    }

    
    List<File> sourceDirectories = Lists.newArrayList(fileSystem.getSourceDirs());
    sourceDirectories.addAll(fileSystem.getTestDirs());
    data.setSourceDirs(sourceDirectories);
    
    data.addOutputFormats(Lists.newArrayList(OutputFormat.XML));

    setTestType(data);

    return data;
  }

  private void addOwnDependenciesToClassPath(Collection<String> classPath) {
    File pitestJar = new File(fileSystem.getSonarWorkingDirectory(), PITEST_JAR_NAME);
    classPath.add(pitestJar.getAbsolutePath());
  }
  
  private void setTestType(ReportOptions data) {
    List<String> excludedTestNGGroups 
      = getConfigurationValues(EXCLUDED_TESTNG_GROUPS);
    List<String> includedTestNGGroups 
      = getConfigurationValues(INCLUDED_TESTNG_GROUPS);
    TestGroupConfig conf 
      = new TestGroupConfig(excludedTestNGGroups, includedTestNGGroups);
    data.setGroupConfig(conf);
    
    data.setConfiguration(configurationBuilder.build(data));
  }
  
  private Collection<Predicate<String>> globStringsToPredicates(
      final Collection<String> excludedMethods) {
    return FCollection.map(excludedMethods, Glob.toGlobPredicate());
  }
 
  private Collection<Predicate<String>> determineTargetTests() {
    return FCollection.map(getConfigurationValues(TARGET_TESTS), Glob.toGlobPredicate());
  }
  
  private Collection<MethodMutatorFactory> determineMutators() {
    List<String> mutators = getConfigurationValues(MUTATORS);
    final Collection<MethodMutatorFactory> result;
    if (mutators.isEmpty()) {
      result = Mutator.DEFAULTS.asCollection();
    } else {
      result = FCollection.flatMap(mutators, stringToMutators());
    }
    return result;
  }

  private F<String, Mutator> stringToMutators() {
    return new F<String, Mutator>() {
      public Mutator apply(final String a) {
        return Mutator.valueOf(a);
      }

    };
  }
  
  private List<String> getConfigurationValues(String key) {
    final List<String> values;
    if (configuration.getStringArray(key) == null) {
      values = Lists.newArrayList();
    } else {
      values = Lists.newArrayList(configuration.getStringArray(key));
    }
    return values;
  }
  
  private Collection<Predicate<String>> determineTargetClasses() {
    final Collection<String> filters = getConfigurationValues(TARGET_CLASSES);
    if (filters.isEmpty()) {
      if (mvnProject==null) {
        throw new SonarException("Incomplete configuration");
      }
      return Collections.<Predicate<String>> singleton(new Glob(mvnProject.getGroupId() + "*"));
    } else {
      return FCollection.map(filters, Glob.toGlobPredicate());
    }
  }
  
  public static class DependencyFilter implements Predicate<Artifact> {

    private final Set<String> allowedGroups = new HashSet<String>();

    public DependencyFilter(final String... groups) {
      this.allowedGroups.addAll(Arrays.asList(groups));
    }

    public Boolean apply(final Artifact a) {
      return this.allowedGroups.contains(a.getGroupId());
    }

  }
}
