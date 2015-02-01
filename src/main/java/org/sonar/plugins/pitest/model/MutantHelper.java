package org.sonar.plugins.pitest.model;

import java.io.StringWriter;
import java.util.List;

import javax.json.Json;
import javax.json.stream.JsonGenerator;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class MutantHelper {

    public static String toJson(final List<Mutant> mutants) {

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

    public static MutantBuilder newMutant() {

        return new MutantBuilder();
    }

}
