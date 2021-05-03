/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.patterns.impl.sufficient;

import java.util.List;
import ntnuhtwg.insecurityrefactoring.base.DataType;
import ntnuhtwg.insecurityrefactoring.base.context.Context;
import ntnuhtwg.insecurityrefactoring.base.context.Requirement;
import ntnuhtwg.insecurityrefactoring.base.context.RequirementList;
import ntnuhtwg.insecurityrefactoring.base.info.ContextInfo;

/**
 *
 * @author blubbomat
 */
public interface Sufficient {

    List<VulnSufficient> getSufficients();

    void setSufficients(List<VulnSufficient> sufficients);

    public static boolean isSufficient(Sufficient sufficientPattern, ContextInfo contextInfo) {
        for (VulnSufficient suff : sufficientPattern.getSufficients()) {
            if (!suff.getVulnType().equals(contextInfo.getVulnType())) {
                continue;
            }
            
            return suff.getRequirements().isSufficient(contextInfo);          
        }
        return true;
    }

    
}
