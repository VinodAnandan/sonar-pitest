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

import static org.sonar.plugins.pitest.PitestPlugin.EFFORT_FACTOR_MISSING_COVERAGE;
import static org.sonar.plugins.pitest.PitestPlugin.EFFORT_FACTOR_SURVIVED_MUTANT;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issuable;
import org.sonar.api.measures.Metric;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Resource;
import org.sonar.api.rules.ActiveRule;
import org.sonar.plugins.pitest.metrics.PitestMetrics;
import org.sonar.plugins.pitest.metrics.ResourceMutantMetrics;
import org.sonar.plugins.pitest.model.Mutant;
import org.sonar.plugins.pitest.model.MutantHelper;
import org.sonar.plugins.pitest.model.MutantStatus;
import org.sonar.plugins.pitest.report.Reports;

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

    private final RulesProfile rulesProfile;
    private final FileSystem fileSystem;
    private final Settings settings;

    public PitestSensor(final Settings settings, final RulesProfile rulesProfile, final FileSystem fileSystem) {

        this.fileSystem = fileSystem;
        this.rulesProfile = rulesProfile;
        this.settings = settings;

    }

    @Override
    public void describe(final SensorDescriptor descriptor) {

        descriptor.name("PIT");
        descriptor.provides(PitestMetrics.getSensorMetrics().toArray(new Metric[0]));
        descriptor.workOnLanguages("java");
        descriptor.workOnFileTypes(Type.MAIN);
        descriptor.createIssuesForRuleRepositories(REPOSITORY_KEY);

    }

    @Override
    public void execute(final SensorContext context) {

        if (!(fileSystem.hasFiles(fileSystem.predicates().hasLanguage("java")) && settings
                .getBoolean(PitestPlugin.SENSOR_ENABLED))) {
            LOG.info("PIT Sensor disabled");
            return;
        }

        final List<ActiveRule> activeRules = rulesProfile.getActiveRulesByRepository(REPOSITORY_KEY);
        if (activeRules.isEmpty()) {
            // ignore violations from report, if rule not activated in Sonar
            LOG.warn("/!\\ PIT rule needs to be activated in the \"{}\" profile.", rulesProfile.getName());
            LOG.warn("Checkout plugin documentation for more detailed explanations: http://docs.codehaus.org/display/SONAR/Pitest");
        }
        try {
            final Collection<Mutant> mutants = readMutants();
            final Collection<ResourceMutantMetrics> metrics = collectMetrics(mutants);
            applyRules(metrics, activeRules, context);
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

        return Reports.readMutants(getReportDirectory());
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
     * @return
     */
    private Collection<ResourceMutantMetrics> collectMetrics(final Collection<Mutant> mutants) {

        final Map<InputFile, ResourceMutantMetrics> metricsByResource = new HashMap<>();

        for (final Mutant mutant : mutants) {
            collectMetricsForMutant(mutant, metricsByResource);
        }
        return metricsByResource.values();
    }

    /**
     * Processes the given {@link Mutant} against the given rule.
     *
     * @param mutant
     *            the {@link Mutant} to be processed
     * @param metricsByResource
     *            a map of all so-far processed resources and their gathered metrics. If multiple mutants are found in
     *            one resource, the metrics are accumulated
     */
    private void collectMetricsForMutant(final Mutant mutant,
            final Map<InputFile, ResourceMutantMetrics> metricsByResource) {

        final InputFile file = getInputFile(mutant);
        if (file != null) {
            if (!metricsByResource.containsKey(file)) {
                metricsByResource.put(file, new ResourceMutantMetrics(file));
            }
            metricsByResource.get(file).addMutant(mutant);
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
     *            the currently active rule
     * @param context
     *            the current sensor context
     */
    private void applyRules(final Collection<ResourceMutantMetrics> metrics, final Collection<ActiveRule> activeRules,
            final SensorContext context) {

        for (final ResourceMutantMetrics resourceMetrics : metrics) {
            applyRules(resourceMetrics, activeRules, context);
        }
    }

    /**
     * Applies the active rules on resource metrics for the {@link Issuable} resource.
     *
     * @param resourceMetrics
     *            the mutants for found for the issuable
     * @param activeRules
     *            the active rules to apply
     * @param context
     *            the current sensor context
     */
    private void applyRules(final ResourceMutantMetrics resourceMetrics, final Collection<ActiveRule> activeRules,
            final SensorContext context) {

        for (final ActiveRule rule : activeRules) {
            applyRule(resourceMetrics, rule, context);
        }
    }

    /**
     * Applies the active rule on the issuable if any of the resource metrics for the issuable violates the rule
     *
     * @param resourceMetrics
     *            the metrics for the {@link Resource} behind the {@link Issuable}
     * @param rule
     *            the active rule to apply
     * @param context
     *            the current sensor context
     */
    private void applyRule(final ResourceMutantMetrics resourceMetrics, final ActiveRule rule,
            final SensorContext context) {

        if (applyThresholdRule(resourceMetrics, rule, context)) {
            return;
        }

        applyMutantRule(resourceMetrics, rule, context);

    }

    /**
     * Creates a the mutation coverage threshold issue if the active rule is the Mutation Coverage rule.
     *
     * @param resourceMetrics
     *            the issuable on which to apply the rule
     * @param rule
     *            the metrics for the resource behind the issuable
     * @param context
     *            the rule to apply.
     * @return <code>true</code> if the rule was the mutation coverage rule and the rule have been applied or
     *         <code>false</code> if it was another rule
     */
    private boolean applyThresholdRule(final ResourceMutantMetrics resourceMetrics, final ActiveRule rule,
            final SensorContext context) {

        if (!RULE_MUTANT_COVERAGE.equals(rule.getRuleKey())) {
            return false;
        }
        final double actualCoverage = resourceMetrics.getMutationCoverage();
        final double threshold = Double.parseDouble(rule.getParameter(PARAM_MUTANT_COVERAGE_THRESHOLD));

        if (resourceMetrics.getMutationCoverage() < threshold) {

            final double minimumKilledMutants = resourceMetrics.getMutationsTotal() * threshold / 100.0;
            final double additionalRequiredMutants = minimumKilledMutants - resourceMetrics.getMutationsKilled();

            // TODO ensure that additional + miniumum > threshold

            //@formatter:off
            context.newIssue()
                .onFile(resourceMetrics.getResource())
                .message(String.format(
                    "%.0f more mutants need to be killed to get the mutation coverage from %.1f%% to %.1f%%",
                    additionalRequiredMutants, actualCoverage, threshold))
                .ruleKey(rule.getRule().ruleKey())
                .effortToFix(settings.getDouble(EFFORT_FACTOR_MISSING_COVERAGE) * additionalRequiredMutants)
                .save();
            // @formatter:on
        }
        return true;
    }

    /**
     * Applies mutant specific rule on each mutant captured in the resource metric. For each mutant assigned to the
     * resource, it is checked if it violates:
     * <ul>
     * <li>the survived mutant rule</li>
     * <li>the uncovered mutant rule</li>
     * <li>the unknown mutator status rule</li>
     * <li>any of the mutator specific rules</li>
     * </ul>
     *
     * @param resourceMetrics
     *            the resource metric containing the resource that might have an issue and all mutants found for that
     *            resource
     * @param rule
     *            the rule that might be violated
     * @param context
     *            the current sensor context
     */
    private void applyMutantRule(final ResourceMutantMetrics resourceMetrics, final ActiveRule rule,
            final SensorContext context) {

        for (final Mutant mutant : resourceMetrics.getMutants()) {
            if (violatesSurvivedMutantRule(rule, mutant)
                    || violatesUncoveredMutantRule(rule, mutant)
                    || violatesUnknownMutantStatusRule(rule, mutant)
                    || violatesMutatorRule(rule, mutant)) {
                //@formatter:off
                context.newIssue()
                    .onFile(resourceMetrics.getResource())
                    .atLine(mutant.getLineNumber())
                    .message(getViolationDescription(mutant))
                    .ruleKey(rule.getRule().ruleKey())
                    .effortToFix(settings.getDouble(EFFORT_FACTOR_SURVIVED_MUTANT))
                    .save();
                // @formatter:on
            }
        }
    }

    /**
     * Checks if the active rule is a mutator-specific rule and if the mutant violates it.
     *
     * @param rule
     *            the rule to verify
     * @param mutant
     *            the mutant that might violate the rule
     * @return <code>true</code> if the rule is violated
     */
    private boolean violatesMutatorRule(final ActiveRule rule, final Mutant mutant) {

        return rule.getRuleKey().equals(MUTANT_RULES_PREFIX + mutant.getMutator().getId())
                && mutant.getMutantStatus().isAlive();
    }

    /**
     * Checks if the rule is the Unknown Mutator Status rule and if the mutant violates it
     *
     * @param rule
     *            the rule to verify
     * @param mutant
     *            the mutant that might violate the rule
     * @return <code>true</code> if the rule is violated
     */
    private boolean violatesUnknownMutantStatusRule(final ActiveRule rule, final Mutant mutant) {

        return RULE_UNKNOWN_MUTANT_STATUS.equals(rule.getRuleKey()) && mutant.getMutantStatus() == MutantStatus.UNKNOWN;
    }

    /**
     * Checks if the rule is the Uncovered Mutant rule and if the mutant violates it
     *
     * @param rule
     *            the rule to verify
     * @param mutant
     *            the mutant that might violate the rule
     * @return <code>true</code> if the rule is violated
     */
    private boolean violatesUncoveredMutantRule(final ActiveRule rule, final Mutant mutant) {

        return RULE_UNCOVERED_MUTANT.equals(rule.getRuleKey()) && mutant.getMutantStatus() == MutantStatus.NO_COVERAGE;
    }

    /**
     * Checks if the rule if the Survived Mutant rule and if the mutant violates it
     *
     * @param rule
     *            the rule to verify
     * @param mutant
     *            the mutant that might violate the rule
     * @return <code>true</code> if the rule is violated
     */
    private boolean violatesSurvivedMutantRule(final ActiveRule rule, final Mutant mutant) {

        return RULE_SURVIVED_MUTANT.equals(rule.getRuleKey()) && mutant.getMutantStatus() == MutantStatus.SURVIVED;
    }

    /**
     * Gets the mutant specific violation description of the mutator of the mutant
     *
     * @param mutant
     *            the mutant to receive the violation description
     * @return the description as string
     */
    private String getViolationDescription(final Mutant mutant) {

        final StringBuilder message = new StringBuilder(mutant.getMutator().getViolationDescription());
        if (!mutant.getMutatorSuffix().isEmpty()) {
            message.append(" (").append(mutant.getMutatorSuffix()).append(')');
        }
        return message.toString();
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

        final InputFile resource = resourceMetrics.getResource();
        context.newMeasure().onFile(resource).forMetric(PitestMetrics.MUTATIONS_TOTAL)
                .withValue(resourceMetrics.getMutationsTotal()).save();
        context.newMeasure().onFile(resource).forMetric(PitestMetrics.MUTATIONS_NO_COVERAGE)
                .withValue(resourceMetrics.getMutationsNoCoverage()).save();
        context.newMeasure().onFile(resource).forMetric(PitestMetrics.MUTATIONS_KILLED)
                .withValue(resourceMetrics.getMutationsKilled()).save();
        context.newMeasure().onFile(resource).forMetric(PitestMetrics.MUTATIONS_SURVIVED)
                .withValue(resourceMetrics.getMutationsSurvived()).save();
        context.newMeasure().onFile(resource).forMetric(PitestMetrics.MUTATIONS_MEMORY_ERROR)
                .withValue(resourceMetrics.getMutationsMemoryError()).save();
        context.newMeasure().onFile(resource).forMetric(PitestMetrics.MUTATIONS_TIMED_OUT)
                .withValue(resourceMetrics.getMutationsTimedOut()).save();
        context.newMeasure().onFile(resource).forMetric(PitestMetrics.MUTATIONS_UNKNOWN)
                .withValue(resourceMetrics.getMutationsUnknown()).save();
        context.newMeasure().onFile(resource).forMetric(PitestMetrics.MUTATIONS_DETECTED)
                .withValue(resourceMetrics.getMutationsDetected()).save();
        context.newMeasure().onFile(resource).forMetric(PitestMetrics.MUTATIONS_DATA)
                .withValue(MutantHelper.toJson(resourceMetrics.getMutants())).save();

    }

    @Override
    public String toString() {

        return getClass().getSimpleName();
    }

}
