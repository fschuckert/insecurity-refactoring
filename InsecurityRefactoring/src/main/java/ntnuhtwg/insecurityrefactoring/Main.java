/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring;

import ntnuhtwg.insecurityrefactoring.refactor.base.PipPathInformation;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import ntnuhtwg.insecurityrefactoring.base.ASTType;
import ntnuhtwg.insecurityrefactoring.base.SourceLocation;
import ntnuhtwg.insecurityrefactoring.base.Util;
import ntnuhtwg.insecurityrefactoring.base.info.PipInformation;
import ntnuhtwg.insecurityrefactoring.base.stats.StringCounter;
import ntnuhtwg.insecurityrefactoring.base.tree.DFATreeNode;
import ntnuhtwg.insecurityrefactoring.gui.GUI;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.Neo4JConnector;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.Neo4jDB;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.Neo4jEmbedded;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;
import ntnuhtwg.insecurityrefactoring.base.patterns.PatternStorage;
import ntnuhtwg.insecurityrefactoring.refactor.base.ScanProgress;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

/**
 *
 * @author blubbomat
 */
public class Main {

    public static void main(String[] args) throws Exception {
        CommandLine cmd = null;
        try {
            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse(options(), args);
        } catch (Exception ex) {
            printHelp();
            System.out.println("" + ex.getMessage());
            return;
        }

        Framework framework = new Framework();
        framework.init();

        LinkedList<String> specificPattern = new LinkedList<>();
        boolean requiresSanitize = cmd.hasOption("r");

        if (cmd.hasOption("v")) {
            String specificPatternStr = cmd.getOptionValue("v");
            specificPattern.add(specificPatternStr);
        }

        if (cmd.hasOption("g")) {
            GUI gui = new GUI();
            gui.init(framework);
        } else if (cmd.hasOption("p")) {
            SourceLocation specificLocation = null;
            if(cmd.hasOption("specific-sink")){
                String sinkPath = cmd.getOptionValue("specific-sink");
                specificLocation = new SourceLocation(sinkPath);
            }
            String path = cmd.getOptionValue("p");
            ScanProgress scanProgress = new ScanProgress();
            framework.scan(path, !cmd.hasOption("n"), specificPattern, scanProgress, cmd.hasOption("c"), specificLocation);
            List<PipInformation> pipInformation = framework.getPipInformation();

            if (cmd.hasOption("o")) {
                for (PipInformation pip : pipInformation) {
                    for (PipPathInformation pathInfo : pip.getPossibleSources()) {
                        if (pip.isVulnSink() && pathInfo.isVulnerable()) {
                            System.out.println("\nVulnerability sink: " + pip.getSink().getObj() + "\n####################################################################################################################################");
                            printSourceCodeRec(framework, pathInfo.getSource(), null);
                            System.out.println("####################################################################################################################################\n");
                            break;
                        }
                    }
                }
            }

            StringCounter pipTypes = new StringCounter();
            StringCounter pipSpecific = new StringCounter();

            for (DFATreeNode pip : framework.getPips(requiresSanitize, false)) {
                String type = pip.getSinkPattern().getVulnType();
                pipTypes.countString(type);
                pipSpecific.countString("(" + type + ") " + pip.getSinkPattern().getName());

            }
            System.out.println("");
            System.out.println("##### Pip info #####");
            pipTypes.prettyPrint("PIP TYPE ");
            pipSpecific.prettyPrint("PIP SPECIFIC ");

            int paths = 0;
            int vulns = 0;
            int timeouts = 0;
            int vulnerabilities = 0;
            for (PipInformation pipInfo : pipInformation) {
                for (PipPathInformation pathInfo : pipInfo.getPossibleSources()) {
                    paths++;
                    if (pathInfo.isVulnerable()) {
                        vulns++;
                    }
                    if (pathInfo.isContainsTimeout()) {
                        timeouts++;
                    }
                }
                if (pipInfo.isVulnerability()) {
                    vulnerabilities++;
                }
            }

            System.out.println("");
            System.out.println("##### Paths info #####");
            System.out.println("Path Amount: " + paths);
            System.out.println("Path Vulns: " + vulns);
            System.out.println("Path Timeouts: " + timeouts);

            System.out.println("");
            System.out.println("##### Summary #####");
            System.out.println("FOUND PIPs: " + framework.getPips(requiresSanitize, false).size());
            System.out.println("Vulnerable: " + vulnerabilities);
        } else if (cmd.hasOption("s")) {
            String path = cmd.getOptionValue("s");
            framework.scanSubFolders(path, specificPattern, requiresSanitize, new ScanProgress(), cmd.hasOption("c"));

        }
        else if(cmd.hasOption("f")){
            String path = cmd.getOptionValue("f");
            framework.scan(path, false, specificPattern, new ScanProgress(), cmd.hasOption("c"), null);
            framework.formatCode();
        }
        else {
            printHelp();
        }

        if (!cmd.hasOption("g")) {
            framework.close();
            System.out.println("Finished closing db... should close now.");
        }

//        cmd.
    }

    private static void printSourceCodeRec(Framework framework, DFATreeNode node, SourceLocation lastPrinted) {
        if (node == null) {
            return;
        }

        if (node.getObj() != null) {
            SourceLocation location = Util.codeLocation(framework.getDb(), node.getObj());
            if (location != null && !location.equals(lastPrinted)) {
                System.out.println(Util.codeLocation(framework.getDb(), node.getObj()).codeSnippet(true));
                lastPrinted = location;
            }
        }
        printSourceCodeRec(framework, node.getParent_(), lastPrinted);
    }

    private static void printHelp() {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("Insecurity Refactoring", options());
    }

    private static Options options() {
        Options options = new Options();
        options.addOption("p", "path", true, "Path to analyse.");

        options.addOption("s", "subprojects", true, "Scan the subfolder and store results in txt files.");

        options.addOption("g", "gui", false, "Launch the User interface to view the results and do refactoring choices. Only works in combination with analyzing one project");
        options.addOption("n", "skip-neo4j-prepare", false, "Skip the preparing of neo4jdb");

        options.addOption("v", "vuln", true, "Specific sink (category currently not supported)");
        options.addOption("r", "requires-san", false, "Specific sink (category currently not supported)");

        options.addOption("o", "output vulns", false, "Outsputs a vulnerable path for each vulnerability");
        options.addOption("f", "format", true, "Formats the folder");
        
        options.addOption("c", "control-check", false, "Do a check for control functions. Takes a lot of time!");
        options.addOption("specificsink", false, "specific sink code location: provided like following path:linenumber");

        return options;
    }
}
