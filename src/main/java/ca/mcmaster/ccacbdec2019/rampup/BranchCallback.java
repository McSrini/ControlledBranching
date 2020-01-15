/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.ccacbdec2019.rampup;

import static ca.mcmaster.ccacbdec2019.Constants.ONE;
import static ca.mcmaster.ccacbdec2019.Constants.TWO;
import static ca.mcmaster.ccacbdec2019.Constants.ZERO;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

/**
 *
 * @author tamvadss
 */
public class BranchCallback  extends IloCplex.BranchCallback {
    
    public NodeAttachment rootAttachment = null  ;
    
    protected void main() throws IloException {        
        //
        if ( getNbranches()> ZERO ){  
            //get node data
            NodeAttachment thisNodeData = (NodeAttachment) getNodeData();
            boolean isRoot = (thisNodeData==null );
            if (isRoot){
                thisNodeData = new NodeAttachment ();
                setNodeData(thisNodeData  );
                rootAttachment= thisNodeData;
                rootAttachment.nodeIdentifer = getNodeId().toString();
            }
            
            
                
            
            //get the branches about to be created
            IloNumVar[][] vars = new IloNumVar[TWO][] ;
            double[ ][] bounds = new double[TWO ][];
            IloCplex.BranchDirection[ ][]  dirs = new  IloCplex.BranchDirection[ TWO][];
            getBranches(  vars, bounds, dirs);
                
            //now allow  both kids to spawn
            for (int childNum = ZERO ;childNum<getNbranches();  childNum++) {   
                
                NodeAttachment attach = new NodeAttachment ();
                attach.parentData = thisNodeData;               
                
                IloNumVar var = vars[childNum][ZERO];
                attach.branchingVarName = var.getName();
                double bound = bounds[childNum][ZERO];
                attach.branchingBound  =  bound;
                IloCplex.BranchDirection dir =  dirs[childNum][ZERO];                        
                attach.isBranchingDirectionDown =dir.equals( IloCplex.BranchDirection.Down) ;
                
               
                
                //create the kid
                IloCplex.NodeId  id = makeBranch(var,bound, dir ,getObjValue(), attach);
                attach.nodeIdentifer=id.toString();
                
                 
                //System.out.println(thisNodeData.nodeIdentifer + " created child " +id + " with var " +var.getName()+
                       //  " bound " + bound + " dir " + (dir.equals( IloCplex.BranchDirection.Down)? "down":"up")) ;

            }                 
                    
        }
    }
    
}
