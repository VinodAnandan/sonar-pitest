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

import static org.sonar.plugins.pitest.PitestPlugin.MODE_SKIP;
import static org.sonar.plugins.pitest.PitestPlugin.REPORT_DIRECTORY_DEF;
import static org.sonar.plugins.pitest.PitestPlugin.REPORT_DIRECTORY_KEY;
import static org.sonar.plugins.pitest.PitestRulesDefinition.REPOSITORY_KEY;
import static org.sonar.plugins.pitest.PitestRulesDefinition.RULE_SURVIVED_MUTANT;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
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
import org.sonar.plugins.pitest.metrics.JavaFileMutants;
import org.sonar.plugins.pitest.metrics.PitestMetrics;
import org.sonar.plugins.pitest.model.Mutant;
import org.sonar.plugins.pitest.model.MutantHelper;
import org.sonar.plugins.pitest.model.MutantStatus;

/**
 * Sonar sensor for pitest mutation coverage analysis.
 *
 * <a href="mailto:aquiporras@gmail.com">Jaime Porras L&oacute;pez</a> <a href="mailto:alexvictoor@gmail.com">Alexandre
 * Victoor</a>
 */
public class PitestSensor implements Sensor {

    private static final Logger LOG = LoggerFactory.getLogger(PitestSensor.class);

    private final JavaFileMutants noResourceMetrics = new JavaFileMutants();

    private final ResultParser parser;
    private final ReportFinder reportFinder;
    private final String executionMode;
    private final RulesProfile rulesProfile;
    private final FileSystem fileSystem;
    private final ResourcePerspectives perspectives;

    public PitestSensor(final ResultParser parser, final RulesProfile rulesProfile, final ReportFinder reportFinder,
            final FileSystem fileSystem, final ResourcePerspectives perspectives) {

        this.parser = parser;
        this.reportFinder = reportFinder;
        this.fileSystem = fileSystem;
        this.perspectives = perspectives;
        // executionMode = configuration.getString(MODE_KEY, MODE_SKIP);
        executionMode = MODE_SKIP;
        this.rulesProfile = rulesProfile;
    }

    @Override
    public boolean shouldExecuteOnProject(final Project project) {

        return project.getAnalysisType().isDynamic(true)
                && fileSystem.hasFiles(fileSystem.predicates().hasLanguage("java"))
                && !PitestPlugin.MODE_SKIP.equals(executionMode);
    }

    @Override
    public void analyse(final Project project, final SensorContext context) {

        final List<ActiveRule> activeRules = rulesProfile.getActiveRulesByRepository(REPOSITORY_KEY);
        if (activeRules.isEmpty()) { // ignore violations from report, if rule not activated in Sonar
            LOG.warn("/!\\ PIT rule needs to be activated in the \"{}\" profile.", rulesProfile.getName());
            LOG.warn("Checkout plugin documentation for more detailed explanations: http://docs.codehaus.org/display/SONAR/Pitest");
        }

        final File projectDirectory = fileSystem.baseDir();
        final String reportDirectoryPath = configuration.getString(REPORT_DIRECTORY_KEY, REPORT_DIRECTORY_DEF);

        final File reportDirectory = new File(projectDirectory, reportDirectoryPath);
        final File xmlReport = reportFinder.findReport(reportDirectory);
        if (xmlReport == null) {
            LOG.warn("No XML PIT report found in directory {} !", reportDirectory);
            LOG.warn("Checkout plugin documentation for more detailed explanations: http://docs.codehaus.org/display/SONAR/Pitest");
        } else {
            final Collection<Mutant> mutants = parser.parseMutants(xmlReport);
            saveMutantsInfo(mutants, context, activeRules);
        }
    }

    private void saveMutantsInfo(final Collection<Mutant> mutants, final SensorContext context,
            final List<ActiveRule> activeRules) {

        final Map<Resource, JavaFileMutants> metrics = collectMetrics(mutants, context, activeRules);
        for (final Entry<Resource, JavaFileMutants> entry : metrics.entrySet()) {
            saveMetricsInfo(context, entry.getKey(), entry.getValue());
        }
    }

    private void saveMetricsInfo(final SensorContext context, final Resource resource, final JavaFileMutants metricsInfo) {

        final double detected = metricsInfo.getMutationsDetected();
        final double total = metricsInfo.getMutationsTotal();
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

    private void saveData(final SensorContext context, final Resource resource, final List<Mutant> mutants) {

        if (mutants != null && !mutants.isEmpty()) {
            final String json = MutantHelper.toJson(mutants);
            final Measure measure = new Measure(PitestMetrics.MUTATIONS_DATA, json);
            context.saveMeasure(resource, measure);
        }
    }

    private Map<Resource, JavaFileMutants> collectMetrics(final Collection<Mutant> mutants,
            final SensorContext context, final List<ActiveRule> activeRules) {

        final Map<Resource, JavaFileMutants> metricsByResource = new HashMap<Resource, JavaFileMutants>();
        final Rule rule = getSurvivedRule(activeRules); // Currently, only survived rule is applied
        Resource resource;
        for (final Mutant mutant : mutants) {

            InputFile file = null;
            for (final InputFile f : fileSystem.inputFiles(fileSystem.predicates().all())) {
                if (f.relativePath().endsWith(mutant.getSonarJavaFileKey())) {
                    file = f;
                    break;
                }
            }

            if (file == null) {
                LOG.warn("Mutation in an unknown resource: {}", mutant.getSonarJavaFileKey());
                LOG.debug("Mutant: {}", mutant);
                processMutant(mutant, noResourceMetrics, null, context, rule);
            } else {
                resource = context.getResource(file);
                processMutant(mutant, getMetricsInfo(metricsByResource, resource), resource, context, rule);
            }
        }
        return metricsByResource;
    }

    private Rule getSurvivedRule(final List<ActiveRule> activeRules) {

        Rule rule = null;
        if (activeRules != null && !activeRules.isEmpty()) {
            rule = activeRules.get(0).getRule();
        }
        return rule;
    }

    private void processMutant(final Mutant mutant, final JavaFileMutants resourceMetricsInfo, final Resource resource,
            final SensorContext context, final Rule rule) {

        resourceMetricsInfo.addMutant(mutant);
        if (resource != null && rule != null && MutantStatus.SURVIVED.equals(mutant.getMutantStatus())) {
            // Only survived mutations are saved as violations
            final Issuable issuable = perspectives.as(Issuable.class, resource);
            if (issuable != null) {
                // can be used
                final Issue issue = issuable.newIssueBuilder()
                        .ruleKey(RuleKey.of(REPOSITORY_KEY, RULE_SURVIVED_MUTANT)).line(mutant.getLineNumber())
                        .message(mutant.getMutator().getViolationDescription()).build();
                issuable.addIssue(issue);
            }
        }
    }

    private static JavaFileMutants getMetricsInfo(final Map<Resource, JavaFileMutants> metrics, final Resource resource) {

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
