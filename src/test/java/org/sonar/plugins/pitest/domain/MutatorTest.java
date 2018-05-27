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

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MutatorTest {

  @Test
  public void parse_of_invalid_key_returns_unknown() {
    // given

    // when
    Mutator mutator = Mutator.parse("invalid-key");

    // then
    assertThat(mutator).isEqualTo(Mutator.UNKNOWN);
  }

  @Test
  public void parse_of_valid_keys_succeeds() {
    List<String> validMutatorKeys = Arrays.asList(
      "org.pitest.mutationtest.engine.gregor.mutators.ArgumentPropagationMutator",
      "org.pitest.mutationtest.engine.gregor.mutators.BooleanFalseReturnValsMutator",
      "org.pitest.mutationtest.engine.gregor.mutators.BooleanTrueReturnValsMutator",
      "org.pitest.mutationtest.engine.gregor.mutators.ConditionalsBoundaryMutator",
      "org.pitest.mutationtest.engine.gregor.mutators.ConstructorCallMutator",
      "org.pitest.mutationtest.engine.gregor.mutators.EmptyObjectReturnValsMutator",
      "org.pitest.mutationtest.engine.gregor.mutators.IncrementsMutator",
      "org.pitest.mutationtest.engine.gregor.mutators.InlineConstantMutator",
      "org.pitest.mutationtest.engine.gregor.mutators.InvertNegsMutator",
      "org.pitest.mutationtest.engine.gregor.mutators.MathMutator",
      "org.pitest.mutationtest.engine.gregor.mutators.NegateConditionalsMutator",
      "org.pitest.mutationtest.engine.gregor.mutators.NonVoidMethodCallMutator",
      "org.pitest.mutationtest.engine.gregor.mutators.NullReturnValsMutator",
      "org.pitest.mutationtest.engine.gregor.mutators.PrimitiveReturnsMutator",
      "org.pitest.mutationtest.engine.gregor.mutators.RemoveConditionalMutator",
      "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator",
      "org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutator",

      "org.pitest.mutationtest.engine.gregor.mutators.experimental.MemberVariableMutator",
      "org.pitest.mutationtest.engine.gregor.mutators.experimental.NakedReceiverMutator",
      "org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveIncrementsMutator",
      "org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator",
      "org.pitest.mutationtest.engine.gregor.mutators.experimental.SwitchMutator");

    
    for (String key : validMutatorKeys) {
      assertThat(Mutator.parse(key)).isNotEqualTo(Mutator.UNKNOWN);
    }

  }
}
