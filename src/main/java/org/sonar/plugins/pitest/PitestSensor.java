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
import static org.sonar.plugins.pitest.PitestRulesDefinition.MUTANT_RULES_PREFIX;
import static org.sonar.plugins.pitest.PitestRulesDefinition.PARAM_MUTANT_COVERAGE_THRESHOLD;
import static org.sonar.plugins.pitest.PitestRulesDefinition.REPOSITORY_KEY;
import static org.sonar.plugins.pitest.PitestRulesDefinition.RULE_MUTANT_COVERAGE;
import static org.sonar.plugins.pitest.PitestRulesDefinition.RULE_SURVIVED_MUTANT;
import static org.sonar.plugins.pitest.PitestRulesDefinition.RULE_UNCOVERED_MUTANT;
import static org.sonar.plugins.pitest.PitestRulesDefinition.RULE_UNKNOWN_MUTANT_STATUS;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issuable.IssueBuilder;
import org.sonar.api.measures.Measure;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
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
 * @author <a href="mailto:gerald.muecke@gmail.com">Gerald Muecke</a>
 *
 */
public class PitestSensor implements Sensor {

    /**
     * SLF4J Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(PitestSensor.class);

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
            final Collection<ResourceMutantMetrics> metrics = collectMetrics(mutants, context);
            applyRules(metrics, activeRules);
            saveMetrics(metrics, context);
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

    /**
     * Collect the metrics per resource (from the context) for the given mutants found on the project.
     *
     * @param mutants
     *            the mutants found in by PIT
     * @param context
     *            the current sensor context
     * @return
     */
    private Collection<ResourceMutantMetrics> collectMetrics(final Collection<Mutant> mutants,
            final SensorContext context) {

        final Map<Resource, ResourceMutantMetrics> metricsByResource = new HashMap<Resource, ResourceMutantMetrics>();

        for (final Mutant mutant : mutants) {
            collectMetricsForMutant(mutant, context, metricsByResource);
        }
        return metricsByResource.values();
    }

    /**
     * Processes the given {@link Mutant} against the given rule.
     *
     * @param mutant
     *            the {@link Mutant} to be processed
     * @param context
     *            the current sensor context
     * @param metricsByResource
     *            a map of all so-far processed resources and their gathered metrics. If multiple mutants are found in
     *            one resource, the metrics are accumulated
     */
    private void collectMetricsForMutant(final Mutant mutant, final SensorContext context,
            final Map<Resource, ResourceMutantMetrics> metricsByResource) {

        final InputFile file = getInputFile(mutant);
        if (file != null) {
            final Resource resource = context.getResource(file);
            if (!metricsByResource.containsKey(resource)) {
                metricsByResource.put(resource, new ResourceMutantMetrics(resource));
            }
            metricsByResource.get(resource).addMutant(mutant);
        } else {
            LOG.warn("No input file found matching mutated class {}", mutant.getMutatedClass());
        }

    }

    /**
     * Determines the Sonar {@link InputFile} for the given {@link Mutant}.
     */
    private InputFile getInputFile(final Mutant mutant) {

        return fileSystem.inputFile(fileSystem.predicates().matchesPathPattern("**/" + mutant.getPathToSourceFile()));
    }

    /**
     * Applies the active rules to the resources based on each resource's metrics.
     *
     * @param metrics
     *            the metrics for each individual resource
     * @param activeRules
     */
    private void applyRules(final Collection<ResourceMutantMetrics> metrics, final Collection<ActiveRule> activeRules) {

        for (final ResourceMutantMetrics resourceMetrics : metrics) {
            final Issuable issuable = perspectives.as(Issuable.class, resourceMetrics.getResource());
            if (issuable != null) {
                applyRules(issuable, resourceMetrics, activeRules);
            }
        }
    }

    /**
     * Applies the active rules on resource metrics for the {@link Issuable} resource.
     *
     * @param issuable
     *            the issuable on which the rules should be applied
     * @param resourceMetrics
     *            the mutants for found for the issuable
     * @param activeRules
     *            the active rules to apply
     */
    private void applyRules(final Issuable issuable, final ResourceMutantMetrics resourceMetrics,
            final Collection<ActiveRule> activeRules) {

        for (final ActiveRule rule : activeRules) {
            applyRule(issuable, resourceMetrics, rule);
        }
    }

    /**
     * Applies the active rule on the issuable if any of the resource metrics for the issuable violates the rule
     *
     * @param issuable
     *            the {@link Issuable} for which an issue is created
     * @param resourceMetrics
     *            the metrics for the {@link Resource} behind the {@link Issuable}
     * @param rule
     *            the active rule to apply
     */
    private void applyRule(final Issuable issuable, final ResourceMutantMetrics resourceMetrics, final ActiveRule rule) {

        if (applyThresholdRule(issuable, resourceMetrics, rule)) {
            return;
        }

        for (final Mutant mutant : resourceMetrics.getMutants()) {
            if (RULE_SURVIVED_MUTANT.equals(rule.getRuleKey()) && mutant.getMutantStatus() == MutantStatus.SURVIVED) {
                addIssue(issuable, rule, mutant);
            } else if (RULE_UNCOVERED_MUTANT.equals(rule.getRuleKey())
                    && mutant.getMutantStatus() == MutantStatus.NO_COVERAGE) {
                addIssue(issuable, rule, mutant);
            } else if (RULE_UNKNOWN_MUTANT_STATUS.equals(rule.getRuleKey())
                    && mutant.getMutantStatus() == MutantStatus.UNKNOWN) {
                addIssue(issuable, rule, mutant);
            } else if (rule.getRuleKey().equals(MUTANT_RULES_PREFIX + mutant.getMutator().getId())
                    && mutant.getMutantStatus().isAlive()) {
                addIssue(issuable, rule, mutant);
            }
        }

    }

    /**
     * Creates a the mutation coverage threshold issue if the active rule is the Mutation Coverage rule.
     *
     * @param issuable
     *            the issuable on which to apply the rule
     * @param resourceMetrics
     *            the metrics for the resource behind the issuable
     * @param rule
     *            the rule to apply.
     * @return <code>true</code> if the rule was the mutation coverage rule and the rule have been applied or
     *         <code>false</code> if it was another rule
     */
    private boolean applyThresholdRule(final Issuable issuable, final ResourceMutantMetrics resourceMetrics,
            final ActiveRule rule) {

        if (!RULE_MUTANT_COVERAGE.equals(rule.getRuleKey())) {
            return false;
        }
        final double actualCoverage = resourceMetrics.getMutationCoverage();
        final double threshold = Double.parseDouble(rule.getParameter(PARAM_MUTANT_COVERAGE_THRESHOLD));

        if (resourceMetrics.getMutationCoverage() < threshold) {

            final double minimumKilledMutants = resourceMetrics.getMutationsTotal() * threshold / 100.0;
            final double additionalRequiredMutants = minimumKilledMutants - resourceMetrics.getMutationsKilled();
            addIssue(issuable, rule, String.format(
                    "%.0f more mutants need to be killed to get the mutation coverage from %.1f%% to %.1f%%",
                    additionalRequiredMutants, actualCoverage, threshold), 0);
        }
        return true;
    }

    /**
     * Adds an issue for the current mutant and the violated rule
     *
     * @param issuable
     *            the issuable resource for which an issue should be created
     * @param rule
     *            the rule that has been violated
     * @param mutant
     *            the mutant that caused the violation
     */
    private void addIssue(final Issuable issuable, final ActiveRule rule, final Mutant mutant) {

        final StringBuilder message = new StringBuilder(mutant.getMutator().getViolationDescription());
        if (!mutant.getMutatorSuffix().isEmpty()) {
            message.append(" (").append(mutant.getMutatorSuffix()).append(')');
        }

        addIssue(issuable, rule, message.toString(), mutant.getLineNumber());
    }

    /**
     * Adds an issue with the given message on the given line for the specified rule.
     *
     * @param issuable
     *            the issuable resource for which an issue should be created
     * @param rule
     *            the rule that has been violated
     * @param message
     *            the message for the issue
     * @param lineNumber
     *            optional line number where the issue was detected. Set to 0 or negative if you have no specific line
     *
     */
    private void addIssue(final Issuable issuable, final ActiveRule rule, final String message, final int lineNumber) {

        //@formatter:off
        final IssueBuilder issueBuilder = issuable.newIssueBuilder()
                .ruleKey(rule.getRule().ruleKey())
                .message(message);
        if(lineNumber > 0) {
            issueBuilder.line(lineNumber);
        }

        // @formatter:on
        issuable.addIssue(issueBuilder.build());
    }

    /**
     * Saves the information of the mutants the sensors context.
     *
     * @param mutants
     *            the mutant information parsed from the PIT report
     * @param context
     *            the current {@link SensorContext}
     */
    private void saveMetrics(final Collection<ResourceMutantMetrics> metrics, final SensorContext context) {

        for (final ResourceMutantMetrics resourceMetrics : metrics) {
            saveResourceMetrics(resourceMetrics, context);
        }
    }

    /**
     * Saves the {@link Mutant} metrics for the given resource in the SonarContext
     *
     *
     * @param resourceMetrics
     *            the actual metrics for the resource to persist
     * @param context
     *            the context to register the metrics
     */
    private void saveResourceMetrics(final ResourceMutantMetrics resourceMetrics, final SensorContext context) {

        final Resource resource = resourceMetrics.getResource();
        context.saveMeasure(resource, PitestMetrics.MUTATIONS_TOTAL, resourceMetrics.getMutationsTotal());
        context.saveMeasure(resource, PitestMetrics.MUTATIONS_NO_COVERAGE, resourceMetrics.getMutationsNoCoverage());
        context.saveMeasure(resource, PitestMetrics.MUTATIONS_KILLED, resourceMetrics.getMutationsKilled());
        context.saveMeasure(resource, PitestMetrics.MUTATIONS_SURVIVED, resourceMetrics.getMutationsSurvived());
        context.saveMeasure(resource, PitestMetrics.MUTATIONS_MEMORY_ERROR, resourceMetrics.getMutationsMemoryError());
        context.saveMeasure(resource, PitestMetrics.MUTATIONS_TIMED_OUT, resourceMetrics.getMutationsTimedOut());
        context.saveMeasure(resource, PitestMetrics.MUTATIONS_UNKNOWN, resourceMetrics.getMutationsUnknown());
        context.saveMeasure(resource, PitestMetrics.MUTATIONS_DETECTED, resourceMetrics.getMutationsDetected());
        context.saveMeasure(resource,
                new Measure<>(PitestMetrics.MUTATIONS_DATA, MutantHelper.toJson(resourceMetrics.getMutants())));
    }

    @Override
    public String toString() {

        return getClass().getSimpleName();
    }

}
