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

import java.io.StringWriter;
import java.util.Collection;

import javax.json.Json;
import javax.json.stream.JsonGenerator;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public final class MutantHelper {

    private MutantHelper() {

    }

    /**
     * Creates a Json representation of the collection of mutants. The Json representation describes an Object
     * containing an array with an array element for each mutant. Each array element is an object itself, containing the
     * mutant's properties
     * <ul>
     * <li>detected</li>
     * <li>status</li>
     * <li>sourceFile</li>
     * <li>mutatedClass</li>
     * <li>mutatedMethod</li>
     * <li>mutator</li>
     * <li>violationDescription</li>
     * <li>mutatorDescription</li>
     * </ul>
     * 
     * @param mutants
     *            the mutants to be rendered as JSon
     * @return a string containing the json representation of the mutants
     */
    public static String toJson(final Collection<Mutant> mutants) {

        final Multimap<Integer, Mutant> mutantsByLine = ArrayListMultimap.create();
        for (final Mutant mutant : mutants) {
            mutantsByLine.put(mutant.getLineNumber(), mutant);
        }

        final StringWriter writer = new StringWriter();
        final JsonGenerator json = Json.createGenerator(writer);
        json.writeStartObject();
        for (final Integer lineNumber : mutantsByLine.keySet()) {
            json.writeStartArray(lineNumber.toString());
            for (final Mutant mutant : mutantsByLine.get(lineNumber)) {
                json.writeStartObject();
                json.write("detected", mutant.isDetected());
                json.write("status", mutant.getMutantStatus().name());
                json.write("sourceFile", mutant.getSourceFile());
                json.write("mutatedClass", mutant.getMutatedClass());
                json.write("mutatedMethod", mutant.getMutatedMethod());
                json.write("mutator", mutant.getMutator().getId());
                json.write("violationDescription", mutant.getMutator().getViolationDescription());
                json.write("mutatorDescription", mutant.getMutator().getMutatorDescription());
                json.writeEnd();
            }

            json.writeEnd();
        }
        json.writeEnd();

        return writer.toString();
    }

    /**
     * Creates a new build to define a mutant. As the {@link Mutant} class is designed as being immutable, the builder
     * allows sequential definition of the {@link Mutant}'s properties instead of passing all at once to the
     * constructor.
     * 
     * @return a {@link MutantBuilder} for creating a {@link Mutant}
     */
    public static MutantBuilder newMutant() {

        return new MutantBuilder();
    }

}
