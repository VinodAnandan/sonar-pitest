/*
 * Sonar Pitest Plugin
 * Copyright (C) 2009-2016 Alexandre Victoor
 * alexvictoor@gmail.com
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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import com.google.common.io.Resources;

public class XmlReportParserTest {

	private XmlReportParser parser;

	@Before
	public void setUp() {
		parser = new XmlReportParser();
	}

	@Test
	public void should_parse_report_and_find_mutants() {
		// given
		File report = new File(Resources.getResource("mutations.xml").getFile());
		
		// when
		Collection<Mutant> mutants = parser.parse(report);

		// then
		assertThat(mutants).hasSize(39);

		assertThat(mutants).usingFieldByFieldElementComparator().contains(new Mutant(true, MutantStatus.KILLED, "org.sonar.plugins.csharp.gallio.GallioSensor", 87,
			"org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator"));
		assertThat(mutants).usingFieldByFieldElementComparator().contains(new Mutant(false, MutantStatus.NO_COVERAGE, "org.sonar.plugins.csharp.gallio.GallioSensor", 162,
				"org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutator"));
		assertThat(mutants).usingFieldByFieldElementComparator().contains(new Mutant(false, MutantStatus.SURVIVED, "org.sonar.plugins.csharp.gallio.GallioSensor", 166,
				"org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutator"));
		assertThat(mutants).usingFieldByFieldElementComparator().contains(new Mutant(true, MutantStatus.MEMORY_ERROR, "org.sonar.plugins.csharp.gallio.GallioSensor", 176,
				"org.pitest.mutationtest.engine.gregor.mutators.NegateConditionalsMutator"));
		assertThat(mutants).extracting("lineNumber").contains(166);
		assertThat(mutants).extracting("mutantStatus").doesNotContain(MutantStatus.UNKNOWN);
	}
}
