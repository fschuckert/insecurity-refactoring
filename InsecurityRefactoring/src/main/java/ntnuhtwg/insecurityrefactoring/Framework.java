/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring;

import ntnuhtwg.insecurityrefactoring.base.info.DataflowPathInfo;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import ntnuhtwg.insecurityrefactoring.base.info.ContextInfo;
import ntnuhtwg.insecurityrefactoring.base.GlobalSettings;
import ntnuhtwg.insecurityrefactoring.base.info.ACIDTree;
import ntnuhtwg.insecurityrefactoring.base.RefactoredCode;
import ntnuhtwg.insecurityrefactoring.base.SourceLocation;
import ntnuhtwg.insecurityrefactoring.base.Util;
import ntnuhtwg.insecurityrefactoring.base.ast.TimeoutNode;
import ntnuhtwg.insecurityrefactoring.base.context.SufficientFilter;
import ntnuhtwg.insecurityrefactoring.base.exception.TimeoutException;
import ntnuhtwg.insecurityrefactoring.base.tree.DFATreeNode;
import ntnuhtwg.insecurityrefactoring.base.db.joern.Prepare;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.Neo4JConnector;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.Neo4jDB;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.dsl.cypher.DataflowDSL;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.DataflowPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SanitizePattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SinkPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.PatternStorage;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SourcePattern;
import ntnuhtwg.insecurityrefactoring.refactor.analyze.ACIDAnalyzer;
import ntnuhtwg.insecurityrefactoring.refactor.acid.ACIDTreeCreator;
import ntnuhtwg.insecurityrefactoring.base.tools.CodeFormatter;
import ntnuhtwg.insecurityrefactoring.base.tools.GitUtil;
import ntnuhtwg.insecurityrefactoring.refactor.analyze.ACIDContextAnalyzer;
import ntnuhtwg.insecurityrefactoring.refactor.InsecurityRefactoring;
import ntnuhtwg.insecurityrefactoring.refactor.PIPFinder;
import ntnuhtwg.insecurityrefactoring.refactor.temppattern.ScanProgress;
import ntnuhtwg.insecurityrefactoring.refactor.temppattern.MissingCall;
import ntnuhtwg.insecurityrefactoring.refactor.temppattern.TempPattern;
import org.javatuples.Pair;
import org.javatuples.Triplet;

/**
 *
 * @author blubbomat
 */
public class Framework {

    private Prepare prepare = new Prepare();

    private PatternStorage patternStorage;

    private InsecurityRefactoring refactoring;
    private PIPFinder pipFinder;
    List<RefactoredCode> refactoredFiles = new LinkedList<>();
    private String path = null;

//    private List<DFATreeNode> pips = new LinkedList<>();
    private Neo4jDB db;

    public void init() throws IOException {
        patternStorage = new PatternStorage();
        patternStorage.readPatterns(GlobalSettings.patternFolder);
        pipFinder = new PIPFinder();
    }

    public void closeDB() {
        try {
            if (db != null) {
                System.out.println("Closing db...");
                db.close();
                db = null;
            }
        } catch (Exception ex) {
            System.out.println("Cannot close db:" + ex.getMessage());
        }

    }

    public void scan(String path, boolean prepareDB, List<String> specificPatterns, ScanProgress scanProgress, boolean controlFlowCheck, SourceLocation specificLocation) {
        this.path = path;
        try {
            if (prepareDB) {
                boolean databasePrepared = prepare.prepareDatabase(path, scanProgress);
                if (!databasePrepared) {
                    System.out.println("Unable to prepare database. Probably a JOERN ERROR");
                    return;
                }
            } else {
                scanProgress.joernScanned();
                scanProgress.joernImported();
            }
            printPatternInfo();
            findPips(specificPatterns, scanProgress, controlFlowCheck, specificLocation);
        } catch (Exception ex) {
            System.out.println("Something went wrong...");
            ex.printStackTrace();
        }
    }

    public void analyze(ACIDTree pipInformation, DataflowPathInfo source) {
        ACIDAnalyzer analyzer = new ACIDAnalyzer(patternStorage, db);
        try {
            analyzer.analyse(pipInformation, source);
        } catch (TimeoutException ex) {
            Logger.getLogger(Framework.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void findPips(List<String> specificPatterns, ScanProgress scanProgress, boolean controlFlowCheck, SourceLocation specificLocation) {
        connectDB();
        patternStorage.setTempPatterns(Collections.EMPTY_LIST);
        pipFinder.findPips(db, patternStorage, specificPatterns, scanProgress, controlFlowCheck, specificLocation);

        refactoring = new InsecurityRefactoring(getDSL(), patternStorage);
    }

    public boolean isFoundPip(boolean requiresSanitize, boolean debugAddAll) {
        return !pipFinder.getPips(db, requiresSanitize, debugAddAll).isEmpty();

    }

    public List<DFATreeNode> getPips(boolean requiresSanitize, boolean debugAddAll) {
        return pipFinder.getPips(db, requiresSanitize, debugAddAll);
    }

    public List<ACIDTree> getPipInformation() {
        List<ACIDTree> retval = new LinkedList<>();

        List<DFATreeNode> pips = getPips(false, false);

        for (DFATreeNode sink : pips) {
            ACIDTree pipInformation = new ACIDTree(sink);
            for (DFATreeNode source : sink.getAllLeafs_()) {
                try {
                    if (!patternStorage.isSource(source, db)) {
                        continue;
                    }
                    DataflowPathInfo pathInformation = getPipPathInformation(source);
                    pipInformation.addPossibleSource(pathInformation);
                } catch (TimeoutException ex) {
                    Logger.getLogger(Framework.class.getName()).log(Level.SEVERE, null, ex);
                    continue;
                }
            }

            retval.add(pipInformation);
        }

        return retval;
    }

    public Map<MissingCall, Integer> getMissingCalls() {
        return pipFinder.getMissingCalls();
    }

    public PatternStorage getPatternStorage() {
        return patternStorage;
    }

    public Neo4jDB getDb() {
        return db;
    }

    public DataflowDSL getDSL() {
        return new DataflowDSL(db);
    }

    public void commitAndPush(String commitMsg) {
        File path = new File(this.path);
        String folderPath = this.path;
        if (!path.isDirectory()) {
            folderPath = path.getParentFile().getAbsolutePath();
        }

        GitUtil.commitAndPush(commitMsg, folderPath);
    }

    public void selectedRefactoring(List<Triplet<DFATreeNode, SanitizePattern, SanitizePattern>> refactorTargets, List< Pair<DFATreeNode, DataflowPattern>> dataflowRefactors, Pair<DFATreeNode, SourcePattern> sourceExchange) throws TimeoutException {
        this.refactoredFiles = refactoring.refactor(refactorTargets, dataflowRefactors, sourceExchange);
    }

    /**
     * scans each sub folder (should be different projects) creates a textfile
     * storing the numbers of pips found
     *
     * @param folder folder containing the subfolders (projects)
     */
    public void scanSubFolders(String folderPath, List<String> specificPatterns, boolean requiresSanitize, ScanProgress scanProgress, boolean controlFlowCheck) {
        File folder = new File(folderPath);

        for (File subFolder : folder.listFiles()) {
            if (subFolder.isDirectory()) {
                scan(subFolder.getAbsolutePath(), true, specificPatterns, scanProgress, controlFlowCheck, null);

                String resultFilePath = folderPath + "/" + subFolder.getName() + ".txt";
                File resultsFile = new File(resultFilePath);
                try {
                    resultsFile.createNewFile();
                    FileWriter myWriter = new FileWriter(resultFilePath);
                    myWriter.write("FOUND PIPS: " + getPips(requiresSanitize, false).size());
                    myWriter.close();
                } catch (IOException iOException) {
                    System.out.println("Cannot write results!" + iOException.getMessage());
                }
            }
        }
    }

    private Neo4jDB waitForDB() {
        while (true) {
            System.out.println("Waiting for db");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
//                Logger.getLogger(Framework.class.getName()).log(Level.SEVERE, null, ex);
            }
            closeDB();
            db = new Neo4JConnector("bolt://localhost:7687", "neo4j", "admin");
            if (db.checkConnection()) {
                return db;
            }
        }
    }

    public List<DataflowPathInfo> getSourceNodes(DFATreeNode dfaRootNode) {
        try {
            return ACIDTreeCreator.getSourceNodes(dfaRootNode, patternStorage, db);
        } catch (TimeoutException ex) {
            Logger.getLogger(Framework.class.getName()).log(Level.SEVERE, null, ex);
            return List.of();
        }
    }

    public void connectDB() {
        try {
            prepare.startDB();
        } catch (IOException ex) {
            Logger.getLogger(Framework.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(Framework.class.getName()).log(Level.SEVERE, null, ex);
        }
        db = waitForDB();
    }

    public void rescan(List<TempPattern> tempPattern, ScanProgress scanProgress, boolean controlFlowCheck) {
        patternStorage.setTempPatterns(tempPattern);
        pipFinder.rescan(db, patternStorage, tempPattern, scanProgress, controlFlowCheck);
    }

    public Iterable<Map.Entry<SinkPattern, Integer>> getSinkCount() {
        return pipFinder.getSinkCount().entrySet();
    }

    public List<DFATreeNode> getSpecificPips(SinkPattern selectedPattern) {
        List<DFATreeNode> retval = new LinkedList<>();
        for (DFATreeNode sink : pipFinder.getAllResults()) {
            try {
                if (selectedPattern.equalsPattern(sink.getObj(), db)) {
                    retval.add(sink);
                }
            } catch (TimeoutException ex) {
                Logger.getLogger(Framework.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return retval;
    }

    private DataflowPathInfo getPipPathInformation(DFATreeNode source) throws TimeoutException {
//        Analyze analyze = new Analyze(patternStorage, db);

        boolean containsTimeout = false;
        DFATreeNode toCheck = source;

        while (toCheck != null) {
            if (toCheck.getObj() instanceof TimeoutNode) {
                containsTimeout = true;
            }

            toCheck = toCheck.getParent_();
        }

        return new DataflowPathInfo(source, containsTimeout);
    }

    private void printPatternInfo() {
        System.out.println("Complete: ");
        System.out.println("Sources: " + patternStorage.getSources().size());
        System.out.println("Sinks: " + patternStorage.getSinks().size());
        System.out.println("Sinks (XSS): " + patternStorage.getSinks().stream().filter(s -> "xss".equals(s.getVulnType())).collect(Collectors.toList()).size());
        System.out.println("Sinks (SQLi): " + patternStorage.getSinks().stream().filter(s -> "sqli".equals(s.getVulnType())).collect(Collectors.toList()).size());
        System.out.println("Sinks (eval): " + patternStorage.getSinks().stream().filter(s -> "eval".equals(s.getVulnType())).collect(Collectors.toList()).size());
        System.out.println("Sinks (unserialize): " + patternStorage.getSinks().stream().filter(s -> "unserialize".equals(s.getVulnType())).collect(Collectors.toList()).size());
        System.out.println("Sanitization:" + patternStorage.getSanitizations().size());
        System.out.println("Passthrought: " + patternStorage.getPassthroughs().size());

        System.out.println("Specific: ");
        ;
    }

    public List<RefactoredCode> getRefactoredCode() {
        return refactoredFiles;
    }

    public void writeToDisk(boolean backupFiles) {
        if (backupFiles) {
            // todo
        }
        for (RefactoredCode refactoredCode : refactoredFiles) {
            try {
                String path = refactoredCode.getSourceLocation().getPath();
                System.out.println("Writing file: " + path);
                FileWriter fileWriter = new FileWriter(path, false);
                System.out.println("code : \n" + refactoredCode.getCode());
                fileWriter.write(refactoredCode.getCode());
                fileWriter.flush();
                fileWriter.close();

            } catch (IOException ex) {
                Logger.getLogger(Framework.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void formatCode() {
        CodeFormatter.formatFolder(path);
    }

    public void stop() {
        closeDB();
        shutdownDB();
    }

    private void shutdownDB() {
        File rootFolder = new File(GlobalSettings.rootFolder);
        String execute = "./stop_neo4j.sh";

        System.out.println("Shutting down Neo4j...");
        try {
            Util.runCommand(execute, rootFolder);
        } catch (IOException ex) {
            Logger.getLogger(Framework.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(Framework.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean isDBRunning() {
        try {
            return db != null && db.checkConnection();
        } catch (Exception ex) {
            return false;
        }
    }

}
