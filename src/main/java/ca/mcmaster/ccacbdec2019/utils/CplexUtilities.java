/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.ccacbdec2019.utils;

import static ca.mcmaster.ccacbdec2019.Constants.*; 
import ca.mcmaster.ccacbdec2019.Parameters;
import static ca.mcmaster.ccacbdec2019.Parameters.*; 
import ca.mcmaster.ccacbdec2019.rampup.NodeAttachment;
import ilog.concert.IloException;
import ilog.concert.IloLPMatrix;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author tamvadss
 */
public class CplexUtilities {
    
        
    public static Map<String, IloNumVar> getVariables (IloCplex cplex) throws IloException{
        Map<String, IloNumVar> result = new HashMap<String, IloNumVar>();
        IloLPMatrix lpMatrix = (IloLPMatrix)cplex.LPMatrixIterator().next();
        IloNumVar[] variables  =lpMatrix.getNumVars();
        for (IloNumVar var :variables){
            result.put(var.getName(),var ) ;
        }
        return result;
    }
    
    public static void setConfigurationParameters_forRampAndReincarnate (IloCplex cplex) throws IloException{
        if (DISABLE_HEURISTICS) cplex.setParam( IloCplex.Param.MIP.Strategy.HeuristicFreq , -ONE);
        if (DISABLE_CUTS) cplex.setParam( IloCplex.Param.MIP.Limits.CutPasses , -ONE);
        
        if (USE_BARRIER_FOR_SOLVING_LP) {
            cplex.setParam( IloCplex.Param.NodeAlgorithm  ,  IloCplex.Algorithm.Barrier);
            cplex.setParam( IloCplex.Param.RootAlgorithm  ,  IloCplex.Algorithm.Barrier);
        }
        
        if (Parameters.IS_BIG_RAMPUP) {
            //needed to avoid node attachment not found on disk error when using callbacks
            cplex.setParam( IloCplex.Param.WorkMem, HUGE_WORKMEM) ;
            cplex.setParam( IloCplex.Param.MIP.Strategy.File , ZERO);
        }
                
        cplex.setParam( IloCplex.Param.Emphasis.MIP , MIP_EMPHASIS );
        if (DISABLE_PRESOLVE_NODE) cplex.setParam( IloCplex.Param.MIP.Strategy.PresolveNode , -ONE  );
        if (DISABLE_PRESOLVE) cplex.setParam( IloCplex.Param.Preprocessing.Presolve,  false);
        cplex.setParam( IloCplex.Param.MIP.Strategy.VariableSelect  , BRANCHING_STRATEGY);
    }
    
    public static void setConfigurationParameters_forSolve (IloCplex cplex) throws IloException{
        cplex.clearCallbacks();            
        cplex.use (new EmptyBranchCallback());            
        cplex.setParam( IloCplex.Param.Threads,  MAX_THREADS);
        cplex.setParam(IloCplex.Param.MIP.Strategy.File , FILE_STRATEGY_DISK_COMPRESSED);            
    }
    
    //given a node attachment, apply its bounds on the original MIP
    // this cplex object will become one candidate in the round robin list
    //
    //the cutoff is the best solution found during ramp up
    public static CplexObjectWith_LPEstimate getMIPWithBoundsChanged (NodeAttachment node, double cutoff) throws IloException {
        IloCplex cplex = new IloCplex ();
        cplex.importModel(  MIP_FILENAME);
        
        //create the cplex object with heuristics etc disabled, just like in the ramped up MIP
        setConfigurationParameters_forRampAndReincarnate(cplex) ;
        
        
        Map< String,Double > upperBoundMap = new HashMap < String,Double > ();
        Map< String,Double > lowerBoundMap = new HashMap < String,Double > ();
        getBranchingConditions(node, upperBoundMap, lowerBoundMap);
        
        Map <String, IloNumVar> allVariables = getVariables(cplex) ;
        for (Map.Entry<String,Double> entry : upperBoundMap.entrySet()){
            updateVariableBounds (allVariables.get(entry.getKey()), entry.getValue(), true );
        }
         for (Map.Entry<String,Double> entry :lowerBoundMap.entrySet() ){
            updateVariableBounds (allVariables.get(entry.getKey()), entry.getValue(), false );
        }
         
        if (cutoff < BILLION) {
            //set cutoff
            cplex.setParam( IloCplex.Param.MIP.Tolerances.UpperCutoff, cutoff) ;
        }
        
        return new CplexObjectWith_LPEstimate (cplex, node.lpRelaxedValue) ;
    }
    
    private static void getBranchingConditions (NodeAttachment node, Map< String,Double > upperBoundMap, Map< String,Double > lowerBoundMap){
        
        NodeAttachment current = node;
        
        while (current.parentData!=null){
            //get branching condition of the current node
            if (current.isBranchingDirectionDown){
                upperBoundMap.put(current.branchingVarName ,  current.branchingBound);
            }else {
                lowerBoundMap.put(current.branchingVarName ,  current.branchingBound);
            }        
            current= current.parentData; 
        }                
         
    }
    
    /**
     * 
     *  Update variable bounds as specified    
     */
    private static   void updateVariableBounds(IloNumVar var, double newBound, boolean isUpperBound   )      throws IloException{
 
        if (isUpperBound){
            if ( var.getUB() > newBound ){
                //update the more restrictive upper bound
                var.setUB( newBound );
                //System.out.println(" var " + var.getName() + " set upper bound " + newBound ) ;
            }
        }else{
            if ( var.getLB() < newBound){
                //update the more restrictive lower bound
                var.setLB(newBound);
                //System.out.println(" var " + var.getName() + " set lower bound " + newBound ) ;
            }
        }  

    } 
    
}