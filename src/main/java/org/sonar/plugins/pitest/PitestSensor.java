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
import java.util.Collection;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Java;
import org.sonar.api.resources.JavaFile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.Violation;
import org.sonar.api.utils.SonarException;

public class PitestSensor implements Sensor {
  
  private static final Logger LOG = LoggerFactory.getLogger(PitestSensor.class);
  
  private final Configuration configuration;
  private final ResultParser parser;
  private final String executionMode;
  private final RuleFinder ruleFinder;
  
  public PitestSensor(Configuration configuration, ResultParser parser, RuleFinder ruleFinder) {
    this.configuration = configuration;
    this.parser = parser;
    this.ruleFinder = ruleFinder;
    this.executionMode = configuration.getString(MODE_KEY, MODE_SKIP);
  }

  public boolean shouldExecuteOnProject(Project project) {
    return project.getAnalysisType().isDynamic(true) 
      && Java.KEY.equals(project.getLanguageKey())
      && !MODE_SKIP.equals(executionMode);
  }

  public void analyse(Project project, SensorContext context) {
    
    if (MODE_ACTIVE.equals(executionMode)) {
      throw new SonarException("Not implemented yet");
    }
    
    File projectDirectory = project.getFileSystem().getBasedir();
    String reportDirectoryPath = configuration.getString(REPORT_DIRECTORY_KEY, REPORT_DIRECTORY_DEF);
    
    File reportDirectory = new File(projectDirectory, reportDirectoryPath);
    File xmlReport = findReport(reportDirectory);
    if (xmlReport == null) {
      LOG.warn("No pitest report found !");
    } else {
      Rule rule = ruleFinder.findByKey(REPOSITORY_KEY, RULE_KEY);
      if (rule != null) { // ignore violations from report, if rule not activated in Sonar
        Collection<Mutant> mutants = parser.parse(xmlReport);
        for (Mutant mutant : mutants) {
          JavaFile resource = new JavaFile(mutant.getSonarJavaFileKey());
          if (context.getResource(resource) != null) {
            Violation violation 
              = Violation.create(rule, resource).setLineId(mutant.getLineNumber()).setMessage(mutant.getMutator());
            context.saveViolation(violation);
          }
        }
      }
    }
  }

  private File findReport(File reportDirectory) {
    Collection<File> reports = FileUtils.listFiles(reportDirectory, new String[]{"xml"}, true);
    File latestReport = null;
    for (File report : reports) {
      if (latestReport == null || FileUtils.isFileNewer(report, latestReport)) {
        latestReport = report;
      }
    }
    return latestReport;
  }

}
