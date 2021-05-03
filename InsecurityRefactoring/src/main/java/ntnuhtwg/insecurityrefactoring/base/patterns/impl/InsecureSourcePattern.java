/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.patterns.impl;

import ntnuhtwg.insecurityrefactoring.base.DataType;
import ntnuhtwg.insecurityrefactoring.base.patterns.Pattern;

/**
 *
 * @author blubbomat
 */
public class InsecureSourcePattern extends SourcePattern{

    public InsecureSourcePattern(DataType dataOutput) {
        super(dataOutput);
    }

    public boolean isRefactorable(SourcePattern sourcePattern) {
        return sourcePattern.getPatternType() == getPatternType() 
                && sourcePattern.getOutputType() == getOutputType();
    
    }
    
    
    
}
