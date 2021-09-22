/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.print;

import java.util.StringJoiner;
import ntnuhtwg.insecurityrefactoring.base.ASTNodeTypes;
import ntnuhtwg.insecurityrefactoring.base.ASTType;
import ntnuhtwg.insecurityrefactoring.base.Util;
import ntnuhtwg.insecurityrefactoring.base.tree.TreeNode;
import ntnuhtwg.insecurityrefactoring.gui.GUI;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.Neo4JConnector;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.Neo4jDB;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.Neo4jEmbedded;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author blubbomat
 */
public class PrintAST {
    
    private int indent = 0;
    
    public String prettyPrint(TreeNode<INode> treeNode){
        if(treeNode.getSpecial() != null){
            return treeNode.getSpecial() + prettyPrintRec(treeNode);
        }
        
        return prettyPrintRec(treeNode);
    }

    
    public String prettyPrintRec(TreeNode<INode> treeNode){
        INode node = treeNode.getObj();
        if(node.containsKey("search")){
            int i =10;
        }
        switch( node.getString("type")){
            case "AST_ARRAY":
            {
                StringJoiner joiner = new StringJoiner(", ");             
                for(TreeNode<INode> childTreeNode : treeNode.getChildren()){
                    joiner.add(prettyPrint(childTreeNode));
                } 
                return '[' + joiner.toString() +  ']';
            }
            case "AST_ARRAY_ELEM":
                if(treeNode.getChild(1).getObj().isNullNode()){
                    return prettyPrint(treeNode.getChild(0));
                }
                return prettyPrint(treeNode.getChild(1)) + " => " + prettyPrint(treeNode.getChild(0));
                
            case "AST_TOPLEVEL":
                return prettyPrint(treeNode.getChildren().get(0));
            case "string":
                if(node.getString("code") == null){
                    return "\"\"";
                }
                
                return '"' +node.getString("code").replace("\"", "\\\"") + '"';
            case "integer":
                return node.getString("code");
            case "AST_CONST":
                return prettyPrint(treeNode.getChild(0));
                
            case "AST_STMT_LIST":
                String retval = "";
                for(TreeNode<INode> childTreeNode : treeNode.getChildren()){
                    String statement = prettyPrint(childTreeNode);
                    statement = cleanupBrackets(statement);                    
                    retval +=  indent() + statement;
                    if(statement.endsWith("}") || statement.endsWith(":") || statement.endsWith(";")){
                        retval += "\n";
                    }
                    else{
                        retval += ";\n";
                    }
                }                
                return retval;
                
                
            case "AST_CALL":
                return prettyPrint(treeNode.getChild(0)) + "(" + prettyPrint(treeNode.getChild(1)) + ")";
            
            case "AST_ARG_LIST":
            {
                StringJoiner joiner = new StringJoiner(", ");             
                for(TreeNode<INode> childTreeNode : treeNode.getChildren()){
                    joiner.add(prettyPrint(childTreeNode));
                } 
                return joiner.toString();
            }
            case "AST_NAME":
                return AST_NAME.string(node) + getCode(treeNode.getChild(0));
                
            case "AST_NAME_LIST":
            {
                StringJoiner joiner = new StringJoiner(", ");
                for(TreeNode child : treeNode.getChildren()){
                    joiner.add(prettyPrint(child));
                }
                return joiner.toString();
            }
                
            case "AST_ASSIGN":
            {
//                String exp = prettyPrint(treeNode.getChild(1));
//                cleanupBrackets(exp);
                return "(" + prettyPrint(treeNode.getChild(0)) + " = " + prettyPrint(treeNode.getChild(1)) + ")";  
            }
            case "AST_ASSIGN_OP":
                return prettyPrint(treeNode.getChild(0)) +  AST_BINARY_OP.string(node).trim()+"= " + prettyPrint(treeNode.getChild(1)); 
            case "AST_ASSIGN_REF":
                return prettyPrint(treeNode.getChild(0)) + " =& " + prettyPrint(treeNode.getChild(1)); 
            case "AST_BINARY_OP":
//                boolean brackets = AST_BINARY_OP.brackets(node);
                boolean brackets = true;
                String binaryOpStr = (brackets ? "(":"") + prettyPrint(treeNode.getChild(0)) + AST_BINARY_OP.string(node) + prettyPrint(treeNode.getChild(1)) + (brackets ? ")":"");
                return binaryOpStr;
//                if(binaryOpStr.)
            case "AST_ECHO":
                return "echo(" + prettyPrint(treeNode.getChild(0)) + ")";
            case "AST_PRINT":
                return "print(" + prettyPrint(treeNode.getChild(0)) + ")";
            case "AST_VAR":
                return "$" + treeNode.getChild(0).getObj().getString("code");
            case "AST_DIM":
                return prettyPrint(treeNode.getChild(0)) + "[" + prettyPrint(treeNode.getChild(1)) + "]";
            case "NULL":
                return "";
                
            case "AST_FOREACH":
            {
                // 0 expr, 1 value, 2 key, 3 stmts
                indent++;
                String keyStr = (treeNode.getChild(2).getObj().isNullNode() ? "" : (prettyPrint(treeNode.getChild(2))) + " => ");
                String forEachRetval = "foreach (" + prettyPrint(treeNode.getChild(0)) + " as " + keyStr + prettyPrint(treeNode.getChild(1)) + "){\n" + prettyPrint(treeNode.getChild(3)) + indent(-1) + "}";
                indent--;//                
                return forEachRetval;
            }
                
            case "AST_PROP":
                return prettyPrint(treeNode.getChild(0)) + "->" + ("string".equals(treeNode.getChild(1).getObj().get("type")) ? treeNode.getChild(1).getObj().getString("code") : prettyPrint(treeNode.getChild(1)) );
                
            case "AST_IF":
            {
                String ifStatements = "";
                int childNum = 0;
                for(TreeNode ifStatement : treeNode.getChildren()){
                    if(childNum == 0){
                        ifStatements += "if(" + cleanupBrackets(prettyPrint(ifStatement.getChild(0))) + ")\n";
                        
                    }
                    else if(childNum+1 == treeNode.getChildren().size()){
                        ifStatements += "\n" + indent() + "else\n";
                    }
                    else {
                        ifStatements += "\n" + indent() + "else if(" + cleanupBrackets(prettyPrint(ifStatement.getChild(0))) + ")\n";
                    }
                    ifStatements += indent() + "{\n";
                    indent++;
                    ifStatements += prettyPrint(ifStatement.getChild(1));
                    indent--;
                    ifStatements += indent() + "}";
                    
                    childNum++;
                }
                
                
                
                
                return ifStatements;
            }
            
            case "AST_BREAK":
                return ("break " + prettyPrint(treeNode.getChild(0))).trim();
                
            case "AST_CONTINUE":
                return ("continue " + prettyPrint(treeNode.getChild(0))).trim();
                
            case "AST_CAST":
                return AST_CAST.string(node) + "(" + prettyPrint(treeNode.getChild(0)) + ")";
                
            case "AST_TRY":
            {     
                // try
                String tryStr =  indent() + "try\n" + indent() + "{\n";
                indent++;
                tryStr += prettyPrint(treeNode.getChild(0));
                indent--;
                tryStr += indent() + "}";
                
                // catch list
                for(TreeNode child : treeNode.getChild(1).getChildren()){
                    tryStr += prettyPrint(child);
                }
                
                // finally
                if(!treeNode.getChild(2).getObj().isNullNode()){
                    tryStr += "\n" + indent() + "finally \n";
                    tryStr += indent() + "{\n";
                    indent++;
                    tryStr += prettyPrint(treeNode.getChild(2));
                    indent--;
                    tryStr += indent() + "}";
                }
                
                return tryStr;
            }
            
        
            case "AST_CATCH":
            {
                String catchStr = "\n" + indent() + "catch(";
                
                // catch class types                
                catchStr += joinPrettyPrintChildren(treeNode.getChild(0), " | ") + " " + prettyPrint(treeNode.getChild(1)) + ")\n";
                catchStr += indent() + "{\n";
                indent++;
                catchStr += prettyPrint(treeNode.getChild(2));
                indent--;
                catchStr += indent() + "}";
                return catchStr;
            }
            
            case "AST_CLASS":
            {
                String classStr = "class " + treeNode.getChild(0).getObj().getString("code");
                
                // extends
                if(!treeNode.getChild(2).getObj().isNullNode()){
                    classStr += " extends " + prettyPrint(treeNode.getChild(2));
                }
                
                // implements
                if(!treeNode.getChild(3).getObj().isNullNode()){
                    classStr += joinPrettyPrintChildren(treeNode.getChild(3), ", ");
                }
                
                classStr += "\n" + indent() + "{\n";
                indent++;
                classStr += prettyPrint(treeNode.getChild(4).getChild(0));
                indent--;
                classStr += indent() + "}";
                
                return classStr;
            }
            
            case "AST_USE_TRAIT":
                return "use " + prettyPrint(treeNode.getChild(0)) + prettyPrint(treeNode.getChild(1));
            
            case "AST_TRAIT_ADAPTATIONS":
            {
                String adaptions = "{\n";
                indent++;
                for(TreeNode adaption : treeNode.getChildren()){
                    adaptions += indent() + prettyPrint(adaption) + ";\n";                    
                }
                indent--;
                adaptions += indent() + "}";
                
                return adaptions;
            }
            
            case "AST_TRAIT_PRECEDENCE":
                return prettyPrint(treeNode.getChild(0)) +  " insteadof " + prettyPrint(treeNode.getChild(1));
            
            case "AST_METHOD_REFERENCE":
                return prettyPrint(treeNode.getChild(0)) + "::" + treeNode.getChild(1).getObj().getString("code");
                
            case "AST_CLASS_CONST_DECL":
                return AST_MODIFIER.string(node) + "const " + joinPrettyPrintChildren(treeNode, ", ");
            case "AST_CONST_ELEM":
                return treeNode.getChild(0).getObj().getString("code") + " = " + prettyPrint(treeNode.getChild(1));
                
            case "AST_PROP_GROUP":
                // todo: check parser for variable type!
                return AST_MODIFIER.string(node) + prettyPrint(treeNode.getChild(0)) 
                        + (treeNode.getChild(0).getObj().isNullNode() ? "" : " " )
                        + prettyPrint(treeNode.getChild(1));
                
            case "AST_PROP_DECL":
                return AST_MODIFIER.string(node) + joinPrettyPrintChildren(treeNode, ", ");
                
            case "AST_PROP_ELEM":
                return "$" + treeNode.getChild(0).getObj().getString("code") 
                        + (treeNode.getChild(1).getObj().isNullNode() ? "" : " = " +prettyPrint(treeNode.getChild(1)));
                

            case "AST_FUNC_DECL":
            case "AST_METHOD":
            {
                String function = AST_MODIFIER.string(node) + "function " + getCode(treeNode.getChild(0)) + "(" + prettyPrint(treeNode.getChild(2)) + ")";
                if(!isNullNode(treeNode.getChild(4))){
                    function += " : " + prettyPrint(treeNode.getChild(4));
                }
                if(!treeNode.getChild(3).getObj().isNullNode()){
                    function += statementsInBrackets(treeNode.getChild(3));
                }
                
                
                
                return function;
            }
            
            case "AST_PARAM_LIST":
                return joinPrettyPrintChildren(treeNode, ", ");
                
            
                
            case "AST_PARAM":
                return  prettyPrint(treeNode.getChild(0)) // type
                        + (isNullNode(treeNode.getChild(0)) ? "" : " ") // space if type is defined
                        + AST_PARAM.reference(node) + AST_PARAM.variadic(node) + "$" + getCode(treeNode.getChild(1)) // variable name
                        + (treeNode.getChild(2).getObj().isNullNode() ? "" : "=" + prettyPrint(treeNode.getChild(2)));
                
                
            case "AST_TYPE":
                return AST_TYPE.string(node);                
            
            case "AST_NAMESPACE":
            {
                String namespace = "namespace " + getCode(treeNode.getChild(0));
                if(!isNullNode(treeNode.getChild(1))){
                    namespace += statementsInBrackets(treeNode.getChild(1));
                }
                return namespace;
            }
            
            case "AST_CLASS_NAME":
                return prettyPrint(treeNode.getChild(0)) + "::class";
                
            case "AST_CLASS_CONST":
                return prettyPrint(treeNode.getChild(0)) + "::"+getCode(treeNode.getChild(1));
              
            case "AST_CLONE":
                return "clone( " + prettyPrint(treeNode.getChild(0)) + " )";   
            
            case "AST_CLOSURE":
            {
                String closure = AST_MODIFIER.string(node) + "function " + RETURNS_REF.string(node) + "(" +prettyPrint(treeNode.getChild(2)) + ")";
                
                if(!isNullNode(treeNode.getChild(3))){
                    closure += " use (" + prettyPrint(treeNode.getChild(3)) + ")";
                }
                if(!isNullNode(treeNode.getChild(5))){
                    closure += ":" + prettyPrint(treeNode.getChild(5));
                }
                if(!isNullNode(treeNode.getChild(4))){
                    closure += statementsInBrackets(treeNode.getChild(4));
                }
                
                return closure;
            }
            
            case "AST_CLOSURE_USES":
                return joinPrettyPrintChildren(treeNode, ", ");
            
            case "AST_CLOSURE_VAR":
                return "$"+getCode(treeNode.getChild(0));
                
            case "AST_RETURN":
                return "return " + prettyPrint(treeNode.getChild(0));
     
            case "AST_WHILE":
                return "while(" + prettyPrint(treeNode.getChild(0)) + ")" + statementsInBrackets(treeNode.getChild(1));
            
            case "AST_DECLARE":
                return "declare(" + prettyPrint(treeNode.getChild(0)) + ")" + statementsInBrackets(treeNode.getChild(1));
            
            case "AST_CONST_DECL":
                return joinPrettyPrintChildren(treeNode, ", ");
                
            case "AST_CONDITIONAL":
                return "(" + prettyPrint(treeNode.getChild(0)) + " ? " + prettyPrint(treeNode.getChild(1)) + " : " + prettyPrint(treeNode.getChild(2)) + ")";
            
            case "AST_DO_WHILE":
                return "do"+statementsInBrackets(treeNode.getChild(0)) + "while(" + prettyPrint(treeNode.getChild(1)) + ")";
            
            case "AST_EMPTY":
                return "empty("+prettyPrint(treeNode.getChild(0))+")";
            
            case "AST_EXIT":
                return "exit("+prettyPrint(treeNode.getChild(0))+")";
            
            case "AST_ENCAPS_LIST":
            {
                String encapsList = "\"";
                for(TreeNode<INode> child : treeNode.getChildren()){
                    if("string".equals( child.getObj().getString("type") )){
                        encapsList += getCode(child);
                    }
                    else{
                        encapsList += prettyPrint(child);
                    }
                }
                encapsList += "\"";
                return encapsList;
            }
            
            case "AST_FOR":
                return "for(" + prettyPrint(treeNode.getChild(0)) + ";" + prettyPrint(treeNode.getChild(1)) + ";" + prettyPrint(treeNode.getChild(2)) + ")" + statementsInBrackets(treeNode.getChild(3));
            
            case "AST_EXPR_LIST":
                return joinPrettyPrintChildren(treeNode, ", ");
            
            case "AST_POST_INC":
                return prettyPrint(treeNode.getChild(0)) + "++";
            case "AST_POST_DEC":
                return prettyPrint(treeNode.getChild(0)) + "--";
            case "AST_PRE_INC":
                return "++" + prettyPrint(treeNode.getChild(0));
            case "AST_PRE_DEC":
                return "--" + prettyPrint(treeNode.getChild(0));
                
            case "AST_GLOBAL":
                return "global " + prettyPrint(treeNode.getChild(0));
            
            case "AST_GOTO":
                return "goto " + getCode(treeNode.getChild(0));
            
            case "AST_LABEL":
                return getCode(treeNode.getChild(0)) + ":";
                
            case "AST_USE":
                return "use " + AST_USE.string(node) + prettyPrint(treeNode.getChild(0));
            
            case "AST_USE_ELEM":
                return AST_USE.string(node) + getCode(treeNode.getChild(0)) 
                        + (isNullNode(treeNode.getChild(1)) ? "" : " as " + getCode(treeNode.getChild(1)));
                
            case "AST_GROUP_USE":
                return "use " + AST_USE.string(node) + getCode(treeNode.getChild(0)) + "\\{" + joinPrettyPrintChildren(treeNode.getChild(1), ", ") + "};";
                
            case "AST_HALT_COMPILER":
                return "__halt_compiler()";
                
            case "AST_INCLUDE_OR_EVAL":
                return AST_INCLUDE_OR_EVAL.string(node) + "(" + prettyPrint(treeNode.getChild(0)) + ")";
                
            case "AST_INSTANCEOF":
                return prettyPrint(treeNode.getChild(0)) + " instanceof " + prettyPrint(treeNode.getChild(1));
                
            case "AST_ISSET":
                return "isset(" + prettyPrint(treeNode.getChild(0)) + ")";
                
            case "AST_MAGIC_CONST":
                return "__" + AST_MAGIC_CONST.string(node) + "__";
            
            case "AST_NEW":
                return "new " + prettyPrint(treeNode.getChild(0)) + "(" +prettyPrint(treeNode.getChild(1)) + ")";
                
            case "AST_METHOD_CALL":
                return prettyPrint(treeNode.getChild(0)) + "->" 
                        + (isString(treeNode.getChild(1)) ? getCode(treeNode.getChild(1)) : prettyPrint(treeNode.getChild(1)))
                        + "("
                        + prettyPrint(treeNode.getChild(2)) + ")";
            
            case "AST_NULLABLE_TYPE":
                return "?"+prettyPrint(treeNode.getChild(0));
            
            case "AST_REF":
                return "&" + prettyPrint(treeNode.getChild(0));
                
            case "AST_SHELL_EXEC":
                return "shell_exec(" + prettyPrint(treeNode.getChild(0)) + ")";
                
            case "AST_STATIC":
                return "static " + prettyPrint(treeNode.getChild(0)) 
                        + (isNullNode(treeNode.getChild(1)) ? "" : "="+prettyPrint(treeNode.getChild(1)));
                
            case "AST_STATIC_CALL":
                return prettyPrint(treeNode.getChild(0)) + "::" + getCode(treeNode.getChild(1)) + "(" + prettyPrint(treeNode.getChild(2)) + ")";
                
            case "AST_THROW":
                return "throw " + prettyPrint(treeNode.getChild(0));
                
            case "AST_UNARY_OP":
                return AST_UNARY_OP.string(node)+prettyPrint(treeNode.getChild(0));
                
            case "AST_UNSET":
                return "unset(" + prettyPrint(treeNode.getChild(0)) + ")";
                
            case "AST_SWITCH":
                return "switch (" + prettyPrint(treeNode.getChild(0)) + ")" + statementsInBrackets(treeNode.getChild(1));
            
            case "AST_SWITCH_LIST":
                return joinPrettyPrintChildren(treeNode, "\n") + "\n";
            
            case "AST_SWITCH_CASE":
                return indent() + "case " + prettyPrint(treeNode.getChild(0)) + ":" + statementsInBrackets(treeNode.getChild(1));
            
            case "AST_YIELD":
                return "yield (" + prettyPrint(treeNode.getChild(0)) + ")" 
                        + (isNullNode(treeNode.getChild(1)) ? "" : " => (" + prettyPrint(treeNode.getChild(1)) + ")");
                
            case "AST_YIELD_FROM":
                return "yield from (" +  prettyPrint(treeNode.getChild(0)) + ")";
                
            case "AST_TRAIT_ALIAS":
                return prettyPrint(treeNode.getChild(0)) + " as " + getCode(treeNode.getChild(1));
                
            case "AST_STATIC_PROP":
                return prettyPrint(treeNode.getChild(0)) + "::$" +getCode(treeNode.getChild(1));
                
            default:
                return "NOT SUPPORTED!" + treeNode.getObj().id();
        }
    }
    
    private String statementsInBrackets(TreeNode<INode> statementList)
    {
        String retval = "\n" + indent() + "{\n";
        indent++;
        retval += prettyPrint(statementList);
        indent--;
        retval += indent() + "}";
        
        return retval;
    }
    
    private boolean isNullNode(TreeNode<INode> treeNode){
        return treeNode.getObj().isNullNode();
    }
    
    private String getCode(TreeNode<INode> treeNode){
        return treeNode.getObj().getString("code");
    }
    
    private boolean isString(TreeNode<INode> treeNode){
        return "string".equals(treeNode.getObj().get("type"));
    }
    
    private String joinPrettyPrintChildren(TreeNode<INode> parent, String dec){
        StringJoiner joiner = new StringJoiner(dec);
        for(TreeNode child : parent.getChildren()){
            joiner.add(prettyPrint(child));
        }
        return joiner.toString();
    }
    
    
    private String indent(){
        return StringUtils.repeat("  ", indent);
    }
    
    private String indent(int offset){
        return StringUtils.repeat("  ", indent + offset);
    }
    
    private String cleanupBrackets(String code){
        if(code.startsWith("(") && code.endsWith(")"))
           return code.substring(1, code.length()-1);
        
        return code;
    }
    
    public static void main(String[] args) throws Exception{
        Neo4jDB db = new Neo4JConnector("bolt://localhost:7687", "neo4j", "admin");       
        PrintAST printAST = new PrintAST();
        
        INode topNode = db.findNode(1L);
        
        TreeNode<INode> tree = Util.createTree(db, topNode);
        
        System.out.println("");
        System.out.println("");
        System.out.println("" + printAST.prettyPrint(tree));
        
        db.close();
        
        int i = 10;
    }
}
