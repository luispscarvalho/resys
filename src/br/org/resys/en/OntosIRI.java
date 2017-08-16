package br.org.resys.en;

/**
 * IRIs of all ontologies.
 * 
 * @author Luis Paulo
 */
public enum OntosIRI {

	ONTOS_IRI("http://www.semanticweb.org/resys/ontologies"), REPOSITORIES_IRI(
			ONTOS_IRI.getIri() + "/2016/1/repositories"), SMELLS_IRI(
					ONTOS_IRI.getIri() + "/2016/1/codesmells"), METRICS_IRI(
							ONTOS_IRI.getIri() + "/2016/1/metrics"), OSORE_IRI(ONTOS_IRI.getIri() + "/2017/4/osore");

	private String iri;

	private OntosIRI(String iri) {
		this.iri = iri;
	}

	public String getIri() {
		return iri;
	}

}
