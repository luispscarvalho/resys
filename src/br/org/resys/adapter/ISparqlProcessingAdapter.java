package br.org.resys.adapter;

import java.util.Properties;

import org.apache.jena.query.QuerySolution;

import br.org.resys.adapter.connector.SparqlConnector;
import br.org.resys.en.Sparqls;

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
	 * @param properties
	 *            props to configure input/output path
	 * @return instance of #ISparqlProcessingAdapter
	 * @throws Exception
	 */
	public ISparqlProcessingAdapter init(Properties properties) throws Exception;

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
