/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.patterns.impl;

import java.util.List;
import ntnuhtwg.insecurityrefactoring.base.DataType;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.dsl.cypher.DataflowDSL;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;
import ntnuhtwg.insecurityrefactoring.base.exception.TimeoutException;
import ntnuhtwg.insecurityrefactoring.base.patterns.PassthroughPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.Pattern;
import scala.NotImplementedError;

/**
 *
 * @author blubbomat
 */
public class ConcatPattern extends Pattern implements PassthroughPattern{
    
    private final DataType dataInput;
    private final DataType dataOutput;
    private final String inputAmount;

    public ConcatPattern(DataType dataInput, DataType dataOutput, String inputAmount) {
        this.dataInput = dataInput;
        this.dataOutput = dataOutput;
        this.inputAmount = inputAmount;
    }

    @Override
    public boolean isPassthrough() {
        return true;
    }

    @Override
    public DataType getDataInputType() {
        return dataInput;
    }

    @Override
    public DataType getDataOutputType() {
        return dataOutput;
    }
    
    public List<INode> findInputNodes(DataflowDSL dsl, INode node)  throws TimeoutException{
        if("...".equals(inputAmount)){
            return findNode(dsl, node, "%input");
        }
        else if("1".equals(inputAmount)){
            return findNode(dsl, node, "%input");
        }
            
        
        throw new NotImplementedError("Currently only support for concat with dots");
        
    }
    
}
