/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.db.neo4j.dsl.cypher;

/**
 *
 * @author blubbomat
 */
public class ControlStatementsQuery {
    public final long fromId;
    public final long toId;
    public final String varName;

    public ControlStatementsQuery(long fromId, long toId, String varName) {
        this.fromId = fromId;
        this.toId = toId;
        this.varName = varName;
    }
    
    
}
