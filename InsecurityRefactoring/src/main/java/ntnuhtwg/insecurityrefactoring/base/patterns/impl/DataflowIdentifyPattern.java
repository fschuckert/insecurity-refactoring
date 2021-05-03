/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.patterns.impl;

import ntnuhtwg.insecurityrefactoring.base.DataType;
import ntnuhtwg.insecurityrefactoring.base.patterns.PassthroughPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.Pattern;

/**
 *
 * @author blubbomat
 */
public class DataflowIdentifyPattern extends Pattern implements PassthroughPattern{
     private boolean passthrough;
    
    private DataType dataInput;
    private DataType dataOutput;
    
    
    public DataflowIdentifyPattern(boolean passthrough, DataType dataInput, DataType dataOutput) {
        this.passthrough = passthrough;
        this.dataInput = dataInput;
        this.dataOutput = dataOutput;
    }

    @Override
    public boolean isPassthrough() {
        return passthrough;
    }

    @Override
    public DataType getDataInputType() {
        return dataInput;
    }

    @Override
    public DataType getDataOutputType() {
        return dataOutput;
    }



 
    
}
