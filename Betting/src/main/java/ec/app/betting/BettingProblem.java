package ec.app.betting;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.gosseyn.betfair.domain.Price;
import com.gosseyn.betfair.domain.Selection;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPIndividual;
import ec.gp.GPProblem;
import ec.simple.SimpleProblemForm;
import ec.util.Parameter;

public class BettingProblem extends GPProblem implements SimpleProblemForm {

	public EntityManager em;
	
	public static final String P_SIZE = "size";

	public List<Selection> selections;
	
	public Selection currentSelection;
	
	public int currentSnapshot;
	
	public Map<int[],List<Price>> prices;

	// we'll need to deep clone this one though.
	public BettingData input;

	public Object clone() {
		// don't bother copying the inputs and outputs; they're read-only :-)
		// don't bother copying the currentValue; it's transitory
		// but we need to copy our regression data
		BettingProblem myobj = (BettingProblem) (super.clone());

		myobj.input = (BettingData) (input.clone());
		return myobj;
	}

	public void setup(final EvolutionState state, final Parameter base) {
		// very important, remember this
		super.setup(state, base);

		// set up our input -- don't want to use the default base, it's unsafe
		input = (BettingData) state.parameters.getInstanceForParameterEq(base
				.push(P_DATA), null, BettingData.class);
		input.setup(state, base.push(P_DATA));

		ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(
				new String[] { "./META-INF/spring/applicationContext.xml" });
		// of course, an ApplicationContext is just a BeanFactory
		BeanFactory factory = (BeanFactory) appContext;
		EntityManagerFactory emf = (EntityManagerFactory) factory
				.getBean("entityManagerFactory");
		em = emf.createEntityManager();

		selections = em.createQuery("from Selection a join fetch a.market join fetch a.prices")
			.getResultList();
		/*
		prices = new HashMap<int[],List<Price>>();
		for(Selection s : selections) {
			for(Price p : s.getPrices()) {
				int[] key = new int[] {s.getSelectionId(),p.getSnapshot()};
				if(!prices.containsKey(key))
					prices.put(key, new ArrayList<Price>());
				prices.get(key).add(p);
			}
		}
		*/
		//System.out.println("Selections="+selections.size()+" Prices="+prices.size());
	}

	public void evaluate(final EvolutionState state, final Individual ind,
			final int subpopulation, final int threadnum) {
		if (!ind.evaluated) {
			int hits = 0;
			double sum = 0.0;

			for(Selection s : selections) {
				currentSelection = s;
				currentSnapshot = s.getMarket().getSnapshot();
				//prices.get(new int[] {3280031,13});
				((GPIndividual) ind).trees[0].child.eval(state, threadnum, input,
						stack, ((GPIndividual) ind), this);
			}
			// the fitness better be KozaFitness!
			BettingFitness f = ((BettingFitness) ind.fitness);
			f.setStandardizedFitness(state, (float) sum);
			f.testProfit = 999;
			f.hits = hits;
			ind.evaluated = true;
		}
	}
}
