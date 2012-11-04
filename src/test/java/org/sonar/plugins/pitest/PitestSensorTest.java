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

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.pitest.PitestConstants.MODE_ACTIVE;
import static org.sonar.plugins.pitest.PitestConstants.MODE_KEY;
import static org.sonar.plugins.pitest.PitestConstants.MODE_REUSE_REPORT;
import static org.sonar.plugins.pitest.PitestConstants.MODE_SKIP;
import static org.sonar.plugins.pitest.PitestConstants.REPORT_DIRECTORY_DEF;
import static org.sonar.plugins.pitest.PitestConstants.REPORT_DIRECTORY_KEY;
import static org.sonar.plugins.pitest.PitestConstants.REPOSITORY_KEY;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Java;
import org.sonar.api.resources.JavaFile;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Project.AnalysisType;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.Violation;
import org.sonar.plugins.pitest.Mutant.MutantStatus;
import org.sonar.test.TestUtils;

public class PitestSensorTest {

	private PitestSensor sensor;
	private RulesProfile rulesProfile;
	private ResultParser parser;
	private Configuration configuration;
	private PitestExecutor executor;
	private Project project;

	@Before
	public void setUp() {
		rulesProfile = mock(RulesProfile.class);
		parser = mock(ResultParser.class);
		configuration = mock(Configuration.class);
		executor = mock(PitestExecutor.class);
		project = mock(Project.class);
		when(project.getLanguageKey()).thenReturn(Java.KEY);
	}

	private void createSensor() {
		sensor = new PitestSensor(configuration, parser, executor, rulesProfile);
	}

	@Test
	public void should_skip_analysis_if_no_specific_pit_configuration() throws Exception {
		when(project.getAnalysisType()).thenReturn(AnalysisType.DYNAMIC);
		when(configuration.getString(MODE_KEY, MODE_SKIP)).thenReturn(MODE_SKIP);
		createSensor();
		assertThat(sensor.shouldExecuteOnProject(project)).isFalse();
	}

	@Test
	public void should_do_analysis_if_pit_mode_set_propertly() throws Exception {
		when(project.getAnalysisType()).thenReturn(AnalysisType.DYNAMIC);
		when(configuration.getString(MODE_KEY, MODE_SKIP)).thenReturn(MODE_ACTIVE);
		createSensor();

		assertThat(sensor.shouldExecuteOnProject(project)).isTrue();
	}

	@Test
	public void should_do_analysis_if_pit_mode_set_to_reuse_report() throws Exception {
		when(project.getAnalysisType()).thenReturn(AnalysisType.DYNAMIC);
		when(configuration.getString(MODE_KEY, MODE_SKIP)).thenReturn(MODE_REUSE_REPORT);
		createSensor();

		assertThat(sensor.shouldExecuteOnProject(project)).isTrue();
	}

	@Test
	public void should_skip_analysis_if_dynamic_analysis_disabled() throws Exception {
		when(project.getAnalysisType()).thenReturn(AnalysisType.STATIC);
		when(configuration.getString(MODE_KEY, MODE_SKIP)).thenReturn(MODE_ACTIVE);
		createSensor();

		assertThat(sensor.shouldExecuteOnProject(project)).isFalse();
	}

	@Test
	public void should_skip_analysis_when_pitest_rule_not_activated() throws Exception {
		when(project.getAnalysisType()).thenReturn(AnalysisType.DYNAMIC);
		when(configuration.getString(MODE_KEY, MODE_SKIP)).thenReturn(MODE_ACTIVE);
		when(rulesProfile.getActiveRulesByRepository(REPOSITORY_KEY)).thenReturn(Collections.EMPTY_LIST);
    when(rulesProfile.getName()).thenReturn("fake pit profile");
    createSensor();

    sensor.analyse(project, null);

    verify(executor, never()).execute();
	}

	@Test
	public void should_launch_pit_and_parse_reports_in_normal_mode() throws Exception {
		when(project.getAnalysisType()).thenReturn(AnalysisType.DYNAMIC);
		when(configuration.getString(MODE_KEY, MODE_SKIP)).thenReturn(MODE_ACTIVE);
		should_parse_report(false, true);
	}

	@Test
	public void should_parse_reports_in_reuse_mode() throws Exception {
		when(project.getAnalysisType()).thenReturn(AnalysisType.DYNAMIC);
		when(configuration.getString(MODE_KEY, MODE_SKIP)).thenReturn(MODE_REUSE_REPORT);
		should_parse_report(true, true);
	}

	private void should_parse_report(boolean reuseMode, boolean activeRule) {
		if (activeRule) {
			ActiveRule fakeActiveRule = mock(ActiveRule.class);
			when(fakeActiveRule.getRule()).thenReturn(Rule.create());
			when(rulesProfile.getActiveRulesByRepository(REPOSITORY_KEY)).thenReturn(Collections.singletonList(fakeActiveRule));
		}
		else {
			when(rulesProfile.getActiveRulesByRepository(REPOSITORY_KEY)).thenReturn(Collections.<ActiveRule>emptyList());
		}
		when(rulesProfile.getName()).thenReturn("fake pit profile");

		ProjectFileSystem fileSystem = mock(ProjectFileSystem.class);
		when(fileSystem.getBasedir()).thenReturn(TestUtils.getResource("."));
		when(project.getFileSystem()).thenReturn(fileSystem);
		when(configuration.getString(REPORT_DIRECTORY_KEY, REPORT_DIRECTORY_DEF)).thenReturn(".");

		List<Mutant> mutants = new ArrayList<Mutant>();
		Mutant survived = new Mutant(false, MutantStatus.SURVIVED, "survived", 42, "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator");
		mutants.add(survived);
		mutants.add(new Mutant(false, MutantStatus.KILLED, "killed", 10, "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator"));
		mutants.add(new Mutant(false, MutantStatus.NO_COVERAGE, "no coverage", -2, "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator"));
		mutants.add(new Mutant(false, MutantStatus.MEMORY_ERROR, "memory error", 1000, null));
		mutants.add(new Mutant(false, MutantStatus.UNKNOWN, "unkwon", 0, null));
		when(parser.parse(any(File.class))).thenReturn(mutants);

		createSensor();

		SensorContext context = mock(SensorContext.class);
		JavaFile javaFile = mock(JavaFile.class);
		when(context.getResource(any(JavaFile.class))).thenReturn(javaFile);
		sensor.analyse(project, context);

		if (reuseMode) {
			verify(executor, never()).execute();
		} else {
			verify(executor, times(1)).execute();
		}

		if (activeRule) {
			ArgumentCaptor<Violation> violationCaptor = ArgumentCaptor.forClass(Violation.class);
			verify(context, times(1)).saveViolation(violationCaptor.capture());
			Violation violation = violationCaptor.getValue();
			assertThat(violation.getLineId()).isEqualTo(survived.getLineNumber());
			assertThat(violation.getMessage()).isEqualTo(survived.getViolationDescription());
		}

		// TODO: Test saved metrics.
	}
}
