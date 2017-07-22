package br.org.resys.si;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
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

import br.org.resys.data.Smell;
import br.org.resys.rre.IRefactoring;
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
		return Smell.getAllLabels().toString();
	}

	/**
	 * recommend refactorings for a sample ontology
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
	 * recommend refactorings for a given ontology
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
	 */
	@GET
	@Path("/recommend/{ocean}")
	@Produces(MediaType.TEXT_PLAIN)
	public String recommend(@PathParam("ocean") String ocean) {
		long millis = (new Date()).getTime();
		String result = "";

		try {
			OceanConnector oceanConnector = OceanConnector.getInstance().init(properties);

			String newOnto = oceanConnector.loadAndReplicate(ocean);
			Map<Smell, List<OWLNamedIndividual>> smells = oceanConnector.loadSmells();
			Map<OWLNamedIndividual, List<IRefactoring>> refactorings = OsoreConnector.getInstance()
					.recommendRefactorings(smells);

			String newZip = oceanConnector.saveRefactorings(refactorings).zip(newOnto);
			millis = (new Date()).getTime() - millis;

			result = "{\"onto\" : \"" + newZip + "\", \"millis\" : \"" + millis + "\"}";
		} catch (OWLOntologyCreationException | IOException | OWLOntologyStorageException e) {
			result = "failed to recommend refactorings";

			e.printStackTrace();
		}

		return result;
	}

}
