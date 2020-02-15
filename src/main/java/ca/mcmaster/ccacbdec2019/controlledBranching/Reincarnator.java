/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.ccacbdec2019.controlledBranching;

import static ca.mcmaster.ccacbdec2019.Constants.*;
import ca.mcmaster.ccacbdec2019.Parameters;
import static ca.mcmaster.ccacbdec2019.Parameters.*;
import ca.mcmaster.ccacbdec2019.utils.CplexUtilities;
import ca.mcmaster.ccacbdec2019.worker.Worker;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import java.io.File;
import static java.lang.System.exit;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap; 
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

/**
 *
 * @author tamvadss
 * 
 * 
 */
public class Reincarnator {
    private static Logger logger = Logger.getLogger(Reincarnator.class);  
    
    private InstructionTree insTree = null;
    private     IloCplex cplex  ; 
    
    ReincarnartionBranchHandler r_branchhandler =null;
    ReincarnationNodeCallback r_nodehandler =new ReincarnationNodeCallback();
    
    static {
        logger.setLevel( LOGGING_LEVEL);
        PatternLayout layout = new PatternLayout("%5p  %d  %F  %L  %m%n");     
        try {
            RollingFileAppender rfa =new  RollingFileAppender(layout,LOG_FOLDER+Reincarnator.class.getSimpleName()+ LOG_FILE_EXTENSION);
            rfa.setMaxBackupIndex(SIXTY);
            logger.addAppender(rfa);
            logger.setAdditivity(false);
        } catch (Exception ex) {
            ///
            System.err.println("Exit: unable to initialize logging"+ex);       
            exit(ONE);
        }
    } 
        
    
    public Reincarnator ( InstructionTree instructionTree ) throws IloException{
        this.insTree= instructionTree;
                
        cplex = new IloCplex ();
        cplex.importModel(  MIP_FILENAME);
        
        r_branchhandler= new ReincarnartionBranchHandler (insTree ,   CplexUtilities.getVariables (cplex));                   
        cplex.use (r_branchhandler );
        cplex.use (r_nodehandler) ;
        
        //set config
        CplexUtilities.setConfigurationParameters_forRampAndReincarnate (cplex);
                         
        
    }
    
    public IloCplex getMergedTree () {
        //avoid returning private variables in the future !
        return cplex;
    }
    
    
    public void reincarnate () throws IloException{
        //reincarnate only if the root node is not perfect
        if (insTree.leftChildId==null && insTree.rightChildId ==null) {
            //do nothing, the root node of the MIP is the LCA
        }else {
            //reincarnate
            cplex.solve ();
        }
    }
        
    public void reincarnate (double cutoff) throws IloException{
        //reincarnate only if the root node is not perfect
        if (insTree.leftChildId==null && insTree.rightChildId ==null) {
            //do nothing, the root node of the MIP is the LCA
        }else {
            //reincarnate
            cplex.setParam( IloCplex.Param.MIP.Tolerances.UpperCutoff, cutoff) ;
            cplex.solve ();
        }
    }
    
    public long getNumberofNodeRedirects () {
        return r_nodehandler.numberOfNodeRedirects;
    } 
    
    //use this method to test the deferred merge
    public void reincarnate (double cutoff, int durationInHours) throws IloException{
                
        //reincarnate only if the root node is not perfect
        if (insTree.leftChildId==null && insTree.rightChildId ==null) {
            //do nothing, the root node of the MIP is the LCA
        }else {
            //reincarnate
            if (cutoff < BILLION ) cplex.setParam( IloCplex.Param.MIP.Tolerances.UpperCutoff, cutoff) ;
            
            //use the same cplex params as worker.solve() or pure.solve() would have used
            cplex.setParam( IloCplex.Param.Threads,  MAX_THREADS);
            cplex.setParam(IloCplex.Param.MIP.Strategy.File , FILE_STRATEGY_DISK_COMPRESSED);    
            
            //now solve for 1 hour at a time            
            cplex.setParam( IloCplex.Param.TimeLimit, SIXTY *SIXTY);

            System.out.println("Reincarnate  durationInHours   " + durationInHours) ;
            logger.info("Reincarnate  durationInHours   " + durationInHours) ;

            int hours = ZERO; 
            boolean hasSolution = false;
            boolean isfeasible = false;
            boolean isOptimal = false;
            for (;hours < durationInHours; hours ++){


                cplex.solve ();
                //temporary callback for statistics
                StatisticsCallback stats = new StatisticsCallback ();
                cplex.use (stats) ;
                cplex.solve ();
                //restore real callbacks
                cplex.clearCallbacks();
                cplex.use (r_branchhandler );
                cplex.use (r_nodehandler) ;

                isfeasible = cplex.getStatus().equals(IloCplex.Status.Feasible);
                isOptimal = cplex.getStatus().equals(IloCplex.Status.Optimal);
                hasSolution = isfeasible || isOptimal;

                logger.info ((ONE+hours)+"," + cplex.getBestObjValue() +
                             ","  + (hasSolution ? cplex.getObjValue() : BILLION )+ 
                             ","  +cplex.getNnodes64() + 
                             ","  +cplex.getNnodesLeft64() + "," + stats.nodesWithBranchingOverrides) ; 

                if (isHaltFilePresent()) break;            
                if (cplex.getStatus().equals( IloCplex.Status.Infeasible)) break;
                if (cplex.getStatus().equals( IloCplex.Status.Optimal)) break;

            }

            logger.info("Printing solution vector ...") ;
            logger.info ( CplexUtilities.getSolutionVector(cplex));
            
            cplex.end ();
            System.out.println("Reincarnate  durationInHours   completed ") ;
            logger.info("Reincarnate  durationInHours   completed ") ;
             
        }//end else
    }//end method reincarnate for duration
    
    
        
    private static boolean isHaltFilePresent (){
        File file = new File(HALT_FILE );         
        return file.exists();
    }
        
}//end class
