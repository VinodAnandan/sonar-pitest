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
package org.sonar.plugins.pitest.metrics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.plugins.pitest.model.Mutant;

/**
 * Metrics for Mutants found in a single resource.
 *
 * @author <a href="mailto:alexvictoor@gmail.com">Alexandre Victoor</a>
 * @author <a href="mailto:gerald.muecke@gmail.com">Gerald Muecke</a>
 *
 */
public class ResourceMutantMetrics {

    private final List<Mutant> mutants = new ArrayList<Mutant>();
    private int mutationsTotal = 0;
    private int mutationsNoCoverage = 0;
    private int mutationsKilled = 0;
    private int mutationsSurvived = 0;
    private int mutationsMemoryError = 0;
    private int mutationsTimedOut = 0;
    private int mutationsUnknown = 0;
    private int mutationsDetected = 0;
    private double mutationCoverage = 0;
    private final InputFile resource;

    public ResourceMutantMetrics(final InputFile resource) {

        this.resource = resource;
    }

    /**
     * Associates the mutant with the resource
     *
     * @param mutant
     */
    public void addMutant(final Mutant mutant) {

        mutants.add(mutant);
        if (mutant.isDetected()) {
            mutationsDetected++;
        }
        mutationsTotal++;
        switch (mutant.getMutantStatus()) {
        case KILLED:
            mutationsKilled++;
            break;
        case NO_COVERAGE:
            mutationsNoCoverage++;
            break;
        case SURVIVED:
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
        updateMutationCoverage();
    }

    private void updateMutationCoverage() {

        if (mutationsTotal > 0) {
            mutationCoverage = 100.0 * mutationsKilled / mutationsTotal;
        }

    }

    public Collection<Mutant> getMutants() {

        return mutants;
    }

    public int getMutationsTotal() {

        return mutationsTotal;
    }

    public int getMutationsNoCoverage() {

        return mutationsNoCoverage;
    }

    public int getMutationsKilled() {

        return mutationsKilled;
    }

    public int getMutationsSurvived() {

        return mutationsSurvived;
    }

    public int getMutationsMemoryError() {

        return mutationsMemoryError;
    }

    public int getMutationsTimedOut() {

        return mutationsTimedOut;
    }

    public int getMutationsUnknown() {

        return mutationsUnknown;
    }

    public int getMutationsDetected() {

        return mutationsDetected;
    }

    public double getMutationCoverage() {

        return mutationCoverage;
    }

    /**
     * The resource to which the mutant metrics are bound.
     *
     * @return
     */
    public InputFile getResource() {

        return resource;
    }

}