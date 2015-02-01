package org.sonar.plugins.pitest.model;

/**
 * A builder for creating a new mutant.
 *
 * @author gerald@moskito.li
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
    private int index;
    private String killingTest;

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
        return this;
    }

    public MutantBuilder usingMutator(final String mutatorName) {

        mutator = Mutator.find(mutatorName);
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
                lineNumber, mutator, index, killingTest);
    }
}
