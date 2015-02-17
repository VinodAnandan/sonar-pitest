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
 * Pojo representing a Mutant. The structure mapps to the PIT output:
 *
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
 * Mutation information from the pitest report.
 *
 * @author gerald.muecke@gmail.com
 *
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

    public Mutant(final boolean detected, final MutantStatus mutantStatus, final String sourceFile,
            final String mutatedClass, final String mutatedMethod, final String methodDescription,
            final int lineNumber, final Mutator mutator, final String mutatorSuffix, final int index,
            final String killingTest) { // NOSONAR

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

    public boolean isDetected() {

        return detected;
    }

    public MutantStatus getMutantStatus() {

        return mutantStatus;
    }

    public String getSourceFile() {

        return sourceFile;
    }

    public String getMutatedClass() {

        return mutatedClass;
    }

    public String getMutatedMethod() {

        return mutatedMethod;
    }

    public String getMethodDescription() {

        return methodDescription;
    }

    public int getLineNumber() {

        return lineNumber;
    }

    public Mutator getMutator() {

        return mutator;
    }

    public String getMutatorSuffix() {

        return mutatorSuffix;
    }

    public int getIndex() {

        return index;
    }

    public String getKillingTest() {

        return killingTest;
    }

    @Override
    public int hashCode() {

        //@formatter:off
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
                        : killingTest.hashCode()
                );
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

    private boolean equalsMutant(final Mutant other) {

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

}
