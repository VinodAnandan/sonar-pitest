/*
 * Sonar Pitest Plugin
 * Copyright (C) 2009-2017 Vinod Anandan
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
package org.sonar.plugins.pitest;

import java.util.StringTokenizer;

/**
 * Mutation information from the pitest report.
 *
 * @author Jaime Porras
 */
public class Mutant {

  public final boolean detected;
  public final MutantStatus mutantStatus;
  public final String className;
  public final String sourceFile;
  public final int lineNumber;
  public final Mutator mutator;

  public Mutant(boolean detected, MutantStatus mutantStatus, String className, int lineNumber, String mutatorKey, String sourceFile) {
    this.detected = detected;
    this.mutantStatus = mutantStatus;
    this.className = className;
    this.sourceFile = sourceFile;
    this.lineNumber = lineNumber;
    this.mutator = Mutator.parse(mutatorKey);
  }


  public String sourceRelativePath() {
      if(sourceFile!=null) {
          return sourceFile;
      }else{
          // the old version of pitest mutate only .java file and don't have a sourceFile
          final StringTokenizer tok = new StringTokenizer(className, "$");
          final String classNameFiltered = tok.nextToken();
          return classNameFiltered.replace('.', '/') + ".java";
      }
  }

  public String violationDescription() {
    return mutator.getDescription() + " without breaking the tests";
  }

  @Override
  public String toString() {
    return "{ \"d\" : " + detected + ", \"s\" : \"" + mutantStatus + "\", \"c\" : \"" + className + "\", \"mname\" : \"" + mutator.getName() + "\", \"mdesc\" : \"" + mutator.getDescription() + "\", \"sourceFile\" : \"" + sourceFile + "\"  }";
  }

}
