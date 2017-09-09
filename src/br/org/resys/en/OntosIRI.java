package br.org.resys.en;

import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

/**
 * IRIs of all ontologies.
 * 
 * @author Luis Paulo
 */
public enum OntosIRI {

	ONTOS_IRI("http://www.semanticweb.org/resys/ontologies", null), REPOSITORIES_IRI(
			ONTOS_IRI.getIri() + "/2016/1/repositories",
			new DefaultPrefixManager("http://www.semanticweb.org/resys/ontologies/2016/1/repositories#")), SMELLS_IRI(
					ONTOS_IRI.getIri() + "/2016/1/codesmells",
					new DefaultPrefixManager(
							"http://www.semanticweb.org/resys/ontologies/2016/1/codesmells#")), METRICS_IRI(
									ONTOS_IRI.getIri() + "/2016/1/metrics",
									new DefaultPrefixManager(
											"http://www.semanticweb.org/resys/ontologies/2016/1/metrics#")), OSORE_IRI(
													ONTOS_IRI.getIri() + "/2017/4/osore",
													new DefaultPrefixManager(
															"http://www.semanticweb.org/resys/ontologies/2017/4/osore#")), OCEAN_IRI(
																	ONTOS_IRI.getIri() + "/2016/2/ocean",
																	new DefaultPrefixManager(
																			"http://www.semanticweb.org/resys/ontologies/2016/2/ocean#"));

	private String iri;
	private PrefixManager prefix;

	private OntosIRI(String iri, PrefixManager prefix) {
		this.iri = iri;
		this.prefix = prefix;
	}

	public String getIri() {
		return iri;
	}

	public PrefixManager getPrefix() {
		return prefix;
	}

}
