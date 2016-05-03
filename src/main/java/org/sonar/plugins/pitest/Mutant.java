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

import com.google.common.base.Splitter;


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
  public String toString() {
    return "{ \"d\" : " + detected + ", \"s\" : \"" + mutantStatus + "\", \"c\" : \"" + className + "\", \"mname\" : \"" + mutator.getName() + "\", \"mdesc\" : \"" + mutator.getDescription() + "\"  }";
  }

}
