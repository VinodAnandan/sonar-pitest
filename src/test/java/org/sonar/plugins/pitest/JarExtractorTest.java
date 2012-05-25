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
import static org.mockito.Mockito.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.After;
import org.junit.Test;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.test.TestUtils;


public class JarExtractorTest {
  
  private File output = new File("target/jcommander-1.12.jar");

  @Test
  public void testExtractJar() throws MalformedURLException {
    ProjectFileSystem fileSystem = mock(ProjectFileSystem.class);
    when(fileSystem.getSonarWorkingDirectory()).thenReturn(new File("target"));
    JarExtractor extractor = new JarExtractor(fileSystem);
    URL jarURL = new URL("jar:file:/"+TestUtils.getResource("sonar-pitest-plugin.jar").getAbsolutePath()+"!/META-INF/lib/pitest.jar");
    
    extractor.extractJar(jarURL, "jcommander-1.12.jar");
    assertThat(output).exists();
  }
  
  @After
  public void cleanUp() {
    if (output.exists()) {
      output.delete();
    }
  }

}
