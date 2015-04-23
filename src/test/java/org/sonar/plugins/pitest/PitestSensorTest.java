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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;
import org.sonar.test.TestUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.sonar.plugins.pitest.PitestConstants.*;

@RunWith(MockitoJUnitRunner.class)
public class PitestSensorTest {

  private PitestSensor sensor;
  @Mock
  private RulesProfile rulesProfile;
  @Mock
  private ResultParser parser;
  @Mock
  private Settings settings;
  @Mock
  private Project project;
  @Mock
  private ReportFinder reportFinder;

  private DefaultFileSystem fileSystem = new DefaultFileSystem(TestUtils.getResource("."));
  @Mock
  private ResourcePerspectives perspectives;
  @Mock
  private SensorContext context;
  @Mock
  private Issuable issuable;
  @Mock
  private InputFile javaFile;

  @Before
  public void setUp() {
    //when(fileSystem.files(any(FileQuery.class))).thenReturn(asList(new File("whatever")));
  }

  @Test
  public void should_skip_analysis_if_no_specific_pit_configuration() throws Exception {
    // given
    when(settings.getString(MODE_KEY)).thenReturn(MODE_SKIP);
    profileWithActiveRule();
    sensor = buildSensor();
    // when
    boolean sensorExecuted = sensor.shouldExecuteOnProject(project);
    // then
    assertThat(sensorExecuted).isFalse();
  }

  @Test
  public void should_do_analysis_if_pit_mode_set_to_reuse_report() throws Exception {
    // given
    workingConfiguration();
    profileWithActiveRule();
    sensor = buildSensor();
    // when
    boolean sensorExecuted = sensor.shouldExecuteOnProject(project);
    // then
    assertThat(sensorExecuted).isTrue();
  }

  @Test
  public void should_not_skip_analysis_when_pitest_rule_not_activated() throws Exception {
    // given
    workingConfiguration();
    profileWithoutActiveRule();
    sensor = buildSensor();
    // when
    sensor.analyse(project, context);
    // then
    verifyMutationsSaved();
  }

  @Test
  public void should_parse_reports_when_reuse_mode_active() throws Exception {
    // given
    workingConfiguration();
    profileWithActiveRule();
    sensor = buildSensor();
    // when
    sensor.analyse(project, context);
    // then
    verifyIssueRaided();
    verifyMutationsSaved();
  }

  @Test
  public void should_not_fail_when_no_report_found() throws Exception {
    // given
    workingConfiguration();
    when(settings.getString(REPORT_DIRECTORY_KEY)).thenReturn("");
    when(reportFinder.findReport(TestUtils.getResource("."))).thenReturn(null);
    sensor = new PitestSensor(settings, parser, rulesProfile, reportFinder, fileSystem, perspectives);
    // when
    sensor.analyse(project, mock(SensorContext.class));
    // then no failure
  }

  private void workingConfiguration() {
    when(settings.getString(MODE_KEY)).thenReturn(MODE_REUSE_REPORT);
    fileSystem
      .add(
        createInputFile("SurvivedClazz")
      )
      .add(
        createInputFile("KilledClazz")
      )
      .add(
        createInputFile("NoCoverageClazz")
      )
    .add(
        createInputFile("MemoryErrorClazz")
    )
    .add(
      createInputFile("UnknownClazz")
    );
  }

  private DefaultInputFile createInputFile(String className) {

    return new DefaultInputFile("module.key", "com/foo/" + className + ".java")
      .setType(InputFile.Type.MAIN)
      .setModuleBaseDir(TestUtils.getResource(".").toPath())
      .setLanguage("java");
  }

  private void profileWithActiveRule() {
    ActiveRule fakeActiveRule = mock(ActiveRule.class);
    when(fakeActiveRule.getRule()).thenReturn(Rule.create());
    when(rulesProfile.getActiveRulesByRepository(REPOSITORY_KEY)).thenReturn(Collections.singletonList(fakeActiveRule));
    when(rulesProfile.getName()).thenReturn("fake pit profile");
  }

  private void profileWithoutActiveRule() {
    when(rulesProfile.getActiveRulesByRepository(REPOSITORY_KEY)).thenReturn(Collections.<ActiveRule>emptyList());
    when(rulesProfile.getName()).thenReturn("fake pit profile");
  }

  private PitestSensor buildSensor() {
    //when(fileSystem.baseDir()).thenReturn(TestUtils.getResource("."));
    when(settings.getString(REPORT_DIRECTORY_KEY)).thenReturn(REPORT_DIRECTORY_DEF);
    when(reportFinder.findReport(any(File.class))).thenReturn(new File("fake-report.xml"));

    List<Mutant> mutants = new ArrayList<Mutant>();
    Mutant survived = new Mutant(false, MutantStatus.SURVIVED, "com.foo.SurvivedClazz", 42, "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator");
    mutants.add(survived);
    mutants.add(new Mutant(false, MutantStatus.KILLED, "com.foo.KilledClazz", 10, "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator"));
    mutants.add(new Mutant(false, MutantStatus.NO_COVERAGE, "com.foo.NoCoverageClazz", -2, "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator"));
    mutants.add(new Mutant(false, MutantStatus.MEMORY_ERROR, "com.foo.MemoryErrorClazz", 1000, null));
    mutants.add(new Mutant(false, MutantStatus.UNKNOWN, "com.foo.UnknownClazz", 0, null));
    when(parser.parse(any(File.class))).thenReturn(mutants);
/*
    when(context.getResource(any(JavaFile.class))).thenAnswer(new Answer<Object>() {
      public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
        return javaFile;
      }
    });
    */
    Issuable.IssueBuilder issueBuilder = mock(Issuable.IssueBuilder.class);
    when(issueBuilder.ruleKey(any(RuleKey.class))).thenReturn(issueBuilder);
    when(issueBuilder.line(anyInt())).thenReturn(issueBuilder);
    when(issueBuilder.message(anyString())).thenReturn(issueBuilder);
    when(issuable.newIssueBuilder()).thenReturn(issueBuilder);
    when(perspectives.as(Issuable.class, createInputFile("SurvivedClazz"))).thenReturn(issuable);

    sensor = new PitestSensor(settings, parser, rulesProfile, reportFinder, fileSystem, perspectives);
    return sensor;
  }

  private void verifyIssueRaided() {
    verify(perspectives).as(Issuable.class, createInputFile("SurvivedClazz"));
    ArgumentCaptor<Issue> issueCaptor = ArgumentCaptor.forClass(Issue.class);
    verify(issuable, times(1)).addIssue(issueCaptor.capture());
  }

  private void verifyMutationsSaved() {
    verify(context, times(5)).saveMeasure(any(InputFile.class), eq(PitestMetrics.MUTATIONS_TOTAL), eq(1d));
  }
}
