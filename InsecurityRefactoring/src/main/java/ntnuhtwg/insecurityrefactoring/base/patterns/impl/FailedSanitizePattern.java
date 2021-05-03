/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.patterns.impl;

import java.util.HashMap;
import java.util.List;
import ntnuhtwg.insecurityrefactoring.base.DataType;
import ntnuhtwg.insecurityrefactoring.base.patterns.PassthroughPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.Pattern;
import ntnuhtwg.insecurityrefactoring.base.context.Requirement;
import ntnuhtwg.insecurityrefactoring.base.context.Context;
import ntnuhtwg.insecurityrefactoring.base.context.NeedsRequirements;
import ntnuhtwg.insecurityrefactoring.base.context.PossibleRequirements;
import ntnuhtwg.insecurityrefactoring.base.context.RequirementList;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.sufficient.VulnSufficient;

/**
 *
 * @author blubbomat
 */
public class FailedSanitizePattern extends SanitizePattern{

    public FailedSanitizePattern(boolean passthrough, DataType dataInput, DataType dataOutput, List<VulnSufficient> sufficients) {
        super(passthrough, dataInput, dataOutput, sufficients);
    }


    
    
}
