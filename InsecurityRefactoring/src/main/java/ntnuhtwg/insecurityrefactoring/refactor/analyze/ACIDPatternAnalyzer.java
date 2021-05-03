/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.refactor.analyze;

import java.util.List;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.Neo4jDB;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.dsl.cypher.DataflowDSL;
import ntnuhtwg.insecurityrefactoring.base.exception.TimeoutException;
import ntnuhtwg.insecurityrefactoring.base.patterns.PatternStorage;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.DataflowPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SanitizePattern;
import ntnuhtwg.insecurityrefactoring.base.tree.DFATreeNode;
import org.javatuples.Pair;

/**
 *
 * @author blubbomat
 */
public class ACIDPatternAnalyzer {
    
    private PatternStorage patternStorage;
    private DataflowDSL dsl;

    public ACIDPatternAnalyzer(PatternStorage patternStorage, DataflowDSL dsl) {
        this.patternStorage = patternStorage;
        this.dsl = dsl;
    }
    
    public void findSanitizeNodesRec(DFATreeNode node, List<Pair<SanitizePattern, DFATreeNode>> sanitizeNodes) throws TimeoutException{
        for(SanitizePattern sanitizePattern : patternStorage.getSanitizations()){
            if(sanitizePattern.isSanitizeNode(dsl, node)){
                System.out.println("Found sanitize: " + sanitizePattern);
                node.setSanitizePattern(sanitizePattern);
                sanitizeNodes.add(new Pair<>(sanitizePattern, node));
            }
        }
        
        for(DataflowPattern possibleDataflowPattern : patternStorage.getDataflows()){            
            if(possibleDataflowPattern.isRefactoringPossible(node, dsl.getDb(), patternStorage)){
                node.addPossibleDataflowPattern(possibleDataflowPattern);
            }
        }
        
        if(node.getParent_() != null){
            findSanitizeNodesRec(node.getParent_(), sanitizeNodes);
        }
    }
}
