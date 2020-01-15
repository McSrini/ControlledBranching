/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.ccacbdec2019.controlledBranching;

import static ca.mcmaster.ccacbdec2019.Constants.*;  
import static ca.mcmaster.ccacbdec2019.Parameters.USE_DEFERRED_MERGING;
import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author tamvadss
 */
public class ReincarnationNodeCallback extends IloCplex.NodeCallback {
    
    public long numberOfNodeRedirects = ZERO;
     
        
    protected void main() throws IloException {
        //
        if (getNremainingNodes64()>ZERO){
            long leafNum = ZERO;
            if (!USE_DEFERRED_MERGING)  {
                leafNum=      selectLeafNumber ();
            }
            
            if (leafNum< ZERO){
                abort();
            }else if (ZERO==leafNum) {
                //take default node selection
                 
            } else   {
                selectNode(leafNum);
                numberOfNodeRedirects++;
                
            }
            
            
        }        
    }
    
 
        
 
     
    private long selectLeafNumber () throws IloException{
        long result = -ONE;
                
        final long LEAFCOUNT =getNremainingNodes64();
                
        for (long leafNum = ZERO; leafNum< LEAFCOUNT;   leafNum++){

            
            if (doesLeafHaveBranchingOverrides(leafNum)) {
                 
                //leaf has branching overrides 
                result = leafNum;
                break;
            }
        }
                
        return result;
    }
    
    private boolean doesLeafHaveBranchingOverrides (long leafNum) throws IloException {
        InstructionTree instructionTree_for_thisNode =  (InstructionTree) getNodeData(leafNum);
        boolean conditionOne =  (null== instructionTree_for_thisNode);
        boolean conditionTwo =  (null!= instructionTree_for_thisNode) && 
                                (null==instructionTree_for_thisNode.leftChildId) &&
                                (null ==instructionTree_for_thisNode.rightChildId);
        
        return ! (conditionOne || conditionTwo) ;
    }
}
