/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.patternparser;

import java.util.LinkedList;
import java.util.List;
import ntnuhtwg.insecurityrefactoring.base.DataType;
import ntnuhtwg.insecurityrefactoring.base.tree.TreeNode;

/**
 *
 * @author blubbomat
 */
public class PatternParser {
    
    public static TreeNode<PatternEntry> parsePatternCode(String code, String patternName){
        return parseCode(code, patternName);
    }
    
    private static TreeNode<PatternEntry> parseCode(String code, String patternName){
//        try{
            if(code.trim().startsWith("<")){
                String identifier = code.split("(?<!\\\\)<", 2)[1].split("(?<!\\\\)>", 2)[0];

                String parameterTemp = code.split("\\(", 2)[1];
                
                boolean endsWithDots = parameterTemp.endsWith("...");
                if(endsWithDots){
                    parameterTemp = parameterTemp.replace("...", "");                
                }
                boolean optional = parameterTemp.endsWith("?");
                if(optional){
                    parameterTemp = parameterTemp.replace("?", "").trim();
                }
                
                // TODO: replace the \< with < ?
                PatternEntry pattern = new PatternEntry(identifier, true, endsWithDots, optional);
                TreeNode<PatternEntry> patternTree = new TreeNode<>(pattern);
                
                parameterTemp = parameterTemp.substring(0, parameterTemp.lastIndexOf(")"));

                List<String> parameters = splitParameters(parameterTemp);

                for(String parameter : parameters){
                    TreeNode<PatternEntry> subTree = parseCode(parameter.trim(), patternName);
                    patternTree.addChild(subTree);
                }

                return patternTree;
            }

            code = code.trim();
            boolean endsWithDots = code.endsWith("...");
            if(endsWithDots){
                code = code.replace("...", "");                
            }
            boolean optional = code.endsWith("?");
            if(optional){
                code = code.replace("?", "").trim();
            }
            PatternEntry pattern = new PatternEntry(code, code.startsWith("%"), endsWithDots, optional);
            return new TreeNode<>(pattern);
//        } catch(Exception ex){
//            System.out.println("Cannot (" + patternName + ") parse pattern: " + code);
//            return null;
//        }
        
    }
    
    private static List<String> splitParameters(String parametersStr){
        List<String> parameters = new LinkedList<>();
        
        int depth = 0;
        String parameter = "";
        for(char c : parametersStr.toCharArray()){
            if(c == ',' && depth == 0){
                parameters.add(parameter);
                parameter = "";
            }
            else{
                parameter += c;
            }
            
            if(c == '('){
                depth++;
            }
            
            if(c == ')'){
                depth--;
            }
        }
        parameters.add(parameter);
        
        return parameters;
    }
    
    public static DataType parseDataType(String datatypeStr){
        String dataTypeTempStr = datatypeStr.split("\\(", 2)[0];
        DataType dataType = DataType.valueOf(dataTypeTempStr);
        
        DataType dataTypeTemp = dataType;
        
        while(datatypeStr.lastIndexOf(")")>=0){
            datatypeStr = datatypeStr.split("\\(", 2)[1];
            datatypeStr = datatypeStr.substring(0, datatypeStr.lastIndexOf(")"));
            DataType dataTypesub = DataType.valueOf(datatypeStr.split("\\(")[0]);
            dataTypeTemp.setArraySubType(dataTypesub);
            
            dataTypeTemp = dataTypesub;
        }
        
        return dataType;
    }
}
