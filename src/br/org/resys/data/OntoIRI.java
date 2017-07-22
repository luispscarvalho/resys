package br.org.resys.data;

/**
 * IRIs of all ontologies. Ocean, Osore and dependencies.
 * 
 * @author Luis Paulo
 */
public enum OntoIRI {

	ONTOS_IRI("http://www.semanticweb.org/resys/ontologies"), REPOSITORIES_IRI(
			ONTOS_IRI.getIri() + "/2016/1/repositories"), SMELLS_IRI(
					ONTOS_IRI.getIri() + "/2016/1/codesmells"), METRICS_IRI(
							ONTOS_IRI.getIri() + "/2016/1/metrics"), OSORE_IRI(ONTOS_IRI.getIri() + "/2017/4/osore");

	private String iri;

	private OntoIRI(String iri) {
		this.iri = iri;
	}

	public String getIri() {
		return iri;
	}

}
