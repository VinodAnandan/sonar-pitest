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
package org.sonar.plugins.pitest;

import java.util.Map;

import com.google.common.collect.Maps;


public class Mutant {
  
  private final static Map<String, String> descriptions = Maps.newHashMap();
  
  static {
    descriptions.put(
        "org.pitest.mutationtest.engine.gregor.mutators.ConditionalsBoundaryMutator", 
        "Conditionals Boundary Mutator\nA relational operator has been replaced by a boundary counterpart"
    );
    descriptions.put(
        "org.pitest.mutationtest.engine.gregor.mutators.NegateConditionalsMutator", 
        "Negate Conditionals Mutator\nA conditional expression has been negated"
    );
    descriptions.put(
        "org.pitest.mutationtest.engine.gregor.mutators.MathMutator", 
        "Math Mutator\nA binary arithmetic operation has been replaced by another one"
    );
    descriptions.put(
        "org.pitest.mutationtest.engine.gregor.mutators.IncrementsMutator", 
        "Increments Mutator\nA local variable increment/decrement has been replaced"
    );
    descriptions.put(
        "org.pitest.mutationtest.engine.gregor.mutators.InvertNegsMutator", 
        "Invert Negatives Mutator\nA number has been replaced by its opposite"
    );
    descriptions.put(
        "org.pitest.mutationtest.engine.gregor.mutators.InlineConstantMutator", 
        "Inline Constant Mutator\nAn inline constant has been changed"
    );
    descriptions.put(
        "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator", 
        "Return Values Mutator\nThe return value of a method call has been replaced"
    );
    descriptions.put(
        "org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutator", 
        "Void Method Calls Mutator\nA method call has been removed"
    );
    descriptions.put(
        "org.pitest.mutationtest.engine.gregor.mutators.NonVoidMethodCallMutator", 
        "Non Void Method Calls Mutator\nA method call has been removed"
    );
    descriptions.put(
        "org.pitest.mutationtest.engine.gregor.mutators.ConstructorCallMutator", 
        "Constructor Calls Mutator\nA constructor call has been removed"
    );
    descriptions.put(
        "org.pitest.mutationtest.engine.gregor.mutators.experimental.InlineConstantMutator", 
        "Experimental Inline Constant Mutator\nAn inline constant has been changed"
    );
    descriptions.put(
        "org.pitest.mutationtest.engine.gregor.mutators.experimental.MemberVariableMutator", 
        "Experimental Member Variable Mutator\nA member variable assignment has been replaced"
    );
  }

  private final String className;
  
  private final int lineNumber;
  
  private final String mutator;

  public Mutant(String className, int lineNumber, String mutator) {
    this.className = className;
    this.lineNumber = lineNumber;
    this.mutator = mutator;
  }
  
  public String getSonarJavaFileKey() {
    if (className.indexOf('$') > -1) {
      return className.substring(0, className.indexOf('$'));
    }
    return className;
  }
  
  public String getClassName() {
    return className;
  }

  
  public int getLineNumber() {
    return lineNumber;
  }

  
  public String getMutatorDescription() {
    return descriptions.get(mutator);
  }
  
}
