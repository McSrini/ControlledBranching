/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.ccacbdec2019.rampup;

import static ca.mcmaster.ccacbdec2019.Constants.*;
import static ca.mcmaster.ccacbdec2019.Parameters.*;
import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author tamvadss
 */
public class NodeCallback extends IloCplex.NodeCallback {
    
    public List<NodeAttachment> leafList = new ArrayList <NodeAttachment> ( );
    public NodeAttachment rootNodeAttachment = null;
     
    protected void main() throws IloException {
        
        if ( RAMP_UP_SIZE <=  getNremainingNodes64 ()){
            
            System.out.println("getNremainingNodes64 = " + getNremainingNodes64 ()) ;
            /*for (long nodeNum = ZERO; nodeNum < getNremainingNodes64(); nodeNum ++) {
                NodeAttachment nodeAttachment = (NodeAttachment) getNodeData (nodeNum) ;  
                System.out.println("leaf id " +nodeAttachment.nodeIdentifer ); 
            }*/
            
            
            //prepare the link from each node to its 2 kids ( or 1 kid )
            for (long nodeNum = ZERO; nodeNum < getNremainingNodes64(); nodeNum ++) {
                NodeAttachment nodeAttachment = (NodeAttachment) getNodeData (nodeNum) ;  
                
                //collect this list into our list of leafs, which is made available to the outside world
                leafList.add (nodeAttachment );
                nodeAttachment.lpRelaxedValue = getObjValue( nodeNum  ) ;
                                               
                //climb up
                NodeAttachment parent = nodeAttachment.parentData;
                while (parent!=null) {
                    
                    //we treat the down branch as the left child
                    if (nodeAttachment.isBranchingDirectionDown) {
                        parent.leftChildReference = nodeAttachment;
                    } else {
                        parent.rightChildReference = nodeAttachment;
                    }
                    
                    if (null==  parent.rightChildReference || null == parent.leftChildReference ) {
                        //climb up
                        nodeAttachment =parent;
                        parent = nodeAttachment.parentData;
                    }else {                       
                        //no need to keep climbing
                        break;
                    }
                }
                
                if (null==parent) {
                    this.rootNodeAttachment = nodeAttachment;
                }
            }
            
            //printTreeFromTopDown (rootNodeAttachment) ;
            
            abort();
        }
    }
    
    private void printTreeFromTopDown (NodeAttachment nodeAttachment) {
        nodeAttachment.printBranchingConditionFromParent();
        if (nodeAttachment.leftChildReference!=null) printTreeFromTopDown (nodeAttachment.leftChildReference );
        if (nodeAttachment.rightChildReference!=null) printTreeFromTopDown (nodeAttachment.rightChildReference );
    }
    
    private void printLeafBranchingConditions (NodeAttachment attach, long nodeNum) throws IloException {
        //System.out.println(getBranchVar(nodeNum).getName()) ;
        while (attach!=null) {
            //print attach
            attach.printBranchingConditionFromParent();
            attach = attach.parentData;
        }
    }
    
}
