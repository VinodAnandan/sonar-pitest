/*
 * Sonar Pitest Plugin
 * Copyright (C) 2015 SonarCommunity
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.server.rule.RulesDefinition;

public class PitestRulesDefinition implements RulesDefinition {

    /**
     * SLF4J Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(PitestRulesDefinition.class);

    public static final String REPOSITORY_KEY = "pitest";
    public static final String REPOSITORY_NAME = "Pitest";

    public static final String RULE_SURVIVED_MUTANT = "pitest.survived.mutant";

    @Override
    public void define(final Context context) {

        final NewRepository repository = context.createRepository(REPOSITORY_KEY, "java").setName(REPOSITORY_NAME);
        final NewRule rule = repository
                .createRule(RULE_SURVIVED_MUTANT)
                .setName("Survived Mutants")
                .setHtmlDescription(
                        "Survived mutant. For more information check out the <a href=\"http://pitest.org/quickstart/mutators\">PIT documentation</a>");
        rule.addTags("pit", "mutation", "testing");
        repository.createRule("test").setName("TestRule").setHtmlDescription("ttt");

        repository.done();
        LOG.info("Defining PIT rule repository {} done", repository);
        // TODO add rule mutation coverage below threshold
        // TODO add separate rule for each mutator
        // TODO add mutation without test coverage
    }

}
