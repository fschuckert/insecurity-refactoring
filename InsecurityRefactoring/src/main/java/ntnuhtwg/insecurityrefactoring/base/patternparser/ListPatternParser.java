/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.patternparser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import ntnuhtwg.insecurityrefactoring.base.ASTType;
import ntnuhtwg.insecurityrefactoring.base.DataType;
import ntnuhtwg.insecurityrefactoring.base.patterns.Pattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SanitizePattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SinkPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.sufficient.VulnSufficient;

/**
 *
 * @author blubbomat
 */
public class ListPatternParser {
    public List<Pattern> parsePatterns(String path, String patternType) throws FileNotFoundException, IOException{
        List<Pattern> retval = new LinkedList<>();
        
        BufferedReader reader = new BufferedReader(new FileReader(path));
	String line = reader.readLine();
	while (line != null) {
            line = line.trim();
            if(line.startsWith("#") || line.isBlank()){
                line = reader.readLine();
                continue;
            }
            
            String[] splitted = line.split(",");
            if(splitted.length != 6){
                line = reader.readLine();
                continue;
            }
            Boolean paramExtended = false;
            String vulnType = splitted[0].trim();
            String callType = splitted[1].trim();
            String callName = splitted[2].trim();
            String paramNumStr = splitted[3].trim();
            if(paramNumStr.endsWith("...")){
                paramExtended = true;
                paramNumStr = paramNumStr.replace("...", "").trim();
            }
            String paramAmountStr = splitted[4].trim();
            boolean paramAmountExtended = false;
            if(paramAmountStr.endsWith("...")){
                paramAmountExtended = true;
                paramAmountStr = paramAmountStr.replace("...", "").trim();
            }
            Integer paramAmount = Integer.valueOf(paramAmountStr);
            Integer paramNum = Integer.valueOf(paramNumStr);
            String optionalType = splitted[5].trim();
            
            Pattern pattern = createPattern(vulnType, callType, callName, paramNum, paramExtended, paramAmount, paramAmountExtended, optionalType, patternType);
            if(pattern != null){
                retval.add(pattern);
            }
            
            line = reader.readLine();
        }
        
        return retval;
    }

    private Pattern createPattern(String vulnType, String callType, String callName, int paramNum, boolean paramExtended, int paramAmount, boolean paramAmountExtended, String optionalParam, String patternType) {
        patternType = patternType.trim();
        String uniqueName = "list_" + callType + ":"+callName + "_" + paramNum;
        String code = "";
        
        String params = "";
//        int parametersCount = Math.max(paramAmount, paramNum + 1);
        for(int i=0; i<paramAmount; i++){
            if(paramNum == i){
                params +=  ", %input";
                if(paramExtended){
                    params += "...";
                }
            }
        else
            params +=  ", <any>()";
        }
        
        if(paramAmountExtended){
            if(params.endsWith("<any>()")){
                params += "...";
            }
            else if(!params.endsWith("...")){
                params += ", <any>()?...";
            }
        }
        
        if("call".equals(callType)){
            code = "<call>(" +callName + params + ")";
        }
        else if ("method".equals(callType)){
            code = "<call_method>(<any>(), " +callName + params + ")";
        }
        else{
            System.out.println("Incorrect list pattern: " + callType);
            return null;
        }
        
        List<String> codeLines = new LinkedList<>();
        codeLines.add(code);

//        System.out.println("CODE: " + code);
        
        Pattern pattern = null;
        if("sanitize".equals(patternType)){
            boolean passthrough = "pass".equals(optionalParam);         
            VulnSufficient vulnSufficient = new VulnSufficient(vulnType);
            List<VulnSufficient> sufficientsFor = new LinkedList();
            sufficientsFor.add(vulnSufficient);
            SanitizePattern sanPattern = new SanitizePattern(passthrough, DataType.String(), DataType.String(), sufficientsFor);
            sanPattern.setReturnOutput(passthrough);
            pattern = sanPattern;
        }
        if("sink".equals(patternType)){
            SinkPattern sinkPattern = new SinkPattern();
            sinkPattern.setVulnType(vulnType);
            sinkPattern.setIsSafe("safe".equals(optionalParam));
            pattern = sinkPattern;
        }        
        pattern.setVulnOnly(vulnType);
        pattern.setType(patternType);
        pattern.setName(uniqueName);
        pattern.setInputType(ASTType.expression);
        pattern.setOutputType(ASTType.expression);
        pattern.setPatternType(ASTType.expression);
        pattern.setCodeLines(codeLines);
        
        
        
        
        return pattern;
    }
}
