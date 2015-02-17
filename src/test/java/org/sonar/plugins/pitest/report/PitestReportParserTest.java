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
package org.sonar.plugins.pitest.report;

import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

import org.codehaus.plexus.util.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.pitest.model.Mutant;
import org.sonar.plugins.pitest.model.MutantStatus;
import org.sonar.plugins.pitest.model.Mutator;

public class PitestReportParserTest {

    private PitestReportParser subject;

    @Before
    public void setUp() {

        subject = new PitestReportParser();
    }

    @Test
    public void testParseReport_findMutants() throws IOException {

        // prepare
        final Path report = FileUtils.toFile(getClass().getResource("PitestReportParserTest_mutations.xml")).toPath();

        // act
        final Collection<Mutant> mutants = subject.parseMutants(report);

        // assert
        assertThat(mutants).isNotEmpty().hasSize(3);

        //@formatter:off
        assertThat(mutants).contains( new Mutant(true,MutantStatus.KILLED,"Mutant.java",
                "org.sonar.plugins.pitest.model.Mutant","equals","(Ljava/lang/Object;)Z",162,
                Mutator.find("org.pitest.mutationtest.engine.gregor.mutators.NegateConditionalsMutator"),"",
                5,"org.sonar.plugins.pitest.model.MutantTest.testEquals_different_false(org.sonar.plugins.pitest.model.MutantTest)"));
        assertThat(mutants).contains(new Mutant(false, MutantStatus.SURVIVED, "Mutant.java",
                "org.sonar.plugins.pitest.model.Mutant", "equals","(Ljava/lang/Object;)Z", 172,
                Mutator.find("org.pitest.mutationtest.engine.gregor.mutators.NegateConditionalsMutator"), "",
                43,""));
        assertThat(mutants).contains(new Mutant(false,MutantStatus.NO_COVERAGE,"Mutant.java",
                "org.sonar.plugins.pitest.model.Mutant","equals","(Ljava/lang/Object;)Z",175,
                Mutator.find("org.pitest.mutationtest.engine.gregor.mutators.NegateConditionalsMutator"),"",
                55, ""));
        // @formatter:on
        assertThat(mutants).onProperty("mutantStatus").excludes(MutantStatus.UNKNOWN);
        assertThat(mutants).onProperty("mutantStatus").excludes(MutantStatus.MEMORY_ERROR);
        assertThat(mutants).onProperty("mutantStatus").excludes(MutantStatus.TIMED_OUT);

    }
}
