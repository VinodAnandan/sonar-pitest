/*
 * Sonar Pitest Plugin
 * Copyright (C) 2015 SonarCommunity
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

import static org.sonar.plugins.pitest.PitestPlugin.REPORT_DIRECTORY_KEY;
import static org.sonar.plugins.pitest.PitestRulesDefinition.REPOSITORY_KEY;
import static org.sonar.plugins.pitest.PitestRulesDefinition.RULE_SURVIVED_MUTANT;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
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
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.measures.Measure;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rules.ActiveRule;
import org.sonar.plugins.pitest.metrics.PitestMetrics;
import org.sonar.plugins.pitest.metrics.ResourceMutantMetrics;
import org.sonar.plugins.pitest.model.Mutant;
import org.sonar.plugins.pitest.model.MutantHelper;
import org.sonar.plugins.pitest.model.MutantStatus;

/**
 *
 * Sonar sensor for pitest mutation coverage analysis.
 *
 * @author <a href="mailto:aquiporras@gmail.com">Jaime Porras L&oacute;pez</a>
 * @author <a href="mailto:alexvictoor@gmail.com">Alexandre Victoor</a>
 * @author <a href="mailto:gerald@moskito.li">Gerald Muecke</a>
 *
 */
public class PitestSensor implements Sensor {

    /**
     * SLF4J Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(PitestSensor.class);

    /**
     * Empty metrics for Java Mutant
     */
    private static final ResourceMutantMetrics EMPTY_RESOURCE_METRICS = new ResourceMutantMetrics(null);

    private final ResultParser parser;
    private final ReportFinder reportFinder;
    private final String executionMode;
    private final RulesProfile rulesProfile;
    private final FileSystem fileSystem;
    private final ResourcePerspectives perspectives;
    private final Settings settings;

    public PitestSensor(final Settings settings, final ResultParser parser, final RulesProfile rulesProfile,
            final ReportFinder reportFinder, final FileSystem fileSystem, final ResourcePerspectives perspectives) {

        this.parser = parser;
        this.reportFinder = reportFinder;
        this.fileSystem = fileSystem;
        this.perspectives = perspectives;
        executionMode = settings.getString(PitestPlugin.MODE_KEY);
        this.rulesProfile = rulesProfile;
        this.settings = settings;

    }

    /**
     * Will execute the plugin if the language is Java and execution mode is not skip.
     */
    @Override
    public boolean shouldExecuteOnProject(final Project project) {

        return fileSystem.hasFiles(fileSystem.predicates().hasLanguage("java"))
                && !PitestPlugin.MODE_SKIP.equals(executionMode);
    }

    @Override
    public void analyse(final Project project, final SensorContext context) {

        final List<ActiveRule> activeRules = rulesProfile.getActiveRulesByRepository(REPOSITORY_KEY);
        if (activeRules.isEmpty()) {
            // ignore violations from report, if rule not activated in Sonar
            LOG.warn("/!\\ PIT rule needs to be activated in the \"{}\" profile.", rulesProfile.getName());
            LOG.warn("Checkout plugin documentation for more detailed explanations: http://docs.codehaus.org/display/SONAR/Pitest");
        }
        try {
            final Collection<Mutant> mutants = readMutants();
            saveMutantsInfo(mutants, context);
        } catch (final IOException e) {
            LOG.error("Could not read mutants", e);
        }

    }

    /**
     * Reads the Mutants from the PIT reports for the current maven project the sensor analyzes
     *
     * @return a collection of all mutants found in the reports. If the report could not be located, the list is empty.
     * @throws IOException
     *             if the search for the report file failed
     */
    private Collection<Mutant> readMutants() throws IOException {

        final Path reportDirectory = getReportDirectory();
        LOG.debug("Searching pit reports in {}", reportDirectory);
        final Path xmlReport = reportFinder.findReport(reportDirectory);

        if (xmlReport == null) {
            LOG.warn("No XML PIT report found in directory {} !", reportDirectory);
            LOG.warn("Checkout plugin documentation for more detailed explanations: http://docs.codehaus.org/display/SONAR/Pitest");
            return Collections.emptyList();
        }

        return parser.parseMutants(xmlReport);
    }

    /**
     * Determine the absolute path of the directory where the PIT reports are located. The path is assembled using the
     * base directory of the fileSystem and the reports directory configured in the plugin's {@link Settings}.
     *
     * @return the path to PIT reports directory
     */
    private Path getReportDirectory() {

        final File projectDirectory = fileSystem.baseDir();
        final String reportDirectoryPath = settings.getString(REPORT_DIRECTORY_KEY);
        return projectDirectory.toPath().resolve(reportDirectoryPath);
    }

    private Map<Resource, ResourceMutantMetrics> collectMetrics(final Collection<Mutant> mutants,
            final SensorContext context) {

        final Map<Resource, ResourceMutantMetrics> metricsByResource = new HashMap<Resource, ResourceMutantMetrics>();

        for (final Mutant mutant : mutants) {
            processMutant(mutant, context, metricsByResource);
        }
        return metricsByResource;
    }

    /**
     * Processes the given mutant agains the given rule.
     *
     * @param mutant
     *            the mutant to be processed
     * @param context
     *            the current sensor context
     * @param metricsByResource
     *            a map of all so-far processed resources and their gathered metrics. If multiple mutants are found in
     *            one resource, the metrics are accumulated
     */
    private void processMutant(final Mutant mutant, final SensorContext context,
            final Map<Resource, ResourceMutantMetrics> metricsByResource) {

        final InputFile file = getInputFile(mutant);

        ResourceMutantMetrics mutantMetrics;
        Resource resource;

        if (file == null) {
            // TODO check if branch can be removed
            resource = null;
            mutantMetrics = EMPTY_RESOURCE_METRICS;
        } else {
            resource = context.getResource(file);
            if (metricsByResource.containsKey(resource)) {
                mutantMetrics = metricsByResource.get(resource);
            } else {
                mutantMetrics = new ResourceMutantMetrics(resource);
                metricsByResource.put(resource, mutantMetrics);
            }
        }

        processMutant(mutant, mutantMetrics, resource, context);
    }

    /**
     * Adds the {@link Mutant} to the resource metrics and creates an issue according to the active rules.
     *
     * @param mutant
     * @param rule
     * @param resourceMetricsInfo
     * @param resource
     * @param context
     */
    private void processMutant(final Mutant mutant, final ResourceMutantMetrics resourceMetricsInfo,
            final Resource resource, final SensorContext context) {

        resourceMetricsInfo.addMutant(mutant);

        if (resource == null) {
            // TODO check if branch can be removed
            return;
        }

        final Issuable issuable = perspectives.as(Issuable.class, resource);
        if (issuable == null) {
            return;
        }
        final List<ActiveRule> activeRules = rulesProfile.getActiveRulesByRepository(REPOSITORY_KEY);
        for (final ActiveRule rule : activeRules) {

            // TODO support no coverage as issue
            if (PitestRulesDefinition.RULE_SURVIVED_MUTANT.equals(rule.getRuleKey())
                    && mutant.getMutantStatus() == MutantStatus.SURVIVED) {
                final Issue issue = issuable.newIssueBuilder()
                        .ruleKey(RuleKey.of(REPOSITORY_KEY, RULE_SURVIVED_MUTANT)).line(mutant.getLineNumber())
                        .message(mutant.getMutator().getViolationDescription()).build();
                issuable.addIssue(issue);
            }
        }
    }

    /**
     * Determines the Sonar {@link InputFile} for the given {@link Mutant}.
     */
    private InputFile getInputFile(final Mutant mutant) {

        return fileSystem.inputFile(fileSystem.predicates().matchesPathPattern("**/" + mutant.getPathToSourceFile()));
    }

    /**
     * Saves the information of the mutants the sensors context.
     *
     * @param mutants
     *            the mutant information parsed from the PIT report
     * @param context
     *            the current {@link SensorContext}
     */
    private void saveMutantsInfo(final Collection<Mutant> mutants, final SensorContext context) {

        final Map<Resource, ResourceMutantMetrics> metrics = collectMetrics(mutants, context);

        for (final Entry<Resource, ResourceMutantMetrics> entry : metrics.entrySet()) {
            saveMetricsInfo(context, entry.getKey(), entry.getValue());
        }
    }

    /**
     * Saves the {@link Mutant} metrics for the given resource in the SonarContext
     *
     * @param context
     *            the context to register the metrics
     * @param resource
     *            the resource to assign the metrics to
     * @param metricsInfo
     *            the actual metrics for the resource to persist
     */
    private void saveMetricsInfo(final SensorContext context, final Resource resource,
            final ResourceMutantMetrics metricsInfo) {

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

    /**
     * Saves the data of all mutants found for the given resource as a string-valued measure to the context
     *
     * @param context
     *            the current sonar context
     * @param resource
     *            the current resource
     * @param mutants
     *            the mutants found for the given resource
     */
    private void saveData(final SensorContext context, final Resource resource, final List<Mutant> mutants) {

        if (mutants != null && !mutants.isEmpty()) {
            final String json = MutantHelper.toJson(mutants);
            final Measure<String> measure = new Measure<>(PitestMetrics.MUTATIONS_DATA, json);
            context.saveMeasure(resource, measure);
        }
    }

    @Override
    public String toString() {

        return getClass().getSimpleName();
    }

}
