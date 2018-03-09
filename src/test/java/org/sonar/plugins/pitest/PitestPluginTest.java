/*
 * Sonar Pitest Plugin
 * Copyright (C) 2009-2018 Vinod Anandan
 * vinod@owasp.org
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

import org.junit.Test;
import org.sonar.api.Plugin;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;

import static org.assertj.core.api.Assertions.assertThat;

public class PitestPluginTest {

  @Test
  public void test_scanner_side_plugin_extensions_compatible_with_5_6() {

    PitestPlugin underTest = new PitestPlugin();
    SonarRuntime runtime = SonarRuntimeImpl.forSonarQube(Version.create(5, 6), SonarQubeSide.SCANNER);
    Plugin.Context context = new Plugin.Context(runtime);
    underTest.define(context);
    assertThat(context.getExtensions()).hasSize(9);
  }

  @Test
  public void test_scanner_side_plugin_extensions_compatible_with_6_7() {

    PitestPlugin underTest = new PitestPlugin();
    SonarRuntime runtime = SonarRuntimeImpl.forSonarQube(Version.create(6, 7), SonarQubeSide.SCANNER);
    Plugin.Context context = new Plugin.Context(runtime);
    underTest.define(context);
    assertThat(context.getExtensions()).hasSize(9);
  }

  @Test
  public void test_compute_engine_side_plugin_extensions_compatible_with_5_6() {

    PitestPlugin underTest = new PitestPlugin();
    SonarRuntime runtime = SonarRuntimeImpl.forSonarQube(Version.create(5, 6), SonarQubeSide.COMPUTE_ENGINE);
    Plugin.Context context = new Plugin.Context(runtime);
    underTest.define(context);
    assertThat(context.getExtensions()).hasSize(9);
  }

  @Test
  public void test_compute_engine_side_plugin_extensions_compatible_with_6_7() {

    PitestPlugin underTest = new PitestPlugin();
    SonarRuntime runtime = SonarRuntimeImpl.forSonarQube(Version.create(6, 7), SonarQubeSide.COMPUTE_ENGINE);
    Plugin.Context context = new Plugin.Context(runtime);
    underTest.define(context);
    assertThat(context.getExtensions()).hasSize(9);
  }
}
