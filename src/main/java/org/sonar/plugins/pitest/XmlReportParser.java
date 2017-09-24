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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.ExtensionPoint;
import org.sonar.api.batch.ScannerSide;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;

@ScannerSide
@ExtensionPoint
public class XmlReportParser {

	private static final Logger LOG = LoggerFactory.getLogger(XmlReportParser.class);

	public Collection<Mutant> parse(File report) {
    return new Parser().parse(report);
	}

  private static class Parser {

    private XMLStreamReader stream;
    private final Collection<Mutant> mutants = new ArrayList<>();

    private boolean detected;
    private MutantStatus mutantStatus;
    private String mutatedClass;
    private String sourceFile;
    private int lineNumber;

    public Collection<Mutant> parse(File file) {

      XMLInputFactory xmlFactory = XMLInputFactory.newInstance();

      try (InputStream is = new FileInputStream(file);
           InputStreamReader reader = new InputStreamReader(is, Charsets.UTF_8)) {
        stream = xmlFactory.createXMLStreamReader(reader);

        while (stream.hasNext()) {
          if (stream.next() == XMLStreamConstants.START_ELEMENT) {
            parseStartElement();
          }
        }
      } catch (IOException | XMLStreamException e) {
        throw Throwables.propagate(e);
      } finally {
        closeXmlStream();
      }

      return mutants;
    }

    private void parseStartElement()  {
      String tagName = stream.getLocalName();

      if ("mutation".equals(tagName)) {
        handleMutationTag();
      } else  if ("mutatedClass".equals(tagName)) {
        handleMutatedClassTag();
      } else  if ("lineNumber".equals(tagName)) {
        handleLineNumber();
      }else  if ("sourceFile".equals(tagName)) {
        handleSourceFile();
      } else  if ("mutator".equals(tagName)) {
        handleMutator();
      } else {
        LOG.debug("Ignoring tag {}", tagName);
      }
    }

    private void handleMutationTag() {
      detected = Boolean.parseBoolean(getAttribute("detected"));
      mutantStatus = MutantStatus.parse(getAttribute("status"));
    }

    private void handleMutatedClassTag() {
      try {
        mutatedClass = stream.getElementText();
      } catch (XMLStreamException e) {
        throw new RuntimeException(e);
      }
    }

    private void handleSourceFile() {
      try {
         sourceFile = stream.getElementText();
      } catch (XMLStreamException e) {
        throw new RuntimeException(e);
      }
    }

    private void handleLineNumber() {
      try {
        lineNumber = Integer.parseInt(stream.getElementText().trim());
      } catch (XMLStreamException e) {
        throw new RuntimeException(e);
      }
    }

    private void handleMutator() {
      String mutator;
      try {
        mutator = stream.getElementText();
      } catch (XMLStreamException e) {
        throw new RuntimeException(e);
      }
      mutants.add(new Mutant(detected, mutantStatus, mutatedClass, lineNumber, mutator, sourceFile));
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
