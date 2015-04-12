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
 * Pojo representing a Mutant. The structure maps to the PIT output:
 * <p/>
 * <pre>
 *  &lt;mutation detected='true' status='KILLED'&gt;
 *      &lt;sourceFile&gt;ResourceInjection.java&lt;/sourceFile&gt;
 *      &lt;mutatedClass&gt;io.inkstand.scribble.inject.ResourceInjection$ResourceLiteral&lt;/mutatedClass&gt;
 *      &lt;mutatedMethod&gt;authenticationType&lt;/mutatedMethod&gt;
 *      &lt;methodDescription&gt;()Ljavax/annotation/Resource$AuthenticationType;&lt;/methodDescription&gt;
 *      &lt;lineNumber&gt;164&lt;/lineNumber&gt;
 *      &lt;mutator&gt;org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator&lt;/mutator&gt;
 *      &lt;index&gt;5&lt;/index&gt;
 *      &lt;killingTest&gt;io.inkstand.scribble.inject.ResourceInjectionTest.testByMappedName_match(io.inkstand.scribble.inject.ResourceInjectionTest)&lt;/killingTest&gt;
 * &lt;/mutation&gt;
 * </pre>
 *
 * @author <a href="mailto:gerald.muecke@gmail.com">Gerald Muecke</a>
 */
public class Mutant {

  private final boolean detected;
  private final MutantStatus mutantStatus;
  private final String sourceFile;
  private final String mutatedClass;
  private final String mutatedMethod;
  private final String methodDescription;
  private final int lineNumber;
  private final Mutator mutator;
  private final int index;
  private final String killingTest;
  private final String mutatorSuffix;

  /**
   * Creates a new Mutant pojo. The constructor is not intended to be invoked directly, though it's possible. The
   * create a Mutant, the {@link MutantBuilder} should be used.
   *
   * @param detected
   *            flag to indicate if the mutant was detected by a test or not
   * @param mutantStatus
   *            the {@link MutantStatus} of the mutant. Only killed mutants are good mutants.
   * @param sourceFile
   *            the path to the sourceFile that contains the mutant. The sourceFile is relative to the project path.
   * @param mutatedClass
   *            the fully qualified class name containing the mutant
   * @param mutatedMethod
   *            the name of the method containing the mutant
   * @param methodDescription
   *            the description of the method that specifies its signature. see {@link http
   *            ://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.3.3}
   * @param lineNumber
   *            the line number where the mutant was found
   * @param mutator
   *            the mutator that was used to create the mutant
   * @param mutatorSuffix
   *            the suffix for the mutator. Some mutators like the RemoveConditionalMutator have variants that are
   *            indicated by a suffix. If no suffix was specified this parameter has to be passes as empty string.
   *            <code>null</code> is not allowed
   * @param index
   *            the index of the mutator. It has no relevance to the sonar results
   * @param killingTest
   *            the fully qualified name of the test including the test method that killed the test. If the mutant was
   *            not killed, this has to be an empty string, <code>null</code> is not allowed.
   */
  public Mutant(final boolean detected, final MutantStatus mutantStatus, final String sourceFile,
    final String mutatedClass, final String mutatedMethod, final String methodDescription, final int lineNumber,
    final Mutator mutator, final String mutatorSuffix, final int index, final String killingTest) { // NOSONAR

    super();
    checkNotNull(mutantStatus, sourceFile, mutatedClass, mutatedMethod, methodDescription, mutator, mutatorSuffix,
      killingTest);
    this.detected = detected;
    this.mutantStatus = mutantStatus;
    this.sourceFile = sourceFile;
    this.mutatedClass = mutatedClass;
    this.mutatedMethod = mutatedMethod;
    this.methodDescription = methodDescription;
    this.lineNumber = lineNumber;
    this.mutator = mutator;
    this.mutatorSuffix = mutatorSuffix;
    this.index = index;
    this.killingTest = killingTest;
  }

  private void checkNotNull(final Object... objs) {

    for (final Object o : objs) {
      if (o == null) {
        throw new IllegalArgumentException("one or more of the arguments are null");
      }
    }

  }

  /**
   *
   * @return flag to indicate if the mutant was detected by a test or not
   *
   */
  public boolean isDetected() {

    return detected;
  }

  /**
   *
   * @return the {@link MutantStatus} of the mutant. Only killed mutants are good mutants.
   */
  public MutantStatus getMutantStatus() {

    return mutantStatus;
  }

  /**
   *
   * @return the path to the sourceFile that contains the mutant. The sourceFile is relative to the project path.
   */
  public String getSourceFile() {

    return sourceFile;
  }

  /**
   *
   * @return the fully qualified class name containing the mutant
   */
  public String getMutatedClass() {

    return mutatedClass;
  }

  /**
   *
   * @return the name of the method containing the mutant
   */
  public String getMutatedMethod() {

    return mutatedMethod;
  }

  /**
   *
   * @return the description of the method that specifies its signature. see {@link http
   *         ://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.3.3}
   */
  public String getMethodDescription() {

    return methodDescription;
  }

  /**
   *
   * @return the line number where the mutant was found
   */
  public int getLineNumber() {

    return lineNumber;
  }

  /**
   *
   * @return the mutator that was used to create the mutant
   */
  public Mutator getMutator() {

    return mutator;
  }

  /**
   *
   * @return the suffix for the mutator. Some mutators like the RemoveConditionalMutator have variants that are
   *         indicated by a suffix. If no suffix was specified this parameter has to be passes as empty string.
   *         <code>null</code> is not allowed
   */
  public String getMutatorSuffix() {

    return mutatorSuffix;
  }

  /**
   *
   * @return the index of the mutator. It has no relevance to the sonar results
   */
  public int getIndex() {

    return index;
  }

  /**
   *
   * @return the fully qualified name of the test including the test method that killed the test. If the mutant was
   *         not killed, this has to be an empty string, <code>null</code> is not allowed.
   */
  public String getKillingTest() {

    return killingTest;
  }

  /**
   * As the source file in the mutant reports is without a package path, the method determines the path to the source
   * file from the fully qualified name of the mutated class.
   *
   * @return returns the full path to the source file including the name of file itself. The path is relative to the
   *         source folder.
   */
  public String getPathToSourceFile() {

    final int packageSeparatorPos = mutatedClass.lastIndexOf('.');
    final String packagePath = mutatedClass.substring(0, packageSeparatorPos).replaceAll("\\.", "/");

    return new StringBuilder(packagePath).append('/').append(sourceFile).toString();
  }

  @Override
  public int hashCode() {

    // @formatter:off
    return calculateHashCode(1,
      index,
      detected ? 1231 : 1237,
      lineNumber,
      methodDescription.hashCode(),
      mutantStatus.hashCode(),
      mutatedClass.hashCode(),
      mutatedMethod.hashCode(),
      mutator.hashCode(),
      mutatorSuffix.hashCode(),
      sourceFile.hashCode(),
      killingTest == null
        ? 0
        : killingTest.hashCode());
    // @formatter:on
  }

  private int calculateHashCode(final int initial, final int... values) {

    final int prime = 31;
    int result = initial;
    for (final int value : values) {
      result = prime * result + value;
    }
    return result;
  }

  @Override
  public boolean equals(final Object obj) {

    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    return equalsMutant((Mutant) obj);
  }

  private boolean equalsMutant(final Mutant other) { // NOSONAR

    if (detected != other.detected) {
      return false;
    }
    if (index != other.index) {
      return false;
    }
    if (lineNumber != other.lineNumber) {
      return false;
    }
    if (!methodDescription.equals(other.methodDescription)) {
      return false;
    }
    if (mutantStatus != other.mutantStatus) {
      return false;
    }
    if (!mutatedClass.equals(other.mutatedClass)) {
      return false;
    }
    if (!mutatedMethod.equals(other.mutatedMethod)) {
      return false;
    }
    if (!mutator.equals(other.mutator)) {
      return false;
    }
    if (!mutatorSuffix.equals(other.mutatorSuffix)) {
      return false;
    }
    if (!sourceFile.equals(other.sourceFile)) {
      return false;
    }
    if (!killingTest.equals(other.killingTest)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {

    return "Mutant [sourceFile="
      + sourceFile
      + ", mutatedClass="
      + mutatedClass
      + ", mutatedMethod="
      + mutatedMethod
      + ", methodDescription="
      + methodDescription
      + ", lineNumber="
      + lineNumber
      + ", mutantStatus="
      + mutantStatus
      + ", mutator="
      + mutator
      + ", killingTest="
      + killingTest
      + "]";
  }

}
