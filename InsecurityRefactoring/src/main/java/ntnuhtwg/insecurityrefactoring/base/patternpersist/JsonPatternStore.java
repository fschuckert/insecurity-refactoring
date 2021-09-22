/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.patternpersist;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import ntnuhtwg.insecurityrefactoring.base.JSONUtil;
import ntnuhtwg.insecurityrefactoring.base.Util;
import ntnuhtwg.insecurityrefactoring.base.context.EscapeChar;
import ntnuhtwg.insecurityrefactoring.base.exception.NotExpected;
import ntnuhtwg.insecurityrefactoring.base.patterns.GenerateFile;
import ntnuhtwg.insecurityrefactoring.base.patterns.Pattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.DataflowPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SinkPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.GenerateParameters;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author blubbomat
 */
public class JsonPatternStore {

    public static void storePattern(Pattern pattern) throws IOException {
        if (pattern.isForGenerate()) {
            throw new NotExpected("Cannot store a generate pattern!" + pattern.getName());
        }

        JSONObject json = jsonToStore(pattern);

        if (pattern instanceof DataflowPattern) {
            addDataflowAttributes((DataflowPattern) pattern, json);
        } else if (pattern instanceof SinkPattern) {
            addSinkAttributes((SinkPattern) pattern, json);
        } else {
            throw new NotExpected("The pattern is currently not supported for editing in GUI " + pattern.getType());
        }

        String path = pattern.getPatternFileLocation();
        FileWriter fileWriter = new FileWriter(path);
        fileWriter.write(json.toString());
        fileWriter.flush();
        fileWriter.close();
        System.out.println("Written to : " + path);
    }

    private static JSONObject jsonToStore(Pattern pattern) {
        JSONObject json = new JSONObject();
        json.put("name", pattern.getName());
        json.put("type", pattern.getType());
        json.put("init", JSONUtil.toJSONArray(pattern.getInitCodeLines()));

        json.put("pattern_type", pattern.getPatternType().toString());

        if (pattern.getInputType() != null) {
            json.put("input_type", pattern.getInputType().toString());
        }
        if (pattern.getOutputType() != null) {
            json.put("output_type", pattern.getOutputType().toString());
        }

        json.put("output_return", pattern.isReturnOutput());

        if(pattern.getDataInputType() != null){
        json.put("data_input", pattern.getDataInputType().toString());
        }
        if(pattern.getDataOutputType() != null){
            json.put("data_output", pattern.getDataOutputType().toString());
        }
        json.put("code", JSONUtil.toJSONArray(pattern.getCodeLines()));
        

        json.put("generate_files", generate_files(pattern));

        json.put("generates", generates(pattern));

        return json;
    }

    private static JSONArray generates(Pattern pattern) {
        JSONArray retval = new JSONArray();

        for (GenerateParameters genParams : pattern.getGeneratesParams()) {
            JSONObject json = new JSONObject();
            json.put("params", genParams.getParameters());
            for (Entry<String, Object> toReplace : genParams.getToReplace().entrySet()) {
                json.put(toReplace.getKey(), toReplace.getValue());
            }
            retval.add(json);
        }

        return retval;
    }

    private static void addDataflowAttributes(DataflowPattern dataflowPattern, JSONObject json) {
        json.put("passthrough", dataflowPattern.isPassthrough());
        json.put("identify_pattern", dataflowPattern.getIdentifyPattern());
        json.put("requirements", requirements(dataflowPattern));
    }

    private static void addSinkAttributes(SinkPattern sinkPattern, JSONObject json) {
        json.put("sufficient_escapes", sufficient_escapes(sinkPattern.getSufficientEscapeChars()));
        json.put("vuln", sinkPattern.getVulnType());
    }

    private static JSONArray sufficient_escapes(Set<EscapeChar> sufficientEscapes) {
        JSONArray retval = new JSONArray();
        for (EscapeChar escapeChar : sufficientEscapes) {
            retval.add(escapeChar.toString());
        }
        return retval;
    }

    private static JSONArray generate_files(Pattern pattern) {
        JSONArray generate_files = new JSONArray();
        for (GenerateFile genFile : pattern.getGenerateFiles()) {
            JSONObject genFileJson = new JSONObject();
            genFileJson.put(genFile.getPath(), JSONUtil.toJSONArray(genFile.getFileContent().split("\n")));
            generate_files.add(genFileJson);
        }
        return generate_files;
    }

    private static JSONArray requirements(DataflowPattern dataflowPattern) {
        JSONArray retval = new JSONArray();

        return retval;
    }
}
