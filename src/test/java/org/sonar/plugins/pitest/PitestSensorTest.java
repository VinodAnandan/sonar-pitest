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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issuable;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.ActiveRule;

@RunWith(MockitoJUnitRunner.class)
public class PitestSensorTest {

    @org.junit.Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @InjectMocks
    private PitestSensor subject;

    // constructor mocks
    @Mock
    private Settings settings;
    @Mock
    private ResultParser parser;
    @Mock
    private RulesProfile rulesProfile;
    @Mock
    private ReportFinder reportFinder;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private FileSystem fileSystem;

    // method arg mocks
    @Mock
    private SensorDescriptor descriptor;
    @Mock
    private SensorContext context;
    @Mock
    private Issuable issuable;
    @Mock
    private InputFile javaFile;
    @Mock
    private Project project;
    @Mock
    private ActiveRule activeRule;

    @Before
    public void setUp() {

        // setup filesystem and settings
        when(fileSystem.baseDir()).thenReturn(folder.getRoot());
        when(settings.getString("sonar.pitest.reports.directory")).thenReturn("target/pit-reports");

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
    public void testExecute_noFiles_noIssuesAndMeasures() throws Exception {

        // prepare
        final FilePredicate predicate = mock(FilePredicate.class);
        when(fileSystem.hasFiles(predicate)).thenReturn(false);
        when(settings.getBoolean("sonar.pitest.enabled")).thenReturn(Boolean.FALSE);

        // act
        subject.execute(context);

        // assert
        verify(context, times(0)).newIssue();
        verify(context, times(0)).newMeasure();

    }

    @Test
    public void testExecute_sensorDisabled_noIssuesAndMeasures() throws Exception {

        // prepare
        final FilePredicate predicate = mock(FilePredicate.class);
        when(fileSystem.hasFiles(predicate)).thenReturn(true);
        when(settings.getBoolean("sonar.pitest.enabled")).thenReturn(Boolean.FALSE);

        // act
        subject.execute(context);

        // assert
        verify(context, times(0)).newIssue();
        verify(context, times(0)).newMeasure();

    }

    @Test
    public void testExecute_sensorEnabled() throws Exception {

        // prepare

        final FilePredicate predicate = mock(FilePredicate.class);
        when(fileSystem.hasFiles(predicate)).thenReturn(true);
        when(settings.getBoolean("sonar.pitest.enabled")).thenReturn(Boolean.TRUE);
        when(rulesProfile.getActiveRules()).thenReturn(Arrays.asList(activeRule));

        // act
        subject.execute(context);

        // assert
        verify(context, times(0)).newIssue();
        verify(context, times(0)).newMeasure();

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
