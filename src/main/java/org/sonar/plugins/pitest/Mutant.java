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
        "<a href=\"http://pitest.org/quickstart/mutators/#conditionals-boundary-mutator-conditionals_boundary\">Conditionals Boundary Mutator</a>"
    );
    descriptions.put(
        "org.pitest.mutationtest.engine.gregor.mutators.NegateConditionalsMutator", 
        "<a href=\"http://pitest.org/quickstart/mutators/#negate-conditionals-mutator-negate_conditionals\">Negate Conditionals Mutator</a>"
    );
    descriptions.put(
        "org.pitest.mutationtest.engine.gregor.mutators.MathMutator", 
        "<a href=\"http://pitest.org/quickstart/mutators/#math-mutator-math\">Math Mutator</a>"
    );
    descriptions.put(
        "org.pitest.mutationtest.engine.gregor.mutators.IncrementsMutator", 
        "<a href=\"http://pitest.org/quickstart/mutators/#increments-mutator-increments\">Increments Mutator</a>"
    );
    descriptions.put(
        "org.pitest.mutationtest.engine.gregor.mutators.InvertNegsMutator", 
        "<a href=\"http://pitest.org/quickstart/mutators/#invert-negatives-mutator-invert_negs\">Invert Negatives Mutator</a>"
    );
    descriptions.put(
        "org.pitest.mutationtest.engine.gregor.mutators.InlineConstantMutator", 
        "<a href=\"http://pitest.org/quickstart/mutators/#inline-constant-mutator-inline_consts\">Inline Constant Mutator</a>"
    );
    descriptions.put(
        "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator", 
        "<a href=\"http://pitest.org/quickstart/mutators/#return-values-mutator-return_vals\">Return Values Mutator</a>"
    );
    descriptions.put(
        "org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutator", 
        "<a href=\"http://pitest.org/quickstart/mutators/#void-method-call-mutator-void_method_calls\">Void Method Calls Mutator</a>"
    );
    descriptions.put(
        "org.pitest.mutationtest.engine.gregor.mutators.NonVoidMethodCallMutator", 
        "<a href=\"http://pitest.org/quickstart/mutators/#non-void-method-call-mutator-non_void_method_calls\">Non Void Method Calls Mutator</a>"
    );
    descriptions.put(
        "org.pitest.mutationtest.engine.gregor.mutators.ConstructorCallMutator", 
        "<a href=\"http://pitest.org/quickstart/mutators/#constructor-call-mutator-constructor_calls\">Constructor Calls Mutator</a>"
    );
    descriptions.put(
        "org.pitest.mutationtest.engine.gregor.mutators.experimental.InlineConstantMutator", 
        "<a href=\"http://pitest.org/quickstart/mutators/#experimental-inline-constant-mutator-experimental_inline_consts\">Experimental Inline Constant Mutator</a>"
    );
    descriptions.put(
        "org.pitest.mutationtest.engine.gregor.mutators.experimental.MemberVariableMutator", 
        "<a href=\"http://pitest.org/quickstart/mutators/#experimental-member-variable-mutator-experimental_member_variable\">Experimental Member Variable Mutator</a>"
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
