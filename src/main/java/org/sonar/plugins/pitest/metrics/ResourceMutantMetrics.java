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
import org.sonar.plugins.pitest.model.MutantStatus;

/**
 * Metrics for Mutants found in a single resource. It is used to collect mutant information for a specific resource.
 *
 * @author <a href="mailto:alexvictoor@gmail.com">Alexandre Victoor</a>
 * @author <a href="mailto:gerald.muecke@gmail.com">Gerald Muecke</a>
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

  /**
   * Constructor for creating a new metrics holder for the given resource
   *
   * @param resource
   *         the Sonar resource for which mutation information should be collected
   */
  public ResourceMutantMetrics(final InputFile resource) {

    this.resource = resource;
  }

  /**
   * Associates the {@link Mutant} with the resource. Invoking this method will update the metrics
   *
   * @param mutant
   *         the mutant to be added
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
      default:
        break;
    }
    // update mutation coverage
    mutationCoverage = 100.0 * mutationsKilled / mutationsTotal;
  }

  /**
   * @return all mutants collected so for the resource
   */
  public Collection<Mutant> getMutants() {

    return mutants;
  }

  /**
   * The total amount of {@link Mutant} added to the metric.
   *
   * @return number of all mutations found in the resource
   */
  public int getMutationsTotal() {

    return mutationsTotal;
  }

  /**
   * The number of {@link Mutant}s added whose {@link MutantStatus} was {code NO_COVERAGE}.
   *
   * @return number of mutations that are not covered
   */
  public int getMutationsNoCoverage() {

    return mutationsNoCoverage;
  }

  /**
   * The number of {@link Mutant}s added whose {@link MutantStatus} was {@code KILLED}
   *
   * @return number of mutation killed by a test
   */
  public int getMutationsKilled() {

    return mutationsKilled;
  }

  /**
   * The number of {@link Mutant}s added whose {@link MutantStatus} was {@code SURVIVED}.
   *
   * @return number of mutations that survived a test
   */
  public int getMutationsSurvived() {

    return mutationsSurvived;
  }

  /**
   * The number of {@link Mutant}s added whose {@link MutantStatus} was {@code MEMORY_ERROR}.
   *
   * @return number of mutations killed by a memory error
   */
  public int getMutationsMemoryError() {

    return mutationsMemoryError;
  }

  /**
   * The number of {@link Mutant}s added whose {@link MutantStatus} was {@code TIMED_OUT}.
   *
   * @return number of mutations killed by a timeout
   */
  public int getMutationsTimedOut() {

    return mutationsTimedOut;
  }

  /**
   * @return number of mutations with unknown status
   */
  public int getMutationsUnknown() {

    return mutationsUnknown;
  }

  /**
   * @return the number of mutations detected at all
   */
  public int getMutationsDetected() {

    return mutationsDetected;
  }

  /**
   * @return the mutation coverage in percent, that is a value between 0.0 and 100.0
   */
  public double getMutationCoverage() {

    return mutationCoverage;
  }

  /**
   * The Sonar resource to which the mutant metrics are bound.
   *
   * @return An {@link InputFile} representing the resource.
   */
  public InputFile getResource() {

    return resource;
  }

}
