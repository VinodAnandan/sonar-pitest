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

import java.io.File;
import java.util.Collection;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.api.BatchExtension;
import org.sonar.api.utils.SonarException;

import com.google.common.collect.Lists;


public class ResultParser implements BatchExtension {

  public Collection<Mutant> parse(File report) {
    List<Mutant> mutants = Lists.newArrayList();
    SMInputFactory inf = new SMInputFactory(XMLInputFactory.newInstance());
    try {
      SMHierarchicCursor rootCursor = inf.rootElementCursor(report);
      rootCursor.advance();
      SMInputCursor mutationCursor = rootCursor.childElementCursor();
      while (mutationCursor.getNext() != null) {
        String detectedFlag = mutationCursor.getAttrValue("detected");
        String mutantStatus = mutationCursor.getAttrValue("status");
        if ("false".equals(detectedFlag) && "SURVIVED".equals(mutantStatus)) {
          String mutatedClass = null;
          int lineNumber = 0; 
          String mutator = null;
          SMInputCursor mutationDetailsCursor = mutationCursor.childElementCursor();
          while (mutationDetailsCursor.getNext() != null) {
            if ("mutatedClass".equals(mutationDetailsCursor.getQName().getLocalPart())) {
              mutatedClass = mutationDetailsCursor.collectDescendantText().trim();
            } else if ("lineNumber".equals(mutationDetailsCursor.getQName().getLocalPart())) {
              lineNumber = Integer.parseInt(mutationDetailsCursor.collectDescendantText().trim());
            } else if ("mutator".equals(mutationDetailsCursor.getQName().getLocalPart())) {
              mutator = mutationDetailsCursor.collectDescendantText().trim();
            }
          }
          mutants.add(new Mutant(mutatedClass, lineNumber, mutator));
        }
      }
    } catch (XMLStreamException e) {
      throw new SonarException(e);
    }
    return mutants;
  }
  
}
