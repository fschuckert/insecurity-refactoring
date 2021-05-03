/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.refactor.analyze;

import java.util.List;
import ntnuhtwg.insecurityrefactoring.base.ASTNodeTypes;
import ntnuhtwg.insecurityrefactoring.base.Util;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.dsl.cypher.DataflowDSL;
import ntnuhtwg.insecurityrefactoring.base.info.ContextInfo;
import ntnuhtwg.insecurityrefactoring.base.tree.DFATreeNode;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

/**
 *
 * @author blubbomat
 */
public class ACIDContextAnalyzer {
        
    
    public ContextInfo analyzeContext(DFATreeNode source, String vulnType){
        String pre = contextUp(source, true);
        String post = contextUp(source, false);
        
        return new ContextInfo(vulnType, pre, post);
    }
    
    private String contextUp(DFATreeNode node, boolean pre){
        DFATreeNode parent = node.getParent_();        
        if(parent == null){
            return "";
        }
        
        String concatStr = "";
        if(parent.isConcat()){
            List<DFATreeNode> concats = pre ?  parent.getChildrenBefore(node) : parent.getChildrenAfter(node);
            for(DFATreeNode concat : concats){
                String context = contextDown(concat);
                concatStr += context;
            }            
        }

        if(pre){
            return contextUp(parent, pre) + concatStr;
        }
        else {
            return concatStr + contextUp(parent, pre);
        }
    }

    private String contextDown(DFATreeNode node) {
        if(Util.isAnyOf(node.getObj(), new String[]{ASTNodeTypes.STRING})){
            return node.getObj().getString("code");
        }
        
        if(node.isConcat()){
            String retval = "";
            for(DFATreeNode child : node.getChildren_()){
                retval += contextDown(child);
            }
            return retval;
        }
        else if(node.isIsExcluding() && node.getChildren_().size() > 1){
            // choose first! -> concept: The inputs will probably be the same context
            return contextDown(node.getChildren_().get(0));
        }
        else{
            if(node.getChildren_().isEmpty()){
                return "";
            }
            else {
                return contextDown(node.getChildren_().get(0));
            }
        }
    }
    
}
