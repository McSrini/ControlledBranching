/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.ccacbdec2019.rampup;

import static ca.mcmaster.ccacbdec2019.Constants.FIVE;
import static ca.mcmaster.ccacbdec2019.Constants.LOGGING_LEVEL;
import static ca.mcmaster.ccacbdec2019.Constants.LOG_FILE_EXTENSION;
import static ca.mcmaster.ccacbdec2019.Constants.LOG_FOLDER;
import static ca.mcmaster.ccacbdec2019.Constants.ONE;
import static ca.mcmaster.ccacbdec2019.Constants.SIXTY;
import static ca.mcmaster.ccacbdec2019.Constants.TEN;
import static ca.mcmaster.ccacbdec2019.Constants.THOUSAND;
import static ca.mcmaster.ccacbdec2019.Constants.TWO;
import static ca.mcmaster.ccacbdec2019.Constants.ZERO;
import ca.mcmaster.ccacbdec2019.Parameters; 
import static ca.mcmaster.ccacbdec2019.Parameters.*;
import static ca.mcmaster.ccacbdec2019.Parameters.MIP_FILENAME; 
import ca.mcmaster.ccacbdec2019.utils.CplexUtilities;
import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.Status;
import static java.lang.System.exit;
import java.util.List;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

/**
 *
 * @author tamvadss
 * ramps up a MIP to ramp up size
 */
public class RampUp {
        
    private static Logger logger = Logger.getLogger(RampUp.class);
    private  static  IloCplex cplex  ;
    private static NodeCallback nodehandler = new NodeCallback ();
    private static BranchCallback branchhandler = new BranchCallback();
    
   
     
    static {
        logger.setLevel( LOGGING_LEVEL);
        PatternLayout layout = new PatternLayout("%5p  %d  %F  %L  %m%n");     
        try {
            RollingFileAppender rfa =new  RollingFileAppender(layout,LOG_FOLDER+RampUp.class.getSimpleName()+ LOG_FILE_EXTENSION);
            rfa.setMaxBackupIndex(SIXTY);
            logger.addAppender(rfa);
            logger.setAdditivity(false);
        } catch (Exception ex) {
            ///
            System.err.println("Exit: unable to initialize logging"+ex);       
            exit(ONE);
        }
    } 
    
    public RampUp () throws IloException{
        //do ramp up
        cplex = new IloCplex ();
        cplex.importModel(  MIP_FILENAME);
        
        cplex.use (branchhandler );
        cplex.use (new EmptyNodeCallaback ());
        
        CplexUtilities.setConfigurationParameters_forRampAndReincarnate (cplex);
        
        if (!IS_BIG_RAMPUP) {
            logger.info("desired ramp up size = " + Parameters.RAMP_UP_SIZE);
        }else {
            logger.info("desired ramp up duration = " + Parameters.BIG_RAMP_UP_DURATION_IN_HOURS);
        }
        
        if (IS_BIG_RAMPUP) {
            cplex.setParam( IloCplex.Param.TimeLimit, BIG_RAMP_UP_DURATION_IN_HOURS * SIXTY *SIXTY); 
                        
            cplex.solve ();
            
            //force the node call back to abort because our ramp up is complete
            RAMP_UP_SIZE = -ONE; // cplex.getNnodesLeft() ;
        } 
        cplex.use (nodehandler) ;
        cplex.solve ();
        logger.info ("leaf count at the end of ramp up "+  cplex.getNnodesLeft() + " leaf list size "+  nodehandler.leafList.size());
        logger.info ("nodes processed by ramp up " + cplex.getNnodes64()) ;
        logger.info ("nonleaf nodes remaining in the  ramped up tree " + getNumberOfNonleafNodes()) ;
    }
    
    public void end () {
        cplex.end ();
    }
    
    public double getLPRelax () throws IloException{
        return cplex.getBestObjValue();
    }
    

    
    public IloCplex getRampedUpTree () {
        return cplex;
    }
    
    public boolean isFeasible () throws IloException{
        return cplex.getStatus().equals(Status.Feasible);
    }
    
    public double getBestKnownSolution() throws IloException{
        return cplex.getObjValue();
    }
  
    public List<NodeAttachment> getLeafList   () {
        return nodehandler.leafList;
    }
    
    public NodeAttachment getRootNode () {
        return branchhandler.rootAttachment;
    }
    
    private int getNumberOfNonleafNodes (){
         return getNumberOfNonleafNodes (getRootNode() );
    }
    
        
    private int getNumberOfNonleafNodes (NodeAttachment subTreeRoot){
        int count = ZERO;
        
        if (null!=subTreeRoot){
            if (subTreeRoot.leftChildReference!=null || subTreeRoot.rightChildReference!=null){
                count = ONE + 
                        getNumberOfNonleafNodes(subTreeRoot.leftChildReference)+ 
                        getNumberOfNonleafNodes (subTreeRoot.rightChildReference);
            }
        }
                
        return count;
    }
}
