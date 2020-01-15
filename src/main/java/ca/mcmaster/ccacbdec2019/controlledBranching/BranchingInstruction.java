/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.ccacbdec2019.controlledBranching;

import ilog.concert.IloNumVar;

/**
 *
 * @author tamvadss
 */
public class BranchingInstruction {
    public  String branchingVarName  =null;
    public  double branchingBound  ;
    public  boolean  isBranchingDirectionDown ; //dir down < 
    
    public void print (){
        if ( branchingVarName  !=null){
            System.out.println(branchingVarName);
            System.out.println(branchingBound);
            System.out.println(isBranchingDirectionDown);
        }
    }
}
