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
import org.sonar.api.rule.RuleStatus;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;
import org.sonar.plugins.pitest.model.Mutator;

/**
 * The definition of pitest rules. A new repository is created for the Pitest plugin and Java language. The rules are
 * defined in the rules.xml file in the classpath. The rule keys are accessible as constants.
 *
 * @author <a href="mailto:gerald@moskito.li">Gerald Muecke</a>
 *
 */
public class PitestRulesDefinition implements RulesDefinition {

    /**
     * SLF4J Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(PitestRulesDefinition.class);

    /**
     * The key for the PITest repository
     */
    public static final String REPOSITORY_KEY = "pitest";
    /**
     * The name for the PITest repository
     */
    public static final String REPOSITORY_NAME = "Pitest";

    /**
     * Rule key for the survived mutants rule.
     */
    public static final String RULE_SURVIVED_MUTANT = "pitest.mutant.survived";

    /**
     * Rule key for the uncovered mutants rule.
     */
    public static final String RULE_UNCOVERED_MUTANT = "pitest.mutant.uncovered";

    /**
     * Rule key for the coverage of mutants not killed by a test
     */
    public static final String RULE_MUTANT_COVERAGE = "pitest.mutant.coverage";
    /**
     * The parameter for the Mutant Coverage rule defining the threshold when an issue is created
     */
    public static final String PARAM_MUTANT_COVERAGE_THRESHOLD = "pitest.mutant.coverage.threshold";

    /**
     * Rule key for the mutants with unknown status rule.
     */
    public static final String RULE_UNKNOWN_MUTANT_STATUS = "pitest.mutant.unknownStatus";

    /**
     * Prefix for mutator rules
     */
    public static final String MUTANT_RULES_PREFIX = "pitest.mutant.";

    /**
     * Loader used to load the rules from an xml files
     */
    private final RulesDefinitionXmlLoader xmlLoader;

    public PitestRulesDefinition(final RulesDefinitionXmlLoader xmlLoader) {

        this.xmlLoader = xmlLoader;
    }

    @Override
    public void define(final Context context) {

        final NewRepository repository = context.createRepository(REPOSITORY_KEY, "java").setName(REPOSITORY_NAME);
        xmlLoader.load(repository, getClass().getResourceAsStream("rules.xml"), "UTF-8");
        addMutatorRules(repository);
        repository.done();
        LOG.info("Defining PIT rule repository {} done", repository);
    }

    /**
     * Enriches the mutator rules with the descriptions from the mutators
     *
     * @param repository
     *            the repository containing the mutator rules
     */
    private void addMutatorRules(final NewRepository repository) {

        for (final Mutator mutator : Mutator.getAllMutators()) {
            //@formatter:off
            final NewRule rule = repository.createRule(MUTANT_RULES_PREFIX + mutator.getId())
                    .setName(mutator.getName())
                    .setHtmlDescription(mutator.getMutatorDescriptionLocation())
                    .setTags("pit","test","test-quality","mutator");
            // @formatter:on

            if (mutator.getId().startsWith("EXPERIMENTAL")) {
                rule.setStatus(RuleStatus.BETA);
            }
        }
    }
}
