package br.org.resys.adapter.impl;

import br.org.resys.en.Sparqls;

/**
 * Select and export data representing the evolution of the incidence of
 * refactorings over time, except that only the refactorings aligned with
 * moments of increasing effort are considered
 * <p>
 * The dataset is stored in a csv file which can later on be used to analyze the
 * recommendation of refactoring.
 * 
 * @author Luis Paulo
 */
public class EffortContextualizedIncidenceOfRefactorings extends IncidenceOfRefactoringsAdapter {

	private double correlation;

	public EffortContextualizedIncidenceOfRefactorings(double correlation) {
		super();

		this.setCorrelation(correlation);
	}

	public EffortContextualizedIncidenceOfRefactorings setCorrelation(double correlation) {
		this.correlation = correlation;

		return this;
	}

	public double getCorrelation() {
		return correlation;
	}

	@Override
	public String getSparql() {
		String sparql = Sparqls.SPARQL_EFFORT_CONTEXTUALIZED_INCIDENCE_OF_REFACTORINGS.getStatement();
		sparql = sparql.replaceAll("#correlation#", correlation + "");
		
		return sparql;
	}

}
