/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.patterns;

import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SanitizePattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SourcePattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.LanguagePattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.DataflowPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SinkPattern;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import ntnuhtwg.insecurityrefactoring.base.ASTNodeTypes;
import ntnuhtwg.insecurityrefactoring.base.Util;
import ntnuhtwg.insecurityrefactoring.base.ast.ASTFactory;
import ntnuhtwg.insecurityrefactoring.base.ast.impl.AstVar;
import ntnuhtwg.insecurityrefactoring.base.context.SufficientFilter;
import ntnuhtwg.insecurityrefactoring.base.exception.TimeoutException;
import ntnuhtwg.insecurityrefactoring.base.tree.DFATreeNode;
import ntnuhtwg.insecurityrefactoring.base.tree.TreeNode;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.Neo4jDB;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.dsl.cypher.DataflowDSL;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;
import ntnuhtwg.insecurityrefactoring.base.info.ContextInfo;
import ntnuhtwg.insecurityrefactoring.base.patternpersist.ListPatternParser;
import ntnuhtwg.insecurityrefactoring.base.patterns.PassthroughPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.Pattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.ConcatPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.DataflowIdentifyPattern;
import ntnuhtwg.insecurityrefactoring.base.patternpersist.JsonPatternParser;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.ContextPattern;
import ntnuhtwg.insecurityrefactoring.refactor.temppattern.TempPattern;

/**
 *
 * @author blubbomat
 */
public class PatternStorage {
    
    private JsonPatternParser jsonPatternParser = new JsonPatternParser();
    
    // S_{dst} / D_{dst}
    private Map< String, SinkPattern> sinks = new HashMap<>(); 
    // S_{src} / S_{src}
    private Map< String, SourcePattern> sources = new HashMap<>(); 
    
    // C
    private Map< String, ConcatPattern> concats = new HashMap<>();
    
    // S_{san}
    private Map< String, SanitizePattern> sanitizations = new HashMap<>();    
    
    // S_{pass}
    private Map< String, PassthroughPattern> passthroughs = new HashMap<>();
    
    // m*
    private Map< String, DataflowPattern> dataflows = new HashMap<>();    
    // m
    private Map< String, DataflowIdentifyPattern> dataflowIdentifies = new HashMap<>();
    
    
    private Map< String, LanguagePattern> languagePatterns = new HashMap<>();
    
    
    private Map< String, ContextPattern> contexts = new HashMap<>();   
    
    // Temporary for manual injecting vulnrabilities -> advanced analysis
    private List<SourcePattern> tempSources = new LinkedList<>();
    private List<PassthroughPattern> tempPassthroughPatterns = new LinkedList<>();
    
    private void parsePattern(File file){
        try {
//            System.out.println("Parsing pattern: " + file);
            
            if(file.getAbsolutePath().endsWith(".json")){ 
                try {
                    for(Pattern pattern : jsonPatternParser.parseJsonPattern(file, this)){
                        addToPatterns(pattern);
                    }
                } catch (Exception ex) {
                    System.out.println(" <<<FAILED!>>> on : " + file);
                    Logger.getLogger(PatternStorage.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else if(file.getAbsolutePath().endsWith(".call.list")){
                String patternType = null;
                if(file.getName().startsWith("sink")){
                    patternType = "sink";
                }
                else if(file.getName().startsWith("sanitize")){
                    patternType = " sanitize";
                }
                else{
                    System.out.println("Failed to load a call list: " + file.getAbsolutePath());
                    return;
                }
                
                List<Pattern> patterns = new ListPatternParser().parsePatterns(file.getAbsolutePath(), patternType);
                for(Pattern pattern : patterns){
//                    System.out.print("\n   ---->" + pattern.getName());
                    pattern.setPatternStorage(this);
                    addToPatterns(pattern);
                }
//                System.out.println("\nFinished");
            }
            
        } catch (IOException ex) {
            Logger.getLogger(PatternStorage.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }

    private boolean addToPatterns(Pattern pattern) {
        switch (pattern.getType()) {
            case "language":
                LanguagePattern langPattern = (LanguagePattern)pattern;
                if (languagePatterns.containsKey(langPattern.getId())) {
                    System.out.println(" <<< FAILED >>> id already load: " + langPattern.getId());
                    return true;
                }
                languagePatterns.put(langPattern.getId(), (LanguagePattern)pattern);
                break;
            case "dataflow":
                if (dataflows.containsKey(pattern.getName())) {
                    System.out.println(" <<< FAILED >>> pattern already load: " + pattern.getName());
                    return true;
                }
                dataflows.put(pattern.getName(), (DataflowPattern)pattern);
                break;
            case "concatenation":
                if(concats.containsKey(pattern.getName())){
                    System.out.println(" <<< FAILED >>> pattern already load: " + pattern.getName());
                }
                concats.put(pattern.getName(), (ConcatPattern)pattern);
                break;
            case "sink":
                if (sinks.containsKey(pattern.getName())) {
                    System.out.println(" <<< FAILED >>> pattern already load: " + pattern.getName());
                    return true;
                }
                sinks.put(pattern.getName(), (SinkPattern)pattern);
                break;
            case "source":
                if (sources.containsKey(pattern.getName())) {
                    System.out.println(" <<< FAILED >>> pattern already load: " + pattern.getName());
                    return true;
                }
                sources.put(pattern.getName(), (SourcePattern)pattern);
                break;
            case "sanitize":
                if (sanitizations.containsKey(pattern.getName())) {
                    System.out.println(" <<< FAILED >>> pattern already load: " + pattern.getName());
                    return true;
                }
                sanitizations.put(pattern.getName(), (SanitizePattern)pattern);
                break;
            case "dataflow_identify":
                if (dataflowIdentifies.containsKey(pattern.getName())) {
                    System.out.println(" <<< FAILED >>> pattern already load: " + pattern.getName());
                    return true;
                }
                dataflowIdentifies.put(pattern.getName(), (DataflowIdentifyPattern)pattern);
                break;
            case "context":
                if (contexts.containsKey(pattern.getName())) {
                    System.out.println(" <<< FAILED >>> pattern already load: " + pattern.getName());
                    return true;
                }
                contexts.put(pattern.getName(), (ContextPattern)pattern);
                break;
        }
        if(pattern instanceof PassthroughPattern){
            if(((PassthroughPattern) pattern).isPassthrough() && !pattern.isForGenerate()) {
                passthroughs.put(pattern.getName(), (PassthroughPattern)pattern);
            }
        }
        return false;
    }
    
    private void parseAllPatterns(final File file) {
    for (final File fileEntry : file.listFiles()) {
        if (fileEntry.isDirectory()) {
            parseAllPatterns(fileEntry);
        } else {
            parsePattern(fileEntry);
        }
    }
    
    }
    
    public void readPatterns(String folder) throws IOException{
        sinks.clear();
        sources.clear();
//        dataflows.clear();
        sanitizations.clear();
        languagePatterns.clear();
        parseAllPatterns(new File(folder));
    }

    public Collection<SinkPattern> getSinks() {
        return sinks.values();
    }

    public Collection<SourcePattern> getSources() {
        if(tempSources.isEmpty()){
            return sources.values();
        }
        
        List<SourcePattern> retval = new LinkedList<>();
        retval.addAll(sources.values());
        retval.addAll(tempSources);
        return retval;
    }

    public Collection<SanitizePattern> getSanitizations() {
        return sanitizations.values();
    }

    public Collection<PassthroughPattern> getPassthroughs() {
        if(tempPassthroughPatterns.isEmpty()){
            return passthroughs.values();
        }
        
        List<PassthroughPattern> retval = new LinkedList<>();
        retval.addAll(passthroughs.values());
        retval.addAll(tempPassthroughPatterns);        
        return retval;
    }

    public Collection<DataflowPattern> getDataflows() {
        return dataflows.values();
    }
    
    public Collection<ConcatPattern> getConcatPatterns(){
        return concats.values();
    }

    public Collection<LanguagePattern> getLanguagePatterns() {
        return languagePatterns.values();
    }

    public Collection<DataflowIdentifyPattern> getDataflowIdentifies() {
        return dataflowIdentifies.values();
    }
    
    public Collection<ContextPattern> getContexts(){
        return contexts.values();
    }
    
    public List<SourcePattern> getInsecureSources(SourcePattern sourcePattern, DFATreeNode source, ContextInfo contextInfo){
        List<SourcePattern> insecureSources = new LinkedList<>();
        for(SourcePattern sourcePatternToCheck : getSources()){
            if(!sourcePattern.isReplacableWith(sourcePatternToCheck)){
                continue;
            }
            
            
            if(!SufficientFilter.isSufficient(sourcePatternToCheck.getCharsAllowed(), contextInfo, sourcePatternToCheck.getAddsEnclosure()).isExploitable()){
                continue;
            }
            
            insecureSources.add(sourcePatternToCheck);
        }
        
        return insecureSources;
    }
    
    
    
    public List<SanitizePattern> getPossibleFailedSanitizePatterns(SanitizePattern sanitizePattern, DFATreeNode node, ContextInfo contextInfo){
        List<SanitizePattern> retval = new LinkedList<>();
        
        System.out.println("Checking for " + sanitizePattern);
        for(SanitizePattern potentialFailedSan : getSanitizations()){
            if(!potentialFailedSan.isReplaceableWithIgnoreDatatype(sanitizePattern)){
                continue;
            }  
            
//            if(!potentialFailedSan.isReplaceableWith(sanitizePattern)){
//                continue;
//            }   
            
            if(potentialFailedSan.containsAny()){
                continue;
            }
            
            if(sanitizePattern.isCheckMethod() != potentialFailedSan.isCheckMethod()){
                continue;
            }
            
            if(!SufficientFilter.isSufficient(potentialFailedSan.getCharsAllowed(), contextInfo, potentialFailedSan.getAddsEnclosure()).isExploitable()){
                continue;
            }
            
//            if(Sufficient.isSufficient(potentialFailedSan, contextInfo)){
//                continue;
//            }
            
            retval.add(potentialFailedSan);
        }

        
        return retval;
    }
    
    
    public DataflowPattern getDataflow(String name) {
        return dataflows.get(name);
    }
    
    

    public void setTempPatterns(List<TempPattern> tempPatterns) {
        tempSources.clear();
        tempPassthroughPatterns.clear();
        
        for(TempPattern tempPattern : tempPatterns){
            SourcePattern sourcePattern = tempPattern.getSourcePattern();
            if(sourcePattern != null){
                sourcePattern.setPatternStorage(this);
                tempSources.add(sourcePattern);
            }
            
            PassthroughPattern passthroughPattern = tempPattern.getPassthroughPattern();
            if(passthroughPattern != null){
                Pattern pattern = (Pattern)passthroughPattern;
                pattern.setPatternStorage(this);
                tempPassthroughPatterns.add(passthroughPattern);
            }
        }
    }

    public LanguagePattern getLanguagePattern(String identifier) {
        return languagePatterns.get(identifier);
    }

    public DataflowIdentifyPattern getDataflowIdentify(String identifyPattern) {
        return dataflowIdentifies.get(identifyPattern);
    }

    public SinkPattern getSinkPattern(String specificPattern) {
        return sinks.get(specificPattern);
    }

    public boolean isSource(DFATreeNode source, Neo4jDB db) throws TimeoutException {
        for(SourcePattern sourcePattern : sources.values()){
            if(sourcePattern.equalsPattern(source.getObj(), db)){
                return true;
            }
        }
        
        return false;
    }

    public boolean isSanitize(DFATreeNode node, Neo4jDB db) throws TimeoutException {
        for(SanitizePattern sanitizePattern : sanitizations.values()){
            if(sanitizePattern.isForGenerate()){
                continue;
            }
            if(sanitizePattern.equalsPattern(node.getObj(), db)){
                return true;
            }
        }
        
        return false;
    }

    public ConcatPattern isConcatenation(DFATreeNode node, DataflowDSL dsl)  throws TimeoutException {
        Neo4jDB db = dsl.getDb();
        for(ConcatPattern concatPattern : concats.values()){
            if(concatPattern.equalsPattern(node.getObj(), db)){
                return concatPattern;
            }
        }
        
        return null;
    }

    public PassthroughPattern isPassthrough(DFATreeNode call, DataflowDSL dsl) throws TimeoutException {
        Neo4jDB db = dsl.getDb();
        for(PassthroughPattern passthrough : passthroughs.values()){
            if(passthrough.equalsPattern(call.getObj(), db)){
                return passthrough;
            }
        }
        
        return null;
    }

    public boolean isPassthroughType(INode obj, DataflowDSL dsl) {
        return Util.isType(obj, ASTNodeTypes.CAST);
    }

    

    
 
    
    
    
    

    
    
    
    
    
    
    
}
