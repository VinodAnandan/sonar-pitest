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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.pitest.PitestPlugin.EFFORT_FACTOR_MISSING_COVERAGE;
import static org.sonar.plugins.pitest.PitestPlugin.EFFORT_FACTOR_SURVIVED_MUTANT;
import static org.sonar.plugins.pitest.PitestRulesDefinition.PARAM_MUTANT_COVERAGE_THRESHOLD;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.verification.VerificationMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputDir;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.batch.sensor.measure.Measure;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issuable;
import org.sonar.api.measures.Metric;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;
import org.sonar.plugins.pitest.metrics.PitestMetrics;
import org.sonar.plugins.pitest.model.Mutator;
import org.sonar.plugins.pitest.report.PitestReportParser;
import org.sonar.plugins.pitest.report.ReportFinder;

@RunWith(MockitoJUnitRunner.class)
public class PitestSensorTest {

    /**
     * SLF4J Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(PitestSensorTest.class);

    @org.junit.Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @InjectMocks
    private PitestSensor subject;

    // constructor mocks
    @Mock
    private Settings settings;
    @Mock
    private PitestReportParser parser;
    @Mock
    private RulesProfile rulesProfile;
    @Mock
    private ReportFinder reportFinder;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private FileSystem fileSystem;

    // method arg mocks
    @Mock
    private SensorDescriptor descriptor;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SensorContext context;
    @Mock
    private Issuable issuable;
    @Mock
    private InputFile javaFile;
    @Mock
    private Project project;

    private final ActiveRule negateConditionalsRule = createRuleMock("pitest.mutant.NEGATE_CONDITIONALS");
    private final ActiveRule conditionalsBoundaryRule = createRuleMock("pitest.mutant.CONDITIONALS_BOUNDARY");
    private final ActiveRule survivedMutantRule = createRuleMock("pitest.mutant.survived");
    private final ActiveRule uncoveredMutantRule = createRuleMock("pitest.mutant.uncovered");
    private final ActiveRule unknownStatusRule = createRuleMock("pitest.mutant.unknownStatus");
    private final ActiveRule coverageThresholdRule = createRuleMock("pitest.mutant.coverage");

    private final Measure<Serializable> measure = createMeasureMock();
    private final Issue issue = createIssueMock();

    @SuppressWarnings("unchecked")
    private Measure<Serializable> createMeasureMock() {

        final Measure<Serializable> measure = mock(Measure.class);
        doReturn(measure).when(measure).onFile(any(InputFile.class));
        doReturn(measure).when(measure).forMetric(any(Metric.class));
        doReturn(measure).when(measure).withValue(any(Serializable.class));
        doReturn(measure).when(measure).onProject();
        return measure;
    }

    private Issue createIssueMock() {

        final Issue issue = mock(Issue.class);
        doReturn(issue).when(issue).onFile(any(InputFile.class));
        doReturn(issue).when(issue).onProject();
        doReturn(issue).when(issue).onDir(any(InputDir.class));
        doReturn(issue).when(issue).atLine(anyInt());
        doReturn(issue).when(issue).message(anyString());
        doReturn(issue).when(issue).ruleKey(any(RuleKey.class));
        doReturn(issue).when(issue).effortToFix(any(Double.class));
        return issue;
    }

    private ActiveRule createRuleMock(final String ruleName) {

        final ActiveRule activeRule = mock(ActiveRule.class);
        final Rule rule = mock(Rule.class);
        final RuleKey ruleKey = mock(RuleKey.class);
        when(activeRule.getRuleKey()).thenReturn(ruleName);
        when(activeRule.getRule()).thenReturn(rule);
        when(rule.getKey()).thenReturn(ruleName);
        when(rule.ruleKey()).thenReturn(ruleKey);
        when(ruleKey.rule()).thenReturn(ruleName);
        return activeRule;
    }

    @Before
    public void setUp() {

        // setup filesystem and settings
        when(fileSystem.baseDir()).thenReturn(folder.getRoot());
        when(settings.getString("sonar.pitest.reports.directory")).thenReturn("target/pit-reports");

    }

    private void setupEnvironment(final boolean hasJavaFiles, final boolean sensorEnabled) {

        final FilePredicate hasJavaFilesPredicate = mock(FilePredicate.class);
        when(fileSystem.predicates().hasLanguage("java")).thenReturn(hasJavaFilesPredicate);
        when(fileSystem.hasFiles(hasJavaFilesPredicate)).thenReturn(hasJavaFiles);
        when(settings.getBoolean("sonar.pitest.enabled")).thenReturn(sensorEnabled);
    }

    private void setupSensorTest(final ActiveRule... rules) throws IOException, FileNotFoundException {

        setupEnvironment(true, true);
        // create input file and filesystem
        TestUtils.tempFileFromResource(folder, "target/pit-reports/mutations.xml", getClass(),
                "PitestSensorTest_mutations.xml");
        when(fileSystem.inputFile(any(FilePredicate.class))).thenReturn(javaFile);
        // the rules repository
        when(rulesProfile.getActiveRulesByRepository("pitest")).thenReturn(Arrays.asList(rules));
        // context measure and issues
        when(context.newMeasure()).thenReturn(measure);
        when(context.newIssue()).thenReturn(issue);
    }

    private void setupSettings(final String settingsKey, final double value) {

        when(settings.getDouble(settingsKey)).thenReturn(Double.valueOf(value));

    }

    @Test
    public void testDescribe() throws Exception {

        // prepare

        // act
        subject.describe(descriptor);

        // assert
        verify(descriptor).name("PIT");
        verify(descriptor).workOnLanguages("java");
        verify(descriptor).workOnFileTypes(Type.MAIN);
        verify(descriptor).createIssuesForRuleRepositories("pitest");

    }

    @Test
    public void testToString() throws Exception {

        assertEquals("PitestSensor", subject.toString());
    }

    @Test
    public void testExecute_noFilesAndSensorDisabled_noIssuesAndMeasures() throws Exception {

        // prepare
        setupEnvironment(false, false);

        // act
        subject.execute(context);

        // assert
        verify(context, times(0)).newIssue();
        verify(context, times(0)).newMeasure();

    }

    @Test
    public void testExecute_withFilesAndSensorDisabled_noIssuesAndMeasures() throws Exception {

        // prepare
        setupEnvironment(true, false);

        // act
        subject.execute(context);

        // assert
        verify(context, times(0)).newIssue();
        verify(context, times(0)).newMeasure();

    }

    @Test
    public void testExecute_withFilesAndSensorEnabled_noReport_noIssuesAndMeasure() throws Exception {

        // prepare
        setupEnvironment(true, true);
        when(rulesProfile.getActiveRulesByRepository("pitest")).thenReturn(Arrays.asList(negateConditionalsRule));

        // act
        subject.execute(context);

        // assert
        verify(context, times(0)).newIssue();
        verify(context, times(0)).newMeasure();

    }

    @Test
    public void testExecute_withFilesAndSensorEnabledAndReportExist_noActiveRule_noIssues() throws Exception {

        // prepare
        setupEnvironment(true, true);
        setupSensorTest();

        // act
        subject.execute(context);

        // assert
        // verify issues have been create
        verify(context, times(0)).newIssue();

        verifyMeasures();
    }

    @Test
    public void testExecute_mutatorSpecificRuleActive() throws Exception {

        // prepare

        final double EXPECTED_EFFORT_TO_FIX = 1.0;
        final double FACTOR = 2.0;
        setupEnvironment(true, true);
        setupSensorTest(negateConditionalsRule);
        setupSettings(EFFORT_FACTOR_SURVIVED_MUTANT, FACTOR);

        // act
        subject.execute(context);

        // assert
        // verify issues have been create
        verify(context, times(2)).newIssue();
        verifyRuleKey(issue, "pitest.mutant.NEGATE_CONDITIONALS", times(2));
        // verify the file and the lines of the mutants
        verify(issue, times(2)).onFile(javaFile);
        verify(issue).atLine(172);
        verify(issue).atLine(175);

        // verify the violation description
        verify(issue).message(Mutator.find("NEGATE_CONDITIONALS").getViolationDescription());
        verify(issue).message(Mutator.find("NEGATE_CONDITIONALS").getViolationDescription() + " (WITH_SUFFIX)");

        verify(issue, times(2)).ruleKey(any(RuleKey.class));
        verifyEffortToFix(issue, EXPECTED_EFFORT_TO_FIX * FACTOR, times(2));
        verify(issue, times(2)).save();

        verifyMeasures();
    }

    @Test
    public void testExecute_survivedMutantRuleActive() throws Exception {

        // prepare
        final double EXPECTED_EFFORT_TO_FIX = 1.0;
        final double FACTOR = 2.0;
        setupEnvironment(true, true);
        setupSensorTest(survivedMutantRule);
        setupSettings(EFFORT_FACTOR_SURVIVED_MUTANT, FACTOR);

        // act
        subject.execute(context);

        // assert
        verify(context).newIssue();
        verifyRuleKey(issue, "pitest.mutant.survived");
        verifyEffortToFix(issue, EXPECTED_EFFORT_TO_FIX * FACTOR);
        verify(issue).onFile(javaFile);
        verify(issue).atLine(172);
        verify(issue).message(anyString());
        verify(issue).save();
        verifyMeasures();
    }

    @Test
    public void testExecute_uncoverdMutantRuleActive() throws Exception {

        // prepare
        final double EXPECTED_EFFORT_TO_FIX = 1.0;
        final double FACTOR = 2.0;
        setupEnvironment(true, true);
        setupSensorTest(uncoveredMutantRule);
        setupSettings(EFFORT_FACTOR_SURVIVED_MUTANT, FACTOR);

        // act
        subject.execute(context);

        // assert
        // verify issues have been create
        verify(context).newIssue();
        verifyRuleKey(issue, "pitest.mutant.uncovered");
        verifyEffortToFix(issue, EXPECTED_EFFORT_TO_FIX * FACTOR);
        verify(issue).onFile(javaFile);
        verify(issue).atLine(175);
        verify(issue).message(anyString());
        verify(issue).save();
        verifyMeasures();
    }

    @Test
    public void testExecute_unknownMutantStatusRuleActive() throws Exception {

        // prepare

        final double EXPECTED_EFFORT_TO_FIX = 1.0;
        final double FACTOR = 2.0;
        setupEnvironment(true, true);
        setupSensorTest(unknownStatusRule);
        setupSettings(EFFORT_FACTOR_SURVIVED_MUTANT, FACTOR);

        // act
        subject.execute(context);

        // assert
        verify(context).newIssue();
        verifyRuleKey(issue, "pitest.mutant.unknownStatus");
        verifyEffortToFix(issue, EXPECTED_EFFORT_TO_FIX * FACTOR);
        verify(issue).onFile(javaFile);
        verify(issue).atLine(175);
        verify(issue).message(anyString());
        verify(issue).save();
        verifyMeasures();
    }

    @Test
    public void testExecute_coverageThresholdRuleActive_belowThreshold_oneMutantMissing() throws Exception {

        // prepare

        final double EXPECTED_EFFORT_TO_FIX = 1.8;
        final double FACTOR = 2.0;
        setupEnvironment(true, true);
        setupSensorTest(coverageThresholdRule);
        when(coverageThresholdRule.getParameter(PARAM_MUTANT_COVERAGE_THRESHOLD)).thenReturn("80.0");
        setupSettings(EFFORT_FACTOR_MISSING_COVERAGE, FACTOR);

        // act
        subject.execute(context);

        // assert
        // verify issues have been create
        verify(context).newIssue();
        verifyRuleKey(issue, "pitest.mutant.coverage");
        verifyEffortToFix(issue, EXPECTED_EFFORT_TO_FIX * FACTOR);
        verify(issue).onFile(javaFile);
        verify(issue).message(anyString());
        verify(issue).save();
        verifyMeasures();
    }

    @Test
    public void testExecute_coverageThresholdRuleActive_belowThreshold_moreMutantsMissing() throws Exception {

        // prepare
        final double EXPECTED_EFFORT_TO_FIX = 2.4;
        final double FACTOR = 2.0;
        setupSensorTest(coverageThresholdRule);
        when(coverageThresholdRule.getParameter(PARAM_MUTANT_COVERAGE_THRESHOLD)).thenReturn("90.0");
        setupSettings(EFFORT_FACTOR_MISSING_COVERAGE, FACTOR);

        // act
        subject.execute(context);

        // assert
        // verify issues have been create
        verify(context).newIssue();
        verifyRuleKey(issue, "pitest.mutant.coverage");
        verifyEffortToFix(issue, EXPECTED_EFFORT_TO_FIX * FACTOR);
        verify(issue).onFile(javaFile);
        verify(issue).message(anyString());
        verify(issue).save();
        verifyMeasures();
    }

    @Test
    public void testExecute_coverageThresholdRuleActive_aboveThreshold() throws Exception {

        // prepare
        setupEnvironment(true, true);
        setupSensorTest(coverageThresholdRule);
        when(coverageThresholdRule.getParameter(PARAM_MUTANT_COVERAGE_THRESHOLD)).thenReturn("20.0");

        // act
        subject.execute(context);

        // assert
        verify(context, times(0)).newIssue();
        verifyMeasures();
    }

    @Test
    public void testExecute_coverageThresholdRuleActive_onThreshold() throws Exception {

        // prepare
        setupEnvironment(true, true);
        setupSensorTest(coverageThresholdRule);
        when(coverageThresholdRule.getParameter(PARAM_MUTANT_COVERAGE_THRESHOLD)).thenReturn("50.0");

        // act
        subject.execute(context);

        // assert
        verify(context, times(0)).newIssue();
        verifyMeasures();
    }

    private void verifyRuleKey(final Issue issue, final String ruleKey) {

        final ArgumentCaptor<RuleKey> captor = forClass(RuleKey.class);
        verify(issue).ruleKey(captor.capture());
        assertEquals(ruleKey, captor.getValue().rule());
    }

    private void verifyRuleKey(final Issue issue, final String ruleKey, final VerificationMode mode) {

        final ArgumentCaptor<RuleKey> captor = forClass(RuleKey.class);
        verify(issue, mode).ruleKey(captor.capture());
        assertEquals(ruleKey, captor.getValue().rule());
    }

    private void verifyEffortToFix(final Issue issue, final double expectedEffortToFix) {

        final ArgumentCaptor<Double> effortCaptor = forClass(Double.class);
        verify(issue).effortToFix(effortCaptor.capture());
        final Double actualEffort = effortCaptor.getValue();
        assertEquals(expectedEffortToFix, actualEffort, 0.01);

    }

    private void verifyEffortToFix(final Issue issue2, final double expectedEffortToFix, final VerificationMode times) {

        final ArgumentCaptor<Double> effortCaptor = forClass(Double.class);
        verify(issue, times).effortToFix(effortCaptor.capture());
        final Double actualEffort = effortCaptor.getValue();
        assertEquals(expectedEffortToFix, actualEffort, 0.01);

    }

    private void verifyMeasures() {

        // verify measures have been recorded
        verify(context, times(9)).newMeasure();
        verify(measure, times(9)).onFile(javaFile);
        verify(measure, times(9)).withValue(any(Serializable.class));
        for (final Metric m : PitestMetrics.getQuantitativeMetrics()) {
            verify(measure).forMetric(m);
        }
        verify(measure, times(9)).save();
    }

}
