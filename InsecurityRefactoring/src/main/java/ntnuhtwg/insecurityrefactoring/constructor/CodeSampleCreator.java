/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.constructor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import ntnuhtwg.insecurityrefactoring.base.patterns.Pattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.DataflowPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.PatternStorage;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.ContextPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.LanguagePattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SanitizePattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SinkPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SourcePattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.sufficient.GenerateParameters;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.sufficient.Sufficient;
import ntnuhtwg.insecurityrefactoring.base.print.PrintAST;

/**
 *
 * @author blubbomat
 */
public class CodeSampleCreator {

    PatternStorage patternStorage;
    PrintAST printAST = new PrintAST();

    public CodeSampleCreator(PatternStorage patternStorage) {
        this.patternStorage = patternStorage;
    }

    public void createAllPossibleSamples(String storePath) throws GenerateException {
//        for(SinkPattern sinkPattern : patternStorage.getSinks()){
        int sampleCount = 0;
        for (SourcePattern sourcePattern : patternStorage.getSources()) {
            if(sourcePattern.isSecure()){
                continue;
            }
            if(sourcePattern.containsAny()){
                continue;
            }
            for (SanitizePattern sanitizePattern : patternStorage.getSanitizations()) {
                if (sanitizePattern.containsAny()) {
                    System.err.println("Skipping because no params for sufficient: " + sanitizePattern.getName());
                    continue;
                }
                
                for (DataflowPattern dataflowPattern : patternStorage.getDataflows()) {
                    for (ContextPattern contextPattern : patternStorage.getContexts()) {                        
                        for (SinkPattern sinkPattern : patternStorage.getSinks()) {
                            
                            if(sinkPattern.isIsSafe()){
                                continue;
                            }
                            if(!contextPattern.getVulnType().equals(sinkPattern.getVulnType())){
                                continue;
                            }
                            if(sinkPattern.containsAny()){
                                continue;
                            }
                            
                            if(!sanitizePattern.getName().startsWith("addslashes")){
                                continue;
                            }
                            boolean sufficient = Sufficient.isSufficient(sanitizePattern, contextPattern.getContextInfo());
                            boolean isSafe = sinkPattern.isIsSafe() || sufficient || sourcePattern.isSecure();
                            
//                            if(!sinkPattern.getName().startsWith("real_query")){
//                                continue;
//                            }
//                            //TODO: check if pattern is correct                                
//                            // generate code
                            int iGen = 0;
                            for (GenerationData source : generateSourcesPermutations(sourcePattern)) {
                                
                                if (!sanitizePattern.getDataInputType().equals(source.outputDatatype)) {
                                    System.out.println("SKIPPING");
                                    continue;
                                }
                                
                                
                                List<TreeNode<INode>> inits = new LinkedList<>();
                                if(sinkPattern.containsInitCode()){
                                    inits.add(sinkPattern.generateInitStatementAst(patternStorage));
                                }

                                GenerationData sanitization = generateSanitize(sanitizePattern, source.outputNode);                                
                                GenerationData dataflow = generatePattern(sanitization, dataflowPattern, "dataflow");
                                GenerationData context = generatePattern(dataflow, contextPattern, "context");
                                GenerationData sink = generateSink(context, sinkPattern);

                                TreeNode<INode> statementList = ASTFactory.createStatementList();
                                statementList.addAll(source.ast);
                                if(sanitizePattern.getDataOutputType().equals(DataType.Boolean())){     
                                    //TODO: if statement not showing up (probably AST not correct)
                                    TreeNode<INode> ifStmtList = ASTFactory.createStatementList();
                                            
                                    TreeNode<INode> ifN = ASTFactory.createIfStatement(sanitization.ast.get(0), ifStmtList);
                                    statementList.addChild(ifN);
                                    
                                    ifStmtList.addAll(dataflow.ast);
                                    ifStmtList.addAll(context.ast);
                                    ifStmtList.addAll(sink.ast);
                                    
                                }
                                else{                                    
                                    statementList.addAll(sanitization.ast);
                                    statementList.addAll(dataflow.ast);
                                    statementList.addAll(context.ast);
                                    statementList.addAll(sink.ast);
                                }
                                
                                StringBuffer content = new StringBuffer("<?php\n");
                                
                                
                                content.append("# Init\n");
                                for(TreeNode<INode> initStatementList : inits){
                                    content.append(printAST.prettyPrint(initStatementList) + "\n");
                                }
                                
                                content.append("\n# Sample\n");
                                content.append(printAST.prettyPrint(statementList) + "\n");
                                
                                content.append("?>");
                                
                                
                                String folder = storePath + "/sink:" + sinkPattern.getVulnType() + "/source:" + sourcePattern.getName() + iGen++ + "/sanitization:" + sanitizePattern.getName() + "/dataflow:" + dataflowPattern.getName() + "/context:" + contextPattern.getName() + "/sink:" + sinkPattern.getName() ;
                                new File(folder).mkdirs();
                                
                                String srcFolder = folder + "/src";
                                new File(srcFolder).mkdirs();
                                
                                String fileName = "sample.php";
                                String path = srcFolder + "/" + fileName;
                                
                                SourceLocation sinkLocation = new SourceLocation("sampleTODO.php:1337");
                                
                                SarifExporter sarifExporter = new SarifExporter(sinkPattern.getVulnType(), storePath, sinkLocation, !isSafe);
                                
                                
                                try {
                                    FileWriter fileWriter = new FileWriter(path, false);
                                    fileWriter.write(content.toString());
                                    fileWriter.flush();
                                    fileWriter.close();
                                    
                                    FileWriter sarifWriter = new FileWriter(folder + "/manifest.sarif");
                                    sarifWriter.write(sarifExporter.export());
                                    sarifWriter.flush();
                                    sarifWriter.close();
                                } catch (IOException ex) {
                                    Logger.getLogger(CodeSampleCreator.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            
                            sampleCount++;

                        }

                    }
                }

            }
        }
        
        System.out.println("Samples: " + sampleCount);
    }

    private GenerationData generateSanitize(SanitizePattern sanPattern, TreeNode<INode> input) {
        TreeNode<INode> varSanitized = ASTFactory.createVar("sanitized");

        List<TreeNode<INode>> sanitizeAST = generatePattern(sanPattern, input, varSanitized, null);
        if (sanPattern.isReturnOutput()) {
            sanitizeAST = generatePattern(patternStorage.getDataflow("assignment"), sanitizeAST.get(0), varSanitized);
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

    public static void main(String[] args) throws Exception {
        PatternStorage patternStorage = new PatternStorage();
        patternStorage.readPatterns("/home/blubbomat/Development/FindPOI/InsecurityRefactoring/target/files/patterns");
//        patternStorage.readPatterns("/home/blubbomat/Development/pattern");

        CodeSampleCreator codeSampleCreator = new CodeSampleCreator(patternStorage);
        codeSampleCreator.createAllPossibleSamples("/home/blubbomat/Development/patterns");
    }

    private GenerationData generatePattern(GenerationData sanitization, Pattern dataflowPattern, String outputVarName) {
        TreeNode<INode> outputVar = ASTFactory.createVar(outputVarName);
        
        List<TreeNode<INode>> sanitizeAST = generatePattern(dataflowPattern, sanitization.outputNode, outputVar, null);
        if (dataflowPattern.isReturnOutput()) {
            sanitizeAST = generatePattern(patternStorage.getDataflow("assignment"), sanitizeAST.get(0), outputVar);
        }

        return new GenerationData(sanitizeAST, dataflowPattern.getDataOutputType(), outputVar);
        
    }

    private GenerationData generateSink(GenerationData previousGenerate, SinkPattern sinkPattern) {
        TreeNode<INode> outputVar = ASTFactory.createVar("useless");
        
        List<TreeNode<INode>> sinkAST = generatePattern(sinkPattern, previousGenerate.outputNode, outputVar, null);
        
        return new GenerationData(sinkAST, null, null);
    }

}
