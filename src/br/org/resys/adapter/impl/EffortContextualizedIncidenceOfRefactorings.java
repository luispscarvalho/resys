package br.org.resys.adapter.impl;

import br.org.resys.en.Sparqls;

public class EffortContextualizedIncidenceOfRefactorings extends IncidenceOfRefactoringsAdapter {

	@Override
	public String getSparql() {
		return Sparqls.SPARQL_EFFORT_CONTEXTUALIZED_INCIDENCE_OF_REFACTORINGS.getStatement();
	}

}
