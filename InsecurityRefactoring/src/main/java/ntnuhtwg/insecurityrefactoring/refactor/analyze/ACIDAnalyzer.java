/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.refactor.analyze;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import ntnuhtwg.insecurityrefactoring.base.ASTNodeTypes;
import ntnuhtwg.insecurityrefactoring.base.DataType;
import ntnuhtwg.insecurityrefactoring.base.Util;
import ntnuhtwg.insecurityrefactoring.base.exception.TimeoutException;
import ntnuhtwg.insecurityrefactoring.base.tree.DFATreeNode;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.dsl.cypher.DataflowDSL;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.Neo4jDB;
import ntnuhtwg.insecurityrefactoring.base.info.ContextInfo;
import ntnuhtwg.insecurityrefactoring.base.info.PipInformation;
import ntnuhtwg.insecurityrefactoring.base.patterns.PassthroughPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.DataflowPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SanitizePattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.PatternStorage;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SourcePattern;
import org.javatuples.Pair;
import org.neo4j.kernel.impl.index.schema.CollectingIndexUpdater;

/**
 *
 * @author blubbomat
 */
public class ACIDAnalyzer {
    
  
    
    private PatternStorage patternStorage;
    private Neo4jDB db;
    private DataflowDSL dsl;

    public ACIDAnalyzer(PatternStorage patternStorage, Neo4jDB db) {
        this.patternStorage = patternStorage;
        this.db = db;
        this.dsl = new DataflowDSL(db);
    }

    
    private void resetNode(DFATreeNode node){
        node.setInputType(DataType.Unknown());
        node.setOutputType(DataType.Unknown());
        node.setSanitizePattern(null);
        node.clearDataflowReplacements();
    }
    
    private void resetRecursive(DFATreeNode node){
        resetNode(node);
        
        for(DFATreeNode child : node.getChildren_()){
            resetRecursive(child);
        }
    }
    
    
    
    
    public void analyse(PipInformation pipInformation, DFATreeNode sourceNode, List<Pair<SanitizePattern, DFATreeNode>> sanitizeNodes) throws TimeoutException{
        DFATreeNode tree = pipInformation.getSink();
        System.out.println("Analyzing: " + tree + " source: " + sourceNode);
        // reset old data
        resetRecursive(tree);
        
        // Data flow type
        new ACIDDataflowTypeAnalyzer(patternStorage, db).analyzeDataflowType(sourceNode);
        
        // Context
        ContextInfo context = new ACIDContextAnalyzer().analyzeContext(sourceNode, tree.getSinkPattern().getVulnType());
        pipInformation.setContextInfo(context);
        
        // Patterns
        sanitizeNodes.clear();
        new ACIDPatternAnalyzer(patternStorage, dsl).findSanitizeNodesRec(sourceNode, sanitizeNodes);       
    }
    
    
    public List<DFATreeNode> getVulnerablePathes(DFATreeNode sink) throws TimeoutException{
        List<DFATreeNode> vulnerableSources = new LinkedList<>();       
        
        if(!sink.getSinkPattern().isIsSafe()){
            for(DFATreeNode source : sink.getAllLeafs_()){
                if(patternStorage.isSource(source, db)){
                    if( !containsSanitizeRec(source) ){
                        vulnerableSources.add(source);
                    }
                }
            }  
        }
        
        return vulnerableSources;
    }

    private boolean containsSanitizeRec(DFATreeNode node) throws TimeoutException {
        if(node == null){
            return false;
        }
        
        if(patternStorage.isSanitize(node, db)){
            return true;
        }
        
        return containsSanitizeRec(node.getParent_());
    }
    
}
