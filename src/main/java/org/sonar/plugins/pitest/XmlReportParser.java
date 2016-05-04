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

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.BatchExtension;
import org.sonar.api.utils.MessageException;

/**
 * Pitest report file parser to obtain the mutants collection.
 * 
 * @author Jaime Porras L&oacute;pez
 */
public class XmlReportParser implements BatchExtension {

	private static final Logger LOG = LoggerFactory.getLogger(XmlReportParser.class);
	private static final String ATTR_DETECTED = "detected";
	private static final String ATTR_STATUS = "status";
	private static final String ATTR_CLASS = "mutatedClass";
	private static final String ATTR_LINE = "lineNumber";
	private static final String ATTR_MUTATOR = "mutator";
	
	public Collection<Mutant> parse(File report) {
		boolean detected;
		MutantStatus mutantStatus;
		String localPart, statusName, mutatedClass, mutator;
		int lineNumber;
		SMInputCursor mutationDetailsCursor ;
		final List<Mutant> mutants = new ArrayList<>();
		final SMInputFactory inf = new SMInputFactory(XMLInputFactory.newInstance());
		try {
			SMHierarchicCursor rootCursor = inf.rootElementCursor(report);
			rootCursor.advance();
			SMInputCursor mutationCursor = rootCursor.childElementCursor();
			while (mutationCursor.getNext() != null) {
				detected = Boolean.parseBoolean(mutationCursor.getAttrValue(ATTR_DETECTED));
				statusName = mutationCursor.getAttrValue(ATTR_STATUS);
				mutantStatus = MutantStatus.parse(statusName);
				if (mutantStatus.equals(MutantStatus.UNKNOWN)) {
					LOG.warn("Unknown mutation status detected: {}", statusName);
				}
				mutatedClass = null;
				mutator = null;
				lineNumber = 0;
				mutationDetailsCursor = mutationCursor.childElementCursor();
				while (mutationDetailsCursor.getNext() != null) {
					localPart = mutationDetailsCursor.getQName().getLocalPart();
					if (ATTR_CLASS.equals(localPart)) {
						mutatedClass = mutationDetailsCursor.collectDescendantText().trim();
					}
					else if (ATTR_LINE.equals(localPart)) {
						lineNumber = Integer.parseInt(mutationDetailsCursor.collectDescendantText().trim());
					}
					else if (ATTR_MUTATOR.equals(localPart)) {
						mutator = mutationDetailsCursor.collectDescendantText().trim();
					}
				}
				mutants.add(new Mutant(detected, mutantStatus, mutatedClass, lineNumber, mutator));
			}
		}
		catch (XMLStreamException e) {
			LOG.error("Error parsing XML report " + report, e);
			throw MessageException.of("Error parsing XML report " + report);
		}
		return mutants;
	}
}
