/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.gui.abego;


import java.awt.*;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Objects;
import ntnuhtwg.insecurityrefactoring.base.tree.DFATreeNode;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.DataflowPattern;
import org.neo4j.driver.types.Node;

/**
 * The trees implementation from Java will add children using the equal method.
 * The abstract syntax nodes are having the same names and maybe some of them
 * have the same value as well but might not be the same node. This class
 * will all have a different id for solving that problem.
 * 
 * @author Felix Schuckert
 */
public class StringNode {
    private String[] value;

    public StringNode(String value){
        this.setValue(value);
    }
    
    public StringNode(INode node){
//        this.setValue(node.asMap().toString());
        
        String val = "" + node.id() + " ";
        val += node.containsKey("type") ? node.getString("type") : "";
        val += "\n";
        
        for(Entry<String, Object> entry : node.asMap().entrySet()){
            val += entry + "\n";
        }
        
        this.setValue(val);
    }
    
    public StringNode(DFATreeNode dfaTreeNode, String connection, String sourceLocation, String sourceCode, boolean showProperties, String optionalHeader, boolean showFlowType){
        INode node = dfaTreeNode.getObj();
        
        if(node != null){
    //        String val = "(" + node.id() + ") " + connection + (node.containsKey("type") ? node.get("type") : "") + "\n";
            String val = "" + connection + " << " ;
            val += node.get("type") + " {"+optionalHeader + "} (" + node.id() + ") << \n\n";

            if(showProperties){
                val+= "## Properties ##\n";
                for(Entry<String, Object> entry : dfaTreeNode.getObj().asMap().entrySet()){
                   val += entry.getKey() + ":" + entry.getValue() + "\n"; 
                }
                val+="\n";
            }

            if(dfaTreeNode.getSanitizePattern() != null){
                val+= "Sanitize pattern: " + dfaTreeNode.getSanitizePattern() + "\n";
            }

            for(DataflowPattern dataflowPattern : dfaTreeNode.getPossibleDataflowReplacements()){
                val+= "Possible dataflow: " + dataflowPattern + "\n";
            }

            if(showFlowType){
                val+= "Output ^^ " + dfaTreeNode.getOutputType() + "\n";
                val+= "Input: " + dfaTreeNode.getInputType()+ "\n";
                val+= "isAssigned: " + dfaTreeNode.isIsAssigned()+ "\n\n";
            }
            val += "Location: "+sourceLocation + "\n";

    //                "(" + node.id() + ") " + connection + (node.containsKey("type") ? node.get("type") : "") + "\n";
            val += sourceCode + "\n";
    //        val += node.containsKey("type") ? node.get("type") : "" + "\n";

            this.setValue(val);
        }
        else {
            this.setValue("<<NULL>>");
        }
    }

    public StringNode(String[] values){
        this.value = values;
    }

    public String[] getValue() {
        return value;
    }

    public int getNumberOfLines(){
        return value.length;
    }

    public int getMaxCharacterLength(FontMetrics fontMetrics){
        int maxLength = 10;

        for(int i = 0; i<value.length; i++){
            int currentLength = calculateLength(value[i], fontMetrics);
               if(currentLength > maxLength){
                   maxLength = currentLength;
               }
        }

        return maxLength;
    }

    private int calculateLength(String line, FontMetrics fontMetrics){
        int retval = 0;

        if(fontMetrics != null){
            for(char c : line.toCharArray()){
                retval += fontMetrics.charWidth(c);
            }

            retval += 10;
        }
        else {
            retval += line.length() * 7;
        }

        return retval;
    }

    public void setValue(String value) {
        this.value = value.split("\n");
    }


    @Override
    public String toString() {
        String retval = "";

        for(int i = 0; i<value.length; i++){
            retval += value[i];
        }

        return retval;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 11 * hash + Objects.hashCode(this.value);
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StringNode that = (StringNode) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(value, that.value);

    }
}
