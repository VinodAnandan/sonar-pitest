/*
 * Sonar Pitest Plugin
 * Copyright (C) 2009-2016 SonarQubeCommunity
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.pitest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

/**
 * Mutants for a given java source file
 */
public class SourceFileReport {
	public final String sourceFileRelativePath;
	private final List<Mutant> mutants = new ArrayList<Mutant>();
	private int mutationsNoCoverage = 0;
	private int mutationsKilled = 0;
	private int mutationsSurvived = 0;
	private int mutationsMemoryError = 0;
	private int mutationsTimedOut = 0;
	private int mutationsUnknown = 0;
	private int mutationsDetected = 0;

	public SourceFileReport(String sourceFileRelativePath) {
		this.sourceFileRelativePath = sourceFileRelativePath;
	}

	public String toJSON() {
		if (mutants.isEmpty()) {
			return null;
		}
		Map<Integer,List<String>> mutantsByLine = new HashMap<Integer,List<String>>();

		for (Mutant mutant : mutants) {
			if(!mutantsByLine.containsKey(mutant.lineNumber)){
				mutantsByLine.put(mutant.lineNumber, new ArrayList<String>());
			}
			mutantsByLine.get(mutant.lineNumber).add(mutant.toString());
		}

		StringBuilder builder = new StringBuilder();
		builder.append("{");
		boolean first = true;
		for (int line : mutantsByLine.keySet()) {
			if (!first) {
				builder.append(",");
			}
			first = false;
			builder.append("\"");
			builder.append(line);
			builder.append("\":[");
			builder.append(Joiner.on(",").join(mutantsByLine.get(line)));
			builder.append("]");
		}
		builder.append("}");

		return builder.toString();
	}


	public void addMutant(Mutant mutant) {
		Preconditions.checkArgument(sourceFileRelativePath.equals(mutant.sourceRelativePath()));
		mutants.add(mutant);
		if (mutant.detected) {
			mutationsDetected++;
		}
		switch (mutant.mutantStatus) {
			case KILLED:
				mutationsKilled++;
				break;
			case NO_COVERAGE:
				mutationsNoCoverage++;
				break;
			case SURVIVED: // Only survived mutations are saved as violations
				mutationsSurvived++;
				break;
			case MEMORY_ERROR:
				mutationsMemoryError++;
				break;
			case TIMED_OUT:
				mutationsTimedOut++;
				break;
			case UNKNOWN:
				mutationsUnknown++;
				break;
		}
	}

	public Collection<Mutant> getMutants() {
		return Collections.unmodifiableList(mutants);
	}

	public int getMutationsTotal() {
		return mutants.size();
	}

	double getMutationsNoCoverage() {
		return mutationsNoCoverage;
	}
	double getMutationsKilled() {
		return mutationsKilled;
	}
	double getMutationsSurvived() {
		return mutationsSurvived;
	}
	double getMutationsMemoryError() {
		return mutationsMemoryError;
	}
	double getMutationsTimedOut() {
		return mutationsTimedOut;
	}
	double getMutationsUnknown() {
		return mutationsUnknown;
	}

	double getMutationsDetected() {
		return mutationsDetected;
	}
}
