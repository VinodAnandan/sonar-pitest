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
package org.sonar.plugins.pitest.viewer.client;

import java.text.MessageFormat;

/**
 * Mutation information from the pitest report.
 *
 * @version Added metrics info by <a href="mailto:aquiporras@gmail.com">Jaime Porras L&oacute;pez</a>. Also, it has been moved to the gwt
 *          client package to avoid code duplications.
 */
public class Mutant {

  private final boolean detected;

  private final MutantStatus mutantStatus;

  private final String className;

  private final int lineNumber;

  private final Mutator mutator;

  private transient String sonarJavaFileKey;

  public Mutant(boolean detected, MutantStatus mutantStatus, String className, int lineNumber, String mutatorKey) {
    this(detected, mutantStatus, className, lineNumber, Mutator.parse(mutatorKey));
  }

  private Mutant(boolean detected, MutantStatus mutantStatus, String className, int lineNumber, Mutator mutator) {
    this.detected = detected;
    this.mutantStatus = mutantStatus;
    this.className = className;
    this.lineNumber = lineNumber;
    this.mutator = mutator;
  }

  /**
   * @return the detected
   */
  public boolean isDetected() {
    return detected;
  }

  /**
   * @return the mutantStatus
   */
  public MutantStatus getMutantStatus() {
    return mutantStatus;
  }

  public String getSonarJavaFileKey() {
    if (sonarJavaFileKey == null) {
      if (className.indexOf('$') > -1) {
        sonarJavaFileKey = className.substring(0, className.indexOf('$'));
      } else {
        sonarJavaFileKey = className;
      }
    }
    return sonarJavaFileKey;
  }

  public String getClassName() {
    return className;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public Mutator getMutator() {
    return mutator;
  }

  public String getViolationDescription() {
    return mutator.getDescription() + " without breaking the tests";
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((className == null) ? 0 : className.hashCode());
    result = prime * result + (detected ? 1231 : 1237);
    result = prime * result + lineNumber;
    result = prime * result + ((mutantStatus == null) ? 0 : mutantStatus.hashCode());
    result = prime * result + ((mutator == null) ? 0 : mutator.hashCode());
    return result;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Mutant other = (Mutant) obj;
    if (className == null) {
      if (other.className != null) {
        return false;
      }
    } else if ( !className.equals(other.className)) {
      return false;
    }
    if (detected != other.detected) {
      return false;
    }
    if (lineNumber != other.lineNumber) {
      return false;
    }
    if (mutantStatus != other.mutantStatus) {
      return false;
    }
    if (mutator == null) {
      if (other.mutator != null) {
        return false;
      }
    } else if ( !mutator.equals(other.mutator)) {
      return false;
    }
    return true;
  }

  public String toJSON() {
    return "{ \"d\" : " + detected + ", \"s\" : \"" + mutantStatus + "\", \"c\" : \"" + className + "\", \"mname\" : \"" + mutator.getName() + "\", \"mdesc\" : \"" + mutator.getDescription() + "\"  }";
  }


  public static enum MutantStatus {
    NO_COVERAGE, KILLED, SURVIVED, MEMORY_ERROR, TIMED_OUT, UNKNOWN;

    public static MutantStatus parse(String statusName) {
      if (statusName == null) {
        return unknown(statusName);
      }
      for (MutantStatus mutantStatus : MutantStatus.values()) {
        if (mutantStatus.name().equals(statusName)) {
          return mutantStatus;
        }
      }
      return unknown(statusName);
    }

    private static MutantStatus unknown(String statusName) {
      return UNKNOWN;
    }

    boolean is(MutantStatus... mutantStatuses) {
      if (mutantStatuses == null) {
        return false;
      }
      for (MutantStatus mutantStatus : mutantStatuses) {
        if (this == mutantStatus) {
          return true;
        }
      }
      return false;
    }
  }

  static enum Mutator {
    COND_BOUNDARY("org.pitest.mutationtest.engine.gregor.mutators.ConditionalsBoundaryMutator", "Conditionals Boundary Mutator",
        "A relational operator has been replaced by a boundary counterpart"), NEGATE_COND(
        "org.pitest.mutationtest.engine.gregor.mutators.NegateConditionalsMutator", "Negate Conditionals Mutator",
        "A conditional expression has been negated"), MATH("org.pitest.mutationtest.engine.gregor.mutators.MathMutator", "Math Mutator",
        "A binary arithmetic operation has been replaced by another one"), INCREMENTS(
        "org.pitest.mutationtest.engine.gregor.mutators.IncrementsMutator", "Increments Mutator",
        "A local variable increment/decrement has been replaced"), INVERT_NEGS(
        "org.pitest.mutationtest.engine.gregor.mutators.InvertNegsMutator", "Invert Negatives Mutator",
        "A number has been replaced by its opposite"), INLINE_CONS("org.pitest.mutationtest.engine.gregor.mutators.InlineConstantMutator",
        "Inline Constant Mutator", "An inline constant has been changed"), RETURN_VALS(
        "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator", "Return Values Mutator",
        "The return value of a method call has been replaced"), VOID_METHOD(
        "org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutator", "Void Method Calls Mutator",
        "A method call has been removed"), NON_VOID_METHOD("org.pitest.mutationtest.engine.gregor.mutators.NonVoidMethodCallMutator",
        "Non Void Method Calls Mutator", "A method call has been removed"), CONSTRUCTOR(
        "org.pitest.mutationtest.engine.gregor.mutators.ConstructorCallMutator", "Constructor Calls Mutator",
        "A constructor call has been removed"), EXP_INLINE_CONS(
        "org.pitest.mutationtest.engine.gregor.mutators.experimental.InlineConstantMutator", "Experimental Inline Constant Mutator",
        "An inline constant has been changed"), EXP_MEMBER_VAR(
        "org.pitest.mutationtest.engine.gregor.mutators.experimental.MemberVariableMutator", "Experimental Member Variable Mutator",
        "A member variable assignment has been replaced"),

    UNKNOWN("", "Unknown mutator", "An unknown mutator has been applied");

    private String key;
    private String name;
    private String description;

    private Mutator(String key, String name, String description) {
      this.key = key;
      this.name = name;
      this.description = description;
    }

    private String getKey() {
      return key;
    }

    String getName() {
      return name;
    }

    String getDescription() {
      return description;
    }

    private static Mutator parse(String mutatorKey) {
      if (mutatorKey == null) {
        return unknown(mutatorKey);
      }
      for (Mutator mutantStatus : Mutator.values()) {
        if (mutantStatus.getKey().equals(mutatorKey)) {
          return mutantStatus;
        }
      }
      return unknown(mutatorKey);
    }

    private static Mutator unknown(String statusName) {
      return UNKNOWN;
    }
  }
}
