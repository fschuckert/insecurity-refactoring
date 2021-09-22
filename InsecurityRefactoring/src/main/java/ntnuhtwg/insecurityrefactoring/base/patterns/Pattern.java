/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.patterns;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.LanguagePattern;
import ntnuhtwg.insecurityrefactoring.base.ASTType;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import ntnuhtwg.insecurityrefactoring.base.ASTNodeTypes;
import ntnuhtwg.insecurityrefactoring.base.DataType;
import ntnuhtwg.insecurityrefactoring.base.Util;
import ntnuhtwg.insecurityrefactoring.base.ast.ASTFactory;
import ntnuhtwg.insecurityrefactoring.base.ast.AnyNode;
import ntnuhtwg.insecurityrefactoring.base.ast.FixedNode;
import ntnuhtwg.insecurityrefactoring.base.exception.TimeoutException;
import ntnuhtwg.insecurityrefactoring.base.patternpersist.PatternEntry;
import ntnuhtwg.insecurityrefactoring.base.patternpersist.PatternParser;
import ntnuhtwg.insecurityrefactoring.base.tree.TreeNode;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.dsl.cypher.DataflowDSL;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.Neo4JConnector;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.Neo4jDB;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;
import ntnuhtwg.insecurityrefactoring.base.exception.NotExpected;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.ParamPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.GenerateParameters;
import org.json.simple.JSONObject;
import org.neo4j.driver.types.Node;
import scala.NotImplementedError;

/**
 *
 * @author blubbomat
 */
public abstract class Pattern {

    protected DataType dataInput;
    protected DataType dataOutput;
    private String type;
    private String vulnOnly;
    private String name;
    private String patternFileLocation;
    private String basePattern;

    private List<String> initCodeLines = new LinkedList<>();
    
    List<String> codeLines;
    
    private List<GenerateFile> generateFiles = new LinkedList<>();
    private List<String> docker_commands = new LinkedList<>();
    private List<String> docker_installs = new LinkedList<>();
    private List<String> defines = new LinkedList();
    private List<String> depends_on = new LinkedList();
    private List<GenerateParameters> generates = new LinkedList<>();

    protected ASTType patternType;
    protected ASTType inputType;
    protected ASTType outputType;

    boolean returnOutput = false;
    
    boolean containsAny = true;

    protected PatternStorage patternStorage;

    public String getBasePattern() {
        return basePattern;
    }

    
    
    public boolean isForGenerate() {
        return basePattern != null;
    }

    public void setForGenerate(String basePatternName) {
        this.basePattern = basePatternName;
    }
    
    public void addGenerateParam(GenerateParameters generate){
        generates.add(generate);
    }

    public List<GenerateParameters> getGeneratesParams() {
        return generates;
    }    

    public boolean containsAny() {
        return containsAny;
    }

    public void setContainsAny(boolean containsAny) {
        this.containsAny = containsAny;
    }

    public String getPatternFileLocation() {
        return patternFileLocation;
    }

    public void setPatternFileLocation(String patternFileLocation) {
        this.patternFileLocation = patternFileLocation;
    }
    
    
    
    public DataType getDataInputType() {
        return dataInput;
    }

    public DataType getDataOutputType() {
        return dataOutput;
    }

    public void setInitCodeLines(List<String> initCodeLines) {
        this.initCodeLines = initCodeLines;
    }

    public List<String> getInitCodeLines() {
        return initCodeLines;
    }
    
    


    public List<String> getDefines() {
        return defines;
    }

    public void setDefines(List<String> defines) {
        this.defines = defines;
    }

    public List<String> getDepends_on() {
        return depends_on;
    }

    public void setDepends_on(List<String> depends_on) {
        this.depends_on = depends_on;
    }
    
    
    
    
    

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVulnOnly() {
        return vulnOnly;
    }

    public void setVulnOnly(String vulnOnly) {
        this.vulnOnly = vulnOnly;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isReturnOutput() {
        return returnOutput;
    }

    public void setReturnOutput(boolean returnOutput) {
        this.returnOutput = returnOutput;
    }

    private boolean checkAttribute(INode expected, INode node, String attribute) {
        if (expected.containsKey("type") && expected.get("type").equals("any")) {
            return true;
        }

        if (!expected.containsKey(attribute)) {
            return true;
        }

        if (expected.containsKey(attribute) && !node.containsKey(attribute)) {
            return false;
        }

        if (!expected.getString(attribute).toLowerCase().equals(node.getString(attribute).toLowerCase())) {
            return false;
        }

        return true;
    }

    public List<GenerateFile> getGenerateFiles() {
        return generateFiles;
    }
    
    public void addGenerateFile(GenerateFile generateFile){
        this.generateFiles.add(generateFile);
    }

    protected boolean checkFlags(INode expected, INode node) {
        if (expected.getFlags().isEmpty()) {
            // nothing expected
            return true;
        }
        if (node.getFlags().isEmpty()) {
            // somethign expected, node does not have any flags
            return false;
        }

        // check flags
        for (String flag : expected.getFlags()) {
            if (!node.getFlags().contains(flag)) {
                return false;
            }
        }

        return true;
    }

    private boolean childrenContainsDots(TreeNode<INode> node) {
        for (TreeNode<INode> child : node.getChildren()) {
            if (child.getObj().containsKey("...")) {
                return true;
            }
        }
        return false;
    }

    private boolean containsQuestionMark(TreeNode<INode> node) {
        for (TreeNode<INode> child : node.getChildren()) {
            if (child.getObj().containsKey("?")) {
                return true;
            }
        }
        return false;
    }

    protected boolean equalsPatternRec(INode node, TreeNode<INode> expectedTree, Neo4jDB db) throws TimeoutException {
        if (node == null || expectedTree == null || expectedTree.getObj() == null) {
            return false;
        }

        INode expected = expectedTree.getObj();

        if (!checkAttribute(expected, node, "type") || !checkAttribute(expected, node, "code")) {
            return false;
        }

        if (!checkFlags(expected, node)) {
            return false;
        }
        
        List<TreeNode<INode>> expectedChildren = expectedTree.getChildren();
        int expectedChildrenSize = expectedChildren.size();        
        int patternChildrenSize = new DataflowDSL(db).children(node).size();
        
        if(Util.isType(node, ASTNodeTypes.ARG_LIST)){

            if (childrenContainsDots(expectedTree)) {
                if (containsQuestionMark(expectedTree)) {
                    expectedChildrenSize--;
                }

                if (patternChildrenSize < expectedChildrenSize) {
                    return false;
                }

            } else {
                // no dots in children
                if (expectedChildren.size() > 0 && expectedChildren.size() != patternChildrenSize) {
                    return false;
                }
            }
        }

        for(int i=0; i<expectedChildrenSize; i++){
            TreeNode<INode> childToCheck = expectedChildren.get(i);
            INode childNode = new DataflowDSL(db).child(node, i);

            if (childNode == null) {
                return false;
            }

            if (!equalsPatternRec(childNode, childToCheck, db)) {
                return false;
            }
        }

        return true;
    }

    public boolean equalsPattern(INode node, Neo4jDB db) throws TimeoutException {

        for (String code : codeLines) {
            TreeNode<PatternEntry> tree = PatternParser.parsePatternCode(code, getName());
            TreeNode<INode> nodeTree = generateASTRec(new HashMap<String, TreeNode<INode>>(), patternStorage, tree);
            return equalsPatternRec(node, nodeTree, db);
        }

        return false;
    }
    
    public boolean containsInitCode() {
        return initCodeLines != null && !initCodeLines.isEmpty();
    }
    
    public List<TreeNode<INode>> generateAst(Map<String, TreeNode<INode>> subtrees, PatternStorage patternStorage) {
        LinkedList<TreeNode<INode>> retval = new LinkedList<>();
        for (String code : codeLines) {
            TreeNode<PatternEntry> tree = PatternParser.parsePatternCode(code, getName());
            retval.add(generateASTRec(subtrees, patternStorage, tree));
        }

        return retval;
    }
    
    public TreeNode<INode> generateInitStatementAst(PatternStorage patternStorage){
        TreeNode<INode> statementList = ASTFactory.createStatementList();
        for (String code : initCodeLines) {
            TreeNode<PatternEntry> tree = PatternParser.parsePatternCode(code, getName());
            statementList.addChild(generateASTRec(Collections.EMPTY_MAP, patternStorage, tree));
        }

        return statementList;        
    }
    
    private void setDotsAndQuestionMark(TreeNode<PatternEntry> pattern, TreeNode<INode> node) {
        if (pattern.getObj().isList) {
            node.getObj().setProperty("...", "true");
        }
        if (pattern.getObj().optional) {
            node.getObj().setProperty("?", "true");
        }
    }

    protected TreeNode<INode> generateASTRec(Map<String, TreeNode<INode>> subtrees, PatternStorage patternStorage, TreeNode<PatternEntry> pattern) {
        List<TreeNode<INode>> childrenSubtrees = new LinkedList<>();
        for (TreeNode<PatternEntry> child : pattern.getChildren()) {
            TreeNode<INode> subTree;
            if (child.getObj().isIdentifier) {
                subTree = generateASTRec(subtrees, patternStorage, child);
            } else {
                subTree = new TreeNode<>(new FixedNode(child.getObj().identifier));
            }
            
            setDotsAndQuestionMark(child, subTree);            
            childrenSubtrees.add(subTree);
        }

        LanguagePattern languagePattern = patternStorage.getLanguagePattern(pattern.getObj().identifier);

//                getLanguagePatterns().get(pattern.getObj().identifier);
        if (languagePattern != null) {
            return languagePattern.generateAst(childrenSubtrees);
        }

        if (pattern.getObj().identifier.startsWith("%")) {
            TreeNode<INode> subTree = subtrees.get(pattern.getObj().identifier);
            if (subTree == null) {
                return new TreeNode<>(new AnyNode().setDots(pattern.getObj().isList));
            }

            setDotsAndQuestionMark(pattern, subTree);            
            return subTree;
        }

        throw new NotExpected("Might be unknown language pattern " + pattern.getObj().identifier);
    }
    
    

    public List<String> getCodeLines() {
        return codeLines;
    }

    public ASTType getPatternType() {
        return patternType;
    }

    public ASTType getInputType() {
        return inputType;
    }

    public ASTType getOutputType() {
        return outputType;
    }

    public void setCodeLines(List<String> codeLines) {
        this.codeLines = codeLines;
    }

    public void setPatternType(ASTType patternType) {
        this.patternType = patternType;
    }

    public void setInputType(ASTType inputType) {
        this.inputType = inputType;
    }

    public void setOutputType(ASTType outputType) {
        this.outputType = outputType;
    }

    public void setPatternStorage(PatternStorage patternStorage) {
        this.patternStorage = patternStorage;
    }
    
    public List<INode> findInputNodes(DataflowDSL dsl, INode node) throws TimeoutException {
        return findNode(dsl, node, "%input");
    }

    public List<INode> findNode(DataflowDSL dsl, INode node, String varId) throws TimeoutException {
        List<INode> retval = new LinkedList<>();

        TreeNode<INode> nodeToFind = new TreeNode<>(new FixedNode("here", false));
        Map<String, TreeNode<INode>> nodes = new HashMap<>();
        nodes.put(varId, nodeToFind);
        List<TreeNode<INode>> astTree = generateAst(nodes, patternStorage);

        List<Integer> path = astTree.get(0).getPathToObj(nodeToFind.getObj());

        boolean isDots = nodeToFind.getObj().containsKey("...");

        INode astNodeToFind = node;
        if (path != null && !path.isEmpty()) {
            for (Integer index : path) {
                astNodeToFind = dsl.child(astNodeToFind, index);
                if (astNodeToFind == null) {
                    return retval;
                }
            }

            if (astNodeToFind != null) {
                retval.add(astNodeToFind);
                if (isDots) {
                    List<INode> siblings = dsl.getSiblingsAfter(astNodeToFind);
                    retval.addAll(siblings);
                }
            }
        }

        return retval;
    }

//    public INode findInputNode(DataflowDSL dsl, INode node){
//        TreeNode<INode> nodeToFind = new TreeNode<>(new FixedNode("here", false));
//        List<TreeNode<INode>> astTree = generateAst(nodeToFind, new TreeNode<>(new AnyNode()), new LinkedList<TreeNode<INode>>(), patternStorage);
//        
//        List<Integer> path = astTree.get(0).getPathToObj(nodeToFind.getObj());
//        
//        INode astNodeToFind = node;
//        if(path != null && !path.isEmpty()){
//            for(Integer index : path){
//                astNodeToFind = dsl.child(astNodeToFind, index);
//                if(astNodeToFind == null){
//                    return null;
//                }
//            }
//        }
//        
//        return astNodeToFind;
//    }
    public TreeNode<INode> findRefactoringNode(TreeNode<INode> node, String varId) {
        TreeNode<INode> nodeToFind = new TreeNode<>(new FixedNode("here", false));
        Map<String, TreeNode<INode>> nodes = new HashMap<>();
        nodes.put(varId, nodeToFind);
        List<TreeNode<INode>> astTree = generateAst(nodes, patternStorage);

        List<Integer> path = astTree.get(0).getPathToObj(nodeToFind.getObj());

        TreeNode<INode> astNodeToFind = node;
        if (path != null && !path.isEmpty()) {
            for (Integer index : path) {
                astNodeToFind = astNodeToFind.getChild(index);
                if (astNodeToFind == null) {
                    return null;
                }
            }
        }

        return astNodeToFind;
    }

    @Override
    public String toString() {
        return getName();
    }

    public Map<String, TreeNode<INode>> findAllRefactoringNodes(TreeNode<INode> node) {
        Map<String, TreeNode<INode>> nodes = new HashMap<>();
        for (String nodeId : requiredNodes()) {
            nodes.put(nodeId, findRefactoringNode(node, nodeId));
        }
        return nodes;
    }

    private List<String> requiredNodes() {
        List<String> retval = new LinkedList<>();
        for (String codeLine : codeLines) {
            TreeNode<PatternEntry> pattern = PatternParser.parsePatternCode(codeLine, getName());
            addNodesRec(pattern, retval);
        }
        return retval;
    }

    private void addNodesRec(TreeNode<PatternEntry> entry, List<String> retval) {
        if (entry.getObj().identifier.startsWith("%")) {
            retval.add(entry.getObj().identifier);
        }

        for (TreeNode<PatternEntry> child : entry.getChildren()) {
            addNodesRec(child, retval);
        }
    }

    public boolean isExpression() {
        return ASTType.expression.equals(patternType);
    }

    /**
     * returns the where clause without WHERE!
     *
     * @param nodeName how the variable is named
     * @return
     */
//    public abstract String cypherWhere(String nodeName);
}
