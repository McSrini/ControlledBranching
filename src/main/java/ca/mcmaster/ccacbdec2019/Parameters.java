/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.ccacbdec2019;

import static ca.mcmaster.ccacbdec2019.Constants.*;

/**
 *
 * @author tamvadss
 */
public class Parameters {
    
    //we use presolved MIPs in sav format 
    //public static final String MIP_FILENAME = "F:\\temporary files here\\timtab1.pre"; 
    //public static final String MIP_FILENAME = "F:\\temporary files here\\dws008.pre.sav"; 
    //public static final String MIP_FILENAME = "F:\\temporary files here\\roi5alpha10n8.pre.sav"; 
    //public static final String MIP_FILENAME = "F:\\temporary files here\\supportcase19.pre.sav"; 
    //public static final String MIP_FILENAME = "F:\\temporary files here\\gfd.pre.sav"; 
    //public static final String MIP_FILENAME = "F:\\temporary files here\\highschool1-aigio.pre.sav"; 
    //public static final String MIP_FILENAME = "F:\\temporary files here\\nursehint03.pre.sav";     
    //public static final String MIP_FILENAME = "b1c1s1.pre.sav"; 
    //public static final String MIP_FILENAME = "sing44.pre.sav";     
    //public static final String MIP_FILENAME = "dws008.pre.sav";     
    //public static final String MIP_FILENAME = "bab6.pre.sav";    
    //public static final String MIP_FILENAME = "comp21-2idx.pre.sav";     
    public static final String MIP_FILENAME = "crypta.pre.sav";     
    //public static final String MIP_FILENAME = "dws008.pre.sav";
    //public static final String MIP_FILENAME = "neosbohle.pre.sav";     
    //public static final String MIP_FILENAME = "sorrell3.pre.sav";     
    //public static final String MIP_FILENAME = "bnatt500.pre.sav";     
    //public static final String MIP_FILENAME = "fhnwbinpack44.pre.sav";   
    //public static final String MIP_FILENAME = "fhnwbinpack448.pre.sav";  
    //public static final String MIP_FILENAME = "neos-3656078-kumeu.pre.sav";
    //public static final String MIP_FILENAME = "opm2-z10-s4.pre.sav";
    //public static final String MIP_FILENAME = "neosnidda.pre.sav";
    //public static final String MIP_FILENAME = "b1c1s1.pre.sav";
    //public static final String MIP_FILENAME = "sorrell3.pre.sav";
    //public static final String MIP_FILENAME = "s100.pre.sav";  
    //public static final String MIP_FILENAME = "splice1k1.pre.sav";  
    //public static final String MIP_FILENAME = "neosberkel.pre.sav";  
    //public static final String MIP_FILENAME = "thor50dday.pre.sav";  
    //public static final String MIP_FILENAME = "proteindesign.pre.sav";  
    //public static final String MIP_FILENAME = "markshare2.pre.sav";  
    //public static final String MIP_FILENAME = "nursehint03.pre.sav";  
    //public static final String MIP_FILENAME = "neoskasavu.pre.sav";
    //public static final String MIP_FILENAME = "neossnowy.pre.sav";
    //public static final String MIP_FILENAME = "neostavua.pre.sav";
    //public static final String MIP_FILENAME = "radiationm40-10-02.pre.sav";
    //public static final String MIP_FILENAME = "nursesched-medium-hint03.pre.sav";
    //public static final String MIP_FILENAME = "supportcase19.pre.sav";
    //public static final String MIP_FILENAME = "traininstance2.pre.sav";     
    
    
     
    
    //ramp up related
    public static final boolean IS_BIG_RAMPUP = true;
    public static   int RAMP_UP_SIZE= IS_BIG_RAMPUP ? BILLION /*use ramp up duration*/ : 500;  
    public static final int BIG_RAMP_UP_DURATION_IN_HOURS = 25;
    
    
    //turn ON to only solve with pure cplex, use IS_BIG_RAMPUP to set large workmem
    public static final boolean ONLY_SOLVE_PURE  = false;
    public static final double STARTING_CUTOFF_FOR_PURE_CPLEX =  BILLION ;
    
    //reincarnate option
    public static final boolean USE_DEFERRED_MERGING= false;   
    
    //worker related
    public static final int SOLUTION_RUN_TIME_IN_HOURS = 1000000 ;  
    public static final int TIME_QUANTUM_SECONDS= 360;    //seconds
    
    //use 1 thread    during solve
    public static final int MAX_THREADS = 1 ; 
    
    //cplex related
    public static final boolean USE_BARRIER_FOR_SOLVING_LP = false; //use barrier for supportcase problems
    public static final int BRANCHING_STRATEGY = 2 ; //pseudo costs
    public static final boolean DISABLE_PRESOLVE = true;
    public static final boolean DISABLE_PRESOLVE_NODE = true;    
    public static final int FILE_STRATEGY_DISK_COMPRESSED= 3;    
    public static boolean DISABLE_HEURISTICS= true;
    public static boolean DISABLE_CUTS= false;
    public static final int MIP_EMPHASIS = 2;
    public static final int HUGE_WORKMEM =HUNDRED*THOUSAND  + SIXTY*THOUSAND;
    
    
    public static String getParameterConfiguration (){
        
        return ("MIP_EMPHASIS "+ MIP_EMPHASIS    ) ;
    }
    
}
