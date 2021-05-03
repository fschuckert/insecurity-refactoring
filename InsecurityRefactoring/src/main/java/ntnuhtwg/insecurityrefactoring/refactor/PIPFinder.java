/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.refactor;

import ntnuhtwg.insecurityrefactoring.refactor.acid.ACIDTreeCreator;
import ntnuhtwg.insecurityrefactoring.refactor.base.ScanProgress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;
import ntnuhtwg.insecurityrefactoring.base.ASTNodeTypes;
import ntnuhtwg.insecurityrefactoring.base.SourceLocation;
import ntnuhtwg.insecurityrefactoring.base.Util;
import ntnuhtwg.insecurityrefactoring.base.exception.TimeoutException;
import ntnuhtwg.insecurityrefactoring.base.tree.DFATreeNode;
import ntnuhtwg.insecurityrefactoring.base.tree.LabeledTreeNode;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.Neo4jDB;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.dsl.cypher.DataflowDSL;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SanitizePattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SinkPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.PatternStorage;
import ntnuhtwg.insecurityrefactoring.refactor.base.MissingCall;
import ntnuhtwg.insecurityrefactoring.refactor.base.TempPattern;
import scala.NotImplementedError;

/**
 *
 * @author blubbomat
 */
public class PIPFinder {
    
//    private List<DFATreeNode> pips = new LinkedList<>();
    private ArrayList<DFATreeNode> allResults = new ArrayList<>();
    private Map<MissingCall, Integer> missingCalls = new HashMap<>();
    private PatternStorage patternStorage;
    private Map<SinkPattern, Integer> sinkCount = new HashMap<>();
    
    
    public void rescan(Neo4jDB db, PatternStorage patternStorage, List<TempPattern> tempPatterns, ScanProgress scanProgress, boolean controlFlowCheck){
        List<ACIDTreeCreator> backwardDataflows = new LinkedList<>();
        DataflowDSL dsl = new DataflowDSL(db);
        
        int i =0;
        for(DFATreeNode result : allResults){
            ACIDTreeCreator backwardDataflow =null;
            try {
                backwardDataflow = requiresRescan(result, dsl, tempPatterns, db, patternStorage, i, controlFlowCheck);
            } catch (TimeoutException ex) {
                ex.printErrorMessage("Rescan check failed");
            }
            if(backwardDataflow != null){
                backwardDataflows.add(backwardDataflow);
            }
            i++;
        }
        
        executeDataflows(backwardDataflows, scanProgress);
        
        for(ACIDTreeCreator backwardDataflow : backwardDataflows){
            allResults.set(backwardDataflow.getReplaceIndex(), backwardDataflow.getResultTree());
        }
        
    }

    private ACIDTreeCreator requiresRescan(DFATreeNode result, DataflowDSL dsl, List<TempPattern> tempPatterns, Neo4jDB db, PatternStorage patternStorage1, int i, boolean controlFlowCheck) throws TimeoutException {
        for (LabeledTreeNode leaflabeled : result.getAllLeafs()) {
            DFATreeNode leaf = (DFATreeNode)leaflabeled;
            if (Util.isAnyCall(leaf.getObj())) {
                String callName = dsl.getCallName(leaf.getObj(), false);
                for (TempPattern tempPattern : tempPatterns) {
                    if (tempPattern.getPassthroughPattern() != null && tempPattern.getMissingCall().getName().equals(callName)) {
                        System.out.println("add to rescan" + callName);
                        ACIDTreeCreator backwardDataflow = new ACIDTreeCreator(db, patternStorage1, result.getObj(), result.getSinkPattern(), controlFlowCheck);
                        backwardDataflow.setReplaceIndex(i);
                        return backwardDataflow;
                    }
                }
            }
        }
        return null;
    }
    
    private void executeDataflows(List<ACIDTreeCreator> backwardDataflows, ScanProgress scanProgress){         
            ExecutorService executor = Executors.newFixedThreadPool(4);
//            executor.
//            ThreadPoolExecutor executor = ;
            
//            executor.
            List<Future> futures = new LinkedList<>();
            for(ACIDTreeCreator backwardDataflow : backwardDataflows){                
                Future future = executor.submit(backwardDataflow);
                futures.add(future);
            }
            
            executor.shutdown();
           
            while(!executor.isTerminated()){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(PIPFinder.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                int sinksScanned = 0;
                for(Future future : futures){
                    if(future.isDone()){
                        sinksScanned++;
                    }
                }
                scanProgress.setSinkScanned(sinksScanned);
//                executor.
            }
    }
    
    public void findPips(Neo4jDB db, PatternStorage patternStorage, List<String> specificPatterns, ScanProgress scanProgress, boolean controlFlowCheck, SourceLocation specificLocation){
        allResults.clear();
        missingCalls.clear();
        sinkCount.clear();
        DataflowDSL dsl = new DataflowDSL(db);
        this.patternStorage = patternStorage;
        
        Collection<SinkPattern> toScan = patternStorage.getSinks();
        
        if(specificPatterns != null && !specificPatterns.isEmpty()){
            toScan = new LinkedList<>();
            for(String specificPattern : specificPatterns){
                SinkPattern sinkPattern = patternStorage.getSinkPattern(specificPattern);
                if(sinkPattern == null){
                    throw new NotImplementedError("tried to scan a sink pattern that does not exists: " + specificPattern);
                }
                
                toScan.add(sinkPattern);
            }
        }
        List<ACIDTreeCreator> backwardDataflows = new LinkedList<>();
        scanProgress.setnSinkTypes(toScan.size());
        int sinksSearched =0;
        for(SinkPattern pattern : toScan){
//            System.out.print("Sink(" + pattern.getName() + "): ");

            List<INode> sinks = List.of();
            try {
                sinks = db.findAll(pattern.findQuery());
            } catch (TimeoutException ex) {
                ex.printErrorMessage("Too many sinks?? " + pattern);
                
            }
            if(!sinks.isEmpty()){
                System.out.println("Sink(" + pattern.getName() + "): " + sinks.size());
            }
            sinkCount.put(pattern, sinks.size());

            for(INode sink : sinks){
                if(specificLocation == null || Util.isPrePath(specificLocation, db, sink) ){
                    backwardDataflows.add(new ACIDTreeCreator(db, patternStorage, sink, pattern, controlFlowCheck));
                }
            }
            scanProgress.setSinksSearched(++sinksSearched);
        }
        scanProgress.setnSinks(backwardDataflows.size());
        executeDataflows(backwardDataflows, scanProgress);
            
        for(ACIDTreeCreator backwardDataflow : backwardDataflows){
            allResults.add(backwardDataflow.getResultTree());
        }
        
        System.out.println("Scanned : " + allResults.size());
        
        System.out.println("Checking missing methods/functions");
        for(DFATreeNode result : allResults){
            for(LabeledTreeNode leaflabeled : result.getAllLeafs()){
                try{
                    DFATreeNode leaf = (DFATreeNode)leaflabeled;
                    if(Util.isAnyCall(leaf.getObj())){
                        int paramCount = dsl.getCallParameters(leaf.getObj()).size();
                        MissingCall missingCall = new MissingCall(leaf.getObj().getString("type"), dsl.getCallName(leaf.getObj(), false), paramCount);
                        if(!missingCalls.containsKey(missingCall)){
                            missingCalls.put(missingCall, 1);
                        }
                        else{
                            missingCalls.put(missingCall, missingCalls.get(missingCall) + 1);
                        }
                    }
                }catch(TimeoutException ex){
                    ex.printErrorMessage("Resolving missing function leafs");
                }
            }
        }
        
        
        for(Entry<MissingCall, Integer> entry : missingCalls.entrySet()){
            System.out.println( entry.getKey() + ":" + entry.getValue());
        }
    }
    
    public void setTempPatterns(List<TempPattern> tempPatterns){
        patternStorage.setTempPatterns(tempPatterns);
    }

    public List<DFATreeNode> getPips(Neo4jDB db, boolean requiresSanitize, boolean debugAddAll){
        LinkedList<DFATreeNode> pips = new LinkedList<>();
        for(DFATreeNode result : allResults){
            if(debugAddAll){
                pips.add(result);
                continue;
            }
            
            try{
            if(!ACIDTreeCreator.getSourceNodes(result, patternStorage, db).isEmpty()){
                if(!requiresSanitize || containsSanitize(db, result)){
                    pips.add(result);
                    continue;
                }
            }
            } catch(TimeoutException ex){
                ex.printErrorMessage("get pips");
            }
        }
        
        return pips;
    }
    
    private boolean containsSanitize(Neo4jDB db, DFATreeNode node) throws TimeoutException{
        for(SanitizePattern sanitizePattern : patternStorage.getSanitizations()){
            if(sanitizePattern.equalsPattern(node.getObj(), db)){
                return true;
            }
        }
        
        if(node.getParent_() == null){
            return false;
        }
        else{
            return containsSanitize(db, node.getParent_());
        }
    }

    public Map<SinkPattern, Integer> getSinkCount() {
        return sinkCount;
    }

    public Map<MissingCall, Integer> getMissingCalls() {
        return missingCalls;
    }

    public List<DFATreeNode> getAllResults() {
        return allResults;
    }
    
    
}
