package br.org.resys.export.connector;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.ModelFactory;

import br.org.resys.en.OntosIRI;
import br.org.resys.export.IExporter;

/**
 * Connector that supports the querying of ontologies
 * <p>
 * This class is capable of:
 * <ul>
 * <li>executing SPARQL statements on ontologies (ONTOCEAN + OSORE)</li>
 * <li>processing/exporting of resultsets through instances of
 * {@link IExporter}</li>
 * </ul>
 * <p>
 * Contrary to other connectors, this one relies on JENA to select data from our
 * ontologies. OWL API was not used because it does not support SPARQL-based
 * querying directly. Third-party components must be plugged in, but they
 * require statements written in a syntax that differs from those accepted by
 * protege. This may be counterproductive as it is easier to try SPARQL
 * statements out in protege before their integration with RESYS.
 * 
 * @author Luis Paulo
 */
public class SparqlConnector {
	private static SparqlConnector instance;

	/**
	 * @return singleton instance of this connector
	 */
	public static SparqlConnector getInstance() {
		if (instance == null) {
			instance = new SparqlConnector();
		}

		return instance;
	}

	private String outputPath;

	/**
	 * Initialization routine. It must be executed first, prior to executing
	 * SPARQL queries.
	 * <p>
	 * Property file must be (re)configured to meet specific input/output paths
	 * needs.
	 * 
	 * @param properties
	 *            props to configure input/output path
	 * @return instance of #SparqlConnector
	 */
	public SparqlConnector init(Properties properties) {
		outputPath = properties.getProperty("ontos.output.path");

		return this;
	}

	/**
	 * Execute a SPARQL statement on a given ontology
	 * <p>
	 * The "ontology" parameter must be a full path to an instance of ontocean
	 * after the smells and refactorings are processed and embedeed into it.
	 * <p>
	 * The sparql statement is provided by {@link IExporter} which is also
	 * responsible for exporting/processing a row of the resultset.
	 * 
	 * @param ontology
	 *            instance of ontocean
	 * @param exporter
	 *            instance of {@link IExporter}
	 * @throws Exception
	 * @return instance of #SparqlConnector
	 */
	public SparqlConnector executeExporter(String ontology, IExporter exporter) throws Exception {
		OntModel model = ModelFactory.createOntologyModel();

		OntDocumentManager docManager = model.getDocumentManager();
		docManager.addAltEntry(OntosIRI.METRICS_IRI.getIri(), "file://" + outputPath + "/metrics.owl");
		docManager.addAltEntry(OntosIRI.SMELLS_IRI.getIri(), "file://" + outputPath + "/codesmells.owl");
		docManager.addAltEntry(OntosIRI.REPOSITORIES_IRI.getIri(), "file://" + outputPath + "/repositories.owl");
		docManager.addAltEntry(OntosIRI.OSORE_IRI.getIri(), "file://" + outputPath + "/osore.owl");

		model.read(new FileReader(new File(outputPath + "/" + ontology)), "RDF/XML");

		Query query = QueryFactory.create(exporter.getSparql());
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		ResultSet results = qexec.execSelect();

		exporter.init(outputPath);
		while (results.hasNext()) {
			QuerySolution result = results.nextSolution();

			exporter.export(result);
		}
		exporter.conclude();
		
		return this;
	}

}
