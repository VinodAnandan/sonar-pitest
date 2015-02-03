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
package org.sonar.plugins.pitest.model;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Representation of a PIT Mutator. The mutators are defined in a classpath located file name
 * <code>mutator-def.xml</code> in the same package. To get an instance of a defined mutator, use the find() method. As
 * key for finding a mutator, the ID, classname or a name that contains the classname as prefix can be used. The mutator
 * itself contains a violation description as well a description of the mutator itself that is copied from the
 * documentation at <a href="http://pitest.org/quickstart/mutators">pitest.org/quickstart/mutators</a>
 *
 * @author gerald@moskito.li
 *
 */
public final class Mutator {

    /**
     * SLF4J Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(Mutator.class);
    /**
     * XPath used for finding mutators and their attributes in the mutator-def.xml file
     */
    private static final XPath XP = XPathFactory.newInstance().newXPath();
    /**
     * Default Mutator definition for an unknown {@link Mutator}
     */
    private static final Mutator UNKNOWN = new Mutator("UNKNOWN", "Unknown mutator", null,
            "An unknown mutator has been applied", null);
    /**
     * URL of the mutator definitions.
     */
    private static final URL MUTATOR_DEF = Mutator.class.getResource("mutator-def.xml");
    /**
     * Contains all instances of {@link Mutator}s defined in the mutator-def.xml
     */
    private static final Set<Mutator> INSTANCES = Collections.newSetFromMap(new ConcurrentHashMap<Mutator, Boolean>());
    /**
     * Fast access map that maps the keys used to find Mutator instances to the according instances.
     */
    private static final Map<String, Mutator> CACHE = new WeakHashMap<>();

    private final String id;
    private final URL mutatorDescriptionLocation;
    private final String violationDescription;
    private final String name;
    private final String className;

    Mutator(final String id, final String name, final String className, final String violationDescription,
            final URL mutatorDescriptionLocation) {

        super();
        this.id = id;
        this.name = name;
        this.className = className;
        this.violationDescription = violationDescription;
        this.mutatorDescriptionLocation = mutatorDescriptionLocation;
    }

    /**
     * Finds the {@link Mutator} using the specified key. The key could be the ID of the Mutator, it's classname or an
     * extended classname, which is the classname with a suffix.
     *
     * @param mutatorKey
     *            the key to use for searching for the mutator
     * @return a matchin {@link Mutator} or an UNKNOWN mutator
     */
    public static Mutator find(final String mutatorKey) {

        Mutator result;
        if (CACHE.containsKey(mutatorKey)) {
            result = CACHE.get(mutatorKey);
        } else {
            result = findMutatorInstance(mutatorKey);
            CACHE.put(mutatorKey, result);
        }
        return result;
    }

    /**
     * Searches the INSTANCES set for a mutator matching the key. the match is determined by
     * <ol>
     * <li>ID</li>
     * <li>ClassName</li>
     * <li>ClassName with Suffix</li>
     * </ol>
     *
     * @param mutatorKey
     * @return the mutator for the key or the UNKNOWN mutator, if no matching mutator was found
     */
    private static Mutator findMutatorInstance(final String mutatorKey) {

        if (INSTANCES.isEmpty()) {
            initializeMutatorDefs();
        }
        Mutator result = UNKNOWN;
        for (final Mutator mutator : INSTANCES) {
            if (mutatorKey.equals(mutator.getId())
                    || mutatorKey.equals(mutator.getClassName())
                    || mutatorKey.startsWith(mutator.getClassName())) {
                result = mutator;
                break;
            }
        }
        return result;
    }

    /**
     * Initializes the INSTANCES set with mutators from the mutator definitions.
     */
    private static void initializeMutatorDefs() {

        try {

            final NodeList mutatorNodes = (NodeList) XP.evaluate("//mutator",
                    new InputSource(MUTATOR_DEF.openStream()), XPathConstants.NODESET);
            for (int i = 0, len = mutatorNodes.getLength(); i < len; i++) {
                final Node mutatorNode = mutatorNodes.item(i);
                INSTANCES.add(toMutator(mutatorNode));
            }

        } catch (IOException | XPathExpressionException e) {
            LOG.error("Could not load mutator definitions", e);
        }

    }

    /**
     * Converts a Mutator from the given {@link Node}
     *
     * @param mutatorNode
     *            the node to convert to {@link Mutator}
     * @return a {@link Mutator} for the {@link Node}
     * @throws XPathExpressionException
     */
    private static Mutator toMutator(final Node mutatorNode) throws XPathExpressionException {

        final String id = XP.evaluate("@id", mutatorNode);
        final String className = XP.evaluate("@class", mutatorNode);
        final String name = XP.evaluate("name", mutatorNode);
        final String violationDescription = XP.evaluate("violationDescription", mutatorNode);
        final URL mutatorDescriptionLocation = Mutator.class.getResource(XP.evaluate("mutatorDescription/@classpath",
                mutatorNode));

        return new Mutator(id, name, className, violationDescription, mutatorDescriptionLocation);
    }

    public String getId() {

        return id;
    }

    public URL getMutatorDescriptionLocation() {

        return mutatorDescriptionLocation;
    }

    /**
     * @return A Stream to the content of the mutator description
     * @throws IOException
     */
    public InputStream getMutatorDescriptionAsStream() throws IOException {

        return mutatorDescriptionLocation.openStream();
    }

    public String getViolationDescription() {

        return violationDescription;
    }

    public String getName() {

        return name;
    }

    public String getClassName() {

        return className;
    }

    public String getMutatorDescription() {

        try {
            return IOUtils.toString(mutatorDescriptionLocation);
        } catch (final IOException e) {
            LOG.warn("Cannot read mutator description for mutator {}", id, e);
            return "No description";
        }
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + (className == null
                ? 0
                : className.hashCode());
        result = prime * result + (id == null
                ? 0
                : id.hashCode());
        result = prime * result + (name == null
                ? 0
                : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Mutator other = (Mutator) obj;
        if (className == null) {
            if (other.className != null) {
                return false;
            }
        } else if (!className.equals(other.className)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

}