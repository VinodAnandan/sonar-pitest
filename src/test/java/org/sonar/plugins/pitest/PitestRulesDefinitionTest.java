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
package org.sonar.plugins.pitest;

import java.util.List;
import org.junit.Test;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinition.Repository;
import org.sonar.api.server.rule.RulesDefinition.Rule;

import static org.assertj.core.api.Assertions.assertThat;

public class PitestRulesDefinitionTest {

  @Test
  public void contextContainsPitestRulesRepository() {
    // given
    RulesDefinition.Context context = createContext();
    PitestRulesDefinition rulesDefinition = new PitestRulesDefinition();

    // when
    // PitestSensor sensor = new PitestSensor(configuration, mockXmlReportParserOnJavaFiles(), mockRulesProfile(true, false),
    // mockXmlReportFinder(), context.fileSystem());
    rulesDefinition.define(context);

    // then
    Repository repository = context.repository(PitestConstants.REPOSITORY_KEY);

    assertThat(repository.key()).isEqualTo(PitestConstants.REPOSITORY_KEY);
  }

  @Test
  public void pitestRepositoryContainsTwo_Rules() {
    // given
    RulesDefinition.Context context = createContext();
    PitestRulesDefinition rulesDefinition = new PitestRulesDefinition();

    // when
    rulesDefinition.define(context);
    Repository repository = context.repository(PitestConstants.REPOSITORY_KEY);

    // then
    assertThat(repository.rules()).hasSize(2);
  }

  @Test
  public void rulesContainTestQualityTag() {
    // given
    RulesDefinition.Context context = createContext();
    PitestRulesDefinition rulesDefinition = new PitestRulesDefinition();

    // when
    rulesDefinition.define(context);
    List<Rule> rules = context.repository(PitestConstants.REPOSITORY_KEY).rules();

    // then
    for (Rule rule : rules) {
      assertThat(rule.tags()).contains(PitestRulesDefinition.TAG_TEST_QUALITY);
    }

  }

  private RulesDefinition.Context createContext() {
    RulesDefinition.Context context = new RulesDefinition.Context();
    return context;
  }

}
