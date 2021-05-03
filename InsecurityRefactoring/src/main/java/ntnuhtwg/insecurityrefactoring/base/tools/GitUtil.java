/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.tools;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import ntnuhtwg.insecurityrefactoring.base.Util;
import ntnuhtwg.insecurityrefactoring.base.Util;

/**
 *
 * @author blubbomat
 */
public class GitUtil {
    public static void commitAndPush(String msg, String gitPath){
        File folder = new File(gitPath);
        
        try {
            String addChanges = "git add .";    
            Util.runCommand(addChanges, folder);
            
            String commit = "git commit -m '" + msg + "'";
            Util.runCommand(commit, folder);
            
            String push = "git push";
            Util.runCommand(push, folder);
            
        } catch (IOException ex) {
            Logger.getLogger(GitUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(GitUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String[] args){
        
        commitAndPush("SQLi redirect to home", "/home/blubbomat/Development/IR_phpbb/phpBB");
    }
}
