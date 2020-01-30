/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.ccacbdec2019.utils;

import static ca.mcmaster.ccacbdec2019.Constants.ZERO;
import ca.mcmaster.ccacbdec2019.rampup.NodeAttachment;
import ilog.concert.IloException;
import ilog.cplex.IloCplex;

/**
 *
 * @author tamvadss
 */
public class CplexObjectWith_LPEstimate {
    
    //the perfect LCA node that gets converted into an IloCplex object
    private NodeAttachment perfect_Node;
    private IloCplex cplex=null;
    private double cutoff ;
    
    public Double lpRelaxEstimate = null;
   
    
    public CplexObjectWith_LPEstimate (NodeAttachment perfectNode, double initialCutoff ){
        this. lpRelaxEstimate= perfectNode.lpRelaxedValue;
        perfect_Node =perfectNode;
        this.cutoff = initialCutoff;
    }
    public IloCplex getCplexObject () throws IloException {
        if (null==cplex){
            //promote  
            cplex = CplexUtilities.promoteNode_To_ILoCplex (perfect_Node,cutoff ) ;
            //set config parameters that will be used to solve this cplex object
            CplexUtilities.setConfigurationParameters_forSolve  (cplex);
            //System.out.println("promoted into cplex object");
        }
        
        return cplex;
    }
    
    public void updateCutoff (double newCutoff) throws IloException {
         this.cutoff = newCutoff;
         if (null!=cplex){
             cplex.setParam( IloCplex.Param.MIP.Tolerances.UpperCutoff, cutoff) ;
         }
    }
    
    public long getNnodesLeft64 (){
        return null==cplex ? ZERO:  cplex.getNnodesLeft64();
    }
}
