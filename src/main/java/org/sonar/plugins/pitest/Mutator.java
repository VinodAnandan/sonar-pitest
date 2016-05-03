/*
 * Sonar Pitest Plugin
 * Copyright (C) 2009-2016 SonarQubeCommunity
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.pitest;

enum Mutator {
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

  Mutator(String key, String name, String description) {
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

  static Mutator parse(String mutatorKey) {
    if (mutatorKey == null) {
      return unknown();
    }
    for (Mutator mutantStatus : Mutator.values()) {
      if (mutantStatus.getKey().equals(mutatorKey)) {
        return mutantStatus;
      }
    }
    return unknown();
  }

  private static Mutator unknown() {
    return UNKNOWN;
  }
}
