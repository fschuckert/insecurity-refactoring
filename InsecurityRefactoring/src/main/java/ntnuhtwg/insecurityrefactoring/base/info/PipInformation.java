/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.info;

import java.util.LinkedList;
import java.util.List;
import ntnuhtwg.insecurityrefactoring.refactor.base.PipPathInformation;
import ntnuhtwg.insecurityrefactoring.base.tree.DFATreeNode;

/**
 *
 * @author blubbomat
 */
public class PipInformation {
    private DFATreeNode sink;
    
    private ContextInfo contextInfo;
    
    private List<PipPathInformation> possibleSources = new LinkedList<>();

    public PipInformation(DFATreeNode sink) {
        this.sink = sink;
    }

    public DFATreeNode getSink() {
        return sink;
    }

    public List<PipPathInformation> getPossibleSources() {
        return possibleSources;
    }

    public void addPossibleSource(PipPathInformation pathInformation){
        this.possibleSources.add(pathInformation);
    }

    public List<DFATreeNode> getVulnerableSources() {
        List<DFATreeNode> retval = new LinkedList<>();
        if(!sink.getSinkPattern().isIsSafe()){
            for(PipPathInformation pipInformation : possibleSources){
                if(pipInformation.isVulnerable()){
                    retval.add(pipInformation.getSource());
                }
            }
        }
        
        return retval;
    }
    
    
    public boolean isVulnSink(){
        return getSink() != null && !getSink().getSinkPattern().isIsSafe();
    }
    
    public boolean isVulnerability(){
        if(!isVulnSink()){
            return false;
        }
        
        for(PipPathInformation pathInfo : getPossibleSources()){
            if(pathInfo.isVulnerable()){
                return true;
            }
        }
        
        return false;
    }

    public ContextInfo getContextInfo() {
        return contextInfo;
    }

    public void setContextInfo(ContextInfo contextInfo) {
        this.contextInfo = contextInfo;
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
