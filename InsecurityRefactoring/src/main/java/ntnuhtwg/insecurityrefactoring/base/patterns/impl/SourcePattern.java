/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.patterns.impl;

import java.util.LinkedList;
import java.util.List;
import ntnuhtwg.insecurityrefactoring.base.DataType;
import ntnuhtwg.insecurityrefactoring.base.ast.FixedNode;
import ntnuhtwg.insecurityrefactoring.base.context.CharsAllowed;
import ntnuhtwg.insecurityrefactoring.base.context.Context;
import ntnuhtwg.insecurityrefactoring.base.context.SufficientFilter;
import ntnuhtwg.insecurityrefactoring.base.context.enclosure.Enclosure;
import ntnuhtwg.insecurityrefactoring.base.exception.GenerateException;
import ntnuhtwg.insecurityrefactoring.base.tree.TreeNode;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.dsl.cypher.DataflowDSL;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.Neo4JConnector;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.Neo4jDB;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;
import ntnuhtwg.insecurityrefactoring.base.exception.NotExpected;
import ntnuhtwg.insecurityrefactoring.base.info.ContextInfo;
import ntnuhtwg.insecurityrefactoring.base.patterns.Pattern;

/**
 *
 * @author blubbomat
 */
public class SourcePattern extends Pattern{
    
    DataType dataOutput;
    
    private CharsAllowed charsAllowed;
    

    
    private Enclosure addsEnclosure;




    public SourcePattern(DataType dataOutput) {
        this.dataOutput = dataOutput;
    }

    public DataType getDataOutput() {
        return dataOutput;
    }

    public CharsAllowed getCharsAllowed() {
        return charsAllowed;
    }

    public void setCharsAllowed(CharsAllowed charsAllowed) {
        this.charsAllowed = charsAllowed;
    }

    public boolean isReplacableWith(SourcePattern sourcePatternToCheck) {
        return sourcePatternToCheck.getPatternType() == getPatternType() 
                && sourcePatternToCheck.getOutputType() == getOutputType();
    }

    public Enclosure getAddsEnclosure() {
        return addsEnclosure;
    }

    public void setAddsEnclosure(Enclosure addsEnclosure) {
        this.addsEnclosure = addsEnclosure;
    }
    
    public boolean isSourceSufficient(ContextInfo contextInfo){        
        return SufficientFilter.isSufficient(getCharsAllowed(), contextInfo, getAddsEnclosure()).isExploitable();
    }
    
    
    
    

  




    
    
    
}
