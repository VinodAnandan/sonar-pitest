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
package org.sonar.plugins.pitest.scanner;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.sonar.api.ExtensionPoint;
import org.sonar.api.batch.ScannerSide;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.pitest.domain.Mutant;
import org.sonar.plugins.pitest.domain.MutantLocation;
import org.sonar.plugins.pitest.domain.MutantStatus;

@ScannerSide
@ExtensionPoint
public class XmlReportParser {

  private static final Logger LOG = Loggers.get(XmlReportParser.class);

  public Collection<Mutant> parse(File report) {
    return new Parser().parse(report);
  }

  private class Parser {

    private XMLStreamReader stream;
    private final Collection<Mutant> mutants = new ArrayList<>();

    private boolean detected;
    private MutantStatus mutantStatus;
    private String sourceFile;
    private String mutatedClass;
    private String mutatedMethod;
    private String methodDescription;
    private int lineNumber;
    private String mutator;
    private int index;
    private String description;
    private String killingTest;

    private void reset() {
      detected = false;
      mutantStatus = null;
      sourceFile = null;
      mutatedClass = null;
      mutatedMethod = null;
      methodDescription = null;
      lineNumber = 0;
      mutator = null;
      index = 0;
      description = null;
      killingTest = null;
    }

    public Collection<Mutant> parse(File file) {

      XMLInputFactory xmlFactory = XMLInputFactory.newInstance();

      try (InputStream is = new FileInputStream(file);
        InputStreamReader reader = new InputStreamReader(is, Charsets.UTF_8)) {
        stream = xmlFactory.createXMLStreamReader(reader);

        while (stream.hasNext()) {
          int next = stream.next();
          if (next == XMLStreamConstants.START_ELEMENT) {
            parseStartElement();
          } else if (next == XMLStreamConstants.END_ELEMENT) {
            processEndElement();
          }
        }
      } catch (IOException | XMLStreamException | IllegalArgumentException e) {
        throw new IllegalStateException("XML is not valid", e);
      } finally {
        closeXmlStream();
      }

      return mutants;
    }

    private void parseStartElement() {
      String tagName = stream.getLocalName();

      if ("mutation".equals(tagName)) {
        reset();
        handleMutationTag();
      } else if ("sourceFile".equals(tagName)) {
        handleSourceFileTag();
      } else if ("mutatedClass".equals(tagName)) {
        handleMutatedClassTag();
      } else if ("mutatedMethod".equals(tagName)) {
        handleMutatedMethod();
      } else if ("methodDescription".equals(tagName)) {
        handleMethodDescription();
      } else if ("lineNumber".equals(tagName)) {
        handleLineNumber();
      } else if ("mutator".equals(tagName)) {
        handleMutator();
      } else if ("index".equals(tagName)) {
        handleIndex();
      } else if ("killingTest".equals(tagName)) {
        handleKillingTest();
      } else if ("description".equals(tagName)) {
        handleDescription();
      } else {
        if (LOG.isDebugEnabled()) {
          // all are processed now, so this is a new element added by pitest
          LOG.debug("Ignoring tag {}", tagName);
        }
      }
    }

    private void handleMutationTag() {
      detected = Boolean.parseBoolean(getAttribute("detected"));
      mutantStatus = MutantStatus.fromPitestDetectionStatus(getAttribute("status"));
    }

    private void handleSourceFileTag() {
      try {
        sourceFile = stream.getElementText();
      } catch (Exception e) {
        logException(e.getClass().getSimpleName(), "processing tag sourceFile");
      }
    }

    private void handleMutatedClassTag() {
      try {
        mutatedClass = stream.getElementText();
      } catch (Exception e) {
        logException(e.getClass().getSimpleName(), "processing tag MutatedClass");
      }
    }

    private void handleMutatedMethod() {
      try {
        mutatedMethod = stream.getElementText();
      } catch (Exception e) {
        logException(e.getClass().getSimpleName(), "processing tag mutatedMethod");
      }
    }

    private void handleMethodDescription() {
      try {
        methodDescription = stream.getElementText();
      } catch (Exception e) {
        logException(e.getClass().getSimpleName(), "processing tag methodDescription");
      }
    }

    private void handleLineNumber() {
      try {
        lineNumber = Integer.parseInt(stream.getElementText().trim());
      } catch (Exception e) {
        logException(e.getClass().getSimpleName(), "processing tag lineNumber");
      }
    }

    private void handleMutator() {
      try {
        mutator = stream.getElementText();
      } catch (Exception e) {
        logException(e.getClass().getSimpleName(), "processing tag mutator");
      }
    }

    private void handleIndex() {
      try {
        index = Integer.parseInt(stream.getElementText().trim());
      } catch (Exception e) {
        logException(e.getClass().getSimpleName(), "processing tag index");
      }
    }

    private void handleKillingTest() {
      try {
        killingTest = stream.getElementText();
      } catch (Exception e) {
        logException(e.getClass().getSimpleName(), "processing tag killingTest");
      }
    }

    private void handleDescription() {
      try {
        description = stream.getElementText();
      } catch (Exception e) {
        logException(e.getClass().getSimpleName(), "processing tag description");
      }
    }

    private void processEndElement() {
      String tagName = stream.getLocalName();
      if ("mutation".equals(tagName)) {
        MutantLocation location = new MutantLocation(mutatedClass, sourceFile, mutatedMethod, methodDescription, lineNumber);
        mutants.add(new Mutant(detected, mutantStatus, location, mutator, index, description, killingTest));
      }
    }

    private void logException(String exceptionName, String activity) {
      LOG.warn("caught {} {}.. ignoring ", exceptionName, activity);
    }

    private void closeXmlStream() {
      if (stream != null) {
        try {
          stream.close();
        } catch (XMLStreamException e) {
          throw Throwables.propagate(e);
        }
      }
    }

    private String getAttribute(String name) {
      for (int i = 0; i < stream.getAttributeCount(); i++) {
        if (name.equals(stream.getAttributeLocalName(i))) {
          return stream.getAttributeValue(i);
        }
      }

      return null;
    }

  }
}
