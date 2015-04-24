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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.measures.Measure;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rules.ActiveRule;

import java.util.Collection;
import java.util.List;

import static org.sonar.plugins.pitest.PitestConstants.*;

/**
 * Sonar sensor for pitest mutation coverage analysis.
 *
 * <a href="mailto:aquiporras@gmail.com">Jaime Porras L&oacute;pez</a>
 * <a href="mailto:alexvictoor@gmail.com">Alexandre Victoor</a>
 */
public class PitestSensor implements Sensor {

  private static final Logger LOG = LoggerFactory.getLogger(PitestSensor.class);

  private final Settings settings;
  private final XmlReportParser parser;
  private final XmlReportFinder xmlReportFinder;
  private final String executionMode;
  private final RulesProfile rulesProfile;
  private final FileSystem fileSystem;
  private final FilePredicate mainFilePredicate;
  private final ResourcePerspectives perspectives;

  public PitestSensor(Settings settings, XmlReportParser parser, RulesProfile rulesProfile, XmlReportFinder xmlReportFinder, FileSystem fileSystem, ResourcePerspectives perspectives) {
    this.settings = settings;
    this.parser = parser;
    this.xmlReportFinder = xmlReportFinder;
    this.fileSystem = fileSystem;
    this.perspectives = perspectives;
    this.executionMode = settings.getString(MODE_KEY);
    this.rulesProfile = rulesProfile;

    this.mainFilePredicate = fileSystem.predicates().and(
      fileSystem.predicates().hasType(InputFile.Type.MAIN),
      fileSystem.predicates().hasLanguage("java"));
  }

  public boolean shouldExecuteOnProject(Project project) {
    return fileSystem.hasFiles(mainFilePredicate) && !MODE_SKIP.equals(executionMode);
  }

  public void analyse(Project project, SensorContext context) {
    List<ActiveRule> activeRules = rulesProfile.getActiveRulesByRepository(REPOSITORY_KEY);
    final boolean generateViolations;
    if (activeRules.isEmpty()) { // ignore violations from report, if rule not activated in Sonar
      LOG.warn("/!\\ PIT rule needs to be activated in the \"{}\" profile.", rulesProfile.getName());
      LOG.warn("Checkout plugin documentation for more detailed explanations: http://docs.codehaus.org/display/SONAR/Pitest");
      generateViolations = false;
    } else {
      generateViolations = true;
    }

    java.io.File projectDirectory = fileSystem.baseDir();
    String reportDirectoryPath = settings.getString(REPORT_DIRECTORY_KEY);

    java.io.File reportDirectory = new java.io.File(projectDirectory, reportDirectoryPath);
    java.io.File xmlReport = xmlReportFinder.findReport(reportDirectory);
    if (xmlReport == null) {
      LOG.warn("No XML PIT report found in directory {} !", reportDirectory);
      LOG.warn("Checkout plugin documentation for more detailed explanations: http://docs.codehaus.org/display/SONAR/Pitest");
    } else {
      Collection<Mutant> mutants = parser.parse(xmlReport);
      ProjectReport projectReport = ProjectReport.buildFromMutants(mutants);
      processProjectReport(projectReport, context, generateViolations);
    }
  }

  private void processProjectReport(ProjectReport projectReport, SensorContext context, boolean generateViolations) {
    Collection<SourceFileReport> sourceFileReports = projectReport.getSourceFileReports();
    for (SourceFileReport sourceFileReport : sourceFileReports) {
      InputFile inputFile = locateFile(sourceFileReport.sourceFileRelativePath);
      if (inputFile == null) {
        LOG.warn("Mutation in an unknown resource: {}", sourceFileReport.sourceFileRelativePath);
        if (LOG.isDebugEnabled()) {
          LOG.debug("File report: {}", sourceFileReport.toJSON());
        }
      }
      else {
        if (generateViolations) {
          saveFileViolations(inputFile, sourceFileReport);
        }
        saveFileMeasures(context, inputFile, sourceFileReport);
      }
    }
  }

  private void saveFileMeasures(SensorContext context, InputFile inputFile, SourceFileReport sourceFileReport) {
    double detected = sourceFileReport.getMutationsDetected();
    double total = sourceFileReport.getMutationsTotal();
    context.saveMeasure(inputFile, PitestMetrics.MUTATIONS_TOTAL, total);
    context.saveMeasure(inputFile, PitestMetrics.MUTATIONS_NO_COVERAGE, sourceFileReport.getMutationsNoCoverage());
    context.saveMeasure(inputFile, PitestMetrics.MUTATIONS_KILLED, sourceFileReport.getMutationsKilled());
    context.saveMeasure(inputFile, PitestMetrics.MUTATIONS_SURVIVED, sourceFileReport.getMutationsSurvived());
    context.saveMeasure(inputFile, PitestMetrics.MUTATIONS_MEMORY_ERROR, sourceFileReport.getMutationsMemoryError());
    context.saveMeasure(inputFile, PitestMetrics.MUTATIONS_TIMED_OUT, sourceFileReport.getMutationsTimedOut());
    context.saveMeasure(inputFile, PitestMetrics.MUTATIONS_UNKNOWN, sourceFileReport.getMutationsUnknown());
    context.saveMeasure(inputFile, PitestMetrics.MUTATIONS_DETECTED, detected);

    String json = sourceFileReport.toJSON();
    Measure measure = new Measure(PitestMetrics.MUTATIONS_DATA, json);
    context.saveMeasure(inputFile, measure);
  }

  private void saveFileViolations(InputFile inputFile, SourceFileReport sourceFileReport) {
    Collection<Mutant> mutants = sourceFileReport.getMutants();
    for (Mutant mutant : mutants) {
      if (MutantStatus.SURVIVED.equals(mutant.mutantStatus)) {
        // Only survived mutations are saved as violations
        Issuable issuable = perspectives.as(Issuable.class, inputFile);
        if (issuable != null) {
          // can be used
          Issue issue
            = issuable.newIssueBuilder()
            .ruleKey(RuleKey.of(PitestConstants.REPOSITORY_KEY, PitestConstants.RULE_KEY))
            .line(mutant.lineNumber)
            .message(mutant.violationDescription())
            .build();
          issuable.addIssue(issue);
        }
      }
    }
  }

  private InputFile locateFile(String sourceFileRelativePath) {
    FilePredicate filePredicate =
      fileSystem.predicates().and(
        fileSystem.predicates().hasType(InputFile.Type.MAIN),
        fileSystem.predicates().matchesPathPattern("**/" + sourceFileRelativePath)
      );
    return fileSystem.inputFile(filePredicate);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
