/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.ccacbdec2019.drivers;

import static ca.mcmaster.ccacbdec2019.Constants.BILLION;
import static ca.mcmaster.ccacbdec2019.Constants.LOGGING_LEVEL;
import static ca.mcmaster.ccacbdec2019.Constants.LOG_FILE_EXTENSION;
import static ca.mcmaster.ccacbdec2019.Constants.LOG_FOLDER;
import static ca.mcmaster.ccacbdec2019.Constants.ONE;
import static ca.mcmaster.ccacbdec2019.Constants.SIXTY;
import static ca.mcmaster.ccacbdec2019.Constants.ZERO;
import ca.mcmaster.ccacbdec2019.Parameters;
import static ca.mcmaster.ccacbdec2019.Parameters.*;
import static ca.mcmaster.ccacbdec2019.Parameters.MIP_FILENAME;
import ca.mcmaster.ccacbdec2019.controlledBranching.InstructionTree;
import ca.mcmaster.ccacbdec2019.controlledBranching.InstructionTreeCreator;
import ca.mcmaster.ccacbdec2019.controlledBranching.Reincarnator;
import ca.mcmaster.ccacbdec2019.pure.PureCplex;
import ca.mcmaster.ccacbdec2019.rampup.RampUp;
import ca.mcmaster.ccacbdec2019.worker.Worker;
import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import static java.lang.System.exit;
import java.time.LocalDateTime;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

/**
 *
 * @author tamvadss
 */
public class CB_Deferred_PureCplex_CompareDriver {
       
    private static Logger logger = Logger.getLogger(CB_Deferred_PureCplex_CompareDriver.class); 
     
    static {
        logger.setLevel( LOGGING_LEVEL);
        PatternLayout layout = new PatternLayout("%5p  %d  %F  %L  %m%n");     
        try {
            RollingFileAppender rfa =new  RollingFileAppender(layout,LOG_FOLDER+CB_Deferred_PureCplex_CompareDriver.class.getSimpleName()+ LOG_FILE_EXTENSION);
            rfa.setMaxBackupIndex(SIXTY);
            logger.addAppender(rfa);
            logger.setAdditivity(false);
        } catch (Exception ex) {
            ///
            System.err.println("Exit: unable to initialize logging"+ex);       
            exit(ONE);
        }
    } 

    public static void main(String[] args) throws IloException {
        
        if (ONLY_SOLVE_PURE) {
            logger.info("");
            logger.info("Starting solve with pure cplex");
            logger.info("");  
            IloCplex originalMIP = new IloCplex ();
            originalMIP.importModel( MIP_FILENAME);
            (new PureCplex( originalMIP  )).solve();
            logger.info("Completed solve with pure cplex");            
            exit (ZERO);
        }
        
        //ramp up the MIP
        logger.info (Parameters.getParameterConfiguration()) ;
        logger.info ("RampUp start " ) ;
        RampUp rampup= new RampUp ();
        logger.info ("RampUp  end " ) ;
        logger.info (" lp relax at the end of ramp up was " + rampup.getLPRelax (  ));
        
        
        //now reincarnate the entire tree
        logger.info ("get instruction tree start ");
        InstructionTreeCreator  cbInstructionTreeCreator =new InstructionTreeCreator (rampup.getRootNode( ));
        cbInstructionTreeCreator.setRefCounts( rampup.getLeafList());
        InstructionTree iTree = cbInstructionTreeCreator.getCBInstructionTree();
        logger.info ("got the instruction tree, it has this many nonleaf nodes " + iTree.getNumberOfNonLeafNodes());
        
        boolean isrampUpFeasible = rampup.isFeasible();
        double rampupBestKnownSolution = isrampUpFeasible? rampup.getBestKnownSolution(): BILLION;
         //we can discard the ramped up tree
        rampup.end();
        
        logger.info ("Reincarnator start ") ;
        System.out.println("Reincarnator start " + LocalDateTime.now()) ;       
        Reincarnator reincarnator = new Reincarnator (iTree );
        logger.info (" ramp up solution used as cutoff "+ rampupBestKnownSolution) ;
        reincarnator.reincarnate(rampupBestKnownSolution,  SOLUTION_RUN_TIME_IN_HOURS );                
        logger.info ("Reincarnator end   "  ) ;
        
       
        
                
        
        
        
    }
    
 
}
