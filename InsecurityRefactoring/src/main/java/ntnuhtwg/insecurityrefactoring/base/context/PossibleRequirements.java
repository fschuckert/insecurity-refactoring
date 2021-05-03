/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.context;

import java.util.LinkedList;
import ntnuhtwg.insecurityrefactoring.base.DataType;
import ntnuhtwg.insecurityrefactoring.base.info.ContextInfo;

/**
 *
 * @author blubbomat
 */
public class PossibleRequirements extends LinkedList<RequirementList>{
    
    /**
     * this function checks if any of the possible requirements is sufficient
     * @param contextInfo
     * @return 
     */
    public boolean isSufficient(ContextInfo contextInfo){
        // no requirements
        if(isEmpty()){
            return true;
        }
        
        for(RequirementList requirements : this){
            if(requirements.isSufficient(contextInfo)){
                return true;
            }                        
        }
        return false;
    }
    
    
    
}
