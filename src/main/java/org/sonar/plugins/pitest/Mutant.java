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


public class Mutant {

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

  
  public String getMutator() {
    return mutator;
  }
  
}
