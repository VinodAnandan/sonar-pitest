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
package org.sonar.plugins.pitest.model;

/**
 * A builder for creating a new mutant. The builder allows a sequential setting of the mutant parameters while the
 * {@link Mutant} class itself is for immutable instances and therefore needs all parameters at construction time.
 *
 * @author <a href="mailto:gerald.muecke@gmail.com">Gerald Muecke</a>
 */
public class MutantBuilder {

  private boolean detected = false;
  private MutantStatus mutantStatus;
  private String sourceFile;
  private String mutatedClass;
  private String mutatedMethod;
  private String methodDescription;
  private int lineNumber;
  private Mutator mutator;
  private String mutatorSuffix;
  private int index;
  private String killingTest = "";

  MutantBuilder() {

  }

  /**
   * @param detected
   *         flag to indicate if the mutant was detected by a test or not
   *
   * @return this builder
   */
  public MutantBuilder detected(final boolean detected) {

    this.detected = detected;
    return this;
  }

  /**
   * @param mutantStatus
   *         the {@link MutantStatus} of the mutant. Only killed mutants are good mutants.
   *
   * @return this builder
   */
  public MutantBuilder mutantStatus(final MutantStatus mutantStatus) {

    this.mutantStatus = mutantStatus;
    return this;
  }

  /**
   * @param statusName
   *         the {@link MutantStatus} of the mutant as a string. Only killed mutants are good mutants.
   *
   * @return this builder
   */
  public MutantBuilder mutantStatus(final String statusName) {

    mutantStatus = MutantStatus.parse(statusName);
    return this;

  }

  /**
   * @param sourceFile
   *         the path to the sourceFile that contains the mutant. The sourceFile is relative to the project path.
   *
   * @return this builder
   */
  public MutantBuilder inSourceFile(final String sourceFile) {

    this.sourceFile = sourceFile;
    return this;
  }

  /**
   * @param mutatedClass
   *         the fully qualified class name containing the mutant
   *
   * @return this builder
   */
  public MutantBuilder inClass(final String mutatedClass) {

    this.mutatedClass = mutatedClass;
    return this;
  }

  /**
   * @param mutatedMethod
   *         the name of the method containing the mutant
   *
   * @return this builder
   */
  public MutantBuilder inMethod(final String mutatedMethod) {

    this.mutatedMethod = mutatedMethod;
    return this;
  }

  /**
   * @param methodDescription
   *         the description of the method that specifies its signature. see {@link http
   *         ://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.3.3}
   *
   * @return this builder
   */
  public MutantBuilder withMethodParameters(final String methodDescription) {

    this.methodDescription = methodDescription;
    return this;
  }

  /**
   * @param lineNumber
   *         the line number where the mutant was found
   *
   * @return this builder
   */
  public MutantBuilder inLine(final int lineNumber) {

    this.lineNumber = lineNumber;
    return this;
  }

  /**
   * @param mutator
   *         the mutator that was used to create the mutant
   *
   * @return this builder
   */
  public MutantBuilder usingMutator(final Mutator mutator) {

    this.mutator = mutator;
    mutatorSuffix = "";
    return this;
  }

  /**
   * @param mutatorName
   *         the mutator that was used to create the mutant specified as String. The string may be either the the ID,
   *         the fully qualified class name or the fully qualified class name and a suffix. If the mutatorName is
   *         specified with suffix, the mutator suffix is set accordingly, otherwise the empty string is used.
   *
   * @return this builder
   */
  public MutantBuilder usingMutator(final String mutatorName) {

    mutator = Mutator.find(mutatorName);

    if (mutatorName.startsWith(mutator.getClassName())) {
      mutatorSuffix = mutatorName.substring(mutator.getClassName().length());
    } else {
      mutatorSuffix = "";
    }

    if (mutatorSuffix.startsWith("_")) {
      mutatorSuffix = mutatorSuffix.substring(1);
    }
    return this;

  }

  /**
   * @param index
   *         the index of the mutator. It has no relevance to the sonar results
   *
   * @return this builder
   */
  public MutantBuilder atIndex(final int index) {

    this.index = index;
    return this;
  }

  /**
   * @param killingTest
   *         the fully qualified name of the test including the test method that killed the test. This method is
   *         optional and only has to be invoked, if the mutant was actually killed. If not invoked, the the
   *         killingTest property is passed as empty string
   *
   * @return this builder
   */
  public MutantBuilder killedBy(final String killingTest) {

    this.killingTest = killingTest;
    return this;
  }

  /**
   * Creates a new {@link Mutant} with all the parameters specified. As the {@link Mutant} requires all parameter to
   * be not-null this method will fail if some parameters are not specified.
   *
   * @return a new instance of a {@link Mutant}
   */
  public Mutant build() {

    return new Mutant(detected, mutantStatus, sourceFile, mutatedClass, mutatedMethod, methodDescription,
      lineNumber, mutator, mutatorSuffix, index, killingTest);
  }
}
