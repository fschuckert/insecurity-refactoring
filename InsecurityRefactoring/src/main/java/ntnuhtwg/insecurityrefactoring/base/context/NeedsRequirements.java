/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.context;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import ntnuhtwg.insecurityrefactoring.base.context.Context;

/**
 *
 * @author blubbomat
 */
public interface NeedsRequirements {
    
    public PossibleRequirements getRequirements();
    
    public void setRequirements(PossibleRequirements requirmentList);
}
