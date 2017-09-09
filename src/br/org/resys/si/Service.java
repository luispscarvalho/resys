package br.org.resys.si;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import com.google.common.collect.Table;

import br.org.resys.adapter.connector.SparqlConnector;
import br.org.resys.adapter.impl.EffortContextualizedIncidenceOfRefactorings;
import br.org.resys.adapter.impl.IncidenceOfRefactoringsAdapter;
import br.org.resys.adapter.impl.RefactoringsByCommittersAdapter;
import br.org.resys.en.Smells;
import br.org.resys.rre.IRefactoring;
import br.org.resys.rre.connector.ECCOBAConnector;
import br.org.resys.rre.connector.OceanConnector;
import br.org.resys.rre.connector.OsoreConnector;

/**
 * Recommendation web service interface
 * <p>
 * Service is basically a restful web service interface. It uses Jersey API
 * (https://jersey.github.io/) to add annotations to methods. Each annotation is
 * responsible for externalizing particular routines to the web.
 * <p>
 * Main functionality is performed by {@link #recommend(String)} which takes a
 * ocean*.owl file as input.
 * <p>
 * Possible improvements are:
 * <ul>
 * <li>Transfer the static initializations to a servlet context loader</li>
 * <li>add/externalize routines to recommend refactorings for inputs otherwise
 * than ocean ontology</li>
 * </ul>
 * 
 * @author Luis Paulo
 */
@Path("si")
public class Service {

	private static final String VERSION = "1.0";
	private static Properties properties;

	static {
		try {
			// load props
			InputStream configStream = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream("/config.properties");
			properties = new Properties();
			properties.load(configStream);
			// config/load ontologies
			OsoreConnector.getInstance().init(properties);
			System.out.println("Refactorings loaded:");
			OsoreConnector.getInstance().printRefactorings();
		} catch (MalformedURLException | OWLOntologyCreationException e) {
			System.err.println("Failed to load ontologies!");

			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Failed to load properties!");

			e.printStackTrace();
		}
	}

	/**
	 * @return current version of the service
	 */
	@GET
	@Path("/version")
	@Produces(MediaType.TEXT_PLAIN)
	public String getVersion() {
		return VERSION;
	}

	/**
	 * @return smells we can recommend refactorings to
	 */
	@GET
	@Path("/smells")
	@Produces(MediaType.TEXT_PLAIN)
	public String getSmells() {
		return Smells.getAllLabels().toString();
	}

	/**
	 * Recommend refactorings for a sample ontology.
	 * 
	 * @return json string containing information about the recommendation
	 *         (further details in {@link #recommend(String)})
	 */
	@GET
	@Path("/test")
	@Produces(MediaType.TEXT_PLAIN)
	public String testRecommendation() {
		return recommend("ocean_test.owl");
	}

	/**
	 * Recommend refactorings for a given ontology.
	 * 
	 * @param ocean
	 *            instance of ocean previously uploaded
	 * @return json string containing information about the recommendation.
	 *         Format:
	 *         <p>
	 *         {"onto" : "*.zip", "millis" : "9999"}
	 *         <p>
	 *         "onto": name of a zip file containing:
	 *         <ul>
	 *         <li>a new instance of ocean with embedded refactoring
	 *         recommendations</li>
	 *         <li>all imported ontologies</li>
	 *         </ul>
	 *         "millis": the duration of the recommendation
	 */
	@GET
	@Path("/recommend/{ocean}")
	@Produces(MediaType.TEXT_PLAIN)
	public String recommend(@PathParam("ocean") String ocean) {
		return recommend(ocean, null, 0);
	}

	/**
	 * Recommend refactorings for a given ontology, but only for the smells
	 * introduced by commits that correlate significantly with development
	 * effort.
	 * <p>
	 * It is necessary to provide an instance of ocean and a dataset of
	 * correlations produced by ECCOBA. Instructions about how to use ECCOBA can
	 * be found at: https://github.com/luispscarvalho/der_codesmells/wiki.
	 * 
	 * @param ocean
	 *            instance of ocean previously uploaded.
	 * @param eccoba.dataset
	 *            a dataset of correlations created by ECCOBA.
	 * @param minimal.correlation
	 *            the minimal correlation used to generate the dataset
	 * @return json string containing information about the recommendation.
	 *         Format:
	 *         <p>
	 *         {"onto" : "*.zip", "millis" : "9999"}
	 *         <p>
	 *         "onto": name of a zip file containing:
	 *         <ul>
	 *         <li>a new instance of ocean with embedded refactoring
	 *         recommendations</li>
	 *         <li>all imported ontologies</li>
	 *         </ul>
	 *         "millis": the duration of the recommendation
	 */
	@GET
	@Path("/recommend/byeffortcorrelation/{ocean}/{eccoba.dataset}/{minimal.correlation}")
	@Produces(MediaType.TEXT_PLAIN)
	public String recommend(@PathParam("ocean") String ocean, @PathParam("eccoba.dataset") String dataset,
			@PathParam("minimal.correlation") double minimalCorrelation) {
		long millis = (new Date()).getTime();
		String result = "";

		try {
			OceanConnector oceanConnector = OceanConnector.getInstance().init(properties);
			String newOnto = oceanConnector.loadAndReplicate(ocean);

			oceanConnector.prepareRecommendation();

			Map<Smells, List<OWLNamedIndividual>> smells = null;
			if (dataset != null) {
				ECCOBAConnector eccobaConnector = ECCOBAConnector.getInstance().init(properties);
				Table<Date, String, Double> correlationsByDateAndCommit = eccobaConnector.loadCorrelations(dataset)
						.getCorrelationsByDateAndCommit();
				smells = oceanConnector.addEffortContext(minimalCorrelation).loadSmells(correlationsByDateAndCommit);
			} else {
				smells = oceanConnector.loadSmells();
			}

			Map<OWLNamedIndividual, List<IRefactoring>> refactorings = OsoreConnector.getInstance()
					.recommendRefactorings(smells);
			String newZip = oceanConnector.saveRefactorings(refactorings).zip(newOnto);

			millis = (new Date()).getTime() - millis;

			result = "{\"onto\" : \"" + newOnto + "\", \"zip\" : \"" + newZip + "\", \"millis\" : \"" + millis + "\"}";
		} catch (OWLOntologyCreationException | IOException | OWLOntologyStorageException | ParseException e) {
			result = "failed to recommend refactorings";

			e.printStackTrace();
		}

		return result;
	}

	/**
	 * Process the incidence of refactorings introduced in the source code by
	 * the project's committers
	 * <p>
	 * As a result, a csv file is produced
	 * 
	 * @param ocean
	 * @return json string containing information about the incidence of
	 *         refactorings. Format:
	 *         <p>
	 *         {"csv" : "*.csv", "millis" : "9999"}
	 *         <p>
	 *         "csv": name of a csv file contained all processed data "millis":
	 *         the duration of the recommendation
	 */
	@GET
	@Path("/refactoringsbycommitters/{ocean}")
	@Produces(MediaType.TEXT_PLAIN)
	public String exportRefactoringsByCommitters(@PathParam("ocean") String ocean) {
		long millis = (new Date()).getTime();
		String result = "";

		SparqlConnector sparqlConn = SparqlConnector.getInstance().init(properties);
		try {
			RefactoringsByCommittersAdapter adapter = new RefactoringsByCommittersAdapter();
			adapter.init(properties);

			sparqlConn.adapt(ocean, adapter);

			millis = (new Date()).getTime() - millis;

			result = "{\"csv\" : \"" + adapter.getCSVFileName() + "\", \"millis\" : \"" + millis + "\"}";
		} catch (Exception e) {
			result = "failed to export the refactorings by committers";

			e.printStackTrace();
		}

		return result;
	}

	/**
	 * Process the incidence of refactorings through a timeline of occurrence of
	 * smells
	 * <p>
	 * As a result, a csv file is produced
	 * 
	 * @param ocean
	 * @return json string containing information about the incidence of
	 *         refactorings. Format:
	 *         <p>
	 *         {"csv" : "*.csv", "millis" : "9999"}
	 *         <p>
	 *         "csv": name of a csv file contained all processed data "millis":
	 *         the duration of the recommendation
	 */
	@GET
	@Path("/incidenceofrefactorings/{ocean}")
	@Produces(MediaType.TEXT_PLAIN)
	public String exportIncidenceOfRefactorings(@PathParam("ocean") String ocean) {
		long millis = (new Date()).getTime();
		String result = "";

		SparqlConnector sparqlConn = SparqlConnector.getInstance().init(properties);
		try {
			IncidenceOfRefactoringsAdapter adapter = new IncidenceOfRefactoringsAdapter();
			adapter.init(properties);

			sparqlConn.adapt(ocean, adapter);

			millis = (new Date()).getTime() - millis;

			result = "{\"csv\" : \"" + adapter.getCSVFileName() + "\", \"millis\" : \"" + millis + "\"}";
		} catch (Exception e) {
			result = "unable to export the incidence of refactorings";

			e.printStackTrace();
		}

		return result;
	}

	/**
	 * Process the incidence of refactorings through a timeline of occurrence of
	 * smells, except that only the refactorings recommended for smells that
	 * correlated with development effort are taken in consideration
	 * <p>
	 * As a result, a csv file is produced
	 * 
	 * @param ocean
	 * @return json string containing information about the incidence of
	 *         refactorings. Format:
	 *         <p>
	 *         {"csv" : "*.csv", "millis" : "9999"}
	 *         <p>
	 *         "csv": name of a csv file contained all processed data "millis":
	 *         the duration of the recommendation
	 */

	@GET
	@Path("/incidenceofrefactorings/contextualizedbyeffort/{ocean}")
	@Produces(MediaType.TEXT_PLAIN)
	public String exportEffortContextualizedIncidenceOfRefactorings(@PathParam("ocean") String ocean) {
		long millis = (new Date()).getTime();
		String result = "";

		SparqlConnector sparqlConn = SparqlConnector.getInstance().init(properties);
		try {
			EffortContextualizedIncidenceOfRefactorings adapter = new EffortContextualizedIncidenceOfRefactorings();
			adapter.init(properties);

			sparqlConn.adapt(ocean, adapter);

			millis = (new Date()).getTime() - millis;

			result = "{\"csv\" : \"" + adapter.getCSVFileName() + "\", \"millis\" : \"" + millis + "\"}";
		} catch (Exception e) {
			result = "unable to export the incidence of refactorings";

			e.printStackTrace();
		}

		return result;
	}

}
