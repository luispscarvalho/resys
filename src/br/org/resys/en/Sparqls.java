package br.org.resys.en;

/**
 * Enumeration of all sparqls used to extract data from our ontologies.
 * 
 * @author Luis Paulo
 */
public enum Sparqls {
	
	SPARQL_SMELLS_BY_COMMITTER("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
			"PREFIX owl: <http://www.w3.org/2002/07/owl#> \n" +
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
			"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n" +
			"PREFIX ocean: <http://www.semanticweb.org/resys/ontologies/2016/2/ocean#> \n" +
			"PREFIX repo: <http://www.semanticweb.org/resys/ontologies/2016/1/repositories#> \n" +
			"PREFIX smells: <http://www.semanticweb.org/resys/ontologies/2016/1/codesmells#> \n" +
			"SELECT ?committer ?codesmell ?datetime ?location \n" +
			"WHERE { \n" +
			"?committer repo:hasCommited ?commit . \n" +
			"?commit repo:datetime ?datetime . \n" +
			"?commit ocean:hasIntroduced ?codesmell . \n" +
			"?codesmell rdf:type smells:LongMethod . \n" +
			"?codesmell ocean:foundIn ?location . \n" +
			"} ORDER BY DESC (?datetime)"),
	SPARQL_INCIDENCE_OF_REFACTORINGS("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
			"PREFIX owl: <http://www.w3.org/2002/07/owl#> \n" + 
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
			"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n" +
			"PREFIX ocean: <http://www.semanticweb.org/resys/ontologies/2016/2/ocean#> \n" +
			"PREFIX repo: <http://www.semanticweb.org/resys/ontologies/2016/1/repositories#> \n" +
			"PREFIX smells: <http://www.semanticweb.org/resys/ontologies/2016/1/codesmells#> \n" +
			"PREFIX osore: <http://www.semanticweb.org/resys/ontologies/2017/4/osore#> \n" +
			"SELECT ?committer ?codesmell ?datetime ?location ?refactoring \n" +
			"WHERE { \n" +
			"?committer repo:hasCommited ?commit . \n" +
			"?commit repo:datetime ?datetime . \n" +
			"?commit ocean:hasIntroduced ?codesmell . \n" +
			"?codesmell ocean:foundIn ?location . \n" +
			"?codesmell ocean:refactoredBy ?refactoring . \n" +
			"} ORDER BY ?datetime"), 
	SPARQL_EFFORT_CONTEXTUALIZED_INCIDENCE_OF_REFACTORINGS("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
			"PREFIX owl: <http://www.w3.org/2002/07/owl#> \n" + 
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
			"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n" +
			"PREFIX ocean: <http://www.semanticweb.org/resys/ontologies/2016/2/ocean#> \n" +
			"PREFIX repo: <http://www.semanticweb.org/resys/ontologies/2016/1/repositories#> \n" +
			"PREFIX smells: <http://www.semanticweb.org/resys/ontologies/2016/1/codesmells#> \n" +
			"PREFIX osore: <http://www.semanticweb.org/resys/ontologies/2017/4/osore#> \n" +
			"SELECT ?committer ?codesmell ?datetime ?location ?refactoring \n" +
			"WHERE { \n" +
			"?committer repo:hasCommited ?commit . \n" +
			"?commit repo:datetime ?datetime . \n" +
			"?commit ocean:hasIntroduced ?codesmell . \n" +
			"?codesmell ocean:foundIn ?location . \n" +
			"?codesmell ocean:refactoredBy ?refactoring . \n" +
			"?recommendation ocean:hasRecommendedFor ?codesmell . \n" +
			"?recommendation osore:contextualizedBy ?effort . \n" +
			"} ORDER BY ?datetime");
	
	private String statement;

	private Sparqls(String statement) {
		this.statement = statement;
	}

	public String getStatement() {
		return statement;
	}

}
