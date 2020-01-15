/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.ccacbdec2019.controlledBranching;

import static ca.mcmaster.ccacbdec2019.Constants.*;
import ca.mcmaster.ccacbdec2019.rampup.NodeAttachment;
import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author tamvadss
 * 
 * in the writeup, show pseudo code blocks for methods in this file
 * 
 * 
 */
public class InstructionTreeCreator {
    private NodeAttachment rootNode =null;
 
    
    public InstructionTreeCreator(NodeAttachment rootNodeData ) {
        rootNode=     rootNodeData;         
    }
    
    //travel up from every selected leaf and clear the parent's refcounts
    ////
    //use this method once the CB instruction tree has been created to re-use the ramped up tree 
    public void clearRefCounts ( List<NodeAttachment> selectedLeafs) {
        for (NodeAttachment thisLeaf : selectedLeafs ){
            
            NodeAttachment currentNode = thisLeaf ;
            NodeAttachment parent = thisLeaf.parentData;
            
            while (null != parent) {
                
                //System.out.println("Current node is " + currentNode.nodeIdentifer) ;
                //System.out.println("Parent node is " + parent.nodeIdentifer) ;
                
                if (currentNode.isBranchingDirectionDown) {
                    parent.leftRefCount_leafs =ZERO;
                    parent.leftRefCount_nonLeafs= ZERO;
                }    else {
                    parent.rightRefCount_leafs=ZERO;
                    parent.rightRefCount_nonLeafs= ZERO;
                }
                
                //climb up
                currentNode = parent;
                parent = parent.parentData;
                                
            }//end while 
            
        }//end for
        System.out.println("Cleared the refcounts.") ;
        //this.printRefCounts(rootNode);
    }
    
    //travel up from every selected leaf and update the parent's refcount on the appropriate side
    public void setRefCounts ( List<NodeAttachment> selectedLeafs) {
        for (NodeAttachment thisLeaf : selectedLeafs ){
            
            NodeAttachment currentNode = thisLeaf ;
            boolean currentNodeIsALeaf = true;
            NodeAttachment parent = thisLeaf.parentData;
            
            while (null != parent) {
                
                if (currentNode.isBranchingDirectionDown) {
                    parent.leftRefCount_leafs ++;
                }    else {
                    parent.rightRefCount_leafs++;
                }
                
                if (!currentNodeIsALeaf){
                    if (currentNode.isBranchingDirectionDown) {
                        parent.leftRefCount_nonLeafs = ONE + (currentNode.leftRefCount_nonLeafs + currentNode.rightRefCount_nonLeafs);
                    }else {
                        parent.rightRefCount_nonLeafs= ONE + (currentNode.leftRefCount_nonLeafs + currentNode.rightRefCount_nonLeafs);
                    }
                }
                               
                //climb up
                currentNode = parent;
                parent = parent.parentData;
                currentNodeIsALeaf=false;
                
            }//end while 
            
        }//end for
        System.out.println("Set the refcounts.") ;
        //this.printRefCounts(rootNode);
    }
    
    
    //start from the root node of the cplex tree
    //create an instruction tree node representing this root node
    //
    //use the left and right refcounts (if any)  to move to the next node N which has both refcounts >0
    //if N is perfect , dont accumulate brannching instructions for it, else repeat the process with N as root
    public InstructionTree getCBInstructionTree () {        
        return getCBInstructionTree(this.rootNode);
    }
    
    //too many recursive calls can lead to stack overflow error
    //
    private InstructionTree getCBInstructionTree (NodeAttachment node) {
        InstructionTree result = new InstructionTree ();
        
        result.nodeId= node.nodeIdentifer;
        
        boolean isLeaf = (ZERO == node.leftRefCount_leafs+node.rightRefCount_leafs) ;
        boolean isPerfectlyPacked = this.isNodePerfectlyPacked(node);
       
        if (! (isLeaf || isPerfectlyPacked)){
             
            if (node.leftRefCount_leafs>ZERO){
                List <BranchingInstruction> branchingInstructions = new ArrayList <BranchingInstruction>  ();
                NodeAttachment nextNode_LeftSide = getNextNodeInPath (   node, true, branchingInstructions);
               
                result.leftChildId=nextNode_LeftSide.nodeIdentifer;
                result.leftBranchInstructions=branchingInstructions;
                result.leftSideInstructionTree=getCBInstructionTree (nextNode_LeftSide);
                 
            }
            
            if (node.rightRefCount_leafs>ZERO){
                List <BranchingInstruction> branchingInstructions = new ArrayList <BranchingInstruction>  ();
                NodeAttachment nextNode_RightSide = getNextNodeInPath (   node, false, branchingInstructions);
                result.rightChildId=nextNode_RightSide.nodeIdentifer;
                result.rightBranchInstructions=branchingInstructions;
                result.rightSideInstructionTree=getCBInstructionTree(nextNode_RightSide);
                 
            }
        }
        
        return result;       
    }
    
    //get next node of the instruction tree in the given direction, and also
    //the branching instructions to get to it
    //
    //Assumes that the next node or leaf exists.
    private NodeAttachment getNextNodeInPath ( NodeAttachment node, boolean isLeftSide, List <BranchingInstruction> branchingInstructions) {
          
        //start with finding the immediate next node in the required direction
        NodeAttachment nextNode = isLeftSide ? node.leftChildReference: node.rightChildReference;  
        
        BranchingInstruction instruction = new BranchingInstruction ();
        instruction.branchingBound=nextNode.branchingBound;
        instruction.branchingVarName=nextNode.branchingVarName;
        instruction.isBranchingDirectionDown=nextNode.isBranchingDirectionDown;
        branchingInstructions.add (instruction);
        
        //traverse until the next node is a leaf  or a non-leaf node with leafs on both sides
        while ( (nextNode.leftRefCount_leafs * nextNode.rightRefCount_leafs ==ZERO) && 
                (nextNode.leftRefCount_leafs + nextNode.rightRefCount_leafs > ZERO) ){     
            
            //move to the side having non zero ref count of leafs
            if (nextNode.leftRefCount_leafs==ZERO){                
                nextNode = nextNode.rightChildReference;     
            }else{                 
                nextNode = nextNode.leftChildReference;                
            }    
            
            BranchingInstruction bi = new BranchingInstruction ();
            bi.branchingBound=nextNode.branchingBound;
            bi.branchingVarName=nextNode.branchingVarName;
            bi.isBranchingDirectionDown=nextNode.isBranchingDirectionDown;
            branchingInstructions.add (bi);
             
        }
        return nextNode;
    }
    
    
    
    private boolean isNodePerfectlyPacked (NodeAttachment node) {
        int leafCount = node.leftRefCount_leafs + node.rightRefCount_leafs;
        int nonLeafCount = ONE +  node.leftRefCount_nonLeafs + node.rightRefCount_nonLeafs; // ONE is added for self
        return ( ONE +nonLeafCount) == leafCount;
    }
    
    private void printRefCounts (NodeAttachment node){
        System.out.println(node.nodeIdentifer + ", " + node.leftRefCount_leafs + ", "+ node.rightRefCount_leafs) ;
        System.out.println(node.nodeIdentifer + ", " + node. leftRefCount_nonLeafs + ", "+ node. rightRefCount_nonLeafs) ;
        if (node.leftChildReference!=null) printRefCounts(node.leftChildReference);
        if (node.rightChildReference!=null)  printRefCounts (node.rightChildReference );
    }
    
    
}
