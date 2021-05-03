/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.patternparser;

import java.io.FileReader;
import java.util.Collections;
import java.util.HashMap;
import ntnuhtwg.insecurityrefactoring.base.patterns.PatternStorage;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import ntnuhtwg.insecurityrefactoring.base.DataType;
import ntnuhtwg.insecurityrefactoring.base.patternparser.PatternParser;
import ntnuhtwg.insecurityrefactoring.base.ASTType;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.DataflowIdentifyPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.DataflowPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.LanguagePattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.Pattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.ConcatPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.ContextPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.FailedSanitizePattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.InsecureSourcePattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SanitizePattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SinkPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SourcePattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.sufficient.Sufficient;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.sufficient.GenerateParameters;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.sufficient.VulnSufficient;
import ntnuhtwg.insecurityrefactoring.base.context.Requirement;
import ntnuhtwg.insecurityrefactoring.base.context.Context;
import ntnuhtwg.insecurityrefactoring.base.context.RequirementList;
import org.apache.commons.lang3.StringUtils;
import org.javatuples.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import scala.NotImplementedError;
import java.io.File;
import ntnuhtwg.insecurityrefactoring.base.context.PossibleRequirements;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author blubbomat
 */
public class JsonPatternParser {

    public List<Pattern> parseJsonPattern(File jsonFile, PatternStorage patternStorage) throws Exception {
        List<Pattern> retval = new LinkedList<>();
        
        FileReader fileReader = new FileReader(jsonFile);
        JSONObject json = (JSONObject) new JSONParser().parse(fileReader);
        fileReader.close();

        
        String patternType = (String) json.get("type");

        Pattern defaultPattern = parseSpecific(patternType, json);
        parseBasic(defaultPattern, json, patternStorage);
        retval.add(defaultPattern);

        for (GenerateParameters params : this.parseGenerate(json)) {
            fileReader = new FileReader(jsonFile);
            JSONObject jsonCopy =(JSONObject) new JSONParser().parse(fileReader);
            replaceGenerateValues(jsonCopy, params);
            Pattern generatePattern = parseSpecific(patternType, jsonCopy);
            parseBasic(generatePattern, jsonCopy, patternStorage);
            retval.add(generatePattern);
            fileReader.close();
        }

        return retval;
    }
    

    private void parseBasic(Pattern pattern, JSONObject json, PatternStorage patternStorage) {
        pattern.setType((String) json.get("type"));
        pattern.setVulnOnly((String) json.get(("vuln")));
        if (pattern.getName() == null) {
            pattern.setName((String) json.get("name"));
        }
        pattern.setPatternStorage(patternStorage);

        boolean outputReturn = getObjectSave(json, "output_return", Boolean.class, false);
        pattern.setReturnOutput(outputReturn);

        String inputStr = getObjectSave(json, "input_type", String.class);
        if (inputStr != null) {
            pattern.setInputType(ASTType.valueOf(inputStr));
        }

        String outputStr = getObjectSave(json, "output_type", String.class);
        if (outputStr != null) {
            pattern.setOutputType(ASTType.valueOf(outputStr));
        }

        String patternTypeStr = getObjectSave(json, "pattern_type", String.class);
        if (patternTypeStr != null) {
            pattern.setPatternType(ASTType.valueOf(patternTypeStr));
        }

        if (!containsCode(pattern)) {
            parseCode(json, pattern);
        }

        checkContainsAny(pattern);

        pattern.setInitCodeLines(getListSave(json, "init", String.class));
    }

    private void checkContainsAny(Pattern pattern) {
        boolean containsAny = false;

        if (pattern.getCodeLines() != null) {
            for (String codeLines : pattern.getCodeLines()) {
                if (codeLines.contains("<any>")) {
                    containsAny = true;
                    break;
                }
            }
        }

        pattern.setContainsAny(containsAny);
    }

    private void parseCode(JSONObject json, Pattern pattern) {
        JSONArray codeStatements = getObjectSave(json, "code", JSONArray.class);
        if (codeStatements != null) {
            LinkedList<String> codes = new LinkedList();
            for (Object code : codeStatements) {
                String codeStr = (String) code;
                codes.add(codeStr);
            }

            pattern.setCodeLines(codes);
        }
    }

    private <E> List<E> getListSave(JSONObject json, String listName, Class<E> clazz) {
        List<E> retval = new LinkedList<>();

        if (json.containsKey(listName)) {
            for (Object obj : getObjectSave(json, listName, JSONArray.class)) {
                if (clazz.isInstance(obj)) {
                    retval.add(clazz.cast(obj));
                }
            }
        }

        return retval;
    }

    private <E> E getObjectSave(JSONObject json, String attribute, Class<E> clazz) {
        return getObjectSave(json, attribute, clazz, null);
    }

    private <E> E getObjectSave(JSONObject json, String attribute, Class<E> clazz, E defaultValue) {
        if (json.containsKey(attribute)) {
            Object obj = json.get(attribute);
            if (clazz.isInstance(obj)) {
                return clazz.cast(obj);
            }
        }

        return defaultValue;
    }

    private RequirementList parseRequirements(JSONArray requirements) {
        RequirementList retval = new RequirementList();
        for (Object req : requirements) {
            String reqStr = (String) req;
            reqStr = reqStr.trim();

            Boolean trueOrFalse = true;

            if (reqStr.startsWith("!")) {
                trueOrFalse = false;
                reqStr = reqStr.replaceFirst("!", "");
            }

            Context requirement = Context.knownRequirements.get(reqStr.toLowerCase().trim());
            if (requirement == null) {
                throw new NotImplementedError("A requirement is not implemented. Maybe not known in the Requirements class? " + reqStr);
            } else {
                retval.addRequirement(new Requirement(requirement, trueOrFalse));
            }
        }

        return retval;
    }

    private Pattern parseSpecific(String patternType, JSONObject json) {
        switch (patternType) {

            case "language": {
                // "<call>(%name[] , %params[]...)"
                String patternLanguage = (String) json.get("pattern_language");
                String patternId = patternLanguage.split("<")[1].split(">")[0]; // call                
                String params = patternLanguage.split("\\(")[1];
                if (params.trim().startsWith(")")) {
                    params = "";
                } else {
                    String split[] = params.split("\\)");
                    params = split[0]; // %name[] , %params[]...
                }
                String[] paramsSplit = params.split(",");
                JSONObject astPhp = (JSONObject) json.get("ast_php");
                LanguagePattern languagePattern = new LanguagePattern(patternId, paramsSplit, astPhp);

                return languagePattern;
            }

            case "dataflow": {
                boolean passthrough = getObjectSave(json, "passthrough", Boolean.class, false);
                DataType outputDataType = PatternParser.parseDataType((String) json.get("data_output"));
                DataType inputDataType = PatternParser.parseDataType((String) json.get("data_input"));

                Double diffMan = (Double) json.get("diff_man");
                Double diffSCA = (Double) json.get("diff_sca");
                Double diffDyn = (Double) json.get("diff_dyn");

                String identifyPattern = getObjectSave(json, "identify_pattern", String.class);

                if (!identifyPattern.isEmpty()) {
                    DataflowPattern dataflow = new DataflowPattern(passthrough, inputDataType, outputDataType, identifyPattern, diffMan, diffSCA, diffDyn);

                    return dataflow;
                }
                return null;
            }

            case "concatenation": {
                DataType outputDataType = PatternParser.parseDataType((String) json.get("data_output"));
                DataType inputDataType = PatternParser.parseDataType((String) json.get("data_input"));
                String inputAmount = (String) json.get("input_amount");

                try {
                    Integer n = Integer.valueOf(inputAmount);
                } catch (NumberFormatException ex) {
                    if (!"...".equals(inputAmount)) {
                        // Skip this pattern it is invalid!
                        throw new NotImplementedError("Pattern error for input amount: " + json);
                    }
                }

                ConcatPattern concatPattern = new ConcatPattern(inputDataType, outputDataType, inputAmount);
                return concatPattern;
            }

            case "sink": {
                SinkPattern sink = parseSink(json);

                return sink;

//                List<GenerateParameters> generateParams = parseGenerate(json);
//                for(GenerateParameters generate : generateParams){
//                    SinkPattern sinkGenerate = parseSink(json);
//                    setSubname(json, sinkGenerate, generate);
//                    parseCode(json, sinkGenerate);
//                    replaceAnyWithParameters(sinkGenerate, generate);
//                    
////                    json.
//                    
////                }
            }

            case "source": {
                DataType outputDataType = PatternParser.parseDataType((String) json.get("data_output"));
                Boolean secure = getObjectSave(json, "secure", Boolean.class, false);

                SourcePattern source = new SourcePattern(outputDataType);
                source.setSecure(secure);

                return source;
            }

            case "sanitize": {
                SanitizePattern sanitizePattern = parseSanitize(json, false);
                return sanitizePattern;
            }

            case "failed_sanitize": {
                SanitizePattern failedSanitizePattern = parseSanitize(json, true);

                return failedSanitizePattern;
            }

            case "source_insecure": {
                DataType outputDataType = PatternParser.parseDataType((String) json.get("data_output"));
                InsecureSourcePattern source = new InsecureSourcePattern(outputDataType);
                source.setSecure(true);

                return source;
            }

            case "dataflow_identify": {
                boolean passthrough = getObjectSave(json, "passthrough", Boolean.class, false);
                DataType outputDataType = PatternParser.parseDataType((String) json.get("data_output"));
                DataType inputDataType = PatternParser.parseDataType((String) json.get("data_input"));

                DataflowIdentifyPattern dataflowIdentify = new DataflowIdentifyPattern(passthrough, inputDataType, outputDataType);

                return dataflowIdentify;
            }

            case "context": {
                DataType inputDataType = PatternParser.parseDataType((String) json.get("data_input"));
                DataType outputDataType = PatternParser.parseDataType((String) json.get("data_output"));

                String vuln = getObjectSave(json, "vuln", String.class, "");
                String pre = getObjectSave(json, "pre", String.class, "");
                String post = getObjectSave(json, "post", String.class, "");

                return new ContextPattern(vuln, pre, post, inputDataType, outputDataType);
            }

        }
        return null;
    }

    private SinkPattern parseSink(JSONObject json) {
        SinkPattern sink = new SinkPattern();
        String vulnType = getObjectSave(json, "vuln", String.class);
        sink.setVulnType(vulnType);
        return sink;
    }

    private void setSubname(JSONObject json, GenerateParameters sufficientParamaters) {
        StringBuffer name = new StringBuffer((String) json.get("name"));

        for (String param : sufficientParamaters.getParameters()) {
            name.append("_");
            name.append(param.replace("/", "_"));
        }

        json.put("name", name.toString());
    }

    private void replaceAnyWithParameters(JSONObject json, GenerateParameters sufficientParamaters) {
        JSONArray code = getObjectSave(json, "code", JSONArray.class);

//        {
//        int parI = -1;
//        for(String toReplace : sufficientParamaters.getParameters()){
//            parI ++;
//            int codeI = 0;
//            for(String codeLine : getListSave(json, "code", String.class)){
//                
//            }
//        }
//        }
//        int replaceIndex = 0;
        String replaced = "";

        int parI = -1;
        for (String toReplace : sufficientParamaters.getParameters()) {
            parI++;
            int codeI = 0;
            for (String codeLine : getListSave(json, "code", String.class)) {
                List<String> remainingParams = sufficientParamaters.getParameters().subList(parI, sufficientParamaters.getParameters().size());
                String remainParamsStr = String.join(", ", remainingParams);
                replaced = codeLine.replaceFirst("<any>\\(\\s*\\)\\?*\\.+", Matcher.quoteReplacement(remainParamsStr));
                code.set(codeI, replaced);
                if (replaced != codeLine) {
                    break;
                }
                replaced = codeLine.replaceFirst("<any>\\(\\s*\\)[\\?\\.]*", Matcher.quoteReplacement(toReplace));
                while (replaced != codeLine) {
                    code.set(codeI, replaced);
                    break;
                }
                codeI++;
            }

        }
    }

    public static void main(String[] args) {
        // Just testing regex
        String input = "<call>(json_encode, %input, <any>()?...)";

        System.out.println("Matches: " + input.matches("<any>\\(\\s*\\)\\?*\\.+"));
        System.out.println("replace: " + input.replaceFirst("<any>\\(\\s*\\)\\?*\\.+", "lala"));
    }

    private SanitizePattern parseSanitize(JSONObject pattern, boolean failed) {
        boolean passthrough = getObjectSave(pattern, "passthrough", Boolean.class, false);
        DataType outputDataType = PatternParser.parseDataType((String) pattern.get("data_output"));
        DataType inputDataType = PatternParser.parseDataType((String) pattern.get("data_input"));

        List<VulnSufficient> sufficients = parseSanitizitionVuln(pattern);

        if(failed){
            return new FailedSanitizePattern(passthrough, inputDataType, outputDataType, sufficients);
        }
        
        SanitizePattern sanitizePattern = new SanitizePattern(passthrough, inputDataType, outputDataType, sufficients);

        return sanitizePattern;
    }

    private List<GenerateParameters> parseGenerate(JSONObject pattern) {
        LinkedList<GenerateParameters> retval = new LinkedList<>();
        for (JSONObject sufficientJson : getListSave(pattern, "generates", JSONObject.class)) {
            GenerateParameters generateParam = new GenerateParameters(getListSave(sufficientJson, "params", String.class));

            sufficientJson.forEach((keyStr, valueJson) -> {
                String key = (String) keyStr;
                if (!"params".equals(key)) {
                    generateParam.addToReplace(key, valueJson);
                }
            });

            retval.add(generateParam);
        }

        return retval;
    }

    private List<VulnSufficient> parseSanitizitionVuln(JSONObject sufficientJson) {
        List<VulnSufficient> sufficients = new LinkedList<>();
        for (JSONObject requirement : getListSave(sufficientJson, "sufficient", JSONObject.class)) {
            VulnSufficient sufficient = new VulnSufficient(getObjectSave(requirement, "type", String.class));

            sufficient.setRequirements(parseAllPossibleRequirements((JSONArray) requirement.get("requirements")));

            sufficients.add(sufficient);
        }
        return sufficients;
    }

    private boolean containsCode(Pattern pattern) {
        return pattern.getCodeLines() != null;
    }

    private void replaceGenerateValues(JSONObject json, GenerateParameters params) {
        setSubname(json, params);
        replaceAnyWithParameters(json, params);

        replaceOtherValues(json, params);
    }

    private void replaceOtherValues(JSONObject json, GenerateParameters params) {
        for (Entry<String, Object> toReplace : params.getToReplace().entrySet()) {
            json.put(toReplace.getKey(), toReplace.getValue());
        }
    }

    private PossibleRequirements parseAllPossibleRequirements(JSONArray objectSave) {
        PossibleRequirements retval = new PossibleRequirements();
        for(Object requirements : objectSave){
            JSONArray requirementsArray = (JSONArray)requirements;
            retval.add(parseRequirements(requirementsArray));
        }
        
        return retval;
    }

}
