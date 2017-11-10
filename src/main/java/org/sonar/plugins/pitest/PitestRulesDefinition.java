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

import org.sonar.api.rule.RuleStatus;
import org.sonar.api.rule.Severity;
import org.sonar.api.rules.RuleType;
import org.sonar.api.server.rule.RulesDefinition;

import static org.sonar.plugins.pitest.PitestConstants.COVERAGE_RATIO_PARAM;
import static org.sonar.plugins.pitest.PitestConstants.INSUFFICIENT_MUTATION_COVERAGE_RULE_KEY;
import static org.sonar.plugins.pitest.PitestConstants.REPOSITORY_KEY;
import static org.sonar.plugins.pitest.PitestConstants.REPOSITORY_NAME;
import static org.sonar.plugins.pitest.PitestConstants.SURVIVED_MUTANT_RULE_KEY;

public class PitestRulesDefinition implements RulesDefinition {

  public static final String TAG_TEST_QUALITY = "test-quality";
  public static final String TAG_TEST_COVERAGE = "test-coverage";

  @Override
  public void define(Context context) {
    NewRepository repository = context
      .createRepository(REPOSITORY_KEY, "java")
      .setName(REPOSITORY_NAME);

    /*
     * Rule: Survived Mutant
     * Current thinking is that a survived mutant is at least as severe as missing code coverage, probably more severe.
     * Reason for more severe: a test covers this code, so there may be a false sense of security regarding test coverage
     */
    repository.createRule(SURVIVED_MUTANT_RULE_KEY)
      .setName("Survived mutant")
      .setHtmlDescription(
        "An issue is created when an existing test fails to identify a mutation in the code. For more information, review the <a href=\"http://pitest.org/quickstart/mutators\">PIT documentation</a>")
      .setStatus(RuleStatus.READY)
      .setSeverity(Severity.MAJOR)
      .setType(RuleType.BUG)
      .setTags(TAG_TEST_QUALITY)
      .setActivatedByDefault(false);

    /*
     * Rule: Insufficient Mutation coverage
     */
    NewRule insufficientMutationCoverageRule = repository.createRule(INSUFFICIENT_MUTATION_COVERAGE_RULE_KEY)
      .setName("Insufficient mutation coverage")
      .setHtmlDescription(
        "An issue is created on a file as soon as the mutation coverage on this file is less than the required threshold. It gives the number of mutations to be covered in order to reach the required threshold.")
      .setStatus(RuleStatus.READY)
      .setSeverity(Severity.MAJOR)
      .setType(RuleType.BUG)
      .setTags(TAG_TEST_QUALITY, TAG_TEST_COVERAGE)
      .setActivatedByDefault(false);

    insufficientMutationCoverageRule
      .createParam(COVERAGE_RATIO_PARAM)
      .setDefaultValue("65")
      .setDescription("The minimum required mutation coverage ratio");

    repository.done();
  }
}
