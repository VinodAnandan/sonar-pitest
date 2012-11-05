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

import java.util.ArrayList;
import java.util.List;

public class JavaFileMutants {
	private List<Mutant> mutants = new ArrayList<Mutant>();
	private double mutationsTotal = 0;
	private double mutationsNoCoverage = 0;
	private double mutationsKilled = 0;
	private double mutationsSurvived = 0;
	private double mutationsMemoryError = 0;
	private double mutationsTimedOut = 0;
	private double mutationsUnknown = 0;
	private double mutationsDetected = 0;

	void addMutant(Mutant mutant) {
		mutants.add(mutant);
		if (mutant.isDetected()) {
			incMutationsDetected();
		}
		switch (mutant.getMutantStatus()) {
			case KILLED:
				incMutationsKilled();
				break;
			case NO_COVERAGE:
				incMutationsNoCoverage();
				break;
			case SURVIVED: // Only survived mutations are saved as violations
				incMutationsSurvived();
				break;
			case MEMORY_ERROR:
				incMutationsMemoryError();
				break;
			case TIMED_OUT:
				incMutationsTimedOut();
				break;
			case UNKNOWN:
				incMutationsUnknown();
				break;
		}
	}

	private void incMutationsTotal() {
		mutationsTotal++;
	}

	private void incMutationsNoCoverage() {
		incMutationsTotal();
		mutationsNoCoverage++;
	}

	private void incMutationsKilled() {
		incMutationsTotal();
		mutationsKilled++;
	}

	private void incMutationsSurvived() {
		incMutationsTotal();
		mutationsSurvived++;
	}

	private void incMutationsMemoryError() {
		incMutationsTotal();
		mutationsMemoryError++;
	}

	private void incMutationsTimedOut() {
		incMutationsTotal();
		mutationsTimedOut++;
	}

	private void incMutationsUnknown() {
		incMutationsTotal();
		mutationsUnknown++;
	}

	private void incMutationsDetected() {
		mutationsDetected++;
	}

	List<Mutant> getMutants() {
		return mutants;
	}

	/**
	 * @return the mutationsTotal
	 */
	double getMutationsTotal() {
		return mutationsTotal;
	}

	/**
	 * @return the mutationsNoCoverage
	 */
	double getMutationsNoCoverage() {
		return mutationsNoCoverage;
	}

	/**
	 * @return the mutationsKilled
	 */
	double getMutationsKilled() {
		return mutationsKilled;
	}

	/**
	 * @return the mutationsSurvived
	 */
	double getMutationsSurvived() {
		return mutationsSurvived;
	}

	/**
	 * @return the mutationsMemoryError
	 */
	double getMutationsMemoryError() {
		return mutationsMemoryError;
	}

	/**
	 * @return the mutationsTimedOut
	 */
	double getMutationsTimedOut() {
		return mutationsTimedOut;
	}

	/**
	 * @return the mutationsUnknown
	 */
	double getMutationsUnknown() {
		return mutationsUnknown;
	}

	/**
	 * @return the mutationsDetected
	 */
	double getMutationsDetected() {
		return mutationsDetected;
	}
}