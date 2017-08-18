/*
 * Sonar Pitest Plugin
 * Copyright (C) 2009-2016 Alexandre Victoor
 * alexvictoor@gmail.com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.pitest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.pitest.PitestConstants.COVERAGE_RATIO_PARAM;
import static org.sonar.plugins.pitest.PitestConstants.INSUFFICIENT_MUTATION_COVERAGE_RULE_KEY;
import static org.sonar.plugins.pitest.PitestConstants.MODE_KEY;
import static org.sonar.plugins.pitest.PitestConstants.MODE_REUSE_REPORT;
import static org.sonar.plugins.pitest.PitestConstants.MODE_SKIP;
import static org.sonar.plugins.pitest.PitestConstants.REPORT_DIRECTORY_DEF;
import static org.sonar.plugins.pitest.PitestConstants.REPORT_DIRECTORY_KEY;
import static org.sonar.plugins.pitest.PitestConstants.REPOSITORY_KEY;
import static org.sonar.plugins.pitest.PitestConstants.SURVIVED_MUTANT_RULE_KEY;
import static org.sonar.plugins.pitest.PitestMetrics.MUTATIONS_DETECTED;
import static org.sonar.plugins.pitest.PitestMetrics.MUTATIONS_KILLED;
import static org.sonar.plugins.pitest.PitestMetrics.MUTATIONS_MEMORY_ERROR;
import static org.sonar.plugins.pitest.PitestMetrics.MUTATIONS_NO_COVERAGE;
import static org.sonar.plugins.pitest.PitestMetrics.MUTATIONS_SURVIVED;
import static org.sonar.plugins.pitest.PitestMetrics.MUTATIONS_TOTAL;
import static org.sonar.plugins.pitest.PitestMetrics.MUTATIONS_UNKNOWN;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.FileMetadata;
import org.sonar.api.batch.fs.internal.Metadata;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.Settings;
import org.sonar.api.config.internal.ConfigurationBridge;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

@RunWith(MockitoJUnitRunner.class)
public class PitestSensorTest {

  @Mock
  private RulesProfile rulesProfile;
  @Mock
  private XmlReportParser parser;
  @Mock
  private XmlReportFinder xmlReportFinder;

  private PitestSensor sensor;
  private Mutant survivedMutant;

  private final File baseDir = new File("src/test/resources");
  private final SensorContextTester context = SensorContextTester.create(baseDir);
  private final Settings settings = new MapSettings() ;
  private final DefaultFileSystem fileSystem = context.fileSystem();


  @Test
  public void should_skip_analysis_if_no_specific_pit_configuration() throws Exception {
    // given
	
	settings.setProperty(MODE_KEY, MODE_SKIP);
    profileWithMutantRule();
    sensor = buildSensor();
    // when
    sensor.execute(context);
    // then
    assertThat(context.allIssues()).isEmpty();
  }

  @Test
  public void should_do_analysis_if_pit_mode_set_to_reuse_report() throws Exception {
    // given
    workingConfiguration();
    profileWithMutantRule();
    sensor = buildSensor();
    // when
    sensor.execute(context);
    // then
    assertThat(context.allIssues()).isNotEmpty();
  }

  @Test
  public void should_save_measures_when_pitest_rule_not_activated() throws Exception {
    // given
    workingConfiguration();
    sensor = buildSensor();
    // when
    sensor.execute(context);
    // then
    verifyMeasuresSaved();
  }

  @Test
  public void should_raise_issue_when_reuse_mode_active_and_mutation_rule_active() throws Exception {
    // given
    workingConfiguration();
    profileWithMutantRule();
    sensor = buildSensor();
    // when
    sensor.execute(context);
    // then
    assertThat(context.allIssues()).isNotEmpty();
  }

  @Test
  public void should_raise_issue_when_reuse_mode_active_and_coverage_rule_active() throws Exception {
    // given
    workingConfiguration();
    profileWithCoverageRule();
    sensor = buildSensor();
    // when
    sensor.execute(context);
    // then
    assertThat(context.allIssues()).isNotEmpty();
  }

  @Test
  public void should_not_raise_issue_when_coverage_above_rule_threshold() throws Exception {
    // given
    workingConfiguration();
    profileWithLowCoverageRule();
    sensor = buildSensor();
    // when
    sensor.execute(context);
    // then
    assertThat(context.allIssues()).isEmpty();
  }

  @Test
  public void should_not_fail_when_no_report_found() throws Exception {
    // given
    workingConfiguration();
    settings.setProperty(REPORT_DIRECTORY_KEY, "");
    
    when(xmlReportFinder.findReport(new File(Resources.getResource(".").toURI()))).thenReturn(null);
    Configuration configuration = new ConfigurationBridge(settings);
    sensor = new PitestSensor(configuration, parser, rulesProfile, xmlReportFinder, fileSystem);
    // when
    sensor.execute(context);
    // then no failure
  }

  private void workingConfiguration() {
    settings.setProperty(MODE_KEY, MODE_REUSE_REPORT);
    fileSystem
      .add(
        createInputFile()
      );
  }

  private DefaultInputFile createInputFile() {
	  
	  // FIXME: revisit
	  InputStream openStream;
	try {
		openStream = Resources.getResource("com/foo/Bar.java").openStream();
		  Metadata fm = new FileMetadata().readMetadata(openStream, Charsets.UTF_8, "com/foo/Bar.java");
	    return new TestInputFileBuilder("module.key", "com/foo/Bar.java")
	      .setType(InputFile.Type.MAIN)
	      .setLines(1000)
	      .setOriginalLineOffsets(new int[]{0, 2, 10, 42, 1000})
	      .setLastValidOffset(1)
	      .setLanguage("java")
	      .setCharset(Charsets.UTF_8)
	      .setMetadata(fm)
	    	  .build();
	} catch (IOException e) {
		throw new IllegalArgumentException("couldn't open resource");
	}

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
    settings.setProperty(REPORT_DIRECTORY_KEY, REPORT_DIRECTORY_DEF);
    when(xmlReportFinder.findReport(any(File.class))).thenReturn(new File("fake-report.xml"));

    List<Mutant> mutants = new ArrayList<>();
    survivedMutant = new Mutant(false, MutantStatus.SURVIVED, "com.foo.Bar", 42, "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator");
    mutants.add(survivedMutant);
    mutants.add(new Mutant(true, MutantStatus.KILLED, "com.foo.Bar", 10, "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator"));
    mutants.add(new Mutant(false, MutantStatus.NO_COVERAGE, "com.foo.Bar", 2, "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator"));
    mutants.add(new Mutant(false, MutantStatus.MEMORY_ERROR, "com.foo.Bar", 1000, null));
    mutants.add(new Mutant(false, MutantStatus.UNKNOWN, "com.foo.Bar", 0, null));
    when(parser.parse(any(File.class))).thenReturn(mutants);

    Configuration configuration = new ConfigurationBridge(settings);
    sensor = new PitestSensor(configuration, parser, rulesProfile, xmlReportFinder, fileSystem);
    return sensor;
  }

  private void verifyMeasuresSaved() {
    String componentKey = "module.key:com/foo/Bar.java";
    assertThat(context.measure(componentKey, MUTATIONS_TOTAL).value()).isEqualTo(5);
    assertThat(context.measure(componentKey, MUTATIONS_DETECTED).value()).isEqualTo(1);
    assertThat(context.measure(componentKey, MUTATIONS_KILLED).value()).isEqualTo(1);
    assertThat(context.measure(componentKey, MUTATIONS_MEMORY_ERROR).value()).isEqualTo(1);
    assertThat(context.measure(componentKey, MUTATIONS_SURVIVED).value()).isEqualTo(1);
    assertThat(context.measure(componentKey, MUTATIONS_UNKNOWN).value()).isEqualTo(1);
    assertThat(context.measure(componentKey, MUTATIONS_NO_COVERAGE).value()).isEqualTo(1);
  }


}
