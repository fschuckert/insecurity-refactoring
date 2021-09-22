/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.patterns.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.bouncycastle.jcajce.provider.digest.GOST3411;
/**
 *
 * @author blubbomat
 */
public class GenerateParameters {
    final List<String> parameters;
    
//    List<String> initCode = new LinkedList<>();
//    List<String> generateOutputCode = new LinkedList<>();
//    List<String> filters = new LinkedList<>();
    
    Map<String, Object> toReplace = new HashMap<>();

    public GenerateParameters(List<String> parameters) {
        this.parameters = parameters;
    }  
    
    public List<String> getParameters() {
        return parameters;
    }
    
    public int parameterSize(){
        return parameters.size();
    }
    
    public String getParameter(int i){
        return parameters.get(i);
    }
    
    public void addToReplace(String key, Object value){
        toReplace.put(key, value);
    }

    public Map<String, Object> getToReplace() {
        return toReplace;
    }
    
    
    public String getRemainingParams(int startIndex){
        List<String> remainingParams = getParameters().subList(startIndex, getParameters().size());
        String remainParamsStr = String.join(",", remainingParams);
        return remainParamsStr;
    }
    

    
    
    
    
    
}
