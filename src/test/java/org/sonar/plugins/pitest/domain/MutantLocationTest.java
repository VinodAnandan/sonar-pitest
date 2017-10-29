/*
 * Sonar Pitest Plugin
 * Copyright (C) 2009-2017 Vinod Anandan
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
package org.sonar.plugins.pitest.domain;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MutantLocationTest {

  @Test
  public void should_calculate_relative_path_java() {
    // given
    MutantLocation mutantLocation = new TestMutantLocationBuilder().sourceFile("Bar.java").className("com.foo.Bar").build();

    // when
    String path = mutantLocation.getRelativePath();

    // then
    assertThat(path).isEqualTo("com/foo/Bar.java");
  }

  @Test
  public void should_calculate_relative_path_java_inner_class() {
    // given
    MutantLocation mutantLocation = new TestMutantLocationBuilder().sourceFile("Bar.java").className("com.foo.Bar$1").build();

    // when
    String path = mutantLocation.getRelativePath();

    // then
    assertThat(path).isEqualTo("com/foo/Bar.java");
  }

  @Test
  public void should_calculate_relative_path_kotlin() {
    // given
    MutantLocation mutantLocation = new TestMutantLocationBuilder().sourceFile("MainKotlin.kt").className("some.Hello").build();

    // when
    String path = mutantLocation.getRelativePath();

    // then
    assertThat(path).isEqualTo("MainKotlin.kt");
  }
}
