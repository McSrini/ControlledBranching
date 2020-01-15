/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.ccacbdec2019.pure;

import static ca.mcmaster.ccacbdec2019.Constants.BILLION;
import static ca.mcmaster.ccacbdec2019.Constants.HALT_FILE;
import static ca.mcmaster.ccacbdec2019.Constants.LOGGING_LEVEL;
import static ca.mcmaster.ccacbdec2019.Constants.LOG_FILE_EXTENSION;
import static ca.mcmaster.ccacbdec2019.Constants.LOG_FOLDER;
import static ca.mcmaster.ccacbdec2019.Constants.ONE;
import static ca.mcmaster.ccacbdec2019.Constants.SIXTY;
import static ca.mcmaster.ccacbdec2019.Constants.ZERO;
import ca.mcmaster.ccacbdec2019.Parameters;
import static ca.mcmaster.ccacbdec2019.Parameters.BRANCHING_STRATEGY;
import static ca.mcmaster.ccacbdec2019.Parameters.DISABLE_HEURISTICS;
import static ca.mcmaster.ccacbdec2019.Parameters.DISABLE_PRESOLVE;
import static ca.mcmaster.ccacbdec2019.Parameters.DISABLE_PRESOLVE_NODE;
import static ca.mcmaster.ccacbdec2019.Parameters.MAX_THREADS;
import static ca.mcmaster.ccacbdec2019.Parameters.MIP_EMPHASIS;
import static ca.mcmaster.ccacbdec2019.Parameters.MIP_FILENAME;
import ca.mcmaster.ccacbdec2019.utils.CplexUtilities;
import ca.mcmaster.ccacbdec2019.worker.Worker;
import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import static java.lang.System.exit;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import static ca.mcmaster.ccacbdec2019.Parameters.FILE_STRATEGY_DISK_COMPRESSED;
import static ca.mcmaster.ccacbdec2019.Parameters.STARTING_CUTOFF_FOR_PURE_CPLEX;
import java.io.File;

/**
 *
 * @author tamvadss
 */
public class PureCplex {
    
    private     IloCplex cplex  ;
    private static Logger logger = Logger.getLogger(PureCplex.class); 
        
    static {
        logger.setLevel( LOGGING_LEVEL);
        PatternLayout layout = new PatternLayout("%5p  %d  %F  %L  %m%n");     
        try {
            RollingFileAppender rfa =new  RollingFileAppender(layout,LOG_FOLDER+PureCplex.class.getSimpleName()+ LOG_FILE_EXTENSION);
            rfa.setMaxBackupIndex(SIXTY);
            logger.addAppender(rfa);
            logger.setAdditivity(false);
        } catch (Exception ex) {
            ///
            System.err.println("Exit: unable to initialize logging"+ex);       
            exit(ONE);
        }
    } 
    
    public PureCplex ( IloCplex mipToSolve){
        cplex = mipToSolve;
    }
    
    public void solve () throws IloException {
        
        CplexUtilities.setConfigurationParameters_forRampAndReincarnate(cplex);
        CplexUtilities.setConfigurationParameters_forSolve (cplex);
        
        cplex.setParam( IloCplex.Param.TimeLimit, SIXTY *SIXTY);
        if (STARTING_CUTOFF_FOR_PURE_CPLEX < BILLION) 
            cplex.setParam( IloCplex.Param.MIP.Tolerances.UpperCutoff, STARTING_CUTOFF_FOR_PURE_CPLEX ) ;
        
        System.out.println("Pure solver  starting   ") ;
        logger.info("Pure solver  starting   ") ;
        
        int hours = ZERO; 
        boolean hasSolution = false;
        boolean isfeasible = false;
        boolean isOptimal = false;
        for (;hours <  Parameters.SOLUTION_RUN_TIME_IN_HOURS ; hours ++){
               
            
            cplex.solve ();
            
            isfeasible = cplex.getStatus().equals(IloCplex.Status.Feasible);
            isOptimal = cplex.getStatus().equals(IloCplex.Status.Optimal);
            hasSolution = isfeasible || isOptimal;
                       
            logger.info ((ONE+hours)+"," + cplex.getBestObjValue() +
                         ","  + (hasSolution ? cplex.getObjValue() : BILLION )+ 
                         ","  +cplex.getNnodes64() + 
                         ","  +cplex.getNnodesLeft64()) ; 
            
            if (isHaltFilePresent()) break;            
            if (cplex.getStatus().equals( IloCplex.Status.Infeasible)) break;
            if (cplex.getStatus().equals( IloCplex.Status.Optimal)) break;
            
        }
        
         
        
        cplex.end ();
        System.out.println("Pure solver completed ") ;
        logger.info("Pure solver completed ") ;
            
    }
    
    private static boolean isHaltFilePresent (){
        File file = new File(HALT_FILE );         
        return file.exists();
    }
        
   
}
