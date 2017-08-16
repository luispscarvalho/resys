package br.org.resys.export;

import org.apache.jena.query.QuerySolution;

import br.org.resys.en.Sparqls;
import br.org.resys.export.connector.SparqlConnector;

/**
 * Adapter of the execution of sparql statements
 * <p>
 * To be used in association with {@link SparqlConnector}. Prior to the
 * selection of a resultset by the connector a listener can be injected into it
 * to adapt the connector to select and process the desired data adequately.
 * <p>
 * 
 * @author Luis Paulo
 */
public interface ISparqlProcessingAdapter {

	/**
	 * Select a fit sparql statement to be executed
	 * <p>
	 * Preferably, the statement must be stored in and selected from
	 * {@link Sparqls}
	 * 
	 * @return a valid sparql statement
	 */
	public String getSparql();

	/**
	 * Initialization of the adapter
	 * <p>
	 * It must be called first, prior to any further processing
	 * 
	 * @param outputPath
	 *            the output path where targeted ontologies can be found
	 * @return instance of #ISparqlProcessingAdapter
	 * @throws Exception
	 */
	public ISparqlProcessingAdapter init(String outputPath) throws Exception;

	/**
	 * Process each of every row obtained from the ontology
	 * 
	 * @param row
	 *            a row from the resultset
	 * @return instance of #ISparqlProcessingAdapter
	 * @throws Exception
	 */
	public ISparqlProcessingAdapter processing(QuerySolution row) throws Exception;

	/**
	 * Execute any pending operations
	 * <p>
	 * E.g., flushing/closing data streams
	 * 
	 * @return instance of #ISparqlProcessingAdapter
	 * @throws Exception
	 */
	public ISparqlProcessingAdapter conclude() throws Exception;

}
