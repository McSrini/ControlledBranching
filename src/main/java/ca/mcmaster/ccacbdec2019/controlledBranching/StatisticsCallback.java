/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.ccacbdec2019.controlledBranching;

import static ca.mcmaster.ccacbdec2019.Constants.ZERO;
import ilog.concert.IloException;
import ilog.cplex.IloCplex;

/**
 *
 * @author tamvadss
 */
public class StatisticsCallback extends IloCplex.NodeCallback {
    public long nodesWithBranchingOverrides = ZERO;
    protected void main() throws IloException {
        //
        nodesWithBranchingOverrides = ZERO;
        if (ZERO<  getNremainingNodes64 ()){
            for (long leafNum = ZERO; leafNum< getNremainingNodes64 ();   leafNum++){

                InstructionTree instructionTree_for_thisNode =  (InstructionTree) getNodeData(leafNum);
                boolean conditionOne =  (null== instructionTree_for_thisNode);
                boolean conditionTwo =  (null!= instructionTree_for_thisNode) && 
                                        (null==instructionTree_for_thisNode.leftChildId) &&
                                        (null ==instructionTree_for_thisNode.rightChildId);
                if (conditionOne || conditionTwo) {
                    //leaf has no branching overrides
                }else {
                    //leaf has branching overrides 
                    nodesWithBranchingOverrides ++;
                }
            }
        }
        abort();
    }
    
}
