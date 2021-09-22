/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.patterns.impl;

import java.util.HashMap;
import java.util.List;
import ntnuhtwg.insecurityrefactoring.base.Util;
import ntnuhtwg.insecurityrefactoring.base.ast.AnyNode;
import ntnuhtwg.insecurityrefactoring.base.ast.BaseNode;
import ntnuhtwg.insecurityrefactoring.base.ast.FixedNode;
import ntnuhtwg.insecurityrefactoring.base.tree.TreeNode;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;
import ntnuhtwg.insecurityrefactoring.base.patterns.Pattern;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import scala.NotImplementedError;

/**
 *
 * @author blubbomat
 */
public class LanguagePattern extends Pattern {

    private String id;

    private String[] params;

    private JSONObject astPhp;

    public LanguagePattern(String id, String[] params, JSONObject astPhp) {
        this.id = id;
        this.params = params;
        this.astPhp = astPhp;
    }

    public String getId() {
        return id;
    }

    public TreeNode<INode> generateAst(List<TreeNode<INode>> inputs) {
        return generateAstRec(this.astPhp, inputs);
    }

    private TreeNode<INode> generateAstRec(JSONObject astPhp, List<TreeNode<INode>> inputs) {
        if (astPhp.containsKey("type")) {
            String type = (String) astPhp.get("type");

            BaseNode baseNode = new BaseNode();
            baseNode.addProperty("type", type);

            if (astPhp.containsKey("code")) {
                String code = (String) astPhp.get("code");
                if (code.contains("%p")) {
                    int inputIndex = getInputIndex((String) astPhp.get("code"));
                    if (inputIndex < inputs.size()) {
                        TreeNode<INode> node = inputs.get(inputIndex);
                        if (node.getObj() instanceof FixedNode && !((FixedNode) node.getObj()).isCheck()) {
                            FixedNode fixedNode = (FixedNode) node.getObj();
                            baseNode.addProperty("code", fixedNode.getFixedValue());
                        } else if (node.getObj() instanceof BaseNode && "any".equals(node.getObj().get("type"))) {

                        } else {
                            baseNode.addProperty("code", inputs.get(inputIndex).getObj().getString("code"));
                            // TODO: improve to actually just providing a string!
                            //                    throw new NotImplementedError("Pattern incorrect. Probably syntax error requires a fixed value! For: " + type + " value:" + node.getObj());
                        }
                    }
                } else {
                    baseNode.addProperty("code", code);
                }

            }

            if (astPhp.containsKey("flags")) {
                JSONArray flags = (JSONArray) astPhp.get("flags");
                for (Object flag : flags) {
                    String flagStr = (String) flag;
                    if (flagStr.startsWith("%p")) {
                        int index = getInputIndex(flagStr);
                        TreeNode<INode> node = inputs.get(index);
                        if (node.getObj() instanceof FixedNode) {
                            FixedNode fixedNode = (FixedNode) node.getObj();
                            baseNode.addFlag(fixedNode.getFixedValue());
                        } else if (node.getObj() instanceof BaseNode && "any".equals(node.getObj().get("type"))) {
                            // do nothing
                        } else {
                            throw new NotImplementedError("Pattern incorrect. Probably syntax error requires a fixed value! For: " + type + " flag:" + node.getObj());
                        }
                    } else {
                        baseNode.addFlag((String) flag);
                    }
                }
            }

            TreeNode<INode> treeNode = new TreeNode<>(baseNode);

            if (astPhp.containsKey("children")) {
                for (Object childObj : (JSONArray) astPhp.get("children")) {
                    JSONObject child = (JSONObject) childObj;
                    if (child.containsKey("subtree_list")) {
                        // add all remaining inputs
                        String paramId = (String) child.get("subtree_list");
                        int inputIndex = getInputIndex(paramId);
                        for (int i = inputIndex; i < inputs.size(); i++) {

                            INode node = inputs.get(i).getObj();
                            if (node instanceof FixedNode && ((FixedNode) node).isCheck()) {
                                throw new NotImplementedError("Pattern incorrect. Probably syntax error requires a generated value! For: " + id + " " + inputs.get(inputIndex).getObj());
                            }
                            treeNode.addChild(inputs.get(i));
                        }
                    } else {
                        TreeNode<INode> childNode = generateAstRec(child, inputs);
                        treeNode.addChild(childNode);
                    }
                }
            }

            return treeNode;
        } else {
            // add sub tree
//            if(astPhp.containsKey("subtree")){
            String paramId = (String) astPhp.get("subtree");
            int inputIndex = getInputIndex(paramId);
//                System.out.println("id: " + id);

            // setting the index for ... parameters
            if (inputs.size() <= inputIndex) {
                TreeNode<INode> inode = inputs.get(inputs.size() - 1);
                if (inode.getObj() instanceof BaseNode) {
                    BaseNode inputNode = (BaseNode) inode.getObj();

                    if (inputNode.containsKey("...")) {
                        inputIndex = inputs.size() - 1;
                    }
                }

                if (inode.getObj() instanceof FixedNode) {
                    FixedNode fixedNode = (FixedNode) inode.getObj();
                    if (fixedNode.isDots()) {
                        inputIndex = inputs.size() - 1;
                    }
                }
            }

            INode node = inputs.get(inputIndex).getObj(); // TODO crash on binary_op paramIndex 1. Using input...

            if (node instanceof FixedNode && ((FixedNode) node).isCheck()) {
                throw new NotImplementedError("Pattern incorrect. Probably syntax error requires a generated value! For: " + id + " " + inputs.get(inputIndex).getObj());
            }

            return inputs.get(inputIndex);
//            }
        }

    }

    private int getInputIndex(String paramId) {
//        System.out.println("Input for" + paramId + " " + this.getId());
        return Integer.valueOf(paramId.split("%p")[1]);
    }

    @Override
    public String toString() {
        return "<" + id + ">(" + Util.joinStr(params, ",") + ')';
    }
    
    

}
