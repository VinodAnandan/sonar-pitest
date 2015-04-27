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

import org.sonar.api.server.rule.RulesDefinition;

import static org.sonar.plugins.pitest.PitestConstants.*;

public class PitestRulesDefinition implements RulesDefinition {

  public void define(Context context) {
    NewRepository repository = context
      .createRepository(REPOSITORY_KEY, "java")
      .setName(REPOSITORY_NAME);

    repository.createRule(SURVIVED_MUTANT_RULE_KEY)
      .setHtmlDescription("Survived mutant. For more information check out the <a href=\"http://pitest.org/quickstart/mutators\">PIT documentation</a>")
      .setName("Survived mutant");

    repository.createRule(INSUFFICIENT_MUTATION_COVERAGE_RULE_KEY)
      .setHtmlDescription("An issue is created on a file as soon as the mutation coverage on this file is less than the required threshold. It gives the number of mutations to be covered in order to reach the required threshold.")
      .setName("Insufficient mutation coverage")
      .createParam(COVERAGE_RATIO_PARAM)
      .setDefaultValue("65")
      .setDescription("The minimum required mutation coverage ratio");

    repository.done();
  }
}
