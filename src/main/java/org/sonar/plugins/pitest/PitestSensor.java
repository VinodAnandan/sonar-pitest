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

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.measures.Measure;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;

import java.io.File;
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

  private JavaFileMutants noResourceMetrics = new JavaFileMutants();

  private final Configuration configuration;
  private final ResultParser parser;
  private final ReportFinder reportFinder;
  private final String executionMode;
  private final RulesProfile rulesProfile;
  private final FileSystem fileSystem;
  private final ResourcePerspectives perspectives;

  public PitestSensor(Configuration configuration, ResultParser parser, RulesProfile rulesProfile, ReportFinder reportFinder, FileSystem fileSystem, ResourcePerspectives perspectives) {
    this.configuration = configuration;
    this.parser = parser;
    this.reportFinder = reportFinder;
    this.fileSystem = fileSystem;
    this.perspectives = perspectives;
    this.executionMode = configuration.getString(MODE_KEY, MODE_SKIP);
    this.rulesProfile = rulesProfile;
  }

  public boolean shouldExecuteOnProject(Project project) {
    return project.getAnalysisType().isDynamic(true)
        && fileSystem.languages().contains("java")
        && !MODE_SKIP.equals(executionMode);
  }

  public void analyse(Project module, SensorContext context) {
    List<ActiveRule> activeRules = rulesProfile.getActiveRulesByRepository(REPOSITORY_KEY);
    if (activeRules.isEmpty()) { // ignore violations from report, if rule not activated in Sonar
      LOG.warn("/!\\ PIT rule needs to be activated in the \"{}\" profile.", rulesProfile.getName());
      LOG.warn("Checkout plugin documentation for more detailed explanations: http://docs.codehaus.org/display/SONAR/Pitest");
    }

    File projectDirectory = fileSystem.baseDir();
    String reportDirectoryPath = configuration.getString(REPORT_DIRECTORY_KEY, REPORT_DIRECTORY_DEF);

    File reportDirectory = new File(projectDirectory, reportDirectoryPath);
    File xmlReport = reportFinder.findReport(reportDirectory);
    if (xmlReport == null) {
      LOG.warn("No XML PIT report found in directory {} !", reportDirectory);
      LOG.warn("Checkout plugin documentation for more detailed explanations: http://docs.codehaus.org/display/SONAR/Pitest");
    } else {
      LOG.info("Analyzing with report: {}", xmlReport);
      Collection<Mutant> mutants = parser.parse(xmlReport);
      saveMutantsInfo(mutants, context, activeRules, module);
    }
  }

  private void saveMutantsInfo(Collection<Mutant> mutants, SensorContext context, List<ActiveRule> activeRules, Project module) {
    Map<Resource, JavaFileMutants> metrics = collectMetrics(mutants, context, activeRules, module);
    for (Entry<Resource, JavaFileMutants> entry : metrics.entrySet()) {
      saveMetricsInfo(context, entry.getKey(), entry.getValue());
    }
  }

  private void saveMetricsInfo(SensorContext context, Resource resource, JavaFileMutants metricsInfo) {
    double detected = metricsInfo.getMutationsDetected();
    double total = metricsInfo.getMutationsTotal();
    context.saveMeasure(resource, PitestMetrics.MUTATIONS_TOTAL, total);
    context.saveMeasure(resource, PitestMetrics.MUTATIONS_NO_COVERAGE, metricsInfo.getMutationsNoCoverage());
    context.saveMeasure(resource, PitestMetrics.MUTATIONS_KILLED, metricsInfo.getMutationsKilled());
    context.saveMeasure(resource, PitestMetrics.MUTATIONS_SURVIVED, metricsInfo.getMutationsSurvived());
    context.saveMeasure(resource, PitestMetrics.MUTATIONS_MEMORY_ERROR, metricsInfo.getMutationsMemoryError());
    context.saveMeasure(resource, PitestMetrics.MUTATIONS_TIMED_OUT, metricsInfo.getMutationsTimedOut());
    context.saveMeasure(resource, PitestMetrics.MUTATIONS_UNKNOWN, metricsInfo.getMutationsUnknown());
    context.saveMeasure(resource, PitestMetrics.MUTATIONS_DETECTED, detected);
    saveData(context, resource, metricsInfo.getMutants());
  }

  private void saveData(SensorContext context, Resource resource, List<Mutant> mutants) {
    if ((mutants != null) && (!mutants.isEmpty())) {
      String json = Mutant.toJSON(mutants);
      Measure measure = new Measure(PitestMetrics.MUTATIONS_DATA, json);
      context.saveMeasure(resource, measure);
    }
  }

  private Map<Resource, JavaFileMutants> collectMetrics(Collection<Mutant> mutants, SensorContext context, List<ActiveRule> activeRules, Project module) {
    Map<Resource, JavaFileMutants> metricsByResource = new HashMap<Resource, JavaFileMutants>();
    Rule rule = getSurvivedRule(activeRules); // Currently, only survived rule is applied
    for (Mutant mutant : mutants) {
      String pattern = "**" + mutant.getSonarJavaFileKey().replace('.', '/') + ".java";
      FilePredicate predicate = fileSystem.predicates().matchesPathPattern(pattern);
      List<File> matchedFiles = Lists.newArrayList(fileSystem.files(predicate));

      if(matchedFiles.size() > 1) {
        LOG.warn("Multiple files for {}, using first: {}", pattern, matchedFiles);
      }

      File firstFile = Iterables.getFirst(matchedFiles, null);
      Resource resource = null;

      if(firstFile != null) {
        resource = context.getResource(org.sonar.api.resources.File.fromIOFile(firstFile, module));
      }

      if (resource == null) {
        LOG.warn("Mutation in an unknown resource: {}", mutant.getSonarJavaFileKey());
        LOG.debug("Mutant: {}", mutant);
        processMutant(mutant, noResourceMetrics, resource, context, rule);
      }
      else {
        processMutant(mutant, getMetricsInfo(metricsByResource, resource), resource, context, rule);
      }
    }
    return metricsByResource;
  }

  private Rule getSurvivedRule(List<ActiveRule> activeRules) {
    Rule rule = null;
    if (activeRules != null && !activeRules.isEmpty()) {
      rule = activeRules.get(0).getRule();
    }
    return rule;
  }

  private void processMutant(Mutant mutant, JavaFileMutants resourceMetricsInfo, Resource resource, SensorContext context, Rule rule) {
    resourceMetricsInfo.addMutant(mutant);
    if (resource != null && rule != null && MutantStatus.SURVIVED.equals(mutant.getMutantStatus())) {
      // Only survived mutations are saved as violations
      Issuable issuable = perspectives.as(Issuable.class, resource);
      if (issuable != null) {
        // can be used
        Issue issue
            = issuable.newIssueBuilder()
                .ruleKey(RuleKey.of(PitestConstants.REPOSITORY_KEY, PitestConstants.RULE_KEY))
                .line(mutant.getLineNumber())
                .message(mutant.getViolationDescription())
                .build();
        issuable.addIssue(issue);
      }
    }
  }

  private static JavaFileMutants getMetricsInfo(Map<Resource, JavaFileMutants> metrics, Resource resource) {
    JavaFileMutants metricsInfo = null;
    if (resource != null) {
      metricsInfo = metrics.get(resource);
      if (metricsInfo == null) {
        metricsInfo = new JavaFileMutants();
        metrics.put(resource, metricsInfo);
      }
    }
    return metricsInfo;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
