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
import java.util.List;

import org.sonar.api.resources.Resource;
import org.sonar.plugins.pitest.model.Mutant;

/**
 * Metrics for Mutants found in a single resource.
 *
 * @author <a href="mailto:alexvictoor@gmail.com">Alexandre Victoor</a>
 * @author <a href="mailto:gerald@moskito.li">Gerald Muecke</a>
 *
 */
public class ResourceMutantMetrics {

    private final List<Mutant> mutants = new ArrayList<Mutant>();
    private double mutationsTotal = 0;
    private double mutationsNoCoverage = 0;
    private double mutationsKilled = 0;
    private double mutationsSurvived = 0;
    private double mutationsMemoryError = 0;
    private double mutationsTimedOut = 0;
    private double mutationsUnknown = 0;
    private double mutationsDetected = 0;
    private final Resource resource;

    public ResourceMutantMetrics(final Resource resource) {

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
            incMutationsDetected();
        }
        switch (mutant.getMutantStatus()) {
        case KILLED:
            incMutationsKilled();
            break;
        case NO_COVERAGE:
            incMutationsNoCoverage();
            break;
        case SURVIVED:
            // Only survived mutations are saved as violations
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

    public List<Mutant> getMutants() {

        return mutants;
    }

    public double getMutationsTotal() {

        return mutationsTotal;
    }

    public double getMutationsNoCoverage() {

        return mutationsNoCoverage;
    }

    public double getMutationsKilled() {

        return mutationsKilled;
    }

    public double getMutationsSurvived() {

        return mutationsSurvived;
    }

    public double getMutationsMemoryError() {

        return mutationsMemoryError;
    }

    public double getMutationsTimedOut() {

        return mutationsTimedOut;
    }

    public double getMutationsUnknown() {

        return mutationsUnknown;
    }

    public double getMutationsDetected() {

        return mutationsDetected;
    }

    /**
     * The resource to which the mutant metrics are bound.
     *
     * @return
     */
    public Resource getResource() {

        return resource;
    }

}