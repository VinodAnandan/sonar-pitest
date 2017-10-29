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
package org.sonar.plugins.pitest.domain;

import java.util.StringTokenizer;

public final class MutantLocation {

  public final String className;
  public final String sourceFile;
  public final String mutatedMethod;
  public final String methodDescription;
  public final int lineNumber;
  public final String relativePath;

  public MutantLocation(String className, String sourceFile, String mutatedMethod, String methodDescription, int lineNumber) {
    this.className = className;
    this.sourceFile = sourceFile;
    this.mutatedMethod = mutatedMethod;
    this.methodDescription = methodDescription;
    this.lineNumber = lineNumber;
    String extension = sourceFile.substring(sourceFile.indexOf('.') + 1);
    if ("kt".equals(extension)) {
      this.relativePath = sourceFile;
    } else if ("java".equals(extension)) {
      this.relativePath = calculateJavaRelativePath(className);
    } else {
      throw new IllegalStateException("unrecognized extension: " + extension);
    }

  }

  public String getClassName() {
    return className;
  }

  public String getSourceFile() {
    return sourceFile;
  }

  public String getMutatedMethod() {
    return mutatedMethod;
  }

  public String getMethodDescription() {
    return methodDescription;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public String getRelativePath() {
    return relativePath;
  }

  private static String calculateJavaRelativePath(String javaClassName) {
    final StringTokenizer tok = new StringTokenizer(javaClassName, "$");
    final String classNameFiltered = tok.nextToken();
    return classNameFiltered.replace('.', '/') + ".java";
  }
}
