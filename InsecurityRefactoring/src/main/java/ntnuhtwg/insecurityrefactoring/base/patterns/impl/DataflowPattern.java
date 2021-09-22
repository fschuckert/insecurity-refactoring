/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.patterns.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import ntnuhtwg.insecurityrefactoring.base.DataType;
import ntnuhtwg.insecurityrefactoring.base.ast.AnyNode;
import ntnuhtwg.insecurityrefactoring.base.ast.FixedNode;
import ntnuhtwg.insecurityrefactoring.base.exception.TimeoutException;
import ntnuhtwg.insecurityrefactoring.base.patternpersist.PatternEntry;
import ntnuhtwg.insecurityrefactoring.base.patternpersist.PatternParser;
import ntnuhtwg.insecurityrefactoring.base.tree.DFATreeNode;
import ntnuhtwg.insecurityrefactoring.base.tree.TreeNode;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.Neo4JConnector;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.Neo4jDB;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;
import ntnuhtwg.insecurityrefactoring.base.patterns.PassthroughPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.Pattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.PatternStorage;

/**
 *
 * @author blubbomat
 */
public class DataflowPattern extends Pattern implements PassthroughPattern{

    private boolean passthrough;
    
    
    
    String identifyPattern;
    
    private Double diffMan;
    private Double diffSca;
    private Double diffDyn;
   
    

    public DataflowPattern(boolean passthrough, DataType dataInput, DataType dataOutput, String identifyPattern, Double diffMan, Double diffSca, Double diffDyn) {
        this.passthrough = passthrough;
        this.dataInput = dataInput;
        this.dataOutput = dataOutput;
        this.identifyPattern = identifyPattern;
        this.diffMan = diffMan;
        this.diffSca = diffSca;
        this.diffDyn = diffDyn;
    }
    
    @Override
    public boolean isPassthrough() {
        return passthrough;
    }
    
    

    public boolean isRefactoringPossible(DFATreeNode node, Neo4jDB db, PatternStorage patternStorage) throws TimeoutException {
        DataflowIdentifyPattern identify = patternStorage.getDataflowIdentify(identifyPattern);
        
                
//                patternStorage.getDataflowIdentifies().get(identifyPattern);
        
        if(identify == null){
            System.out.println(getName() + " cannot find dataflow identify pattern: " + identifyPattern);
            return false;
        }
        
        return identify.equalsPattern(node.getObj(), db) && dataflowTypesEqual(node);
    }

    public String getIdentifyPattern() {
        return identifyPattern;
    }
    
    private boolean dataflowTypesEqual(DFATreeNode node){
        return this.getDataInputType().equalsAny(node.getOutputType()) && this.getDataOutputType().equalsAny(node.getOutputType());
    }
    
}
