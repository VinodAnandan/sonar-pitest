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

import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;


/**
 * Mutation information from the pitest report.
 *
 * @author Jaime Porras
 */
public class Mutant {

  public final boolean detected;
  public final MutantStatus mutantStatus;
  public final String className;
  public final int lineNumber;
  public final Mutator mutator;

  public Mutant(boolean detected, MutantStatus mutantStatus, String className, int lineNumber, String mutatorKey) {
    this.detected = detected;
    this.mutantStatus = mutantStatus;
    this.className = className;
    this.lineNumber = lineNumber;
    this.mutator = Mutator.parse(mutatorKey);
  }


  public String sourceRelativePath() {
    Splitter splitter = Splitter.on('$');
    String classNameFiltered = splitter.split(className).iterator().next();
    return classNameFiltered.replace('.', '/') + ".java";
  }

  public String violationDescription() {
    return mutator.getDescription() + " without breaking the tests";
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(className, detected, lineNumber, mutantStatus, mutator);
  }

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

    return Objects.equal(className, other.className)
      && Objects.equal(detected, other.detected)
      && Objects.equal(lineNumber, other.lineNumber)
      && Objects.equal(mutantStatus, other.mutantStatus)
      && Objects.equal(mutator, other.mutator);
  }

  @Override
  public String toString() {
    return "{ \"d\" : " + detected + ", \"s\" : \"" + mutantStatus + "\", \"c\" : \"" + className + "\", \"mname\" : \"" + mutator.getName() + "\", \"mdesc\" : \"" + mutator.getDescription() + "\"  }";
  }

}
