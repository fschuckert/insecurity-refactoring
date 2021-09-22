/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.patternpersist;

import java.io.FileReader;
import java.util.Collections;
import java.util.HashMap;
import ntnuhtwg.insecurityrefactoring.base.patterns.PatternStorage;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import ntnuhtwg.insecurityrefactoring.base.DataType;
import ntnuhtwg.insecurityrefactoring.base.patternpersist.PatternParser;
import ntnuhtwg.insecurityrefactoring.base.ASTType;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.DataflowIdentifyPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.DataflowPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.LanguagePattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.Pattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.ConcatPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.ContextPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SanitizePattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SinkPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SourcePattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.GenerateParameters;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import scala.NotImplementedError;
import java.io.File;
import java.util.StringJoiner;
import ntnuhtwg.insecurityrefactoring.base.JSONUtil;
import ntnuhtwg.insecurityrefactoring.base.Util;
import ntnuhtwg.insecurityrefactoring.base.context.CharsAllowed;
import ntnuhtwg.insecurityrefactoring.base.context.EscapeChar;
import ntnuhtwg.insecurityrefactoring.base.context.enclosure.Apostrophe;
import ntnuhtwg.insecurityrefactoring.base.context.enclosure.Enclosure;
import ntnuhtwg.insecurityrefactoring.base.context.enclosure.Quotes;
import ntnuhtwg.insecurityrefactoring.base.exception.NotExpected;
import ntnuhtwg.insecurityrefactoring.base.exception.ParsingException;
import ntnuhtwg.insecurityrefactoring.base.patterns.GenerateFile;
import org.apache.lucene.queryparser.flexible.core.nodes.QuotedFieldQueryNode;
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
        parseBasic(jsonFile, defaultPattern, json, patternStorage);
        retval.add(defaultPattern);

        for (GenerateParameters params : this.parseGenerate(json)) {
            defaultPattern.addGenerateParam(params);
            fileReader = new FileReader(jsonFile);
            JSONObject jsonCopy = (JSONObject) new JSONParser().parse(fileReader);
            replaceGenerateValues(jsonCopy, params);
            Pattern generatePattern = parseSpecific(patternType, jsonCopy);
            parseBasic(jsonFile, generatePattern, jsonCopy, patternStorage);
            generatePattern.setForGenerate(defaultPattern.getName());
            retval.add(generatePattern);
            fileReader.close();
        }

        return retval;
    }

    private void parseBasic(File file, Pattern pattern, JSONObject json, PatternStorage patternStorage) {
        pattern.setPatternFileLocation(file.getAbsolutePath());
        pattern.setType((String) json.get("type"));
        pattern.setVulnOnly((String) json.get(("vuln")));
        if (pattern.getName() == null) {
            pattern.setName((String) json.get("name"));
        }
        pattern.setPatternStorage(patternStorage);

        boolean outputReturn = JSONUtil.getObjectSave(json, "output_return", Boolean.class, false);
        pattern.setReturnOutput(outputReturn);

        String inputStr = JSONUtil.getObjectSave(json, "input_type", String.class);
        if (inputStr != null) {
            pattern.setInputType(ASTType.valueOf(inputStr));
        }

        String outputStr = JSONUtil.getObjectSave(json, "output_type", String.class);
        if (outputStr != null) {
            pattern.setOutputType(ASTType.valueOf(outputStr));
        }

        String patternTypeStr = JSONUtil.getObjectSave(json, "pattern_type", String.class);
        if (patternTypeStr != null) {
            pattern.setPatternType(ASTType.valueOf(patternTypeStr));
        }

        if (!containsCode(pattern)) {
            parseCode(json, pattern);
        }

        parseGenerateFiles(pattern, json);

        checkContainsAny(pattern);

        pattern.setDefines(JSONUtil.getListSave(json, "defines", String.class));
        pattern.setDepends_on(JSONUtil.getListSave(json, "depends_on", String.class));

        pattern.setInitCodeLines(JSONUtil.getListSave(json, "init", String.class));
    }

    private void parseGenerateFiles(Pattern pattern, JSONObject json) {
        List<JSONObject> files = JSONUtil.getListSave(json, "generate_files", JSONObject.class);
        for (JSONObject file : files) {
            if (file.entrySet().size() != 0) {
//                throw new NotExpected("A generate file is incorrect for: " + pattern.getName());
            }

            for (Object key : file.keySet()) {
                String filepath = (String) key;
                List<String> contentList = JSONUtil.getListSave(file, filepath, String.class);
                String content = Util.joinStr(contentList, "\n");
                GenerateFile genFile = new GenerateFile(filepath, content);
                pattern.addGenerateFile(genFile);
            }
        }
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
        JSONArray codeStatements = JSONUtil.getObjectSave(json, "code", JSONArray.class);
        if (codeStatements != null) {
            LinkedList<String> codes = new LinkedList();
            for (Object code : codeStatements) {
                String codeStr = (String) code;
                codes.add(codeStr);
            }

            pattern.setCodeLines(codes);
        }
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
                boolean passthrough = JSONUtil.getObjectSave(json, "passthrough", Boolean.class, false);
                DataType outputDataType = PatternParser.parseDataType((String) json.get("data_output"));
                DataType inputDataType = PatternParser.parseDataType((String) json.get("data_input"));

                Double diffMan = (Double) json.get("diff_man");
                Double diffSCA = (Double) json.get("diff_sca");
                Double diffDyn = (Double) json.get("diff_dyn");

                String identifyPattern = JSONUtil.getObjectSave(json, "identify_pattern", String.class);

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
                Boolean secure = JSONUtil.getObjectSave(json, "secure", Boolean.class, false);

                CharsAllowed charsAllowed = parseFilters(json);

                SourcePattern source = new SourcePattern(outputDataType);
                source.setCharsAllowed(charsAllowed);
                source.setAddsEnclosure(get_adds_enclosure(json));
                
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

            case "dataflow_identify": {
                boolean passthrough = JSONUtil.getObjectSave(json, "passthrough", Boolean.class, false);
                DataType outputDataType = PatternParser.parseDataType((String) json.get("data_output"));
                DataType inputDataType = PatternParser.parseDataType((String) json.get("data_input"));

                DataflowIdentifyPattern dataflowIdentify = new DataflowIdentifyPattern(passthrough, inputDataType, outputDataType);

                return dataflowIdentify;
            }

            case "context": {
                DataType inputDataType = PatternParser.parseDataType((String) json.get("data_input"));
                DataType outputDataType = PatternParser.parseDataType((String) json.get("data_output"));

                String vuln = JSONUtil.getObjectSave(json, "vuln", String.class, "");
                String pre = JSONUtil.getObjectSave(json, "pre", String.class, "");
                String post = JSONUtil.getObjectSave(json, "post", String.class, "");

                return new ContextPattern(vuln, pre, post, inputDataType, outputDataType);
            }

        }
        return null;
    }

    private SinkPattern parseSink(JSONObject json) {
        SinkPattern sink = new SinkPattern();
        String vulnType = JSONUtil.getObjectSave(json, "vuln", String.class);
        sink.setVulnType(vulnType);

        for (String escapeChar : JSONUtil.getListSave(json, "sufficient_escapes", String.class)) {
            if (escapeChar.equals("double")) {
                sink.addSufficientEscapeChar(new EscapeChar(null));
                continue;
            }

            sink.addSufficientEscapeChar(new EscapeChar(escapeChar.charAt(0)));
        }

        sink.setIsSafe(JSONUtil.getObjectSave(json, "safe", Boolean.class, false));
        sink.setGenerateOutputCodeLines(JSONUtil.getListSave(json, "generate_output_code", String.class));
        return sink;
    }

    private void setSubname(JSONObject json, GenerateParameters sufficientParamaters) {
        StringBuffer name = new StringBuffer((String) json.get("name"));
        name.append("_prm_");

        for (String param : sufficientParamaters.getParameters()) {
            name.append("_");
            name.append(param.replace("/", "_"));
        }

        json.put("name", name.toString());
    }

    protected void replaceAnyWithParameters(JSONObject json, GenerateParameters sufficientParamaters) {
        JSONArray code = JSONUtil.getObjectSave(json, "code", JSONArray.class);

        String replaced = "";

        String dec = "###THIS_SHOULD_BE_UNIQUE###";

        String fullCode = Util.joinStr(code, dec);

        String[] splitted = fullCode.split("\\s*<\\s*any\\s*>\\s*\\(\\s*\\)\\s*");

        replaced = splitted[0];

        for (int i = 1; i < splitted.length; i++) {
            if (Util.isLastIndex(splitted, i)) {
                replaced = replaceLastParameter(splitted, i, sufficientParamaters, replaced);
                break;
            }

            replaced += sufficientParamaters.getParameter(i - 1) + splitted[i];
        }

        String[] codeLinesReplaced = replaced.split(dec);

        if (codeLinesReplaced.length != code.size()) {
            throw new ParsingException("A parsing error exists in replacing any parameters! for: " + json.toString());
        }

        for (int i = 0; i < codeLinesReplaced.length; i++) {
            code.set(i, codeLinesReplaced[i]);
        }

//        int parI = -1;
//        for (String toReplace : sufficientParamaters.getParameters()) {
//            parI++;
//            int codeI = 0;
//            for (String codeLine : getListSave(json, "code", String.class)) {
//                
//                
//                List<String> remainingParams = sufficientParamaters.getParameters().subList(parI, sufficientParamaters.getParameters().size());
//                String remainParamsStr = String.join(", ", remainingParams);
//                replaced = codeLine.replaceFirst("<any>\\(\\s*\\)\\?*\\.+", Matcher.quoteReplacement(remainParamsStr));
//                code.set(codeI, replaced);
//                if (replaced != codeLine) {
//                    break;
//                }
//                replaced = codeLine.replaceFirst("<any>\\(\\s*\\)[\\?\\.]*", Matcher.quoteReplacement(toReplace));
//                while (replaced != codeLine) {
//                    code.set(codeI, replaced);
//                    break;
//                }
//                codeI++;
//            }
//
//        }
    }

    /**
     * replaces the remaining parameters
     *
     * @param splitted
     * @param i
     * @param sufficientParamaters
     * @param replaced
     * @return
     */
    private String replaceLastParameter(String[] splitted, int i, GenerateParameters sufficientParamaters, String replaced) {
        boolean isOptional = false;
        boolean isList = false;
        // remove ?
        if (splitted[i].startsWith("?")) {
            isOptional = true;
            if (i - 1 >= sufficientParamaters.parameterSize()) {
                // optional parameter and no generate parameter -> remove the comma
                if (replaced.endsWith(",")) {
                    replaced = replaced.substring(0, replaced.length() - 1);
                }
            }
            splitted[i] = splitted[i].replaceFirst("\\?", "").trim();
        }

        // remove ...
        if (splitted[i].startsWith("...")) {
            isList = true;
            splitted[i] = splitted[i].replaceFirst("\\.\\.\\.", "").trim();
        }

        // validation checks
        if (!isOptional && sufficientParamaters.parameterSize() < i) {
            throw new ParsingException("Error in parsing the last parameter. Not a optional used, but not enough parameters provided");
        }

        if (!isList && sufficientParamaters.parameterSize() > i) {
            throw new ParsingException("Error in parsing the last parameter. Not a list used and to many parameters");
        }

        replaced += sufficientParamaters.getRemainingParams(i - 1) + splitted[i];

        return replaced;
    }

    private SanitizePattern parseSanitize(JSONObject pattern, boolean failed) {
        boolean passthrough = JSONUtil.getObjectSave(pattern, "passthrough", Boolean.class, false);
        DataType outputDataType = PatternParser.parseDataType((String) pattern.get("data_output"));
        DataType inputDataType = PatternParser.parseDataType((String) pattern.get("data_input"));

        CharsAllowed charsAllowed = parseFilters(pattern);

        
        boolean noDetection = JSONUtil.getObjectSave(pattern, "no_detection", Boolean.class, false);

        SanitizePattern sanitizePattern = new SanitizePattern(passthrough, inputDataType, outputDataType, charsAllowed, noDetection);

        sanitizePattern.setAddsEnclosing(get_adds_enclosure(pattern));
     

        return sanitizePattern;
    }

    private Enclosure get_adds_enclosure(JSONObject json) {
        String enclosingStr = JSONUtil.getObjectSave(json, "adds_enclosing", String.class);
        if(enclosingStr == null){
            return null;
        }
        
        if (enclosingStr.startsWith("apostrophe")) {
            return new Apostrophe();
        } else if (enclosingStr.startsWith("quotes")) {
            return new Quotes();
        }
        return null;
    }

    private CharsAllowed parseFilters(JSONObject pattern) {
        CharsAllowed charsAllowed = new CharsAllowed();
        if(!pattern.containsKey("filters")){
            System.out.println("WARNING: missing filters for: " + JSONUtil.getObjectSave(pattern, "name", String.class));
        }
        List<String> filters = JSONUtil.getListSave(pattern, "filters", String.class);
        
        for (String filtered : filters) {
            if (filtered.length() == 1) {
                charsAllowed.addFiltersOut(filtered.charAt(0));
                continue;
            }

            // "allowed_specials:!#$%&'*+-=?^_`{|}~@.[]"
            if (filtered.startsWith("allowed_")) {
                filtered = filtered.replaceFirst("allowed_", "");
                if (filtered.startsWith("specials:")) {
                    filtered = filtered.replaceFirst("specials:", "");
                    charsAllowed.allowedSpecials(filtered);
                } else if (filtered.startsWith("numbers:")) {
                    filtered = filtered.replaceFirst("numbers:", "");
                    charsAllowed.allowedNumbers(filtered);
                } else if (filtered.startsWith("letters:")) {
                    filtered = filtered.replaceFirst("letters:", "");
                    charsAllowed.allowedLetters(filtered);
                } else {
                    System.err.println("Unknown allowed filter: " + filtered);
                }

                continue;
            }
            // e.g. escape(\\):'"
            // e.g. escape(double):'
            if (filtered.startsWith("escape(")) {
                filtered = filtered.replaceFirst("escape\\(", "");
                EscapeChar escapeChar;
                if (filtered.startsWith("double):")) {
                    filtered = filtered.replaceFirst("double\\):", "");
                    escapeChar = new EscapeChar(null);
                } else {
                    // escape(\):',a -> filtered: \):',a
                    escapeChar = new EscapeChar(filtered.charAt(0));
                    filtered = filtered.substring(3, filtered.length());
                }

                charsAllowed.addEscape(escapeChar, filtered.toCharArray());

                continue;
            }

            switch (filtered) {
                case "numbers":
                    charsAllowed.setNumbers(false);
                    break;
                case "letters":
                    charsAllowed.setLetter(false);
                    break;
                case "specials":
                    charsAllowed.setSpecial(false);
                    break;
                default:
                    //System.err.println("Unknown filter: " + filtered);
                    for (int i = 0; i < filtered.length(); i++) {
                        charsAllowed.addFiltersOut(filtered.charAt(i));
                    }

            }
        }

        return charsAllowed;
    }

    private List<GenerateParameters> parseGenerate(JSONObject pattern) {
        LinkedList<GenerateParameters> retval = new LinkedList<>();
        for (JSONObject sufficientJson : JSONUtil.getListSave(pattern, "generates", JSONObject.class)) {
            GenerateParameters generateParam = new GenerateParameters(JSONUtil.getListSave(sufficientJson, "params", String.class));

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

   

}
