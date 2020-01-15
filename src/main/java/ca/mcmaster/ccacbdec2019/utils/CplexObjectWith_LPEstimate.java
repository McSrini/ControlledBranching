/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.ccacbdec2019.utils;

import ilog.cplex.IloCplex;

/**
 *
 * @author tamvadss
 */
public class CplexObjectWith_LPEstimate {
    public IloCplex cplex=null;
    public Double lpRelaxEstimate = null;
    public CplexObjectWith_LPEstimate (IloCplex cplex,Double lpRelaxEstimate ){
        this. lpRelaxEstimate=lpRelaxEstimate;
        this. cplex = cplex;
    }
}
