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
import org.sonar.api.rules.Rule;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
  private final ResultParser parser;
  private final ReportFinder reportFinder;
  private final String executionMode;
  private final RulesProfile rulesProfile;
  private final FileSystem fileSystem;
  private final FilePredicate mainFilePredicate;
  private final ResourcePerspectives perspectives;
  private final Map<InputFile, SourceFileMutants> mutantsForResources = new HashMap<InputFile, SourceFileMutants>();

  public PitestSensor(Settings settings, ResultParser parser, RulesProfile rulesProfile, ReportFinder reportFinder, FileSystem fileSystem, ResourcePerspectives perspectives) {
    this.settings = settings;
    this.parser = parser;
    this.reportFinder = reportFinder;
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
    if (activeRules.isEmpty()) { // ignore violations from report, if rule not activated in Sonar
      LOG.warn("/!\\ PIT rule needs to be activated in the \"{}\" profile.", rulesProfile.getName());
      LOG.warn("Checkout plugin documentation for more detailed explanations: http://docs.codehaus.org/display/SONAR/Pitest");
    }

    java.io.File projectDirectory = fileSystem.baseDir();
    String reportDirectoryPath = settings.getString(REPORT_DIRECTORY_KEY);

    java.io.File reportDirectory = new java.io.File(projectDirectory, reportDirectoryPath);
    java.io.File xmlReport = reportFinder.findReport(reportDirectory);
    if (xmlReport == null) {
      LOG.warn("No XML PIT report found in directory {} !", reportDirectory);
      LOG.warn("Checkout plugin documentation for more detailed explanations: http://docs.codehaus.org/display/SONAR/Pitest");
    } else {
      Collection<Mutant> mutants = parser.parse(xmlReport);
      saveMutantsInfo(mutants, context, activeRules);
    }
  }

  private void saveMutantsInfo(Collection<Mutant> mutants, SensorContext context, List<ActiveRule> activeRules) {
    collectMutantsForAllResources(mutants, activeRules);
    for (Entry<InputFile, SourceFileMutants> entry : mutantsForResources.entrySet()) {
      saveFileMutantsData(context, entry.getKey(), entry.getValue());
    }
  }

  private void saveFileMutantsData(SensorContext context, InputFile resource, SourceFileMutants fileMutants) {
    double detected = fileMutants.getMutationsDetected();
    double total = fileMutants.getMutationsTotal();
    context.saveMeasure(resource, PitestMetrics.MUTATIONS_TOTAL, total);
    context.saveMeasure(resource, PitestMetrics.MUTATIONS_NO_COVERAGE, fileMutants.getMutationsNoCoverage());
    context.saveMeasure(resource, PitestMetrics.MUTATIONS_KILLED, fileMutants.getMutationsKilled());
    context.saveMeasure(resource, PitestMetrics.MUTATIONS_SURVIVED, fileMutants.getMutationsSurvived());
    context.saveMeasure(resource, PitestMetrics.MUTATIONS_MEMORY_ERROR, fileMutants.getMutationsMemoryError());
    context.saveMeasure(resource, PitestMetrics.MUTATIONS_TIMED_OUT, fileMutants.getMutationsTimedOut());
    context.saveMeasure(resource, PitestMetrics.MUTATIONS_UNKNOWN, fileMutants.getMutationsUnknown());
    context.saveMeasure(resource, PitestMetrics.MUTATIONS_DETECTED, detected);

    String json = fileMutants.toJSON();
    Measure measure = new Measure(PitestMetrics.MUTATIONS_DATA, json);
    context.saveMeasure(resource, measure);

  }

  private void collectMutantsForAllResources(Collection<Mutant> mutants, List<ActiveRule> activeRules) {
    Rule rule = getSurvivedRule(activeRules); // Currently, only survived rule is applied

    for (Mutant mutant : mutants) {
      FilePredicate filePredicate =
        fileSystem.predicates().and(
          fileSystem.predicates().hasType(InputFile.Type.MAIN),
          fileSystem.predicates().matchesPathPattern("**/" + mutant.sourceRelativePath())
        );
      InputFile inputFile = fileSystem.inputFile(filePredicate);

      if (inputFile == null) {
        LOG.warn("Mutation in an unknown resource: {}", mutant.sourceRelativePath());
        LOG.debug("Mutant: {}", mutant);
      }
      else {
        processMutant(mutant, getResourceMutants(inputFile), inputFile, rule);
      }
    }
  }

  private Rule getSurvivedRule(List<ActiveRule> activeRules) {
    Rule rule = null;
    if (activeRules != null && !activeRules.isEmpty()) {
      rule = activeRules.get(0).getRule();
    }
    return rule;
  }

  private void processMutant(Mutant mutant, SourceFileMutants resourceMetricsInfo, InputFile resource, Rule rule) {
    resourceMetricsInfo.addMutant(mutant);
    if (resource != null && rule != null && MutantStatus.SURVIVED.equals(mutant.mutantStatus)) {
      // Only survived mutations are saved as violations
      Issuable issuable = perspectives.as(Issuable.class, resource);
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

  private SourceFileMutants getResourceMutants(InputFile resource) {
    SourceFileMutants metricsInfo = null;
    if (resource != null) {
      metricsInfo = mutantsForResources.get(resource);
      if (metricsInfo == null) {
        metricsInfo = new SourceFileMutants();
        mutantsForResources.put(resource, metricsInfo);
      }
    }
    return metricsInfo;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
