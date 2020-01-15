/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.ccacbdec2019.rampup;

import static ca.mcmaster.ccacbdec2019.Constants.ONE;
import static ca.mcmaster.ccacbdec2019.Constants.ZERO;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author tamvadss
 */
public class NodeAttachment {
    
    public String  nodeIdentifer = null;
    
    //reference to parent node
    public  NodeAttachment  parentData = null;
    
    //ref to kids, filled at the end of ramp up
    public  NodeAttachment  leftChildReference = null;
    public  NodeAttachment  rightChildReference = null;
    
    //branching condition from parent
    public  String branchingVarName  =null;
    public  double branchingBound  ;
    public  boolean  isBranchingDirectionDown ; //dir down < 
    
    //populated into all the leafs at the end of ramp up, used during best first sequencing
    public Double lpRelaxedValue = null;
    
    
    //reference counts used by controlled branching
    public int  leftRefCount_leafs=ZERO;
    public int  rightRefCount_leafs=ZERO;
    public int  leftRefCount_nonLeafs=ZERO;
    public int  rightRefCount_nonLeafs=ZERO;
  
    
    public void printBranchingConditionFromParent () {
        
        System.out.println( " Node id = " +  nodeIdentifer + " is " + 
                " Parent id = " + (parentData==null? -ONE : parentData.nodeIdentifer)) ;
        
        if (null!=branchingVarName) {
            System.out.println("\n"+branchingVarName) ;
            System.out.println(branchingBound) ;
            System.out.println(isBranchingDirectionDown) ;
        }else {
            System.out.println("\n reached root !") ;
        }
        
    }
}
