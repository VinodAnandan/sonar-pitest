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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;
import org.sonar.test.TestUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.sonar.plugins.pitest.PitestConstants.*;

@RunWith(MockitoJUnitRunner.class)
public class PitestSensorTest {

  @Mock
  private RulesProfile rulesProfile;

  @Mock
  private XmlReportParser parser;
  @Mock
  private Settings settings;
  @Mock
  private Project project;
  @Mock
  private XmlReportFinder xmlReportFinder;

  @Mock
  private ResourcePerspectives perspectives;
  @Mock
  private SensorContext context;
  @Mock
  private Issuable issuable;
  @Mock
  private Issuable.IssueBuilder issueBuilder;
  @Mock
  private InputFile javaFile;

  private DefaultFileSystem fileSystem = new DefaultFileSystem(TestUtils.getResource("."));
  private PitestSensor sensor;
  private Mutant survivedMutant;

  @Test
  public void should_skip_analysis_if_no_specific_pit_configuration() throws Exception {
    // given
    when(settings.getString(MODE_KEY)).thenReturn(MODE_SKIP);
    profileWithMutantRule();
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
    profileWithMutantRule();
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
    sensor = buildSensor();
    // when
    sensor.analyse(project, context);
    // then
    verifyMutationsSaved();
  }

  @Test
  public void should_raise_issue_when_reuse_mode_active_and_mutation_rule_active() throws Exception {
    // given
    workingConfiguration();
    profileWithMutantRule();
    sensor = buildSensor();
    // when
    sensor.analyse(project, context);
    // then
    verifyMutationsSaved();
    verifyIssueRaided();
    verify(issueBuilder).message(survivedMutant.violationDescription());
  }

  @Test
  public void should_raise_issue_when_reuse_mode_active_and_coverage_rule_active() throws Exception {
    // given
    workingConfiguration();
    profileWithCoverageRule();
    sensor = buildSensor();
    // when
    sensor.analyse(project, context);
    // then
    verifyMutationsSaved();
    verifyIssueRaided();
    verify(issueBuilder).message(startsWith("4 more mutants need to be covered"));
  }

  @Test
  public void should_not_raise_issue_when_coverage_above_rule_threshold() throws Exception {
    // given
    workingConfiguration();
    profileWithLowCoverageRule();
    sensor = buildSensor();
    // when
    sensor.analyse(project, context);
    // then
    verifyNoIssueRaided();
  }

  @Test
  public void should_not_fail_when_no_report_found() throws Exception {
    // given
    workingConfiguration();
    when(settings.getString(REPORT_DIRECTORY_KEY)).thenReturn("");
    when(xmlReportFinder.findReport(TestUtils.getResource("."))).thenReturn(null);
    sensor = new PitestSensor(settings, parser, rulesProfile, xmlReportFinder, fileSystem, perspectives);
    // when
    sensor.analyse(project, mock(SensorContext.class));
    // then no failure
  }

  private void workingConfiguration() {
    when(settings.getString(MODE_KEY)).thenReturn(MODE_REUSE_REPORT);
    fileSystem
      .add(
        createInputFile()
      );
  }

  private DefaultInputFile createInputFile() {

    return new DefaultInputFile("module.key", "com/foo/Bar.java")
      .setType(InputFile.Type.MAIN)
      .setModuleBaseDir(TestUtils.getResource(".").toPath())
      .setLanguage("java");
  }

  private void profileWithMutantRule() {
    ActiveRule fakeActiveRule = mock(ActiveRule.class);
    when(fakeActiveRule.getRule()).thenReturn(Rule.create());
    when(rulesProfile.getActiveRule(REPOSITORY_KEY, SURVIVED_MUTANT_RULE_KEY)).thenReturn(fakeActiveRule);
    when(rulesProfile.getName()).thenReturn("fake pit profile");
  }

  private void profileWithCoverageRule() {
    ActiveRule fakeActiveRule = mock(ActiveRule.class);
    when(fakeActiveRule.getParameter(COVERAGE_RATIO_PARAM)).thenReturn("100");
    when(fakeActiveRule.getRule()).thenReturn(Rule.create());
    when(rulesProfile.getActiveRule(REPOSITORY_KEY, INSUFFICIENT_MUTATION_COVERAGE_RULE_KEY)).thenReturn(fakeActiveRule);
    when(rulesProfile.getName()).thenReturn("fake pit profile");
  }

  private void profileWithLowCoverageRule() {
    ActiveRule fakeActiveRule = mock(ActiveRule.class);
    when(fakeActiveRule.getParameter(COVERAGE_RATIO_PARAM)).thenReturn("10");
    when(fakeActiveRule.getRule()).thenReturn(Rule.create());
    when(rulesProfile.getActiveRule(REPOSITORY_KEY, INSUFFICIENT_MUTATION_COVERAGE_RULE_KEY)).thenReturn(fakeActiveRule);
    when(rulesProfile.getName()).thenReturn("fake pit profile");
  }

  private PitestSensor buildSensor() {
    when(settings.getString(REPORT_DIRECTORY_KEY)).thenReturn(REPORT_DIRECTORY_DEF);
    when(xmlReportFinder.findReport(any(File.class))).thenReturn(new File("fake-report.xml"));

    List<Mutant> mutants = new ArrayList<Mutant>();
    survivedMutant = new Mutant(false, MutantStatus.SURVIVED, "com.foo.Bar", 42, "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator");
    mutants.add(survivedMutant);
    mutants.add(new Mutant(true, MutantStatus.KILLED, "com.foo.Bar", 10, "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator"));
    mutants.add(new Mutant(false, MutantStatus.NO_COVERAGE, "com.foo.Bar", -2, "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator"));
    mutants.add(new Mutant(false, MutantStatus.MEMORY_ERROR, "com.foo.Bar", 1000, null));
    mutants.add(new Mutant(false, MutantStatus.UNKNOWN, "com.foo.Bar", 0, null));
    when(parser.parse(any(File.class))).thenReturn(mutants);

    when(issueBuilder.ruleKey(any(RuleKey.class))).thenReturn(issueBuilder);
    when(issueBuilder.line(anyInt())).thenReturn(issueBuilder);
    when(issueBuilder.message(anyString())).thenReturn(issueBuilder);
    when(issuable.newIssueBuilder()).thenReturn(issueBuilder);
    when(perspectives.as(Issuable.class, createInputFile())).thenReturn(issuable);

    sensor = new PitestSensor(settings, parser, rulesProfile, xmlReportFinder, fileSystem, perspectives);
    return sensor;
  }

  private void verifyIssueRaided() {
    verify(perspectives).as(Issuable.class, createInputFile());
    verify(issuable).addIssue(any(Issue.class));
  }

  private void verifyNoIssueRaided() {
    verify(perspectives).as(Issuable.class, createInputFile());
    verify(issuable, never()).addIssue(any(Issue.class));
  }

  private void verifyMutationsSaved() {
    verify(context).saveMeasure(any(InputFile.class), eq(PitestMetrics.MUTATIONS_TOTAL), eq(5d));
    verify(context).saveMeasure(any(InputFile.class), eq(PitestMetrics.MUTATIONS_DETECTED), eq(1d));
    verify(context).saveMeasure(any(InputFile.class), eq(PitestMetrics.MUTATIONS_KILLED), eq(1d));
    verify(context).saveMeasure(any(InputFile.class), eq(PitestMetrics.MUTATIONS_MEMORY_ERROR), eq(1d));
    verify(context).saveMeasure(any(InputFile.class), eq(PitestMetrics.MUTATIONS_SURVIVED), eq(1d));
    verify(context).saveMeasure(any(InputFile.class), eq(PitestMetrics.MUTATIONS_UNKNOWN), eq(1d));
    verify(context).saveMeasure(any(InputFile.class), eq(PitestMetrics.MUTATIONS_NO_COVERAGE), eq(1d));

  }
}
