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

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.pitest.model.Mutant;
import org.sonar.plugins.pitest.model.MutantStatus;
import org.sonar.plugins.pitest.model.Mutator;
import org.sonar.test.TestUtils;

public class ResultParserTest {

    private ResultParser subject;

    @Before
    public void setUp() {

        subject = new ResultParser();
    }

    @Test
    public void should_parse_report_and_find_mutants() {

        final File report = TestUtils.getResource("mutations.xml");
        final Collection<Mutant> mutants = subject.parseMutants(report);
        assertThat(mutants).isNotEmpty().hasSize(10);

        //@formatter:off
        assertThat(mutants).contains(
                new Mutant(true,MutantStatus.KILLED,"ResourceInjection.java","io.inkstand.scribble.inject.ResourceInjection$ResourceLiteral","authenticationType","()Ljavax/annotation/Resource$AuthenticationType;",164,Mutator.find("org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator"),5,"io.inkstand.scribble.inject.ResourceInjectionTest.testByMappedName_match(io.inkstand.scribble.inject.ResourceInjectionTest)"));
        assertThat(mutants).contains(
                new Mutant(false, MutantStatus.NO_COVERAGE, "RemoteContentRepository.java","io.inkstand.scribble.rules.jcr.RemoteContentRepository", "after", "()V", 197, Mutator.find("org.pitest.mutationtest.engine.gregor.mutators.NegateConditionalsMutator"), 5,null));
        assertThat(mutants).contains(
                new Mutant(false,MutantStatus.SURVIVED,"ContentRepository.java","io.inkstand.scribble.rules.jcr.ContentRepository","before","()V",63,Mutator.find("org.pitest.mutationtest.engine.gregor.mutators.RemoveConditionalMutator_EQUAL_IF"),5, null));
        // @formatter:on
        assertThat(mutants).onProperty("mutantStatus").excludes(MutantStatus.UNKNOWN);
        assertThat(mutants).onProperty("mutantStatus").excludes(MutantStatus.MEMORY_ERROR);
        assertThat(mutants).onProperty("mutantStatus").excludes(MutantStatus.TIMED_OUT);

    }
}
