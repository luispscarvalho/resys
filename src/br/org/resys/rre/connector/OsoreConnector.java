package br.org.resys.rre.connector;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import br.org.resys.en.Smells;
import br.org.resys.rre.IRefactoring;
import br.org.resys.rre.ITemplate;
import br.org.resys.rre.impl.Refactoring;
import br.org.resys.rre.impl.Template;

/**
 * Connector that manipulates OSORE
 * <p>
 * This class is capable of:
 * <ul>
 * <li>loading instances of osore</li>
 * <li>retrieving instances of refactorings stored in osore</li>
 * <li>binding refactorings to instances of smells</li>
 * </ul>
 * <p>
 * Possible improvements are:
 * <ul>
 * <li>add a lazy loading of osore instances</li>
 * <li>add a lazy recommendation of refactorings</li>
 * </ul>
 * 
 * @author Luis Paulo
 */
@SuppressWarnings("deprecation")
public class OsoreConnector {
	private static OsoreConnector instance;

	/**
	 * @return singleton instance of this connector
	 */
	public static OsoreConnector getInstance() {
		if (instance == null) {
			instance = new OsoreConnector();
		}

		return instance;
	}

	// pool of refactorings (refactorings per smell)
	private Map<Smells, List<IRefactoring>> refactorings;

	private OWLOntologyManager manager;
	private OWLOntology osore;

	/**
	 * Initialization routine. It must be executed first, prior to manipulating
	 * the ontology, in order to setup urls & iris.
	 * <p>
	 * Property file must be (re)configured to meet specific osore referenced
	 * path+file.
	 * 
	 * @param properties
	 *            props to configure osore's path+file.
	 * @return instance of #OsoreConnector
	 * @throws MalformedURLException
	 *             if urls are malformed and ontologies cannot be found
	 */
	public OsoreConnector init(Properties properties) throws MalformedURLException, OWLOntologyCreationException {
		// where is osore?
		String url = "file://" + properties.getProperty("ontos.osore.path") + "/osore.owl";
		System.out.println("Attempting to read OSORE at: " + url);
		// initiate everything
		refactorings = new HashMap<Smells, List<IRefactoring>>();
		manager = OWLManager.createOWLOntologyManager();
		IRI osoreLocation = IRI.create(new URL(url));
		// load osore
		osore = manager.loadOntology(osoreLocation);
		// populate pool of refactorings
		loadRefactorings();

		return this;
	}

	/**
	 * Load refactoring techniques from an instance of osore
	 * <p>
	 * Prior to recommending refactorings, a mapping of refactorings per smell
	 * is loaded from the ontology. The refactorings are hardcoded in osore as
	 * ontological individuals. After being retrieved they are parsed into
	 * instances of {@link IRefactoring}. Each refactoring is mapped to a
	 * instance of {@link Smells}. The resulting mapping will link 1-smell to
	 * N-refactoring.
	 * 
	 * @return instance of #OsoreConnector
	 */
	private OsoreConnector loadRefactorings() {
		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		// create a new structural reasoner to retrieve refactorings
		OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(osore);
		for (OWLClass c : osore.getClassesInSignature()) {
			// if the ontological class is a refactoring...
			if (c.getIRI().getFragment().equals("Refactoring")) {
				// ...fetch all of its instances...
				NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(c, false);
				for (OWLNamedIndividual owlRefactoring : instances.getFlattened()) {
					// ...that are transformed into a pair of
					// 1-smell--->N-refactorings...
					IRefactoring refactoring = fromOWLRefactoring(owlRefactoring);
					List<Smells> targSmells = getTargetedSmells(osore.getDataPropertyAssertionAxioms(owlRefactoring));
					// ...which is added to the refactoring pool
					addToRefactorings(targSmells, refactoring);
				}
			}
		}

		return this;
	}

	/**
	 * Extract a concrete instance of {@link ITemplate} from an ontological
	 * instance of refactoring template.
	 * <p>
	 * Basically, a Template is composed of a "before" and "after" pair of
	 * sample source-code or modeling diagram (usually, a UML class diagram). We
	 * are mining only source-code-based templates.
	 * 
	 * @param owlTemplate
	 *            the ontological instance of template extracted from osore.
	 * @return instance of {@link Template}
	 */
	private Template fromOWLTemplate(OWLIndividual owlTemplate) {
		Template template = new Template();
		// retrieve the set of data properties of a template...
		Set<OWLDataPropertyAssertionAxiom> dataProps = osore.getDataPropertyAssertionAxioms(owlTemplate);
		// ...verify if the prop is...
		for (OWLDataPropertyAssertionAxiom prop : dataProps) {
			// ...a before sample
			if (prop.getProperty().toString().contains("#before")) {
				// ...to set it as a before sample in the template
				template.setBefore(prop.getObject().getLiteral());
				// ...or a after sample...
			} else if (prop.getProperty().toString().contains("#after")) {
				// ...to set it as an after sample in the template
				template.setAfter(prop.getObject().getLiteral());
			}
		}

		return template;
	}

	/**
	 * Extract a concrete instance of {@link IRefactoring} from an ontological
	 * instance of refactoring.
	 * <p>
	 * A refactoring has a name, a description and a template, which shows how
	 * the refactoring must be applied.
	 * 
	 * @param owlRefactoring
	 *            the ontological instance of refactoring extracted from osore.
	 * @return instance of {@link Refactoring}
	 */
	private Refactoring fromOWLRefactoring(OWLNamedIndividual owlRefactoring) {
		Refactoring refactoring = new Refactoring();
		refactoring.setAcronym(owlRefactoring.getIRI().getRemainder().get());
		// from a given ontological instance of a refactoring technique...
		// ...extract all data props...
		Set<OWLDataPropertyAssertionAxiom> dataProps = osore.getDataPropertyAssertionAxioms(owlRefactoring);
		for (OWLDataPropertyAssertionAxiom prop : dataProps) {
			// ...and localize the name prop...
			if (prop.getProperty().toString().contains("#name")) {
				// ...to set it in the refactoring...
				refactoring.setName(prop.getObject().getLiteral());
				// ...and the description prop...
			} else if (prop.getProperty().toString().contains("#description")) {
				// ...to set it in the refactoring as well.
				refactoring.setDescription(prop.getObject().getLiteral());
			}
		}

		// ...also extract the object props...
		Set<OWLObjectPropertyAssertionAxiom> objProps = osore.getObjectPropertyAssertionAxioms(owlRefactoring);
		for (OWLObjectPropertyAssertionAxiom prop : objProps) {
			// ...to find the ontological instance of the template that show how
			// to apply the refactoring
			if (prop.getProperty().toString().contains("#hasTemplate")) {
				refactoring.setTemplate(fromOWLTemplate(prop.getObject()));
			}
		}

		// in the end, the original ontological instance of refactoring is also
		// stored in the refactoring for future use
		refactoring.setOwlRefactoring(owlRefactoring);

		return refactoring;
	}

	/**
	 * Retrieve smells which a particular refactoring is applicable to.
	 * <p>
	 * Osore also informs the smells that can be mitigated by the application of
	 * refactorings. This information is used to create a mapping between smells
	 * and their respective refactorings.
	 * 
	 * @param dataProps
	 *            collection of data props of the refactoring
	 * @return list of smells which can be mitigated by the refactoring.
	 */
	private List<Smells> getTargetedSmells(Set<OWLDataPropertyAssertionAxiom> dataProps) {
		List<Smells> smells = new ArrayList<Smells>();
		// from all properties of the ontological instance of the refactoring...
		for (OWLDataPropertyAssertionAxiom prop : dataProps) {
			// ...it must be found the one which links the refactoring to the
			// targeted smells
			if (prop.getProperty().toString().contains("#applicableTo")) {
				// ...once a smell is found, it is added to the list of targeted
				// smells
				smells.add(Smells.fromOntoType(prop.getObject().getLiteral()));
			}
		}

		return smells;
	}

	/**
	 * Map a refactoring to its list of targeted smells
	 * <p>
	 * The mapping is used as a glossary of refactorings that is indexed by the
	 * smells. This meaning, to recommend refactorings an instance of smell is
	 * used to find them. Important to say: a refactoring can be mapped to more
	 * than one smell.
	 * 
	 * @param targetedSmells
	 *            the list of smell which the refactoring is applicable to
	 * @param refactoring
	 *            the refactoring that is mapped to the smells
	 * @return
	 */
	private OsoreConnector addToRefactorings(List<Smells> targetedSmells, IRefactoring refactoring) {
		// considering each smell in the list of targeted smells...
		for (Smells smell : targetedSmells) {
			// ...if the pool already contains the smell...
			if (refactorings.containsKey(smell)) {
				// ...the list of refactorings is retrieved...
				List<IRefactoring> refacs = refactorings.get(smell);
				// ...to add the new refactoring
				refacs.add(refactoring);
			} else {
				// otherwise, the smell has not been added before, so...
				// a new list of refactorings is created...
				List<IRefactoring> refacs = new ArrayList<IRefactoring>();
				// ...to add the new refactoring...
				refacs.add(refactoring);
				// ...that is paired with the new smell
				refactorings.put(smell, refacs);
			}
		}

		return this;
	}

	/**
	 * Recommend a list of refactorings for a given mapping of smells and its
	 * ontological instances (individuals).
	 * <p>
	 * As the instances of smells are mined from ocean, they are mapped by
	 * {@link OceanConnector} to their correspondent {@link Smells}. For each
	 * instance a list of refactorings is provided to be stored in a new
	 * instance of ocean.
	 * 
	 * @param smells
	 *            mapping of smells and their respective ontological instances
	 * @return a mapping between ontological instances of smells and
	 *         refactorings
	 */
	public Map<OWLNamedIndividual, List<IRefactoring>> recommendRefactorings(
			Map<Smells, List<OWLNamedIndividual>> smells) {
		Map<OWLNamedIndividual, List<IRefactoring>> refacs = new HashMap<OWLNamedIndividual, List<IRefactoring>>();

		for (Entry<Smells, List<OWLNamedIndividual>> e : smells.entrySet()) {
			Smells smell = e.getKey();

			List<OWLNamedIndividual> smellInds = e.getValue();
			for (OWLNamedIndividual smellInd : smellInds) {
				refacs.put(smellInd, refactorings.get(smell));
			}
		}

		return refacs;
	}

	/**
	 * Print all refactorings
	 * 
	 * @return instance of #OsoreConnector
	 */
	public OsoreConnector printRefactorings() {
		for (Entry<Smells, List<IRefactoring>> e : refactorings.entrySet()) {
			System.out.println(e.getKey().getLabel() + " --> " + e.getValue());
		}
		
		return this;
	}

}
