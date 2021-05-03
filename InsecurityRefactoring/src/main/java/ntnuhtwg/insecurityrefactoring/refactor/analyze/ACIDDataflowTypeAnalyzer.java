/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.refactor.analyze;

import java.util.Arrays;
import java.util.List;
import ntnuhtwg.insecurityrefactoring.base.ASTNodeTypes;
import ntnuhtwg.insecurityrefactoring.base.DataType;
import ntnuhtwg.insecurityrefactoring.base.Util;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.Neo4jDB;
import ntnuhtwg.insecurityrefactoring.base.exception.TimeoutException;
import ntnuhtwg.insecurityrefactoring.base.patterns.PassthroughPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.PatternStorage;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SourcePattern;
import ntnuhtwg.insecurityrefactoring.base.tree.DFATreeNode;

/**
 *
 * @author blubbomat
 */
public class ACIDDataflowTypeAnalyzer {
    
    private PatternStorage patternStorage;
    private Neo4jDB db;
    
    private static List<String> typesWithoutChanges = Arrays.asList(
                ASTNodeTypes.ASSIGN,        
                ASTNodeTypes.ENCAPS_LIST,
                ASTNodeTypes.PARAM,
                ASTNodeTypes.VARIABLE,
                ASTNodeTypes.RETURN
                
                
    );
    
    private static List<String> concatTypes = Arrays.asList(      
                ASTNodeTypes.ASSIGN_OP,
                ASTNodeTypes.BINARY_OP
    );

    public ACIDDataflowTypeAnalyzer(PatternStorage patternStorage, Neo4jDB db) {
        this.patternStorage = patternStorage;
        this.db = db;
    }
    
    
    
    
    
    public void analyzeDataflowType(DFATreeNode source) throws TimeoutException{
        if(source != null){
            for(SourcePattern sourcePattern : patternStorage.getSources()){
                if(sourcePattern.equalsPattern(source.getObj(), db)){                
                    setTypes(source, sourcePattern.getDataOutput());
                    source.setSourcePattern(sourcePattern);
                }
            }
        }
        
    }
    
    
     private void setTypes(DFATreeNode sourceNode, DataType sourceType) throws TimeoutException{
        sourceNode.setOutputType(sourceType);
        
        setTypesRec(sourceNode.getParent_(), sourceType);
    }
    
    private void setTypesRec(DFATreeNode node, DataType inputType) throws TimeoutException{  
        if(node == null){
            return;
        }
        String type = node.getObj().getString("type");
        if(typesWithoutChanges.contains(type)){
            node.setInputType(inputType);
            node.setOutputType(inputType);
            setTypesRec(node.getParent_(), inputType);
        }
        else if(concatTypes.contains(type)){
            // TODO can be improved with additional analysis of other sub tree types
            node.setInputType(inputType);
            node.setOutputType(DataType.String());
            setTypesRec(node.getParent_(), DataType.String());
        }
        else if(Util.isType(node.getObj(), ASTNodeTypes.DIM)){
            if(node.isIsAssigned()){
                node.setInputType(inputType);
                node.setOutputType(DataType.Array().setArraySubType(inputType));
                setTypesRec(node.getParent_(), node.getOutputType());
            }   
            else{
                if(inputType.isArray()){
                    node.setInputType(inputType);
                    node.setOutputType(inputType.getArraySubType());
                    setTypesRec(node.getParent_(), node.getOutputType());
                }
            }
        }
        else{            
            if(Util.isAnyCall(node.getObj()) && !node.isIsPassthrough()){
                // a resolved function call -> maintain datatype
                maintainDatatype(node, inputType);
                return;
            }
            else if(node.isConditionNode()){
                // condittion nodes are not changing data!
                maintainDatatype(node, inputType);
                return;
            }
            for(PassthroughPattern passthrough : patternStorage.getPassthroughs()){
                if(passthrough.equalsPattern(node.getObj(), db)){
                    node.setInputType(passthrough.getDataInputType());
                    node.setOutputType(passthrough.getDataOutputType());
                    setTypesRec(node.getParent_(), passthrough.getDataOutputType());
                    break;
                }
            }
        }
    }
    
    private void maintainDatatype(DFATreeNode node, DataType inputType) throws TimeoutException{
        node.setInputType(inputType);
        node.setOutputType(inputType);
        setTypesRec(node.getParent_(), inputType);
    }
}
