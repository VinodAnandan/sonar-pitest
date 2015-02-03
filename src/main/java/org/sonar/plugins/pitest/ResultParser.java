/*
 * Sonar Pitest Plugin
 * Copyright (C) 2015 SonarCommunity
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

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.BatchExtension;
import org.sonar.plugins.pitest.model.Mutant;
import org.sonar.plugins.pitest.model.MutantBuilder;
import org.sonar.plugins.pitest.model.MutantHelper;

/**
 * Parser for PIT reports to read the mutants in the file into a {@link Collection} of {@link Mutant}s. The format of
 * the PIT reports is like
 *
 * <pre>
 * &lt;mutations&gt;
 *   &lt;mutation detected='true' status='KILLED'&gt;
 *     &lt;sourceFile&gt;ResourceInjection.java&lt;/sourceFile&gt;
 *     &lt;mutatedClass&gt;io.inkstand.scribble.inject.ResourceInjection$ResourceLiteral&lt;/mutatedClass&gt;
 *     &lt;mutatedMethod&gt;authenticationType&lt;/mutatedMethod&gt;
 *     &lt;methodDescription&gt;()Ljavax/annotation/Resource$AuthenticationType;&lt;/methodDescription&gt;
 *     &lt;lineNumber&gt;164&lt;/lineNumber&gt;
 *     &lt;mutator&gt;org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator&lt;/mutator&gt;
 *     &lt;index&gt;5&lt;/index&gt;
 *     &lt;killingTest&gt;io.inkstand.scribble.inject.ResourceInjectionTest.testByMappedName_match(io.inkstand.scribble.inject.ResourceInjectionTest)&lt;/killingTest&gt;
 *   &lt;/mutation&gt;
 *   ...
 * &lt;/mutantions&gt;
 * </pre>
 *
 * @author <a href="mailto:gerald.muecke@gmail.com">Gerald Muecke</a>
 *
 */
public class ResultParser implements BatchExtension {

    /**
     * SLF4J Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(ResultParser.class);

    private static final String ATTR_DETECTED = "detected";

    private static final String ATTR_STATUS = "status";

    private static final String ELEMENT_KILLING_TEST = "killingTest";

    private static final String ELEMENT_INDEX = "index";

    private static final String ELEMENT_MUTATOR = "mutator";

    private static final String ELEMENT_LINE_NUMBER = "lineNumber";

    private static final String ELEMENT_METHOD_DESCRIPTION = "methodDescription";

    private static final String ELEMENT_MUTATED_METHOD = "mutatedMethod";

    private static final String ELEMENT_MUTATED_CLASS = "mutatedClass";

    private static final String ELEMENT_SOURCE_FILE = "sourceFile";

    private static final String ELEMENT_MUTATION = "mutation";

    private static final String NAMESPACE_URI = null;

    /**
     * Parses the contents of the report file into a list of {@link Mutant}s. The report file must be a PIT report.
     *
     * @param report
     *            the {@link Path} to the PIT report file to be parsed
     * @return a {@link Collection} of {@link Mutant}s
     * @throws IOException
     *             if the report file could not be read
     */
    public Collection<Mutant> parseMutants(final Path report) throws IOException {

        Collection<Mutant> result;

        try (InputStream stream = Files.newInputStream(report)) {
            final XMLInputFactory inf = XMLInputFactory.newInstance();
            final XMLStreamReader reader = inf.createXMLStreamReader(stream);
            result = readMutants(reader);
        } catch (FileNotFoundException | XMLStreamException e) {
            LOG.error("Parsing report failed", e);
            result = Collections.emptyList();
        }
        return result;
    }

    /**
     * Reads the Mutants from the XML Stream. The method reads the stream for occurrences of &lt;mutation&gt; elements
     * and then parses the element's contents into a {@link Mutant} instance.
     *
     * @param reader
     *            the XMLStream to read
     * @return a {@link Collection} of {@link Mutant}s found on the stream
     * @throws XMLStreamException
     */
    private Collection<Mutant> readMutants(final XMLStreamReader reader) throws XMLStreamException {

        final Collection<Mutant> result = new ArrayList<>();
        int event;
        while (reader.hasNext()) {
            event = reader.next();
            switch (event) {
            case START_ELEMENT:
                if (ELEMENT_MUTATION.equals(reader.getLocalName())) {
                    final Mutant mutant = parseMutant(reader);
                    LOG.debug("Found mutant {}", mutant);
                    result.add(mutant);
                }
                break;
            }
        }

        return result;
    }

    /**
     * The method assumes, the reader is at the start element position of a <code>&lt;mutation&gt;</code> element.
     *
     * @param reader
     * @return
     * @throws XMLStreamException
     */
    private Mutant parseMutant(final XMLStreamReader reader) throws XMLStreamException {

        final MutantBuilder builder = MutantHelper.newMutant().detected(isMutantDetected(reader))
                .mutantStatus(getMutantStatus(reader));
        while (reader.hasNext()) {
            final int event = reader.next();
            if (event == START_ELEMENT) {
                buildMutant(reader, builder);
            } else if (event == END_ELEMENT && ELEMENT_MUTATION.equals(reader.getLocalName())) {
                break;
            }
        }

        return builder.build();
    }

    /**
     * Builds the {@link Mutant} by calling the builder methods of the builder on occurrence of the according mutant
     * elements
     *
     * @param reader
     *            the reader to read the elements from the XML stream
     * @param builder
     *            the builder for the current {@link Mutant} whose builder methods are invoked
     * @throws XMLStreamException
     */
    private void buildMutant(final XMLStreamReader reader, final MutantBuilder builder) throws XMLStreamException {

        switch (reader.getLocalName()) {
        case ELEMENT_SOURCE_FILE:
            builder.inSourceFile(reader.getElementText());
            break;
        case ELEMENT_MUTATED_CLASS:
            builder.inClass(reader.getElementText());
            break;
        case ELEMENT_MUTATED_METHOD:
            builder.inMethod(reader.getElementText());
            break;
        case ELEMENT_METHOD_DESCRIPTION:
            builder.withMethodParameters(reader.getElementText());
            break;
        case ELEMENT_LINE_NUMBER:
            builder.inLine(Integer.parseInt(reader.getElementText()));
            break;
        case ELEMENT_MUTATOR:
            builder.usingMutator(reader.getElementText());
            break;
        case ELEMENT_INDEX:
            builder.atIndex(Integer.parseInt(reader.getElementText()));
            break;
        case ELEMENT_KILLING_TEST:
            if (!reader.isStandalone()) {
                builder.killedBy(reader.getElementText());
            }
            break;
        }
    }

    /**
     * Reads the status of {@link Mutant} from the XMLStream.
     *
     * @param reader
     *            the {@link XMLStreamReader} whose cursor is at the start element position of a &lt;mutation&gt;
     *            element
     * @return the mutant status as a string
     */
    private String getMutantStatus(final XMLStreamReader reader) {

        return reader.getAttributeValue(NAMESPACE_URI, ATTR_STATUS);
    }

    /**
     * Checks if the mutant was detected or not
     *
     * @param reader
     *            the {@link XMLStreamReader} whose cursor is at the start element position of a &lt;mutation&gt;
     *            element
     * @return <code>true</code> if the mutant was detected
     */
    private boolean isMutantDetected(final XMLStreamReader reader) {

        return Boolean.parseBoolean(reader.getAttributeValue(NAMESPACE_URI, ATTR_DETECTED));
    }
}
