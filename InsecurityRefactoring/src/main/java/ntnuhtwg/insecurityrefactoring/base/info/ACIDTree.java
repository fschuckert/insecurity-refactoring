/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.info;

import java.util.LinkedList;
import java.util.List;
import ntnuhtwg.insecurityrefactoring.base.tree.DFATreeNode;

/**
 *
 * @author blubbomat
 */
public class ACIDTree {
    private DFATreeNode sink;
    
    
    
    private List<DataflowPathInfo> possibleSources = new LinkedList<>();

    public ACIDTree(DFATreeNode sink) {
        this.sink = sink;
    }

    public DFATreeNode getSink() {
        return sink;
    }

    public List<DataflowPathInfo> getPossibleSources() {
        return possibleSources;
    }

    public void addPossibleSource(DataflowPathInfo pathInformation){
        this.possibleSources.add(pathInformation);
    }
    
    
    public boolean isVulnSink(){
        return getSink() != null && !getSink().getSinkPattern().isIsSafe();
    }
    

    @Override
    public String toString() {
        String retval = "PIP(";
        if(sink != null){
            retval += sink.getSinkPattern().getVulnType() + ")";
            retval += ": " + sink.getSourceLocation().toString();
        }
        return retval;
    }
    
    
    
    
}
