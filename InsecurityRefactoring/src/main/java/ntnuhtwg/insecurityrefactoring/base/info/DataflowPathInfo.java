/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.info;

import java.util.LinkedList;
import java.util.List;
import ntnuhtwg.insecurityrefactoring.base.context.CharsAllowed;
import ntnuhtwg.insecurityrefactoring.base.context.SufficientFilter;
import ntnuhtwg.insecurityrefactoring.base.context.VulnerabilityDescription;
import ntnuhtwg.insecurityrefactoring.base.context.enclosure.Enclosure;
import ntnuhtwg.insecurityrefactoring.base.exception.NotExpected;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SanitizePattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SourcePattern;
import ntnuhtwg.insecurityrefactoring.base.tree.DFATreeNode;
import org.javatuples.Pair;

/**
 *
 * @author blubbomat
 */
public class DataflowPathInfo {
    final DFATreeNode dataflowSource;
    final boolean containsTimeout;
    
    final List<Pair<SanitizePattern, DFATreeNode>> sanitizeNodes = new LinkedList<>();
    
    private ContextInfo contextInfo;

    public DataflowPathInfo(DFATreeNode source, boolean containsTimeout) {
        this.dataflowSource = source;
        this.containsTimeout = containsTimeout;
    }

    public DFATreeNode getSource() {
        return dataflowSource;
    }

    public boolean isContainsTimeout() {
        return containsTimeout;
    }

    

    public ContextInfo getContextInfo() {
        return contextInfo;
    }

    public void setContextInfo(ContextInfo contextInfo) {
        this.contextInfo = contextInfo;
    }

    public List<Pair<SanitizePattern, DFATreeNode>> getSanitizeNodes() {
        return sanitizeNodes;
    }
    
    public CharsAllowed getMergedAllowedChars(){
        CharsAllowed mergedAllows = new CharsAllowed();
        
        mergedAllows.mergeFromAnother(getSource().getSourcePattern().getCharsAllowed());
        
        for(Pair<SanitizePattern, DFATreeNode> sanitize : sanitizeNodes){
            mergedAllows.mergeFromAnother(sanitize.getValue0().getCharsAllowed());
        }
        return mergedAllows;
    }
    
    public List<Enclosure> getMergedEnclosureList(){
        LinkedList<Enclosure> addsEnclosureMerged = new LinkedList<>();
        if(dataflowSource.getSourcePattern().getAddsEnclosure() != null){
            addsEnclosureMerged.add(dataflowSource.getSourcePattern().getAddsEnclosure());
        }
        
        for(Pair<SanitizePattern, DFATreeNode> sanitize : sanitizeNodes){
            addsEnclosureMerged.add(sanitize.getValue0().getAddsEnclosure());
        }
        return addsEnclosureMerged;
    }

    public VulnerabilityDescription getVulnerabilityInfo() {      
        return SufficientFilter.isSufficient(getMergedAllowedChars(), contextInfo, getMergedEnclosureList());
    }
    
    
    
    
    
    
    
    
}
