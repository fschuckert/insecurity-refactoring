/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.refactor.base;

import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;
import ntnuhtwg.insecurityrefactoring.base.ASTNodeTypes;
import ntnuhtwg.insecurityrefactoring.base.DataType;
import ntnuhtwg.insecurityrefactoring.base.patterns.PassthroughPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.DataflowIdentifyPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.DataflowPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SourcePattern;
import org.neo4j.cypher.internal.runtime.interpreted.commands.PathPattern;
import scala.NotImplementedError;

/**
 *
 * @author blubbomat
 */
public class TempPattern {
    private MissingCall missingCall;
    private int inputIndex = -1;
    private boolean isTempSource;
    private DataType output;
    private DataType input;

    private TempPattern(MissingCall missingCall, boolean isTempSource, DataType output, DataType input, int inputIndex) {
        this.missingCall = missingCall;
        this.isTempSource = isTempSource;
        this.output = output;
        this.input = input;
        this.inputIndex = inputIndex;
    }

    public MissingCall getMissingCall() {
        return missingCall;
    }
    
    
    
    public static TempPattern createSource(MissingCall missingCall, DataType output){
        return new TempPattern(missingCall, true, output, null, -1);
    }
    
    public static TempPattern createPassthrough(MissingCall missingCall, DataType input, DataType output, int inputIndex){        
        return new TempPattern(missingCall, false, output, input, inputIndex);
    }

    public SourcePattern getSourcePattern() {
        if(isTempSource){
            SourcePattern sourcePattern = new SourcePattern(output);
            List<String> codes = new LinkedList<>();
            codes.add(getCode(-1));
            sourcePattern.setCodeLines(codes);
            sourcePattern.setName("temp source: " + missingCall.name);
            return sourcePattern;
        }
        
        return null;
    }

    public PassthroughPattern getPassthroughPattern() {
        if(inputIndex >= 0){
            DataflowIdentifyPattern dataflowPattern = new DataflowIdentifyPattern(true, input, output);
            dataflowPattern.setReturnOutput(true);
            List<String> codes = new LinkedList<>();
            codes.add(getCode(inputIndex));
            dataflowPattern.setCodeLines(codes);
            dataflowPattern.setName("temp passthrough: " + missingCall.name + "(" + inputIndex + ")");
            return dataflowPattern;
        }
        
        return null;
    }
    
    private String getCode(int inputIndex){
        String code = "";
        if(ASTNodeTypes.CALL.equals(missingCall.callType)){
            code += "<call>("+missingCall.name;
        }        
        else if(ASTNodeTypes.METHOD_CALL.equals(missingCall.callType)){
            code += "<call_method>(<any>(), "+missingCall.name;
        }       
        else if(ASTNodeTypes.STATIC_CALL.equals(missingCall.callType)){
            code += "<call_static>(<any>(), "+missingCall.name;
        }
        else {
            throw new NotImplementedError("Incorrect temp pattern... for call type: " + missingCall.callType);
        }
        
        List<String> parameters = new LinkedList<>();
        for(int i=0; i<missingCall.numberOfParams; i++){
            if(i==inputIndex){
                parameters.add("%input");
            }
            else{
                parameters.add("<any>()");
            }
        }
        
        StringJoiner stringJoiner = new StringJoiner(", ");        
        stringJoiner.add(code);
        for(String parameter : parameters){
            stringJoiner.add(parameter);
        }
        
        code = stringJoiner.toString() + ")";
        
//        System.out.println("Code: " + code);
        
        return code;
    }
}
