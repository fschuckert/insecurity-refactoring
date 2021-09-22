/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.stream.Stream;
import javax.swing.JOptionPane;
import ntnuhtwg.insecurityrefactoring.Framework;
import ntnuhtwg.insecurityrefactoring.base.ast.ASTFactory;
import ntnuhtwg.insecurityrefactoring.base.exception.TimeoutException;
import ntnuhtwg.insecurityrefactoring.base.tree.TreeNode;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.dsl.cypher.DataflowDSL;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.Neo4JConnector;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.Neo4jDB;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;
import ntnuhtwg.insecurityrefactoring.base.patterns.PatternStorage;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.DataflowPattern;
import ntnuhtwg.insecurityrefactoring.base.tree.DFATreeNode;
import ntnuhtwg.insecurityrefactoring.print.PrintAST;
import org.apache.commons.io.FileUtils;
import org.neo4j.driver.Record;
import org.neo4j.driver.Values;
import org.neo4j.driver.internal.value.NodeValue;
import org.neo4j.driver.types.Node;
import scala.NotImplementedError;

/**
 *
 * @author blubbomat
 */
public class Util {
    
    public static void printSourceCodeRec(Neo4jDB db, DFATreeNode node, SourceLocation lastPrinted) {
        if (node == null) {
            return;
        }

        if (node.getObj() != null) {
            SourceLocation location = Util.codeLocation(db, node.getObj());
            if (location != null && !location.equals(lastPrinted)) {
                System.out.println(Util.codeLocation(db, node.getObj()).codeSnippet(true));
                lastPrinted = location;
            }
        }
        printSourceCodeRec(db, node.getParent_(), lastPrinted);
    }

    public static <T> boolean isLastIndex(T[] array, int index) {
        return index + 1 == array.length;
    }

    public static String joinStr(String[] array, String decimator) {
        StringJoiner joiner = new StringJoiner(decimator);
        for (String str : array) {
            joiner.add(str);
        }

        return joiner.toString();
    }

    public static String formatJSON(String unformated) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(unformated);
        return gson.toJson(je);
    }

    public static String joinStr(Collection<String> array, String decimator) {
        StringJoiner joiner = new StringJoiner(decimator);
        for (String str : array) {
            joiner.add(str);
        }

        return joiner.toString();
    }
    
    public static List<String> splitOnLines(String str, String regexOnSplitLine){
        List<String> retval = new LinkedList<>();
        StringJoiner joiner = new StringJoiner("\n");
        for(String line : str.split("\n")){            
            if(line.matches(regexOnSplitLine)){
                retval.add(joiner.toString());
                joiner = new StringJoiner("\n");
                continue;
            }
            
            joiner.add(line);
        }
        
        retval.add(joiner.toString());
        
        return retval;
    }

    public static boolean isType(INode node, String type) {
        return node != null && type.equals(node.getString("type"));
    }

    public static boolean isAnyCall(INode node) {
        return isType(node, ASTNodeTypes.CALL) || isType(node, ASTNodeTypes.METHOD_CALL) || isType(node, ASTNodeTypes.STATIC_CALL);
    }

    public static void prettyPrint(String pre, INode node) {
        System.out.println(pre + node + " line:" + node.get("lineno"));
    }

    public static void debugPrintLoc(Neo4jDB db, INode node) {
        SourceLocation loc = Util.codeLocation(db, node);
        System.out.println("" + loc.codeSnippet(true));
    }

    public static boolean isPrePath(SourceLocation prePath, Neo4jDB db, INode node) {
        if (prePath.getLineNumber() > 0 && node.getInt("lineno") != prePath.getLineNumber()) {
            return false;
        }
        SourceLocation nodeLoc = codeLocation(db, node);

        return nodeLoc.getPath().contains(prePath.getPath());
    }

    public static SourceLocation codeLocation(Neo4jDB db, INode node) {
        if (node == null) {
            return new SourceLocation("/", "/", -1);
        }

        INode topLevel = null;
        if (Util.isType(node, ASTNodeTypes.TOPLEVEL)) {
            topLevel = node;
        } else {
            DataflowDSL dsl = new DataflowDSL(db);
            try {
                topLevel = dsl.getTopLevelOfFile(node);
            } catch (TimeoutException ex) {
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (topLevel != null) {
            String fullPath = topLevel.getString("name");
            int lastIndex = fullPath.lastIndexOf("/");
            String dirPath = fullPath.subSequence(0, lastIndex).toString();
            String file = fullPath.subSequence(lastIndex + 1, fullPath.length()).toString();

            return new SourceLocation(dirPath, file, node.containsKey("lineno") ? node.getInt("lineno") : -1);
        }

//        System.out.println("cannot find top file for: " + node.id());
        return null;
    }

    public static String getOriginalFileContent(Neo4jDB db, INode node, long from, long until) {
        SourceLocation sourceLocation = codeLocation(db, node);
        return sourceLocation.codeSnippet(from, until);
    }

//    private static String directoryPath(Neo4jDB db, INode node){
//        Long id = node.id();
////        List<Record> result = this.runRead("MATCH (d) - [:DIRECTORY_OF] -> (c) WHERE id(c)=$id return d", Values.parameters("id", id));
//        List<INode> result = db.findAll(
//                "MATCH (d) - [:DIRECTORY_OF] -> (c) WHERE id(c)=$id return d", 
//                "id", id
//        );
//        if(result.size() == 1){            
//            INode folderNode = result.get(0);
//            String folderName = folderNode.getString("name");
//            return directoryPath(db, folderNode) + "/" + folderName;
//        }
//        else {
//            return "";
//        }
//    }
    public static TreeNode<INode> createTree(Neo4jDB db, INode topNode) throws TimeoutException {
        TreeNode<INode> retval = new TreeNode(topNode);
        DataflowDSL dsl = new DataflowDSL(db);
        constructTreeRec(dsl, retval);

        return retval;
    }

    private static void constructTreeRec(DataflowDSL dsl, TreeNode<INode> parent) throws TimeoutException {
        List<INode> children = dsl.children(parent.getObj());
        for (INode childNode : children) {
            TreeNode<INode> child = new TreeNode(childNode);
            parent.addChild(child);

            constructTreeRec(dsl, child);
        }
    }

    public static String sha1FromFile(String pathStr) {
        Path path = Path.of(pathStr);

        try {
            String input = Files.readString(path);

            MessageDigest mDigest = MessageDigest.getInstance("SHA1");
            byte[] result = mDigest.digest(input.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < result.length; i++) {
                sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
            }

            return sb.toString();
        } catch (Exception ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }

        return "sha1 not found!";
    }

    public static String getASTasJSONRec(DataflowDSL dsl, INode node) throws TimeoutException {
        if (node == null) {
            return "";
        }

        String json = "{\n\"type\":\"" + node.get("type") + "\"";

        if (node.containsKey("code") && !"".equals(node.get("code"))) {
            json += ",\n\"code\":\"" + node.get("code") + "\"";
        }

        List<String> flags = node.getFlags();

        if (!flags.isEmpty()) {
            json += ",\n\"flags\":[";

            StringJoiner flagJoiner = new StringJoiner(", ");
            for (String flag : flags) {
                flagJoiner.add("\"" + flag + "\"");
            }
            json += flagJoiner + "]";
        }

        List<INode> children = dsl.children(node);
        if (!children.isEmpty()) {
            json += ",\n\"children\":\n[\n";
            StringJoiner joiner = new StringJoiner(",\n");
            for (INode child : children) {
                String childStr = getASTasJSONRec(dsl, child);
                joiner.add(childStr);
            }
            json += joiner.toString() + "]\n";
        }

        return json + "\n}\n";
    }

    public static Map<String, TreeNode<INode>> createMap(Object... vars) {
        Map<String, TreeNode<INode>> retval = new HashMap<>();
        if (vars.length % 2 == 1) {
            throw new NotImplementedError("create map with incorrect length of parameters: " + vars.length);
        }

        for (int i = 0; i < vars.length; i = i + 2) {
            retval.put((String) vars[i], (TreeNode<INode>) vars[i + 1]);
        }

        return retval;
    }

    public static String readLineByLineJava8(String filePath) {
        StringBuilder contentBuilder = new StringBuilder();
        try ( Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contentBuilder.toString();
    }

    public static boolean isAnyOf(INode node, String[] types) {
        for (String type : types) {
            if (isType(node, type)) {
                return true;
            }
        }
        return false;
    }
    
    public static void runCommandOnBashDANGEROES(String command, File folder) throws IOException, InterruptedException{
        File out = new File("output.txt"); // File to write stdout to
        File err = new File("output.txt"); // File to write stderr to
        ProcessBuilder builder = new ProcessBuilder("/bin/sh", "-c", command);
        builder.directory(folder);
//        builder.
        builder.redirectOutput(out); // Redirect stdout to file
        if (out == err) {
            builder.redirectErrorStream(true); // Combine stderr into stdout
        } else {
            builder.redirectError(err); // Redirect stderr to file
        }
        System.out.println("[" + folder.getAbsolutePath() + "]: " + command);
        Process process = builder.start();
        System.out.println("Wait for");
        process.waitFor();
        String output = readLineByLineJava8("output.txt");
        System.out.println(output);
        System.out.println("Finished");
    }

    public static void runCommand(String command, File folder) throws IOException, InterruptedException {
        File out = new File("output.txt"); // File to write stdout to
        File err = new File("output.txt"); // File to write stderr to
        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(folder);
        builder.command(command.split(" "));
//        builder.
        builder.redirectOutput(out); // Redirect stdout to file
        if (out == err) {
            builder.redirectErrorStream(true); // Combine stderr into stdout
        } else {
            builder.redirectError(err); // Redirect stderr to file
        }
        System.out.println("[" + folder.getAbsolutePath() + "]: " + command);
        Process process = builder.start();
        System.out.println("Wait for");
        process.waitFor();
        String output = readLineByLineJava8("output.txt");
        System.out.println(output);
        System.out.println("Finished");
    }

    public static String codeSnippet(Neo4jDB db, INode child) {
        SourceLocation loc = codeLocation(db, child);
        if (loc != null) {
            return loc.codeSnippet();
        }
        return "No snippet for: " + child;
    }

    public static TreeNode<INode> previewAST(PatternStorage patternStorage, List<String> codeLines) throws Exception {
        DataflowPattern dataflowPattern = new DataflowPattern(true, DataType.String(), DataType.String(), "id", 0.0, 0.0, 0.0);
        dataflowPattern.setCodeLines(codeLines);

        Map<String, TreeNode<INode>> subtrees = new HashMap<>();
        subtrees.put("%input", ASTFactory.createVar("inputVar"));
        subtrees.put("%output", ASTFactory.createVar("outputVar"));

        List<TreeNode<INode>> ASTs = dataflowPattern.generateAst(subtrees, patternStorage);

        TreeNode<INode> finalAST = null;
        TreeNode<INode> statementList = ASTFactory.createStatementList();
        for (TreeNode<INode> statement : ASTs) {
            statementList.addChild(statement);
        }
        finalAST = statementList;

        return finalAST;
    }

    public static String previewCode(PatternStorage patternStorage, List<String> codeLines) throws Exception {
        PrintAST printAST = new PrintAST();
        TreeNode<INode> finalAST = previewAST(patternStorage, codeLines);

        return printAST.prettyPrint(finalAST);
    }

    public static void deleteFolder(String path) throws IOException {
        FileUtils.deleteDirectory(new File(path));
    }
    
    public static <E> List<E> list(E... elements){
        LinkedList<E> list = new LinkedList<>();
        for(E elem : elements){
            list.add(elem);
        }
        return list;
    }
}
