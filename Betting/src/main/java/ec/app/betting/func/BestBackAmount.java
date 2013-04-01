/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.betting.func;

import org.apache.log4j.Logger;

import ec.EvolutionState;
import ec.Problem;
import ec.app.betting.BettingData;
import ec.app.betting.BettingProblem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.util.Parameter;

/* 
 * Add.java
 * 
 * Created: Wed Nov  3 18:26:37 1999
 * By: Sean Luke
 */

/**
 * @author Sean Luke
 * @version 1.0 
 */

public class BestBackAmount extends GPNode
    {
	
	final static Logger LOG = Logger.getLogger(BestBackAmount.class);
	
    public String toString() { return "bestBack"; }

    public void checkConstraints(final EvolutionState state,
        final int tree,
        final GPIndividual typicalIndividual,
        final Parameter individualBase)
        {
        super.checkConstraints(state,tree,typicalIndividual,individualBase);
        if (children.length!=0)
            state.output.error("Incorrect number of children for node " + 
                toStringForError() + " at " +
                individualBase);
        }

    public void eval(final EvolutionState state,
        final int thread,
        final GPData input,
        final ADFStack stack,
        final GPIndividual individual,
        final Problem problem)
        {
        double result;
        BettingData rd = ((BettingData)(input));
        BettingProblem prob = (BettingProblem)problem;

        //LOG.debug("snapshot="+prob.currentSnapshot+" data="+rd+" prob="+prob+" em="+prob.em);
        long t = System.currentTimeMillis();
        rd.x = 
        (Double) prob.em.createQuery("select max(a.backAmountAvailable) from Price a where snapshot<?")
        	.setParameter(1, prob.currentSnapshot)
        	.getSingleResult();
        LOG.debug(System.currentTimeMillis() - t);
        /*	
        List<Price> prices = prob.prices.get(new int[] {prob.currentSelection.getSelectionId(),prob.currentSnapshot});
       	double best = 0.0;
       	for(Price p : prices) {
       		if(p.getBackAmountAvailable() > best)
       			best = p.getBackAmountAvailable();
       	}
       	rd.x = best;
        */
        }
    }



