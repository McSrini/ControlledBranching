/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.ccacbdec2019.controlledBranching;

import static ca.mcmaster.ccacbdec2019.Constants.ZERO;
import ca.mcmaster.ccacbdec2019.rampup.NodeAttachment;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author tamvadss
 */
public class InstructionTree {
    
    
    //node id 
    public String nodeId=null;
    
    //each node has instructions for creating the LCA nodes on its left and right
    //
    // Note that this instruction tree node may have only 1 kid at the root
    
    public String leftChildId=null;
    public List<BranchingInstruction>  leftBranchInstructions= new ArrayList<BranchingInstruction>();
    public InstructionTree leftSideInstructionTree=null;
    
    public String  rightChildId =null;
    public List<BranchingInstruction>   rightBranchInstructions=  new ArrayList<BranchingInstruction>(); 
    public InstructionTree rightSideInstructionTree=null;
    
    public boolean isLeaf () {
        return null == leftChildId && null== rightChildId;
    }
    
    public  void print(){
        System.out.println ("InstructionTree: " + nodeId);
        
        if (null!=leftChildId){
            System.out.println ("leftChildId: " + leftChildId);
            for (BranchingInstruction bi:  leftBranchInstructions) {
                bi.print();
            }
        }
        
        if (rightChildId!=null){
            System.out.println ("rightChildId: " + rightChildId);
            for (BranchingInstruction bi:  rightBranchInstructions) {
                bi.print();
            }
        }
        
        if (null!=leftSideInstructionTree) leftSideInstructionTree.print();
        if (null != rightSideInstructionTree) rightSideInstructionTree.print();
    }
    
        
    public int getNumberOfNonLeafNodes (   ){
        return getNumberOfNonLeafNodes(this) ;
    }
    
       
    private int getNumberOfNonLeafNodes (InstructionTree iTree){
        int count = ZERO;
        int leftSideCount = ZERO;
        int rightSideCount = ZERO;
        
        if ((iTree.leftChildId!=null) || (iTree.rightChildId!=null)){
            count ++;
        }
        
        if (iTree.leftChildId!=null){
            leftSideCount =  getNumberOfNonLeafNodes (iTree.leftSideInstructionTree) ;
        }
        if (iTree.rightChildId!=null){
            rightSideCount =   getNumberOfNonLeafNodes (iTree.rightSideInstructionTree) ;
        }
        
        return count+ leftSideCount + rightSideCount;
    }
    
}
