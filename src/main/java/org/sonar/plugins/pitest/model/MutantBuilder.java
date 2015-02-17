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
 * A builder for creating a new mutant.
 *
 * @author gerald.muecke@gmail.com
 *
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
     * Indicate the mutant was detected
     *
     * @param detected
     *
     * @return
     */
    public MutantBuilder detected(final boolean detected) {

        this.detected = detected;
        return this;
    }

    public MutantBuilder mutantStatus(final MutantStatus mutantStatus) {

        this.mutantStatus = mutantStatus;
        return this;
    }

    public MutantBuilder mutantStatus(final String statusName) {

        mutantStatus = MutantStatus.parse(statusName);
        return this;

    }

    public MutantBuilder inSourceFile(final String sourceFile) {

        this.sourceFile = sourceFile;
        return this;
    }

    public MutantBuilder inClass(final String mutatedClass) {

        this.mutatedClass = mutatedClass;
        return this;
    }

    public MutantBuilder inMethod(final String mutatedMethod) {

        this.mutatedMethod = mutatedMethod;
        return this;
    }

    public MutantBuilder withMethodParameters(final String methodDescription) {

        this.methodDescription = methodDescription;
        return this;
    }

    public MutantBuilder inLine(final int lineNumber) {

        this.lineNumber = lineNumber;
        return this;
    }

    public MutantBuilder usingMutator(final Mutator mutator) {

        this.mutator = mutator;
        mutatorSuffix = "";
        return this;
    }

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

    public MutantBuilder atIndex(final int index) {

        this.index = index;
        return this;
    }

    public MutantBuilder killedBy(final String killingTest) {

        this.killingTest = killingTest;
        return this;
    }

    public Mutant build() {

        return new Mutant(detected, mutantStatus, sourceFile, mutatedClass, mutatedMethod, methodDescription,
                lineNumber, mutator, mutatorSuffix, index, killingTest);
    }
}
