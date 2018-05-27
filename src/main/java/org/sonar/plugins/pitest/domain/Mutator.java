/*
 * Sonar Pitest Plugin
 * Copyright (C) 2009-2018 Vinod Anandan
 * vinod@owasp.org
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
package org.sonar.plugins.pitest.domain;

/**
 * https://github.com/hcoles/pitest/blob/master/pitest/src/main/java/org/pitest/mutationtest/engine/gregor/config/Mutator.java
   https://github.com/hcoles/pitest/tree/master/pitest/src/main/java/org/pitest/mutationtest/engine/gregor/mutators *
 * @author bwflood
 *
 */
public enum Mutator {

  INVERT_NEGS(
    "org.pitest.mutationtest.engine.gregor.mutators.InvertNegsMutator", "Invert Negatives Mutator",
    "A number has been replaced by its opposite"),
  RETURN_VALS(
    "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator", "Return Values Mutator",
    "The return value of a method call has been replaced"),
  INLINE_CONSTS("org.pitest.mutationtest.engine.gregor.mutators.InlineConstantMutator",
    "Inline Constant Mutator", "An inline constant has been changed"),
  MATH("org.pitest.mutationtest.engine.gregor.mutators.MathMutator", "Math Mutator",
    "A binary arithmetic operation has been replaced by another one"),
  VOID_METHOD_CALLS(
    "org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutator", "Void Method Calls Mutator",
    "A method call has been removed"),
  NEGATE_CONDITIONALS(
    "org.pitest.mutationtest.engine.gregor.mutators.NegateConditionalsMutator", "Negate Conditionals Mutator",
    "A conditional expression has been negated"),
  CONDITIONALS_BOUNDARY("org.pitest.mutationtest.engine.gregor.mutators.ConditionalsBoundaryMutator", "Conditionals Boundary Mutator",
    "A relational operator has been replaced by a boundary counterpart"),
  INCREMENTS(
    "org.pitest.mutationtest.engine.gregor.mutators.IncrementsMutator", "Increments Mutator",
    "A local variable increment/decrement has been replaced"),
  // Note: the Key indicates it's NOT experimental, but the class resides in experimental package
  REMOVE_INCREMENTS("org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveIncrementsMutator", "Remove Increments Mutator",
    "An increment operation was removed"),
  NON_VOID_METHOD_CALLS("org.pitest.mutationtest.engine.gregor.mutators.NonVoidMethodCallMutator",
    "Non Void Method Calls Mutator", "A method call has been removed"),
  CONSTRUCTOR_CALLS(
    "org.pitest.mutationtest.engine.gregor.mutators.ConstructorCallMutator", "Constructor Calls Mutator",
    "A constructor call has been removed"),
  
  REMOVE_CONDITIONALS_EQ_IF(MutatorConstants.REMOVE_COND_MUTATOR_KEY, MutatorConstants.REMOVE_COND_MUTATOR_NAME, "A conditional statement has been removed - EQ IF"),
  REMOVE_CONDITIONALS_EQ_ELSE(MutatorConstants.REMOVE_COND_MUTATOR_KEY, MutatorConstants.REMOVE_COND_MUTATOR_NAME,
    "A conditional statement has been removed - EQ ELSE"),
  REMOVE_CONDITIONALS_ORD_IF(MutatorConstants.REMOVE_COND_MUTATOR_KEY, MutatorConstants.REMOVE_COND_MUTATOR_NAME,
    "A conditional statement has been removed - EQ ORD IF"),
  REMOVE_CONDITIONALS_ORD_ELSE(MutatorConstants.REMOVE_COND_MUTATOR_KEY, MutatorConstants.REMOVE_COND_MUTATOR_NAME,
    "A conditional statement has been removed - EQ ORD ELSE"),

  TRUE_RETURNS(
    "org.pitest.mutationtest.engine.gregor.mutators.BooleanTrueReturnValsMutator", "Boolean True ReturnVals Mutator",
    "Replaced Boolean return with True"),
  FALSE_RETURNS(
    "org.pitest.mutationtest.engine.gregor.mutators.BooleanFalseReturnValsMutator", "Boolean False ReturnVals Mutator",
    "Replaced Boolean return with False"),
  PRIMITIVE_RETURNS(
    "org.pitest.mutationtest.engine.gregor.mutators.PrimitiveReturnsMutator", "Primitive Returns Mutator",
    "Replaced primitive return value with 0"),
  EMPTY_RETURNS(
    "org.pitest.mutationtest.engine.gregor.mutators.EmptyObjectReturnValsMutator", "Empty Object Returns Mutator",
    "Replaced return value with empty object"),
  NULL_RETURNS(
    "org.pitest.mutationtest.engine.gregor.mutators.NullReturnValsMutator", "Null Returns Mutator",
    "Replaced return value with null"),

  EXPERIMENTAL_MEMBER_VARIABLE(
    "org.pitest.mutationtest.engine.gregor.mutators.experimental.MemberVariableMutator", "Experimental Member Variable Mutator",
    "A member variable assignment has been replaced"),
  EXPERIMENTAL_SWITCH("org.pitest.mutationtest.engine.gregor.mutators.experimental.SwitchMutator", "Experimental Switch Mutator", "A switch label has been swapped with another"),
  // Note: the Key indicates it's experimental, but the class resides in mutators package
  EXPERIMENTAL_ARGUMENT_PROPAGATION("org.pitest.mutationtest.engine.gregor.mutators.ArgumentPropagationMutator", "Experimental Argument Propagation Mutator",
    "A method return value was replaced with a method parameter"),
  EXPERIMENTAL_NAKED_RECEIVER("org.pitest.mutationtest.engine.gregor.mutators.experimental.NakedReceiverMutator", "Experimental Naked ReceiverMutator",
    "A method return value was replaced with receiver"),

  // Note: the Key indicates it's NOT experimental, but the class resides in experimental package
  REMOVE_SWITCH("org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator", "Experimental Remove Switch Mutator", "A switch statement was removed"),

  UNKNOWN("", "Unknown mutator", "An unknown mutator has been applied");

  private String key;
  private String name;
  private String description;

  Mutator(String key, String name, String description) {
    this.key = key;
    this.name = name;
    this.description = description;
  }

  String getKey() {
    return key;
  }

  String getName() {
    return name;
  }

  String getDescription() {
    return description;
  }

  static Mutator parse(String mutatorKey) {

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
