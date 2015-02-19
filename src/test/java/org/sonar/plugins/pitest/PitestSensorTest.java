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
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.pitest.PitestPlugin.EFFORT_FACTOR_SURVIVED_MUTANT;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
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
    private final ActiveRule survivedMutantRule = createRuleMock("pitest.mutant.survived");
    private final ActiveRule uncoveredMutantRule = createRuleMock("pitest.mutant.uncovered");
    private final ActiveRule unknownStatusRule = createRuleMock("pitest.mutant.unknownStatus");

    private final Measure<Serializable> measure = createMeasureMock();
    private final Issue issue = createIssueMock();

    @Before
    public void setUp() {

        // setup filesystem and settings
        when(fileSystem.baseDir()).thenReturn(folder.getRoot());
        when(settings.getString("sonar.pitest.reports.directory")).thenReturn("target/pit-reports");

    }

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

    private void setupEnvironment(final boolean hasJavaFiles, final boolean sensorEnabled) {

        final FilePredicate hasJavaFilesPredicate = mock(FilePredicate.class);
        when(fileSystem.predicates().hasLanguage("java")).thenReturn(hasJavaFilesPredicate);
        when(fileSystem.hasFiles(hasJavaFilesPredicate)).thenReturn(hasJavaFiles);
        when(settings.getBoolean("sonar.pitest.enabled")).thenReturn(sensorEnabled);
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

        final double EXPECTED_EFFORT_TO_FIX = 12.3;
        setupEnvironment(true, true);
        tempFileFromResource("target/pit-reports/mutations.xml", "PitestSensorTest_mutations.xml");
        when(rulesProfile.getActiveRulesByRepository("pitest")).thenReturn(Collections.<ActiveRule> emptyList());
        when(fileSystem.inputFile(any(FilePredicate.class))).thenReturn(javaFile);
        when(settings.getDouble(EFFORT_FACTOR_SURVIVED_MUTANT)).thenReturn(Double.valueOf(EXPECTED_EFFORT_TO_FIX));
        when(context.newMeasure()).thenReturn(measure);
        when(context.newIssue()).thenReturn(issue);

        // act
        subject.execute(context);

        // assert
        // verify issues have been create
        verify(context, times(0)).newIssue();

        verifyMeasures();
    }

    @Test
    public void testExecute_withFilesAndSensorEnabledAndReportExist_mutatorSpecificRuleActive() throws Exception {

        // prepare

        final double EXPECTED_EFFORT_TO_FIX = 12.3;
        setupEnvironment(true, true);
        tempFileFromResource("target/pit-reports/mutations.xml", "PitestSensorTest_mutations.xml");
        when(rulesProfile.getActiveRulesByRepository("pitest")).thenReturn(Arrays.asList(negateConditionalsRule));
        when(fileSystem.inputFile(any(FilePredicate.class))).thenReturn(javaFile);
        when(settings.getDouble(EFFORT_FACTOR_SURVIVED_MUTANT)).thenReturn(Double.valueOf(EXPECTED_EFFORT_TO_FIX));
        when(context.newMeasure()).thenReturn(measure);
        when(context.newIssue()).thenReturn(issue);

        // act
        subject.execute(context);

        // assert
        // verify issues have been create
        verify(context, times(2)).newIssue();
        final ArgumentCaptor<RuleKey> captor = forClass(RuleKey.class);
        verify(issue, times(2)).ruleKey(captor.capture());
        assertEquals("pitest.mutant.NEGATE_CONDITIONALS", captor.getValue().rule());
        verify(issue, times(2)).onFile(javaFile);
        verify(issue).atLine(172);
        verify(issue).atLine(175);
        verify(issue, times(2)).message(anyString());
        verify(issue, times(2)).ruleKey(any(RuleKey.class));
        verify(issue, times(2)).effortToFix(EXPECTED_EFFORT_TO_FIX);
        verify(issue, times(2)).save();

        verifyMeasures();
    }

    @Test
    public void testExecute_withFilesAndSensorEnabledAndReportExist_SurvivedMutantRuleActive() throws Exception {

        // prepare

        final double EXPECTED_EFFORT_TO_FIX = 12.3;
        setupEnvironment(true, true);
        tempFileFromResource("target/pit-reports/mutations.xml", "PitestSensorTest_mutations.xml");
        when(rulesProfile.getActiveRulesByRepository("pitest")).thenReturn(Arrays.asList(survivedMutantRule));
        when(fileSystem.inputFile(any(FilePredicate.class))).thenReturn(javaFile);
        when(settings.getDouble(EFFORT_FACTOR_SURVIVED_MUTANT)).thenReturn(Double.valueOf(EXPECTED_EFFORT_TO_FIX));
        when(context.newMeasure()).thenReturn(measure);
        when(context.newIssue()).thenReturn(issue);

        // act
        subject.execute(context);

        // assert
        // verify issues have been create
        verify(context).newIssue();
        final ArgumentCaptor<RuleKey> captor = forClass(RuleKey.class);
        verify(issue).ruleKey(captor.capture());
        assertEquals("pitest.mutant.survived", captor.getValue().rule());
        verify(issue).onFile(javaFile);
        verify(issue).atLine(172);
        verify(issue).message(anyString());
        verify(issue).effortToFix(EXPECTED_EFFORT_TO_FIX);
        verify(issue).save();

        verifyMeasures();
    }

    @Test
    public void testExecute_withFilesAndSensorEnabledAndReportExist_UncoverdMutantRuleActive() throws Exception {

        // prepare

        final double EXPECTED_EFFORT_TO_FIX = 12.3;
        setupEnvironment(true, true);
        tempFileFromResource("target/pit-reports/mutations.xml", "PitestSensorTest_mutations.xml");
        when(rulesProfile.getActiveRulesByRepository("pitest")).thenReturn(Arrays.asList(uncoveredMutantRule));
        when(fileSystem.inputFile(any(FilePredicate.class))).thenReturn(javaFile);
        when(settings.getDouble(EFFORT_FACTOR_SURVIVED_MUTANT)).thenReturn(Double.valueOf(EXPECTED_EFFORT_TO_FIX));
        when(context.newMeasure()).thenReturn(measure);
        when(context.newIssue()).thenReturn(issue);

        // act
        subject.execute(context);

        // assert
        // verify issues have been create
        verify(context).newIssue();
        final ArgumentCaptor<RuleKey> captor = forClass(RuleKey.class);
        verify(issue).ruleKey(captor.capture());
        assertEquals("pitest.mutant.uncovered", captor.getValue().rule());
        verify(issue).onFile(javaFile);
        verify(issue).atLine(175);
        verify(issue).message(anyString());
        verify(issue).effortToFix(EXPECTED_EFFORT_TO_FIX);
        verify(issue).save();

        // verify measures have been recorded
        verify(context, times(9)).newMeasure();
        verify(measure, times(9)).onFile(javaFile);
        verify(measure, times(9)).withValue(any(Serializable.class));
        for (final Metric m : PitestMetrics.getQuantitativeMetrics()) {
            verify(measure).forMetric(m);
        }
        verify(measure, times(9)).save();
    }

    @Test
    public void testExecute_withFilesAndSensorEnabledAndReportExist_UnknownMutantStatusRuleActive() throws Exception {

        // prepare

        final double EXPECTED_EFFORT_TO_FIX = 12.3;
        setupEnvironment(true, true);
        tempFileFromResource("target/pit-reports/mutations.xml", "PitestSensorTest_mutations.xml");
        when(rulesProfile.getActiveRulesByRepository("pitest")).thenReturn(Arrays.asList(unknownStatusRule));
        when(fileSystem.inputFile(any(FilePredicate.class))).thenReturn(javaFile);
        when(settings.getDouble(EFFORT_FACTOR_SURVIVED_MUTANT)).thenReturn(Double.valueOf(EXPECTED_EFFORT_TO_FIX));
        when(context.newMeasure()).thenReturn(measure);
        when(context.newIssue()).thenReturn(issue);

        // act
        subject.execute(context);

        // assert
        // verify issues have been create
        verify(context).newIssue();
        final ArgumentCaptor<RuleKey> captor = forClass(RuleKey.class);
        verify(issue).ruleKey(captor.capture());
        assertEquals("pitest.mutant.unknownStatus", captor.getValue().rule());
        verify(issue).onFile(javaFile);
        verify(issue).atLine(175);
        verify(issue).message(anyString());
        verify(issue).effortToFix(EXPECTED_EFFORT_TO_FIX);
        verify(issue).save();

        // verify measures have been recorded
        verify(context, times(9)).newMeasure();
        verify(measure, times(9)).onFile(javaFile);
        verify(measure, times(9)).withValue(any(Serializable.class));
        for (final Metric m : PitestMetrics.getQuantitativeMetrics()) {
            verify(measure).forMetric(m);
        }
        verify(measure, times(9)).save();
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

    protected File tempFileFromResource(final String filePath, final String resource) throws IOException,
            FileNotFoundException {

        final File tempFile = newTempFile(folder, filePath);
        final URL url = getClass().getResource(resource);
        assertNotNull("Resource " + resource + " not found", url);
        copyResourceToFile(url, tempFile);
        return tempFile;
    }

    public static void copyResourceToFile(final URL resource, final File tempFile) throws IOException,
            FileNotFoundException {

        IOUtils.copy(resource.openStream(), new FileOutputStream(tempFile));
        LOG.info("Created temp file {}", tempFile.getAbsolutePath());
    }

    /**
     * Creates a new temporary file in the {@link TemporaryFolder}. The file may be specified as path relative to the
     * root of the temporary folder
     *
     * @param folder
     *            the temporary folder in which to create the new file
     * @param filePath
     *            the name of the file or a relative path to the file to be created
     * @return the {@link File} reference to the newly created file
     * @throws IOException
     */
    public static File newTempFile(final TemporaryFolder folder, final String filePath) throws IOException {

        String path;
        String filename;
        final int lastPathSeparator = filePath.lastIndexOf('/');
        if (lastPathSeparator != -1) {
            path = filePath.substring(0, lastPathSeparator);
            filename = filePath.substring(lastPathSeparator + 1);
        } else {
            path = null;
            filename = filePath;
        }
        File tempFile;
        if (path != null) {
            final String[] pathSegments = path.split("\\/");
            final File newFolder = folder.newFolder(pathSegments);
            tempFile = new File(newFolder, filename);
        } else {
            tempFile = folder.newFile(filename);
        }
        return tempFile;
    }

    @Test
    public void testToString() throws Exception {

        assertEquals("PitestSensor", subject.toString());
    }

    // @Test
    // public void should_skip_analysis_if_no_specific_pit_configuration() throws Exception {
    //
    // // given
    // when(project.getAnalysisType()).thenReturn(AnalysisType.DYNAMIC);
    // when(configuration.getString(MODE_KEY, MODE_SKIP)).thenReturn(MODE_SKIP);
    // profileWithActiveRule();
    // subject = buildSensor();
    // // when
    // final boolean sensorExecuted = subject.shouldExecuteOnProject(project);
    // // then
    // assertThat(sensorExecuted).isFalse();
    // }
    //
    // @Test
    // public void should_do_analysis_if_pit_mode_set_to_reuse_report() throws Exception {
    //
    // // given
    // workingConfiguration();
    // profileWithActiveRule();
    // subject = buildSensor();
    // // when
    // final boolean sensorExecuted = subject.shouldExecuteOnProject(project);
    // // then
    // assertThat(sensorExecuted).isTrue();
    // }
    //
    // @Test
    // public void should_skip_analysis_if_dynamic_analysis_disabled() throws Exception {
    //
    // // given
    // when(project.getAnalysisType()).thenReturn(AnalysisType.STATIC);
    // when(configuration.getString(MODE_KEY, MODE_SKIP)).thenReturn(MODE_REUSE_REPORT);
    // profileWithActiveRule();
    // subject = buildSensor();
    // // when
    // final boolean sensorExecuted = subject.shouldExecuteOnProject(project);
    // // then
    // assertThat(sensorExecuted).isFalse();
    // }
    //
    // @Test
    // public void should_not_skip_analysis_when_pitest_rule_not_activated() throws Exception {
    //
    // // given
    // workingConfiguration();
    // profileWithoutActiveRule();
    // subject = buildSensor();
    // // when
    // subject.analyse(project, context);
    // // then
    // verifyMutationsSaved();
    // }
    //
    // @Test
    // public void should_parse_reports_when_reuse_mode_active() throws Exception {
    //
    // // given
    // workingConfiguration();
    // profileWithActiveRule();
    // subject = buildSensor();
    // // when
    // subject.analyse(project, context);
    // // then
    // verifyIssueRaided();
    // verifyMutationsSaved();
    // }
    //
    // @Test
    // public void should_not_fail_when_no_report_found() throws Exception {
    //
    // // given
    // workingConfiguration();
    // when(fileSystem.baseDir()).thenReturn(TestUtils.getResource("."));
    // when(configuration.getString(REPORT_DIRECTORY_KEY, REPORT_DIRECTORY_DEF)).thenReturn("");
    // when(reportFinder.findReport(TestUtils.getResource("."))).thenReturn(null);
    // subject = new PitestSensor(configuration, parser, rulesProfile, reportFinder, fileSystem, perspectives);
    // // when
    // subject.analyse(project, mock(SensorContext.class));
    // // then no failure
    // }
    //
    // private void workingConfiguration() {
    //
    // when(project.getAnalysisType()).thenReturn(AnalysisType.DYNAMIC);
    // when(configuration.getString(MODE_KEY, MODE_SKIP)).thenReturn(MODE_REUSE_REPORT);
    // }
    //
    // private void profileWithActiveRule() {
    //
    // final ActiveRule fakeActiveRule = mock(ActiveRule.class);
    // when(fakeActiveRule.getRule()).thenReturn(Rule.create());
    // when(rulesProfile.getActiveRulesByRepository(REPOSITORY_KEY)).thenReturn(
    // Collections.singletonList(fakeActiveRule));
    // when(rulesProfile.getName()).thenReturn("fake pit profile");
    // }
    //
    // private void profileWithoutActiveRule() {
    //
    // when(rulesProfile.getActiveRulesByRepository(REPOSITORY_KEY)).thenReturn(Collections.<ActiveRule> emptyList());
    // when(rulesProfile.getName()).thenReturn("fake pit profile");
    // }
    //
    // private PitestSensor buildSensor() {
    //
    // when(fileSystem.baseDir()).thenReturn(TestUtils.getResource("."));
    // when(configuration.getString(REPORT_DIRECTORY_KEY, REPORT_DIRECTORY_DEF)).thenReturn("");
    // when(reportFinder.findReport(TestUtils.getResource("."))).thenReturn(new File("fake-report.xml"));
    //
    // final List<Mutant> mutants = new ArrayList<Mutant>();
    // final Mutant survived = new Mutant(false, MutantStatus.SURVIVED, "survived", 42,
    // "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator");
    // mutants.add(survived);
    // mutants.add(new Mutant(false, MutantStatus.KILLED, "killed", 10,
    // "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator"));
    // mutants.add(new Mutant(false, MutantStatus.NO_COVERAGE, "no coverage", -2,
    // "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator"));
    // mutants.add(new Mutant(false, MutantStatus.MEMORY_ERROR, "memory error", 1000, null));
    // mutants.add(new Mutant(false, MutantStatus.UNKNOWN, "unkwon", 0, null));
    // when(parser.parse(any(File.class))).thenReturn(mutants);
    //
    // when(context.getResource(any(JavaFile.class))).thenAnswer(new Answer<Object>() {
    //
    // @Override
    // public Object answer(final InvocationOnMock invocationOnMock) throws Throwable {
    //
    // return javaFile;
    // }
    // });
    // final Issuable.IssueBuilder issueBuilder = mock(Issuable.IssueBuilder.class);
    // when(issueBuilder.ruleKey(any(RuleKey.class))).thenReturn(issueBuilder);
    // when(issueBuilder.line(anyInt())).thenReturn(issueBuilder);
    // when(issueBuilder.message(anyString())).thenReturn(issueBuilder);
    // when(issuable.newIssueBuilder()).thenReturn(issueBuilder);
    // when(perspectives.as(Issuable.class, javaFile)).thenReturn(issuable);
    //
    // subject = new PitestSensor(configuration, parser, rulesProfile, reportFinder, fileSystem, perspectives);
    // return subject;
    // }
    //
    // private void verifyIssueRaided() {
    //
    // verify(perspectives).as(Issuable.class, javaFile);
    // final ArgumentCaptor<Issue> issueCaptor = ArgumentCaptor.forClass(Issue.class);
    // verify(issuable, times(1)).addIssue(issueCaptor.capture());
    // }
    //
    // private void verifyMutationsSaved() {
    //
    // verify(context).saveMeasure(any(Resource.class), eq(PitestMetrics.MUTATIONS_TOTAL), eq(5d));
    // }
}
