/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.gui.astrenderer;

/**
 *
 * @author blubbomat
 */
public class FileId {
    private final long id;
    private final String path;

    public FileId(long id, String path) {
        this.id = id;
        this.path = path;
    }

    public long getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return path;
    }
    
    
    
    
}
