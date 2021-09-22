/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.constructor;

import bibliothek.util.Path;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import ntnuhtwg.insecurityrefactoring.base.ast.ASTFactory;
import ntnuhtwg.insecurityrefactoring.base.ast.impl.AstStmtList;
import ntnuhtwg.insecurityrefactoring.base.ast.impl.AstString;
import ntnuhtwg.insecurityrefactoring.base.exception.GenerateException;
import ntnuhtwg.insecurityrefactoring.base.tree.TreeNode;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;
import ntnuhtwg.insecurityrefactoring.base.ASTType;
import ntnuhtwg.insecurityrefactoring.base.DataType;
import ntnuhtwg.insecurityrefactoring.base.SourceLocation;
import ntnuhtwg.insecurityrefactoring.base.Util;
import ntnuhtwg.insecurityrefactoring.base.context.CharsAllowed;
import ntnuhtwg.insecurityrefactoring.base.context.SufficientFilter;
import ntnuhtwg.insecurityrefactoring.base.context.VulnerabilityDescription;
import ntnuhtwg.insecurityrefactoring.base.context.enclosure.Enclosure;
import ntnuhtwg.insecurityrefactoring.base.exception.NotExpected;
import ntnuhtwg.insecurityrefactoring.base.info.ContextInfo;
import ntnuhtwg.insecurityrefactoring.base.info.DataflowPathInfo;
import ntnuhtwg.insecurityrefactoring.base.patterns.GenerateFile;
import ntnuhtwg.insecurityrefactoring.base.patterns.Pattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.DataflowPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.PatternStorage;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.ContextPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.LanguagePattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SanitizePattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SinkPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SourcePattern;
import ntnuhtwg.insecurityrefactoring.base.stats.StringCounter;
import ntnuhtwg.insecurityrefactoring.base.tree.DFATreeNode;
import ntnuhtwg.insecurityrefactoring.print.PrintAST;
import org.javatuples.Pair;
import org.neo4j.cypher.internal.runtime.RelationshipIterator;

/**
 *
 * @author blubbomat
 */
public class CodeSampleCreator {

    PatternStorage patternStorage;
    PrintAST printAST = new PrintAST();

    private int toPrintSteps = 1000;
    private boolean onlyCount = true;

    private boolean createManifest = true;
    private boolean createGenerateFiles = true;
    private Map<String, String> onlyPattern = new HashMap<>();

    private String splitBy = "";

    private boolean sampleInfo = true;
    private boolean debugInfo = false; // will probably trigger XSS reports!

    public CodeSampleCreator(PatternStorage patternStorage) {
        this.patternStorage = patternStorage;
    }

    public void setCreateManifest(boolean createManifest) {
        this.createManifest = createManifest;
    }

    public void setCreateGenerateFiles(boolean createGenerateFiles) {
        this.createGenerateFiles = createGenerateFiles;
    }

    public void setSplitBy(String patternTypeName) {
        this.splitBy = patternTypeName;
    }

    public void addGenerateOnlyPattern(String patternType, String patternName) {
        onlyPattern.put(patternType, patternName);
    }

    public void setOnlyCount(boolean onlyCount) {
        this.onlyCount = onlyCount;
    }

    private <E extends Pattern> Collection<E> filterPatterns(Collection<E> listToFilter) {
        if (listToFilter.isEmpty()) {
            return listToFilter;
        }

        E obj = listToFilter.iterator().next();
        if (onlyPattern.containsKey(obj.getType())) {
            String patternName = onlyPattern.get(obj.getType());
            for (E pattern : listToFilter) {
                if (pattern.getName().equals(patternName)) {
                    return Arrays.asList(pattern);
                }
            }

            return Collections.EMPTY_LIST;
        }

        return listToFilter;
    }
    
    private String pathSafeName(String name){
        String folder = name;
        
        folder = folder.replace(" ", "");
        folder = folder.replace("%", "");
        folder = folder.replace("<", "");
        folder = folder.replace(">", "");
        folder = folder.replace(":", "c");
        folder = folder.replace("?", "");
        folder = folder.replace("|", "");
        folder = folder.replace(";", "s");
        folder = folder.replace("'", "a");
        folder = folder.replace("\"", "q");
        folder = folder.replace("$", "D");
        folder = folder.replace("*", "s");
        folder = folder.replace("^", "d");
        folder = folder.replace("/", "");
        folder = folder.replace("\\", "");
        
        return folder;
    }

    public void createAllPossibleSamples(String storePath) {

        HashSet<String> sources = new HashSet<>();
        HashSet<String> sanitization = new HashSet<>();
        HashSet<String> dataflow = new HashSet<>();
        HashSet<String> context = new HashSet<>();
        HashMap<String, HashSet<String>> contextType = new HashMap<>();
        contextType.put("xss", new HashSet<>());
        contextType.put("sqli", new HashSet<>());

        HashSet<String> sink = new HashSet<>();
        HashMap<String, HashSet<String>> sinkType = new HashMap<>();
        sinkType.put("xss", new HashSet<>());
        sinkType.put("sqli", new HashSet<>());

        StringCounter vulnTypes = new StringCounter();

        int sampleCount = 0;
        for (SourcePattern sourcePattern : filterPatterns(patternStorage.getSources())) {
            for (SanitizePattern sanitizePattern : filterPatterns(patternStorage.getSanitizations())) {
                for (DataflowPattern dataflowPattern : filterPatterns(patternStorage.getDataflows())) {
                    if (!dataflowPattern.getIdentifyPattern().equals("assignment")) {
                        continue;
                    }
                    for (ContextPattern contextPattern : filterPatterns(patternStorage.getContexts())) {
                        for (SinkPattern sinkPattern : filterPatterns(patternStorage.getSinks())) {
                            String splitBy = storePath + "/SCAN_" + getSplitFolder(contextPattern, sinkPattern, dataflowPattern, sourcePattern, sanitizePattern);
                            String folder = splitBy 
                                    + "/src_" + pathSafeName(sourcePattern.getName())
                                    + "/san_" + pathSafeName(sanitizePattern.getName())
                                    + "/df_" + pathSafeName(dataflowPattern.getName())
                                    + "/con_" + pathSafeName(contextPattern.getName())
                                    + "/dst_" + pathSafeName(sinkPattern.getName());
                

                            try {
                                List<VulnerabilityDescription> vulnDescriptions = createSample(contextPattern, sinkPattern, dataflowPattern, sourcePattern, sanitizePattern, folder);

                                if(vulnDescriptions.isEmpty()){
                                    continue;
                                }
                                sampleCount += 1;
                                if (sampleCount % toPrintSteps == 0) {
                                    System.out.println("Generated: " + sampleCount);
                                }

                                sources.add(sourcePattern.getName());
                                sanitization.add(sanitizePattern.getName());
                                dataflow.add(dataflowPattern.getName());
                                context.add(contextPattern.getName());
                                sink.add(sinkPattern.getName());

//                                for(VulnerabilityDescription desc : vulnDescriptions){
                                    vulnTypes.countString(sinkPattern.getVulnType());
//                                }

                                contextType.get(sinkPattern.getVulnType()).add(contextPattern.getName());
                                sinkType.get(sinkPattern.getVulnType()).add(sinkPattern.getName());
                            } catch (GenerateException ex) {
                                continue;
                            }

                        }

                    }
                }

            }
        }

        System.out.println("### Pattern usage:");
        System.out.println("Source: " + sources.size());
        System.out.println("Sanitization: " + sanitization.size());
        System.out.println("Dataflow: " + dataflow.size());
        for (Entry<String, HashSet<String>> entry : contextType.entrySet()) {
            System.out.println("\t" + entry.getKey() + ":" + entry.getValue().size());
        }
        System.out.println("Context: " + context.size());
        for (Entry<String, HashSet<String>> entry : sinkType.entrySet()) {
            System.out.println("\t" + entry.getKey() + ":" + entry.getValue().size());
        }
        System.out.println("Sink: " + sink.size());

        System.out.println("# Vuln Types");
        vulnTypes.prettyPrint(storePath);

        System.out.println("Samples: " + sampleCount);
        System.out.println("Written: " + written);
    }

    public List<VulnerabilityDescription> createSample(ContextPattern contextPattern, SinkPattern sinkPattern, DataflowPattern dataflowPattern, SourcePattern sourcePattern, SanitizePattern sanitizePattern, String folder) throws GenerateException {
        List<VulnerabilityDescription> retval = new LinkedList<>();

        String findSinkId = "###HEREISTHESINK###";
        if (!contextPattern.getVulnType().equals(sinkPattern.getVulnType())) {
            throw new GenerateException("Context and sink are not fitting");
        }
        if (contextPattern.containsAny()) {
            throw new GenerateException("Context contains any");
        }
        if (sinkPattern.containsAny()) {
            throw new GenerateException("Sink contains any");
        }
        if (dataflowPattern.containsAny()) {
            throw new GenerateException("dataflowPattern contains any");
        }
        if (sourcePattern.containsAny()) {
            throw new GenerateException("sourcePattern contains any");
        }
        if (contextPattern.containsAny()) {
            throw new GenerateException("Context contains any");
        }
        if (sanitizePattern.containsAny()) {
            throw new GenerateException("sanitizePattern contains any");
        }
        if (!dataflowPattern.getIdentifyPattern().equals("assignment")) {
            throw new GenerateException("Dataflow is not with assignemt identify");
        }

        if (onlyCount) {
            return null;
        }

        Set<String> defines = new HashSet<>();
        defines.addAll(sourcePattern.getDefines());
        defines.addAll(sanitizePattern.getDefines());
        defines.addAll(dataflowPattern.getDefines());
        defines.addAll(contextPattern.getDefines());
        defines.addAll(sinkPattern.getDefines());
        Set<String> depends_on = new HashSet<>();
        depends_on.addAll(sourcePattern.getDepends_on());
        depends_on.addAll(sanitizePattern.getDepends_on());
        depends_on.addAll(dataflowPattern.getDepends_on());
        depends_on.addAll(contextPattern.getDepends_on());
        depends_on.addAll(sinkPattern.getDepends_on());
        if (!checkDependencies(defines, depends_on)) {
            throw new GenerateException("Dependecies are missing");
        }

        for (GenerationData source : generateSourcesPermutations(sourcePattern)) {

            if (!sanitizePattern.getDataInputType().equalsAny(source.outputDatatype)) {
                continue;
            }

            VulnerabilityDescription vulnDescription = createVulnDescription(sourcePattern, sanitizePattern, contextPattern, sinkPattern);

            List< TreeNode< INode>> inits = new LinkedList<>();

            addInitCode(sinkPattern, inits);
            addInitCode(contextPattern, inits);
            addInitCode(dataflowPattern, inits);
            addInitCode(sanitizePattern, inits);
            addInitCode(sourcePattern, inits);

            GenerationData sanitization = generateSanitize(sanitizePattern, source.outputNode);

            GenerationData dataflow = generatePattern(sanitization, dataflowPattern, "dataflow");
            GenerationData context = generatePattern(dataflow, contextPattern, "context");

            GenerationData sink = generateSink(context, sinkPattern);
            sink.ast.get(0).setSpecial(findSinkId);

            TreeNode<INode> statementList = ASTFactory.createStatementList();
            statementList.addAll(source.ast);
            if (sanitizePattern.getDataOutputType().equals(DataType.Boolean())) {
                TreeNode<INode> ifStmtList = ASTFactory.createStatementList();

                TreeNode<INode> ifN = ASTFactory.createIfStatement(sanitization.ast.get(0), ifStmtList);
                statementList.addChild(ifN);

                ifStmtList.addAll(generateAssign("tainted", "sanitized"));
                ifStmtList.addAll(dataflow.ast);
                ifStmtList.addAll(context.ast);
                addSinkToStmtList(sinkPattern, sink, ifStmtList);

            } else {
                statementList.addAll(sanitization.ast);
                statementList.addAll(dataflow.ast);
                statementList.addAll(context.ast);
                addSinkToStmtList(sinkPattern, sink, statementList);
            }

            StringBuffer content = new StringBuffer();
            if (sampleInfo) {
                content.append("<!--\n");
                content.append("## Sample information ##\n");
                content.append("Source: ").append(sourcePattern.getName()).append(" ==> ").append(sourcePattern.getCharsAllowed().prettyPrint()).append("\n");
                content.append("Sanitization: ").append(sanitizePattern.getName()).append(" ==> ").append(sanitizePattern.getCharsAllowed().prettyPrint()).append("\n");
                content.append("Filters complete: ").append(vulnDescription.getDataflowPathInfo().getMergedAllowedChars().prettyPrint()).append("\n");
                content.append("Dataflow: ").append(dataflowPattern.getName()).append("\n");
                content.append("Context: ").append(contextPattern.getName()).append("\n");
                content.append("Sink: ").append(sinkPattern.getName()).append("\n");
                content.append("State: ").append(vulnDescription.isVulnerable() ? "Bad" : "Good").append("\n");
                content.append("Exploitable: ").append(vulnDescription.isExploitable() ? "Yes" : "Not found").append("\n");
                if (vulnDescription.isExploitable()) {
                    content.append("\n## Exploit description ##\n");
                    content.append(Util.joinStr(vulnDescription.getExploitPath(), "\n")).append("\n");
                }

                content.append("-->\n");
            }

            content.append("<?php\n");
            content.append("# Init\n");
            for (TreeNode<INode> initStatementList : inits) {
                content.append(printAST.prettyPrint(initStatementList) + "\n");
            }

            content.append("\n# Sample\n");
            content.append(printAST.prettyPrint(statementList) + "\n");
            content.append("?>");

            String vulnDescriptionManifest
                    = "Source: " + sourcePattern.getName() + "\n"
                    + "Sanitization: " + sanitizePattern.getName() + "\n"
                    + "Dataflow: " + dataflowPattern.getName() + "\n"
                    + "Context: " + contextPattern.getName() + "\n"
                    + "Sink: " + sinkPattern.getName();

            if (!writeFiles(folder, content, findSinkId, sinkPattern, folder, vulnDescription.isVulnerable(), vulnDescriptionManifest, contextPattern)) {
                continue;
            }

            retval.add(vulnDescription);

        }

        return retval;
    }

    private VulnerabilityDescription createVulnDescription(SourcePattern sourcePattern, SanitizePattern sanitizePattern, ContextPattern contextPattern, SinkPattern sinkPattern) {
        DFATreeNode sourceNode = new DFATreeNode(null);
        sourceNode.setSourcePattern(sourcePattern);
        DataflowPathInfo dataFlowPathInfo = new DataflowPathInfo(sourceNode, false);
        dataFlowPathInfo.setContextInfo(contextPattern.getContextInfo(sinkPattern.getSufficientEscapeChars()));
        dataFlowPathInfo.getSanitizeNodes().add(new Pair<>(sanitizePattern, null));

        VulnerabilityDescription vulnDescription = dataFlowPathInfo.getVulnerabilityInfo();

        // if sink or source are safe -> it is not exploitable
        if (sinkPattern.isIsSafe()) {
            vulnDescription.setExploitable(false);
            vulnDescription.setNotVulnerable();
        }

        vulnDescription.setDataflowPathInfo(dataFlowPathInfo);

        return vulnDescription;
    }

    private void addInitCode(Pattern pattern, List<TreeNode<INode>> inits) {
        if (pattern.containsInitCode()) {
            inits.add(pattern.generateInitStatementAst(patternStorage));
        }
    }
    
    private int written = 0;

    private boolean writeFiles(String folder, StringBuffer content, String findSinkId, SinkPattern sinkPattern, String storePath, boolean isVulnerable, String vulnDescription, ContextPattern contextPattern) {
        String srcFolder = folder + "/src";
        String fileName = "sample.php";
        String subFile = "src/" + fileName;
        String path = srcFolder + "/" + fileName;
        String fileContent = content.toString();
        int sinkLinenumber = sinkLineNumber(fileContent, findSinkId);
        if (sinkLinenumber <= 0) {
            System.out.println("Could not find the sink line number! " + path);
            System.out.println(content.toString());
            return false;
        }
        if (fileContent.contains("NOT SUPPORTED!")) {
            System.out.println("Something is not supported " + path);
            System.out.println(content.toString());
            return false;
        }
        new File(folder).mkdirs();
        new File(srcFolder).mkdirs();
        fileContent = fileContent.replace(findSinkId, "");
        SourceLocation sinkLocation = new SourceLocation("src/sample.php:" + sinkLinenumber);
        SarifExporter sarifExporter = new SarifExporter(sinkPattern.getVulnType(), storePath, sinkLocation, isVulnerable, vulnDescription);
        sarifExporter.addFile(subFile);
        try {
            FileWriter fileWriter = new FileWriter(path, false);
            fileWriter.write(fileContent);
            fileWriter.flush();
            fileWriter.close();

            if (createGenerateFiles) {
                List<GenerateFile> mergedGenFiles = mergedFiles(sinkPattern.getGenerateFiles(), contextPattern.getGenerateFiles());
                createGenerateFiles(folder, mergedGenFiles);
            }

            if (createManifest) {
                FileWriter sarifWriter = new FileWriter(folder + "/manifest.sarif");
                sarifWriter.write(sarifExporter.export());
                sarifWriter.flush();
                sarifWriter.close();
            }

        } catch (IOException ex) {
            Logger.getLogger(CodeSampleCreator.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        written++;
        return true;
    }

    private void addSinkToStmtList(SinkPattern sinkPattern, GenerationData sink, TreeNode<INode> stmtList) {
        if (debugInfo) {
            stmtList.addAll(generateAST("<echo>(<con>(<s>(DEBUG OUTPUT OF INPUT: ), <con>(<$>(context), <s>(\\<br\\>\\n))))"));
        }

        if (sinkPattern.generateOutputContainsInput()) {
            if (ASTType.expression != sinkPattern.getPatternType()) {
                throw new NotExpected("A sink is not an expression and the output requires an expression: " + sinkPattern.getName());
            }
            stmtList.addAll(sinkPattern.generateOutputStatementAst(patternStorage, sink.ast.get(0)).getChildren());
            return;
        }

        stmtList.addAll(sink.ast);
        stmtList.addAll(sinkPattern.generateOutputStatementAst(patternStorage, null).getChildren());
    }

    private void createGenerateFiles(String folderPath, List<GenerateFile> files) {
        for (GenerateFile file : files) {
            try {
                FileWriter writer = new FileWriter(folderPath + "/" + file.getPath(), false);
                writer.write(file.getFileContent());
                writer.flush();
                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(CodeSampleCreator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    protected List<GenerateFile> mergedFiles(List<GenerateFile>... generateFiles) {
        List<GenerateFile> mergedPlain = new LinkedList<>();
        for (List<GenerateFile> generateFileList : generateFiles) {
            mergedPlain.addAll(generateFileList);
        }

        HashMap<String, GenerateFile> pre = new HashMap<>();
        HashMap<String, GenerateFile> after = new HashMap<>();

        for (GenerateFile file : mergedPlain) {
            if (!file.getPath().startsWith("|")) {
                continue;
            }
            HashMap<String, GenerateFile> toAdd = null;
            if (file.getPath().startsWith("|pre|")) {
                toAdd = pre;
            } else if (file.getPath().startsWith("|aft|")) {
                toAdd = after;
            } else {
                throw new NotExpected("A pre or after generate file syntax error for: " + file.getPath());
            }

            String path = file.getPath().substring(5, file.getPath().length());
            if (toAdd.containsKey(path)) {
                throw new NotExpected("To Many file modified for " + file.getPath());
            }
            toAdd.put(path, file);
        }

        List<GenerateFile> retval = new LinkedList<>();

        for (GenerateFile file : mergedPlain) {
            if (file.getPath().startsWith("|")) {
                continue;
            }
            String path = file.getPath();
            String content = pre.containsKey(path) ? pre.get(path).getFileContent() + "\n" : "";
            content += file.getFileContent();
            content += after.containsKey(path) ? "\n" + after.get(path).getFileContent() : "";

            retval.add(new GenerateFile(path, content));
        }

        return retval;
    }

    private int sinkLineNumber(String fileContent, String identifier) {
        String[] lines = fileContent.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.contains(identifier)) {
                return i + 1;
            }
        }
        return -1;
    }

    private GenerationData generateSanitize(SanitizePattern sanPattern, TreeNode<INode> input) {
        TreeNode<INode> varSanitized = ASTFactory.createVar("sanitized");

        List<TreeNode<INode>> sanitizeAST = generatePattern(sanPattern, input, varSanitized, null);
        if (sanPattern.isReturnOutput() && !sanPattern.getDataOutputType().equals(DataType.Boolean())) {
            sanitizeAST = generatePattern(patternStorage.getDataflow("assignment"), sanitizeAST.get(0), varSanitized);
        }

        if (sanPattern.getDataOutputType().isArray()) {
            sanitizeAST.addAll(generateAST("<=>(<$>(sanitized), <call>(implode, <$>(sanitized), <s>(_)))"));
        }

        return new GenerationData(sanitizeAST, sanPattern.getDataOutputType(), varSanitized);
    }

    /**
     * It generates: $tainted = <source> and $tainted = <source>['tainted'];
     * generates source with array data and non array data (most patterns use
     * non array data)
     *
     * @param sourcePattern
     * @return
     */
    private List<GenerationData> generateSourcesPermutations(SourcePattern sourcePattern) {
        List<GenerationData> retval = generateSourcesPattern(sourcePattern);

        if (sourcePattern.getDataOutput().isArray()) {
            for (GenerationData sourceWithArray : generateSourcesPattern(sourcePattern)) {
                TreeNode<INode> varTainted = ASTFactory.createVar("tainted");
                TreeNode<INode> parameter = ASTFactory.createString("t");
                List<TreeNode<INode>> assignment = generatePattern(patternStorage.getDataflow("assignment_dim"), sourceWithArray.outputNode, varTainted, parameter);

                sourceWithArray.ast.addAll(assignment);

                retval.add(new GenerationData(sourceWithArray.ast, sourcePattern.getDataOutput().getArraySubType(), varTainted));
            }
        }

        return retval;
    }

    private List<GenerationData> generateSourcesPattern(SourcePattern sourcePattern) {
        List<GenerationData> retval = new LinkedList<>();
        TreeNode<INode> varTainted = ASTFactory.createVar("tainted");
        Map<String, TreeNode<INode>> outputMap = new HashMap<>();
        outputMap.put("%output", varTainted);
        List<TreeNode<INode>> source = sourcePattern.generateAst(outputMap, patternStorage);
        if (sourcePattern.isReturnOutput()) {
            DataflowPattern assign = patternStorage.getDataflow("assignment");
            List<TreeNode<INode>> assignment = generatePattern(assign, source.get(0), varTainted);

            retval.add(new GenerationData(assignment, sourcePattern.getDataOutput(), varTainted));
        } else {
            retval.add(new GenerationData(source, sourcePattern.getDataOutput(), varTainted));
        }
        return retval;
    }

    private List<TreeNode<INode>> generateAssign(String assignedTo, String assignedValue) {
        DataflowPattern assign = patternStorage.getDataflow("assignment");
        TreeNode<INode> to = ASTFactory.createVar(assignedTo);
        TreeNode<INode> val = ASTFactory.createVar(assignedValue);

        return generatePattern(assign, to, val);
    }

    private List<TreeNode<INode>> generatePattern(Pattern pattern, TreeNode<INode> input, TreeNode<INode> output) {
        return generatePattern(pattern, input, output, null);
    }

    private List<TreeNode<INode>> generatePattern(Pattern pattern, TreeNode<INode> input, TreeNode<INode> output, TreeNode<INode> param) {
        Map<String, TreeNode<INode>> parameters = new HashMap<>();
        parameters.put("%input", input);
        parameters.put("%output", output);
        parameters.put("%paramId", param);
        List<TreeNode<INode>> ast = pattern.generateAst(parameters, patternStorage);
        return ast;
    }

    private GenerationData generatePattern(GenerationData previousGen, Pattern pattern, String outputVarName) throws GenerateException {
        TreeNode<INode> outputVar = ASTFactory.createVar(outputVarName);

        if (!pattern.getDataInputType().equalsAny(previousGen.outputDatatype)) {
            throw new GenerateException("Input data type from " + pattern.getName() + " does not match. From: " + previousGen.outputDatatype + " to: " + pattern.getDataInputType());
        }

        List<TreeNode<INode>> sanitizeAST = generatePattern(pattern, previousGen.outputNode, outputVar, null);

        return new GenerationData(sanitizeAST, pattern.getDataOutputType(), outputVar);

    }

    private GenerationData generateSink(GenerationData previousGenerate, SinkPattern sinkPattern) {
        TreeNode<INode> outputVar = ASTFactory.createVar("useless");

        List<TreeNode<INode>> sinkAST = generatePattern(sinkPattern, previousGenerate.outputNode, outputVar, null);

        return new GenerationData(sinkAST, null, null);
    }

    private Collection<TreeNode<INode>> generateAST(String... patternLanguages) {
        Pattern pattern = new Pattern() {
        };

        List<String> codeLines = new LinkedList<>();
        for (String patternLine : patternLanguages) {
            codeLines.add(patternLine);
        }

        pattern.setCodeLines(codeLines);

        return generatePattern(pattern, null, null);
    }

    private boolean checkDependencies(Set<String> defines, Set<String> depends_on) {
        for (String dependency : depends_on) {
            if (!defines.contains(dependency)) {
                return false;
            }
        }
        return true;
    }

    private String getSplitFolder(ContextPattern contextPattern, SinkPattern sinkPattern, DataflowPattern dataflowPattern, SourcePattern sourcePattern, SanitizePattern sanitizePattern) {
        switch (splitBy.toLowerCase()) {
            case "context":
                return "cont_" + pathSafeName(contextPattern.getName());
            case "sink":
                return "dst_" + pathSafeName(sinkPattern.getName());
            case "dataflow":
                return "df" + pathSafeName(dataflowPattern.getName());
            case "source":
                return "src_" + pathSafeName(sourcePattern.getName());
            case "sanitize":
                return "san_" + pathSafeName(sanitizePattern.getName());

            default:
                return "type_" + pathSafeName(sinkPattern.getVulnType());
        }

    }

}
