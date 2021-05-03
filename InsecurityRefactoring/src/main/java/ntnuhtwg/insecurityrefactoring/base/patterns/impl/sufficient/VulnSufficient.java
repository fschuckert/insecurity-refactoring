/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.patterns.impl.sufficient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ntnuhtwg.insecurityrefactoring.base.context.Context;
import ntnuhtwg.insecurityrefactoring.base.context.NeedsRequirements;
import ntnuhtwg.insecurityrefactoring.base.context.PossibleRequirements;
import ntnuhtwg.insecurityrefactoring.base.context.Requirement;
import ntnuhtwg.insecurityrefactoring.base.context.RequirementList;

/**
 *
 * @author blubbomat
 */
public class VulnSufficient implements NeedsRequirements{
    
    private final String vulnType;
    
    private PossibleRequirements required;
    

    public VulnSufficient(String vulnType) {
        this.vulnType = vulnType;
    }
    

    

    public String getVulnType() {
        return vulnType;
    }

    @Override
    public PossibleRequirements getRequirements() {
        return required;
    }

    @Override
    public void setRequirements(PossibleRequirements requirmentList) {
        this.required = requirmentList;
    }

    
    
    
    
}
