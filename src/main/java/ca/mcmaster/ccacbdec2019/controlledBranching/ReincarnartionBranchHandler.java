/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.ccacbdec2019.controlledBranching;

import static ca.mcmaster.ccacbdec2019.Constants.*;  
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.BranchDirection;
 
import java.util.List;
import java.util.Map;

/**
 *
 * @author tamvadss
 *  
 * 
 */
public class ReincarnartionBranchHandler extends IloCplex.BranchCallback {
    
    private  Map<String, IloNumVar> variableMap = null;
    private InstructionTree instruction_Tree;
      
    
    public  ReincarnartionBranchHandler (  InstructionTree insTree  , Map<String, IloNumVar> variablesInModel ){
        this.variableMap =variablesInModel;
        instruction_Tree=     insTree; 
    }
    
    
 
    protected void main() throws IloException {
        //
        if ( getNbranches()> 0 ){  
            
            InstructionTree instructionTree_for_thisNode =  (InstructionTree) getNodeData();
            
            String thisNodeID=getNodeId().toString();
            if (thisNodeID.equals( MIPROOT_NODE_ID)){
                //root node
                instructionTree_for_thisNode= instruction_Tree;
                
                 
            } 
            
             
            if (null != instructionTree_for_thisNode) {
                
                if (instructionTree_for_thisNode.leftChildId!=null){
                 
                    List<BranchingInstruction> leftChildBranchingInstructions = instructionTree_for_thisNode.leftBranchInstructions;
                    InstructionTree leftSideInstructionTree= instructionTree_for_thisNode.leftSideInstructionTree ;

                    //create the left child 
                    IloCplex.NodeId  id  = createChild (leftChildBranchingInstructions, leftSideInstructionTree) ;

                    //System.out.println("thisNodeID "+ thisNodeID + " left child is " + 
                            //id.toString() + " corresponds to "+ leftSideInstructionTree.nodeId);
                }
                
                if (instructionTree_for_thisNode.rightChildId!=null){
                 
                    List<BranchingInstruction> rightChildBranchingInstructions = instructionTree_for_thisNode.rightBranchInstructions;
                    InstructionTree rightSideInstructionTree = instructionTree_for_thisNode.rightSideInstructionTree;

                    //create the right child
                    IloCplex.NodeId  id  = createChild (rightChildBranchingInstructions, rightSideInstructionTree) ;
                    String rightChildID = id.toString();

                    //System.out.println("thisNodeID "+ thisNodeID + 
                            //" right child is " + rightChildID + " corresponds to "+ rightSideInstructionTree.nodeId);
                }
            } 
        } // end  getNbranches()> 0
    }//main
    
    private IloCplex.NodeId  createChild (List<BranchingInstruction> branchingInstructions,
            InstructionTree iTree ) throws IloException {
        
        // branches about to be created
        int size = branchingInstructions.size();
        IloNumVar[] vars = new IloNumVar[size] ;
        double[] bounds = new double[size];
        IloCplex.BranchDirection[ ]  dirs = new  IloCplex.BranchDirection[size];
        
        for (int index = ZERO; index <   branchingInstructions.size(); index ++){
            BranchingInstruction bi = branchingInstructions.get(index);
            vars[index] = variableMap.get(bi.branchingVarName);
            bounds[index] = bi.branchingBound;
            dirs[index] = bi.isBranchingDirectionDown ? BranchDirection.Down : BranchDirection.Up;
            
            //System.out.print("Reincaranating child with ") ;
            //bi.print();
        }
        
        return makeBranch (vars, bounds, dirs ,   getObjValue(), iTree);
    }
    
    

}
