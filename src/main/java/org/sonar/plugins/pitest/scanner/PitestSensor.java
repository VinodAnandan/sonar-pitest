/*
 * Sonar Pitest Plugin
 * Copyright (C) 2009-2017 Vinod Anandan
 * vinod@owasp.org
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.pitest.scanner;

import java.io.Serializable;
import java.util.Collection;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.measure.Metric;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.config.Configuration;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.pitest.domain.Mutant;
import org.sonar.plugins.pitest.domain.MutantStatus;

import static org.sonar.plugins.pitest.PitestConstants.COVERAGE_RATIO_PARAM;
import static org.sonar.plugins.pitest.PitestConstants.INSUFFICIENT_MUTATION_COVERAGE_RULE_KEY;
import static org.sonar.plugins.pitest.PitestConstants.MODE_KEY;
import static org.sonar.plugins.pitest.PitestConstants.MODE_SKIP;
import static org.sonar.plugins.pitest.PitestConstants.REPORT_DIRECTORY_KEY;
import static org.sonar.plugins.pitest.PitestConstants.REPOSITORY_KEY;
import static org.sonar.plugins.pitest.PitestConstants.SURVIVED_MUTANT_RULE_KEY;

/**
 * Sonar sensor for pitest mutation coverage analysis.
 *
 * <a href="mailto:aquiporras@gmail.com">Jaime Porras L&oacute;pez</a>
 * <a href="mailto:vinod@owasp.org">Alexandre Victoor</a>
 */
public class PitestSensor implements Sensor {

  private static final Logger LOGGER = Loggers.get(PitestSensor.class);
  static final String SENSOR_NAME = "Pitest Sensor";

  private final Configuration configuration;
  private final XmlReportParser parser;
  private final RulesProfile rulesProfile;
  private final XmlReportFinder xmlReportFinder;
  private final FileSystem fileSystem;
  private final String executionMode;
  private final FilePredicate fileSystemExecutionPredicate;

  public PitestSensor(Configuration configuration, XmlReportParser parser, RulesProfile rulesProfile, XmlReportFinder xmlReportFinder, FileSystem fileSystem) {
    this.configuration = configuration;
    this.parser = parser;
    this.rulesProfile = rulesProfile;
    this.xmlReportFinder = xmlReportFinder;
    this.fileSystem = fileSystem;
    this.executionMode = configuration.get(MODE_KEY).orElse(null);
    this.fileSystemExecutionPredicate = fileSystem.predicates().and(
      fileSystem.predicates().hasType(InputFile.Type.MAIN),
      fileSystem.predicates().hasLanguages("java"));
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.name(SENSOR_NAME);
    descriptor.createIssuesForRuleRepository(REPOSITORY_KEY);
    descriptor.onlyOnFileType(InputFile.Type.MAIN);
    descriptor.onlyOnLanguages("java");
  }

  @Override
  public void execute(SensorContext context) {
    if (!fileSystem.hasFiles(fileSystemExecutionPredicate)) {
      LOGGER.debug("file system execution predicate not satisfied {}. returning", fileSystemExecutionPredicate);
      return;
    }

    if (MODE_SKIP.equals(executionMode)) {
      LOGGER.debug("executionMode is skip. returning");
      return;
    }

    java.io.File projectDirectory = fileSystem.baseDir();
    String reportDirectoryPath = configuration.get(REPORT_DIRECTORY_KEY).orElse(null);

    java.io.File reportDirectory = new java.io.File(projectDirectory, reportDirectoryPath);
    java.io.File xmlReport = xmlReportFinder.findReport(reportDirectory);
    if (xmlReport == null) {
      LOGGER.warn("No XML PIT report found in directory {} !", reportDirectory);
      LOGGER.warn("Checkout plugin documentation for more detailed explanations: https://github.com/SonarQubeCommunity/sonar-pitest");
      return;
    }

    Collection<Mutant> mutants = parser.parse(xmlReport);
    processProjectReport(new ProjectReport(mutants), context);
  }

  private void processProjectReport(ProjectReport projectReport, SensorContext context) {
    Collection<SourceFileReport> sourceFileReports = projectReport.getSourceFileReports();

    for (SourceFileReport sourceFileReport : sourceFileReports) {
      InputFile inputFile = locateFile(sourceFileReport.getRelativePath());
      if (inputFile == null) {
        LOGGER.warn("Mutation in an unknown resource: {}", sourceFileReport.getRelativePath());
        continue;
      }

      if (isMutantRuleActive(rulesProfile)) {
        addIssueForSurvivingMutants(context, inputFile, sourceFileReport);
      }

      ActiveRule coverageRule = rulesProfile.getActiveRule(REPOSITORY_KEY, INSUFFICIENT_MUTATION_COVERAGE_RULE_KEY);
      if (coverageRule != null && !isMutantCoverageThresholdReached(sourceFileReport, coverageRule)) {
        addIssueForMutantKilledThresholdNotReached(context, inputFile, coverageRule.getParameter(COVERAGE_RATIO_PARAM));
      }

      if (sourceFileReport.getMutationsKilled() > 0) {
        addCoverageForKilledMutants(context, inputFile, sourceFileReport);
      }

      // FIXME: figure out why this causes analysis to fail
//      saveMeasureOnFile(context, inputFile, PitestMetrics.MUTATIONS_NOT_COVERED, sourceFileReport.getMutationsNoCoverage());
//      saveMeasureOnFile(context, inputFile, PitestMetrics.MUTATIONS_GENERATED, sourceFileReport.getMutationsTotal());
//      saveMeasureOnFile(context, inputFile, PitestMetrics.MUTATIONS_KILLED, sourceFileReport.getMutationsKilled());
//      saveMeasureOnFile(context, inputFile, PitestMetrics.MUTATIONS_SURVIVED, sourceFileReport.getMutationsSurvived());
//      saveMeasureOnFile(context, inputFile, PitestMetrics.MUTATIONS_ERROR, sourceFileReport.getMutationsOther());
//      saveMeasureOnFile(context, inputFile, PitestMetrics.MUTATIONS_UNKNOWN, sourceFileReport.getMutationsUnknown());
//      saveMeasureOnFile(context, inputFile, PitestMetrics.MUTATIONS_DATA, sourceFileReport.toJSON());
    }
  }

  private <T extends Serializable> void saveMeasureOnFile(SensorContext context, InputFile inputFile, Metric<T> metric, T value) {
    context.<T>newMeasure()
      .withValue(value)
      .forMetric(metric)
      .on(inputFile)
      .save();
  }

  private boolean isMutantCoverageThresholdReached(SourceFileReport sourceFileReport, ActiveRule coverageRule) {
    int killed = sourceFileReport.getMutationsKilled();
    int total = sourceFileReport.getMutationsTotal();
    int threshold = Integer.parseInt(coverageRule.getParameter(COVERAGE_RATIO_PARAM));

    int scaledKilledPercent = new Double(killed * 100d / total).intValue();
    return scaledKilledPercent >= threshold;
  }

  private void addIssueForMutantKilledThresholdNotReached(SensorContext context, InputFile inputFile, String threshold) {
    String issueMsg = "More mutants need to be covered by unit tests to reach the minimum threshold of " + threshold + "% mutant coverage";

    NewIssue newIssue = context.newIssue();

    NewIssueLocation location = newIssue.newLocation()
      .on(inputFile)
      .message(issueMsg);

    newIssue.at(location);

    newIssue.forRule(RuleKey.of(REPOSITORY_KEY, INSUFFICIENT_MUTATION_COVERAGE_RULE_KEY));
    newIssue.save();
  }

  private void addIssueForSurvivingMutants(SensorContext context, InputFile inputFile, SourceFileReport sourceFileReport) {
    Collection<Mutant> mutants = sourceFileReport.getMutants();
    for (Mutant mutant : mutants) {

      if (MutantStatus.SURVIVED.equals(mutant.mutantStatus)) {
        NewIssue newIssue = context.newIssue()
          .forRule(RuleKey.of(REPOSITORY_KEY, SURVIVED_MUTANT_RULE_KEY));

        NewIssueLocation location = newIssue.newLocation()
          .on(inputFile)
          .at(inputFile.selectLine(mutant.lineNumber()))
          .message(mutant.violationDescription());

        newIssue.at(location);
        newIssue.save();
      }
    }
  }

  private void addCoverageForKilledMutants(SensorContext context, InputFile inputFile, SourceFileReport sourceFileReport) {
    Collection<Mutant> mutants = sourceFileReport.getMutants();
    for (Mutant mutant : mutants) {
      if (MutantStatus.KILLED.equals(mutant.mutantStatus)) {
        context.newCoverage()
          .onFile(inputFile)
          .lineHits(mutant.lineNumber(), 1)
          .save();
      }
    }
  }

  private InputFile locateFile(String sourceFileRelativePath) {
    FilePredicate filePredicate = fileSystem.predicates().and(
      fileSystem.predicates().hasType(InputFile.Type.MAIN),
      fileSystem.predicates().matchesPathPattern("**/" + sourceFileRelativePath));
    return fileSystem.inputFile(filePredicate);
  }

  private boolean isMutantRuleActive(RulesProfile qualityProfile) {
    return (qualityProfile.getActiveRule(REPOSITORY_KEY, SURVIVED_MUTANT_RULE_KEY) != null);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
