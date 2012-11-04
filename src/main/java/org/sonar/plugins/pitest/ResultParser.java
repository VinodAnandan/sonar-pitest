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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.BatchExtension;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.pitest.Mutant.MutantStatus;

import com.google.common.collect.Lists;

/**
 * Pitest report file parser to obtain the mutants collection.
 * 
 * @version Parse all mutants to obtain metrics info. By <a href="mailto:aquiporras@gmail.com">Jaime Porras L&oacute;pez</a>
 */
public class ResultParser implements BatchExtension {

	private static final Logger LOG = LoggerFactory.getLogger(ResultParser.class);
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
		List<Mutant> mutants = Lists.newArrayList();
		SMInputFactory inf = new SMInputFactory(XMLInputFactory.newInstance());
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
			throw new SonarException(e);
		}
		return mutants;
	}
}
