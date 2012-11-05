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
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;


/**
 * Mutation information from the pitest report.
 *
 * @version Added metrics info by <a href="mailto:aquiporras@gmail.com">Jaime Porras L&oacute;pez</a>. Also, it has been moved to the gwt
 *          client package to avoid code duplications.
 */
public class Mutant {

  private final boolean detected;

  private final MutantStatus mutantStatus;

  private final String className;

  private final int lineNumber;

  private final Mutator mutator;

  private transient String sonarJavaFileKey;

  public Mutant(boolean detected, MutantStatus mutantStatus, String className, int lineNumber, String mutatorKey) {
    this(detected, mutantStatus, className, lineNumber, Mutator.parse(mutatorKey));
  }

  private Mutant(boolean detected, MutantStatus mutantStatus, String className, int lineNumber, Mutator mutator) {
    this.detected = detected;
    this.mutantStatus = mutantStatus;
    this.className = className;
    this.lineNumber = lineNumber;
    this.mutator = mutator;
  }

  /**
   * @return the detected
   */
  public boolean isDetected() {
    return detected;
  }

  /**
   * @return the mutantStatus
   */
  public MutantStatus getMutantStatus() {
    return mutantStatus;
  }

  public String getSonarJavaFileKey() {
    if (sonarJavaFileKey == null) {
      if (className.indexOf('$') > -1) {
        sonarJavaFileKey = className.substring(0, className.indexOf('$'));
      } else {
        sonarJavaFileKey = className;
      }
    }
    return sonarJavaFileKey;
  }

  public String getClassName() {
    return className;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public Mutator getMutator() {
    return mutator;
  }

  public String getViolationDescription() {
    return mutator.getDescription() + " without breaking the tests";
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((className == null) ? 0 : className.hashCode());
    result = prime * result + (detected ? 1231 : 1237);
    result = prime * result + lineNumber;
    result = prime * result + ((mutantStatus == null) ? 0 : mutantStatus.hashCode());
    result = prime * result + ((mutator == null) ? 0 : mutator.hashCode());
    return result;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
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
    if (className == null) {
      if (other.className != null) {
        return false;
      }
    } else if ( !className.equals(other.className)) {
      return false;
    }
    if (detected != other.detected) {
      return false;
    }
    if (lineNumber != other.lineNumber) {
      return false;
    }
    if (mutantStatus != other.mutantStatus) {
      return false;
    }
    if (mutator == null) {
      if (other.mutator != null) {
        return false;
      }
    } else if ( !mutator.equals(other.mutator)) {
      return false;
    }
    return true;
  }

  public static String toJSON(List<Mutant> mutants) {
    Multimap<Integer, String> mutantsByLine = ArrayListMultimap.create();

    for (Mutant mutant : mutants) {
      mutantsByLine.put(mutant.getLineNumber(), mutant.toJSON());
    }

    StringBuilder builder = new StringBuilder();
    builder.append("{");
    boolean first = true;
    for (int line : mutantsByLine.keySet()) {
      if (!first) {
        builder.append(",");
      }
      first = false;
      builder.append("\"");
      builder.append(line);
      builder.append("\":[");
      builder.append(Joiner.on(",").join(mutantsByLine.get(line)));
      builder.append("]");
    }
    builder.append("}");

    return builder.toString();
  }

  private String toJSON() {
    return "{ \"d\" : " + detected + ", \"s\" : \"" + mutantStatus + "\", \"c\" : \"" + className + "\", \"mname\" : \"" + mutator.getName() + "\", \"mdesc\" : \"" + mutator.getDescription() + "\"  }";
  }
}
