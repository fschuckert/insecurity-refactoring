/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.patterns.impl.sufficient;

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
    
    Map<String, Object> toReplace = new HashMap<>();

    public GenerateParameters(List<String> parameters) {
        this.parameters = parameters;
    }  
    
    public List<String> getParameters() {
        return parameters;
    }
    
    public void addToReplace(String key, Object value){
        toReplace.put(key, value);
    }

    public Map<String, Object> getToReplace() {
        return toReplace;
    }
    
    

    
    
    
    
    
}
