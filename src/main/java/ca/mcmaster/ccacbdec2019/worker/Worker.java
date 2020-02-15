/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.ccacbdec2019.worker;
 
import static ca.mcmaster.ccacbdec2019.Constants.LOGGING_LEVEL;
import static ca.mcmaster.ccacbdec2019.Constants.LOG_FILE_EXTENSION;
import static ca.mcmaster.ccacbdec2019.Constants.LOG_FOLDER;
import static ca.mcmaster.ccacbdec2019.Constants.*;
import static ca.mcmaster.ccacbdec2019.Constants.SIXTY;
import static ca.mcmaster.ccacbdec2019.Constants.ZERO;
import static ca.mcmaster.ccacbdec2019.Parameters.*;
import ca.mcmaster.ccacbdec2019.controlledBranching.InstructionTree;
import ca.mcmaster.ccacbdec2019.utils.CplexObjectWith_LPEstimate;
import ca.mcmaster.ccacbdec2019.utils.CplexUtilities;
import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.Status;
import java.io.File;
import static java.lang.System.exit;
import java.util.HashMap;
import java.util.List;
import java.util.Map; 
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

/**
 *
 * @author tamvadss
 */
public class Worker {
    
    private static Logger logger = Logger.getLogger(Worker.class); 
     
    private IloCplex mergedProblem = null;
    private  Map<Integer,   CplexObjectWith_LPEstimate>  individualProblemsMap= new HashMap<Integer,  CplexObjectWith_LPEstimate> () ;
    
    
    static {
        logger.setLevel( LOGGING_LEVEL);
        PatternLayout layout = new PatternLayout("%5p  %d  %F  %L  %m%n");     
        try {
            RollingFileAppender rfa =new  RollingFileAppender(layout,LOG_FOLDER+Worker.class.getSimpleName()+ LOG_FILE_EXTENSION);
            rfa.setMaxBackupIndex(SIXTY);
            logger.addAppender(rfa);
            logger.setAdditivity(false);
        } catch (Exception ex) {
            ///
            System.err.println("Exit: unable to initialize logging"+ex);       
            exit(ONE);
        }
    } 
    
    
    public Worker ( IloCplex cplexMerged ) throws IloException{
        this.mergedProblem = cplexMerged;
        CplexUtilities.setConfigurationParameters_forSolve ( this.mergedProblem );
    }
    
    //round robin worker
    public Worker (  List<CplexObjectWith_LPEstimate> individualProblems  ) throws IloException{       
        int index = ZERO;
        for (CplexObjectWith_LPEstimate cplexWithLP :  individualProblems ){
            
            
            
            this.individualProblemsMap.put (index, cplexWithLP);
            index ++;
        }        
    }
    

    
    public void solveMergedProblem() throws IloException{
        IloCplex cplex = mergedProblem;
        cplex.setParam( IloCplex.Param.TimeLimit, SIXTY *SIXTY);
        
        System.out.println("Worker starting to solve merged problem ") ;
        logger.info("Worker starting to solve merged problem ") ;
        
        for (int hours = ZERO; hours < SOLUTION_RUN_TIME_IN_HOURS; hours ++){
                        
            cplex.solve ();
            
            boolean isfeasible = cplex.getStatus().equals(IloCplex.Status.Feasible);
            boolean isOptimal = cplex.getStatus().equals(IloCplex.Status.Optimal);
            boolean hasSolution = isfeasible || isOptimal;
                       
            logger.info ((ONE+hours)+"," + cplex.getBestObjValue() +
                         ","  + (hasSolution ? cplex.getObjValue() : BILLION )+ 
                         ","  +cplex.getNnodes64() + 
                         ","  +cplex.getNnodesLeft64()) ; 
            
            if (isHaltFilePresent()) break;            
            if (cplex.getStatus().equals( IloCplex.Status.Infeasible)) break;
            if (cplex.getStatus().equals( IloCplex.Status.Optimal)) break;
            
            //note: we print the best solution found AFTER ramp up. The ramp up solution is used as cutoff, not as a MIP start.
            
        }
        
        logger.info("Printing solution vector ...") ;
        logger.info ( CplexUtilities.getSolutionVector(cplex));
        
        cplex.end ();
        System.out.println("Worker completed merged problem ") ;
        logger.info("Worker completed merged problem ") ;
    }
        
    public void roundRobinIndividualProblems() throws IloException{
        
        logger.info("Worker starting to round robin ") ;
        long numLeafsLeft =ZERO ;
        long numNodesProcessedSoFar=ZERO ;
        double lowestRemainingObjValue =BILLION ;
        
        //the best   solution found AFTER ramp up
        double bestKnownSolution =BILLION;  
        
        //get best problem using best bound
        int bestSubProblemID = getSubProblemWithSmallestBound () ;
        IloCplex cplex = null;
        if (bestSubProblemID>= ZERO) {
            //this is the problem which won the time quantum allocation
            cplex = this.individualProblemsMap.get( bestSubProblemID).getCplexObject();
            lowestRemainingObjValue =  this.individualProblemsMap.get( bestSubProblemID).lpRelaxEstimate;
        }
         
        for (int hours = ZERO; (hours < SOLUTION_RUN_TIME_IN_HOURS)  && (bestSubProblemID>= ZERO); hours ++){
            
            if (isHaltFilePresent()) break;  
            
            
            //round robin the subproblems until 1 hour passes
            double iterationEndTime = System.currentTimeMillis()+ THOUSAND*THIRTYSIX_HUNDRED;
            while (iterationEndTime > System.currentTimeMillis()){
                
                //solve best remaining problem for time quantum
                long numNodesProcessed_before = cplex.getNnodes64();
                logger.debug (" numNodesProcessed_before "+ numNodesProcessed_before +   " leafs left is cplex.getNnodesLeft64() " + 
                        cplex.getNnodesLeft64());
                
                cplex.setParam(  IloCplex.Param.TimeLimit,TIME_QUANTUM_SECONDS  );
                cplex.solve();
                //update map of individual problems with the improved LP relax for this cplex object
                this.individualProblemsMap.get( bestSubProblemID).lpRelaxEstimate=cplex.getBestObjValue();
                
                long numNodesProcessed_after = cplex.getNnodes64();
                
                numNodesProcessedSoFar += (numNodesProcessed_after-numNodesProcessed_before);
                bestKnownSolution = Math.min ( bestKnownSolution,
                        (cplex.getStatus().equals(Status.Feasible)|| cplex.getStatus().equals(Status.Optimal))? cplex.getObjValue() : BILLION);
                numLeafsLeft = getNumberOfLeafsLeft();
                               
                
                //must end() problem that is solved to completion, and remove it from sub problem list
                if (cplex.getStatus().equals(Status.Optimal) || cplex.getStatus().equals(Status.Infeasible)){
                    
                   
                    cplex.end();
                    this.individualProblemsMap.remove(bestSubProblemID );
                    if (individualProblemsMap.size()==ZERO){
                        //all subproblems solved
                        logger.info ("all subproblems solved, stopping round robin") ;
                        break;
                    }
                }
                
                 //  update cutoffs on all sub problems
                if (BILLION > bestKnownSolution) updateCutoffs(bestKnownSolution);
                //get next subproblem to solve for 6 minutes
                bestSubProblemID = getSubProblemWithSmallestBound () ;
                cplex = this.individualProblemsMap.get( bestSubProblemID).getCplexObject(); 
                lowestRemainingObjValue =  this.individualProblemsMap.get( bestSubProblemID).lpRelaxEstimate;
                
            }//end while
            
            //print the total number of nodes processed and leafs leftover, best bound, and best solution

            logger.info ((ONE+hours)+"," + lowestRemainingObjValue +
                         ","  + bestKnownSolution + 
                         ","  + numNodesProcessedSoFar  + 
                         ","  +   numLeafsLeft + " , " + this.individualProblemsMap.size() ) ; 
            
            if (individualProblemsMap.size()==ZERO){
                //all subproblems solved
                logger.info ("all subproblems solved") ;
                //end 1 hour cycles
                break;
            }
            
        }//end for 1 hour
        
        logger.info("Worker  ended round robin") ;
        
    }
    
    private void updateCutoffs (double cutoff) throws IloException{
        for (CplexObjectWith_LPEstimate cplexWithLPEstimate: individualProblemsMap.values()){
                            
            cplexWithLPEstimate.updateCutoff(cutoff);
                    
                    
            
        }
        
        //logger.info("Cutoff updated  to " + cutoff) ;
    }
    
    private int getSubProblemWithSmallestBound () throws IloException{         
       
        double smallestKnownBound = BILLION;
        
        int bestReaminingSubproblemID = -ONE;
        
        for (Map.Entry<Integer, CplexObjectWith_LPEstimate> entry:  individualProblemsMap.entrySet()){
            
            logger .debug(" ID " + entry.getKey() + " obj val " +entry.getValue() .lpRelaxEstimate )  ;
             
            if (entry.getValue() .lpRelaxEstimate< smallestKnownBound){
                smallestKnownBound =entry.getValue() .lpRelaxEstimate;
                bestReaminingSubproblemID =entry.getKey();
            }
        }
         
        logger.debug(" best ID " +bestReaminingSubproblemID);
        return bestReaminingSubproblemID ;
    }
    
    private static boolean isHaltFilePresent (){
        File file = new File(HALT_FILE );         
        return file.exists();
    }
        
   
    
    private long getNumberOfLeafsLeft (){
        long result = ZERO;
        for (CplexObjectWith_LPEstimate cplexWithLPEstimate:  individualProblemsMap.values()){
            result += cplexWithLPEstimate.getNnodesLeft64();
        }
        
        return result;
    }
}
