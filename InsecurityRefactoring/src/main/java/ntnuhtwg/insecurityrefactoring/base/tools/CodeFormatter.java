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
import ntnuhtwg.insecurityrefactoring.base.GlobalSettings;
import ntnuhtwg.insecurityrefactoring.base.GlobalSettings;
import ntnuhtwg.insecurityrefactoring.base.Util;
import ntnuhtwg.insecurityrefactoring.base.Util;

/**
 *
 * @author blubbomat
 */
public class CodeFormatter {

    public static void formatFolder(String path) {
        File baseDir = new File(path);
        formatRecursively(baseDir);
    }

    private static void formatRecursively(File folder) {
//        format(folder);
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                formatRecursively(file);
            }
            else if(file.getName().endsWith(".php")){
                format(file);
            }
        }
    }

    private static void format(File phpFile) {

        try {      
            String command = "php ./phptidy/phptidy.php replace " + phpFile.getAbsolutePath() + " " + phpFile.getAbsolutePath();
            File rootFolder = new File(GlobalSettings.rootFolder);
            Util.runCommand(command, rootFolder);
        } catch (IOException ex) {
            Logger.getLogger(CodeFormatter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(CodeFormatter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        formatFolder("/home/blubbomat/Development/simple");
    }
}
