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
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.pitest.mutationtest.ReportOptions;
import org.pitest.mutationtest.report.OutputFormat;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.test.TestUtils;
import org.testng.collections.Maps;

import com.google.common.collect.Lists;

public class ReportOptionsBuilderTest {

  private Configuration configuration;
  
  private ProjectFileSystem fileSystem;
  
  private MavenProject mvnProject;
  
  private ReportOptionsBuilder builder;
  
  private PitestConfigurationBuilder configurationBuilder;
  
  private File buildDir = new File("target");
  private File buildOutputDir = new File("target/classes");
  private File srcDir = new File("src/main/java");
  
  @Before
  public void setUp() throws Exception {
    configuration = new MapConfiguration(Maps.newHashMap());
    fileSystem = mock(ProjectFileSystem.class);
    when(fileSystem.getBuildDir()).thenReturn(buildDir);
    when(fileSystem.getBuildOutputDir()).thenReturn(buildOutputDir);
    when(fileSystem.getSourceDirs()).thenReturn(Lists.newArrayList(srcDir));
    
    mvnProject = mock(MavenProject.class);
    when(mvnProject.getTestClasspathElements()).thenReturn(getClasspathElements("test.fake.jar"));
    when(mvnProject.getCompileClasspathElements()).thenReturn(getClasspathElements("compile.fake.jar"));
    when(mvnProject.getRuntimeClasspathElements()).thenReturn(getClasspathElements("runtime.fake.jar"));
    when(mvnProject.getSystemClasspathElements()).thenReturn(getClasspathElements("system.fake.jar"));

    configurationBuilder = mock(PitestConfigurationBuilder.class);
    when(configurationBuilder.build(any(ReportOptions.class))).thenReturn(null);
    
    builder = new ReportOptionsBuilder(fileSystem, mvnProject, configuration, configurationBuilder);
  }
  
  private List<String> getClasspathElements(String fileName) {
    return Collections.singletonList(TestUtils.getResource("fake_libs/"+fileName).getAbsolutePath());
  }
  
  @Test
  public void should_build_options_with_default_maven_configuration() throws Exception {
    
    ReportOptions options = builder.build();
    assertThat(options).isNotNull();
   
    assertThat(options.getDependencyAnalysisMaxDistance()).isEqualTo(-1);
    assertThat(options.getClassPathElements()).hasSize(4);
    assertThat(options.getReportDir()).endsWith("target" + File.separator + "pit-reports");
    assertThat(options.getSourceDirs()).hasSize(1);
    File configuredSrcDir = options.getSourceDirs().iterator().next();
    assertThat(configuredSrcDir.getAbsolutePath()).endsWith("src" + File.separator + "main" + File.separator + "java");
    
    assertThat(options.getCodePaths()).hasSize(1);
    assertThat(options.getCodePaths().iterator().next()).endsWith("target" + File.separator + "classes");
    
    assertThat(options.getOutputFormats()).hasSize(1);
    assertThat(options.getOutputFormats().iterator().next()).isEqualTo(OutputFormat.XML);
    //System.out.println(options.getClassPathElements());
  }
  
  
}
