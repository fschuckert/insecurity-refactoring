/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.exception;

/**
 *
 * @author blubbomat
 */
public class NotExpected extends RuntimeException{

    public NotExpected(String message) {
        super(message);
    }

    public NotExpected(String message, Throwable cause) {
        super(message, cause);
    }
    
    
    
}
