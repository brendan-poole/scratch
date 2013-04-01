package ec.app.betting;

import ec.gp.koza.KozaFitness;

public class BettingFitness extends KozaFitness {
	
	public double testProfit;
	
    public String fitnessToStringForHumans()
    {
    return super.fitnessToStringForHumans()+" Test profit: "+testProfit;
    }

}
