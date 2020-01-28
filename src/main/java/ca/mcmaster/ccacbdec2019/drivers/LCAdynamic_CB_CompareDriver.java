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
import ca.mcmaster.ccacbdec2019.Parameters;
import ca.mcmaster.ccacbdec2019.controlledBranching.InstructionTree;
import ca.mcmaster.ccacbdec2019.controlledBranching.InstructionTreeCreator;
import ca.mcmaster.ccacbdec2019.controlledBranching.Reincarnator;
import ca.mcmaster.ccacbdec2019.pure.PureCplex;
import ca.mcmaster.ccacbdec2019.rampup.LCA_Finder;
import ca.mcmaster.ccacbdec2019.rampup.NodeAttachment;
import ca.mcmaster.ccacbdec2019.rampup.RampUp;
import ca.mcmaster.ccacbdec2019.utils.CplexObjectWith_LPEstimate;
import ca.mcmaster.ccacbdec2019.utils.CplexUtilities;
import ca.mcmaster.ccacbdec2019.worker.Worker;
import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import static java.lang.System.exit;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

/**
 *
 * @author tamvadss
 */
public class LCAdynamic_CB_CompareDriver {
       
    private static Logger logger = Logger.getLogger(LCAdynamic_CB_CompareDriver.class); 
     
    static {
        logger.setLevel( LOGGING_LEVEL);
        PatternLayout layout = new PatternLayout("%5p  %d  %F  %L  %m%n");     
        try {
            RollingFileAppender rfa =new  RollingFileAppender(layout,LOG_FOLDER+LCAdynamic_CB_CompareDriver.class.getSimpleName()+ LOG_FILE_EXTENSION);
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
        //ramp up the MIP
        logger.info (Parameters.getParameterConfiguration()) ;
        logger.info ("RampUp start "  ) ;
        RampUp rampup= new RampUp ();
        logger.info ("RampUp  end "  ) ;
                
        ///collect LCA nodes and round robin them
        LCA_Finder lcaFinder = new LCA_Finder (rampup.getRootNode( )) ;    
        List<NodeAttachment> perfectLCANodeList =  lcaFinder.getPerfectLCANodes() ;
        
        logger.info ("Ramp up to leafs " + rampup.getLeafList().size()) ;
        logger.info ("perfectLCANodeList  size " +  perfectLCANodeList.size()) ;
        System.out.println ("Ramp up to leafs " + rampup.getLeafList().size()) ;
        System.out.println   ("perfectLCANodeList  size " +  perfectLCANodeList.size()) ;
        
        List<CplexObjectWith_LPEstimate> individualSubProblems_for_RoundRobin = new ArrayList<CplexObjectWith_LPEstimate> () ;
        InstructionTreeCreator  cbInstructionTreeCreator =new InstructionTreeCreator (rampup.getRootNode( ));
        
        logger.info ("creating each lca node separately") ;
        double bestSolutionFoundByRampup = (rampup.isFeasible()) ?  rampup.getBestKnownSolution() : BILLION;
        for (NodeAttachment perfect : perfectLCANodeList) {            
            individualSubProblems_for_RoundRobin.add ( CplexUtilities.getMIPWithBoundsChanged(perfect, bestSolutionFoundByRampup) );            
        }
        logger.info ("created each lca node separately") ;
        
        
        //reincarnate the entire tree 
        cbInstructionTreeCreator.setRefCounts( rampup.getLeafList());
        InstructionTree iTree = cbInstructionTreeCreator.getCBInstructionTree();
        //iTree.print(); 
        
        //end the ramp up, we no longer need it 
        rampup.end();
        
        logger.info ("Reincarnator full tree start" ) ;
        System.out.println("Reincarnator full tree start" + LocalDateTime.now()) ;       
        logger.info ("number of nonleaf nodes in the CB instruction tree is " + iTree.getNumberOfNonLeafNodes() );
                

        Reincarnator reincarnator = new Reincarnator (iTree);
        if (bestSolutionFoundByRampup < BILLION) {            
            //feasible ramp up
            logger.info ("reincarnate with best known solution as cutoff " + bestSolutionFoundByRampup);
            reincarnator.reincarnate(bestSolutionFoundByRampup);
        }else {
            logger.info ("reincarnate with no cutoff");
            reincarnator.reincarnate();
        }
        
        logger.info ("Reincarnation tree end" ) ;
        
        
        //TEST 1 : solve the merged tree
        logger.info ("merged tree solve start") ;
        logger.info (" leafs in merged tree " + reincarnator.getMergedTree().getNnodesLeft64());
        Worker worker = new Worker (reincarnator.getMergedTree()) ;
        worker.solveMergedProblem();
        logger.info ("merged tree solve end") ;
        
        //TEST 2: round robin thru the collection of leafs
        logger.info ("round robin start") ;
        worker = new Worker (individualSubProblems_for_RoundRobin ) ;
        worker.roundRobinIndividualProblems();
        logger.info ("round robin complete") ;
        
        
        
    }
}
