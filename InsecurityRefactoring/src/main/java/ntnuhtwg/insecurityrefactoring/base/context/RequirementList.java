/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.context;

import java.util.LinkedList;
import java.util.List;
import ntnuhtwg.insecurityrefactoring.base.DataType;
import ntnuhtwg.insecurityrefactoring.base.info.ContextInfo;

/**
 *
 * @author blubbomat
 */
public class RequirementList {
    private List<Requirement> requirements = new LinkedList<>();

    public void addRequirement(Requirement requirement) {
        requirements.add(requirement);
    }
    
    public List<Requirement> asList(){
        return requirements;
    }

    public Boolean contains(Requirement requirement) {
        return requirements.contains(requirement);
    }

    /**
     * checks if all contexts exist or not exist to be sufficient
     * @param contextInfo
     * @return 
     */
    boolean isSufficient(ContextInfo contextInfo) {        
        for(Requirement req : requirements){
            if(!isRequirementSuff(req, contextInfo)){
                return false;
            }
        }
        
        return true;
    }
    
    
    private static boolean isRequirementSuff(Requirement req, ContextInfo contextInfo) {
        boolean fullFillsReq = req.getContext().fullfillsRequirement(contextInfo);
        if (fullFillsReq != req.hasToBeTrue()) {
            return false;
        }
        return true;
    }
}
