package br.org.resys.rre.connector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import com.google.common.collect.Table;

import br.org.resys.en.OntosIRI;
import br.org.resys.en.Smells;
import br.org.resys.rre.IRefactoring;
import br.org.resys.util.Util;

/**
 * Connector that manipulates instances of ONTOCEAN
 * <p>
 * This class is capable of:
 * <ul>
 * <li>loading instances of ocean</li>
 * <li>retrieving instances of smells stored in ocean</li>
 * <li>binding refactorings to instances of smells</li>
 * <li>combining elements of ocean and osore in a new ontology</li>
 * </ul>
 * <p>
 * 
 * @author Luis Paulo
 */
public class OceanConnector {

	/**
	 * @return non-singleton instance of the connector
	 */
	public static OceanConnector getInstance() {
		return new OceanConnector();
	}

	private OWLOntologyManager manager;
	private OWLOntology ocean;
	private OWLDataFactory factory;

	private OWLNamedIndividual recommendationInd;
	private OWLObjectProperty refactoredByProp;
	private OWLObjectProperty recommendedForProp;

	private String inputPath, outputPath;

	/**
	 * Initialization routine. It must be executed first, prior to manipulating
	 * the ontology, since importing dependencies is necessary to load ocean. It
	 * also mandatory to setup urls & iris.
	 * <p>
	 * Property file must be (re)configured to meet specific input/output paths
	 * needs.
	 * 
	 * @param properties
	 *            props to configure input/output path
	 * @return instance of #OceanConnector
	 * @throws MalformedURLException
	 *             if urls are malformed and ontologies cannot be found
	 */
	public OceanConnector init(Properties properties) throws MalformedURLException {
		// configure urls & paths
		inputPath = properties.getProperty("ontos.input.path");
		outputPath = properties.getProperty("ontos.output.path");

		String metricsUrl = "file://" + outputPath + "/metrics.owl";
		String codesmellsUrl = "file://" + outputPath + "/codesmells.owl";
		String repositoriesUrl = "file://" + outputPath + "/repositories.owl";
		// initiate everything
		manager = OWLManager.createConcurrentOWLOntologyManager();
		IRI repositoriesIRI = IRI.create(OntosIRI.REPOSITORIES_IRI.getIri());
		IRI repositoriesLocation = IRI.create(repositoriesUrl);
		IRI codesmellsIRI = IRI.create(OntosIRI.SMELLS_IRI.getIri());
		IRI codesmellsLocation = IRI.create(codesmellsUrl);
		IRI metricsIRI = IRI.create(OntosIRI.METRICS_IRI.getIri());
		IRI metricsLocation = IRI.create(metricsUrl);
		// importing referenced ontologies
		manager.getIRIMappers().add(new SimpleIRIMapper(repositoriesIRI, repositoriesLocation));
		manager.getIRIMappers().add(new SimpleIRIMapper(codesmellsIRI, codesmellsLocation));
		manager.getIRIMappers().add(new SimpleIRIMapper(metricsIRI, metricsLocation));

		return this;
	}

	/**
	 * Ontologies must be uploaded to resys' input path, as informed in the
	 * property file. Once they are uploaded, copies are saved in the output
	 * path. Original ontologies are not going to be manipulated so that If
	 * recommendation fails a new upload will not be required.
	 * <p>
	 * Loading the ontology requires the setup of dependencies. Thus,
	 * {@link #init(Properties)} must be executed first to prepare all of the
	 * necessary urls & iris.
	 * 
	 * @param ontology
	 *            an instance of ocean to base recommendations on
	 * @return new ontology saved in the output path
	 * @throws OWLOntologyCreationException
	 *             if it fails to create an ontology
	 * @throws IOException
	 *             if it fails to manipulate the ontology's physical file
	 */
	public String loadAndReplicate(String ontology) throws OWLOntologyCreationException, IOException {
		// replicate osore (from input to output)
		String newOnto = replicate(ontology);
		// load osore
		String url = "file://" + outputPath + "/" + newOnto;
		IRI oceanLocation = IRI.create(new URL(url));

		System.out.println("Reading smells from: " + url + "...");
		ocean = manager.loadOntology(oceanLocation);

		return newOnto;
	}

	/**
	 * 
	 * @return
	 */
	public OceanConnector prepareRecommendation() {
		factory = manager.getOWLDataFactory();
		// create a new recommendation class
		recommendationInd = addRecommendation();
		// link each instance of smell to respective refactorings
		// add an object property (CodeSmell --- refactoredBy --->
		// Refactoring)
		refactoredByProp = addRefactoredByProperty();
		// and to recommendation (Recommendation --- hasRecommendedFor --->
		// CodeSmell)
		recommendedForProp = addHasRecommendedForProperty();

		return this;
	}

	/**
	 * Load all smells from an instance of ocean
	 * <p>
	 * Prior to recommending refactorings, smells must be retrieved from the
	 * ontology. As a result a mapping between each type of {@link Smells} and
	 * respective ontological instances is created. The mapping is passed to
	 * {@link OsoreConnector#recommendRefactorings(Map)} in order to recommend
	 * refactorings.
	 * 
	 * @return mapping between smells and their ontological instances
	 */
	@SuppressWarnings("deprecation")
	public Map<Smells, List<OWLNamedIndividual>> loadSmells() {
		Map<Smells, List<OWLNamedIndividual>> smells = new HashMap<Smells, List<OWLNamedIndividual>>();
		// create a new structural reasoner to retrieve smell classes
		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(ocean);
		for (OWLClass c : ocean.getClassesInSignature()) {
			String clazz = c.getIRI().getFragment();
			Smells smell = Smells.UNKNOWN;
			// if the class' type is a known smell...
			if ((smell = Smells.fromOntoType(clazz)) != Smells.UNKNOWN) {
				// ... retrieve all of its instances...
				NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(c, false);
				if ((instances != null) && (!instances.isEmpty())) {
					List<OWLNamedIndividual> smellInstances = new ArrayList<OWLNamedIndividual>();
					for (OWLNamedIndividual owlSmell : instances.getFlattened()) {
						smellInstances.add(owlSmell);
					}
					// ... and add them to the mapping
					smells.put(smell, smellInstances);
				}
			}
		}

		return smells;
	}

	/**
	 * Load smells from an instance of ocean, except that smells must have been
	 * introduced by commits that have correlated significantly with effort
	 * <p>
	 * Prior to recommending refactorings, smells must be retrieved from the
	 * ontology. As a result a mapping between each type of {@link Smells} and
	 * respective ontological instances is created. The mapping is passed to
	 * {@link OsoreConnector#recommendRefactorings(Map)} in order to recommend
	 * refactorings.
	 * <p>
	 * Not all instances of code smell will be taken in consideration. Only
	 * those that has been introduced by a commit that correlates in time with a
	 * period of high effort. It takes a table of correlations (DATE x COMMITS)
	 * to filter the smells. The table is provided by the input of a
	 * ECCOBA-generated csv file in {@link ECCOBAConnector}.
	 * 
	 * @param correlationsByDateAndCommits
	 *            a table representing the correlated commits through time
	 * @return mapping between smells and their ontological instances
	 */
	@SuppressWarnings("deprecation")
	public Map<Smells, List<OWLNamedIndividual>> loadSmells(Table<Date, String, Double> correlationsByDateAndCommits) {
		Map<Smells, List<OWLNamedIndividual>> smells = new HashMap<Smells, List<OWLNamedIndividual>>();
		// create a new structural reasoner to retrieve commit classes
		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(ocean);
		for (OWLClass c : ocean.getClassesInSignature()) {
			String clazz = c.getIRI().getFragment();
			if (clazz.equals("Commit")) {
				NodeSet<OWLNamedIndividual> commits = reasoner.getInstances(c, false);
				// after retrieving a commit class...
				for (OWLNamedIndividual owlCommit : commits.getFlattened()) {
					String commitId = owlCommit.getIRI().getFragment();
					// ... verify if it correlates highly with effort...
					if (correlationsByDateAndCommits.containsColumn(commitId)) {
						// ...if so, extract all props...
						Set<OWLObjectPropertyAssertionAxiom> objProps = ocean
								.getObjectPropertyAssertionAxioms(owlCommit);
						for (OWLObjectPropertyAssertionAxiom prop : objProps) {
							// ...to find instances of code smell
							if (prop.getProperty().toString().contains("#hasIntroduced")) {
								OWLNamedIndividual owlSmell = prop.getObject().asOWLNamedIndividual();
								Stream<OWLClassExpression> expressions = EntitySearcher.getTypes(owlSmell, ocean);
								for (Iterator<OWLClassExpression> i = expressions.iterator(); i.hasNext();) {
									OWLClassExpression classExpression = i.next();
									Smells smell = Smells.UNKNOWN;
									// if the type is a known smell...
									if ((smell = Smells.fromOntoType(
											classExpression.asOWLClass().getIRI().getFragment())) != Smells.UNKNOWN) {
										// ... it is added to the map
										if (!smells.containsKey(smell)) {
											smells.put(smell, new ArrayList<OWLNamedIndividual>());
										}
										smells.get(smell).add(owlSmell);
									}
								}
							}
						}
					}
				}
			}
		}

		return smells;
	}

	/**
	 * Associate the recommendation with a effort context
	 * <p>
	 * The context is based on the correlation between the evolution of effort
	 * and the incidence of codesmells as calculated by ECCOBA. As a result, if
	 * a ECCOBA's dataset is provided, a new object property links Effort and
	 * recommendation individuals:
	 * <p>
	 * Recommendation --> contextualizedBy --> Effort
	 * 
	 * @param minimalCorrelation
	 *            the minimal value used as threshold to calculate the
	 *            correlation
	 * @return instance of #OceanConnector
	 */
	public OceanConnector addEffortContext(double minimalCorrelation) {
		// create a new effort contextualization class
		OWLClass effortClazz = factory.getOWLClass(OntosIRI.OSORE_IRI.getIri() + "#Effort");
		OWLNamedIndividual effortInd = factory.getOWLNamedIndividual(Util.generateUid(),
				OntosIRI.OSORE_IRI.getPrefix());
		OWLClassAssertionAxiom contextAssertion = factory.getOWLClassAssertionAxiom(effortClazz, effortInd);
		manager.addAxiom(ocean, contextAssertion);
		// set correlation prop value
		OWLDataProperty correlationValue = factory.getOWLDataProperty("correlation", OntosIRI.OSORE_IRI.getPrefix());
		OWLDataPropertyAssertionAxiom dateValueAssertion = factory.getOWLDataPropertyAssertionAxiom(correlationValue,
				effortInd, minimalCorrelation);
		manager.addAxiom(ocean, dateValueAssertion);
		// link Context to Recommendation
		OWLObjectProperty contextualizedByProp = factory
				.getOWLObjectProperty(IRI.create(OntosIRI.OSORE_IRI.getIri() + "#contextualizedBy"));
		OWLObjectPropertyAssertionAxiom contextualizedByAssertion = factory
				.getOWLObjectPropertyAssertionAxiom(contextualizedByProp, recommendationInd, effortInd);
		manager.addAxiom(ocean, contextualizedByAssertion);

		return this;
	}

	/**
	 * Bind and save all recommended refactorings to the smells in the
	 * ontology's physical file
	 * <p>
	 * It is necessary to add osore as a dependency (import) in ocean. It is
	 * also imperative that a "refactoredBy" object property is added to the
	 * ontology as well. The property will link each instance of smells to its
	 * respective refactoring techniques.
	 * <p>
	 * As a result, a new instance of ocean, in which refactorings are bound to
	 * smells, is generated.
	 * 
	 * @param refactorings
	 *            a mapping between ontological instances of smells and
	 *            applicable refactorings, as generated by
	 *            {@link OsoreConnector#recommendRefactorings(Map)}
	 * @return instance of #OceanConnector
	 * @throws OWLOntologyStorageException
	 */
	@SuppressWarnings("deprecation")
	public OceanConnector saveRefactorings(Map<OWLNamedIndividual, List<IRefactoring>> refactorings)
			throws OWLOntologyStorageException {
		// add osore as a new import in ocean
		OWLImportsDeclaration importDeclaration = factory
				.getOWLImportsDeclaration(IRI.create(OntosIRI.OSORE_IRI.getIri()));
		manager.applyChange(new AddImport(ocean, importDeclaration));

		// linking the smells...
		int numOfRefactorings = 0;
		List<OWLObjectPropertyAssertionAxiom> objPropsAssertionAxioms = new ArrayList<OWLObjectPropertyAssertionAxiom>();
		for (Entry<OWLNamedIndividual, List<IRefactoring>> e : refactorings.entrySet()) {
			List<IRefactoring> refacs = e.getValue();
			if (refacs != null) {
				// ... to refactorings
				for (IRefactoring refactoring : refacs) {
					OWLObjectPropertyAssertionAxiom refactoredByAssertion = factory.getOWLObjectPropertyAssertionAxiom(
							refactoredByProp, e.getKey(), refactoring.getOwlRefactoring());
					objPropsAssertionAxioms.add(refactoredByAssertion);

					numOfRefactorings++;
				}
				// ... to recommendation
				OWLObjectPropertyAssertionAxiom recommendedForAssertion = factory
						.getOWLObjectPropertyAssertionAxiom(recommendedForProp, recommendationInd, e.getKey());
				objPropsAssertionAxioms.add(recommendedForAssertion);
			}
		}
		System.out.println("Recommending refactorings...");
		manager.addAxioms(ocean, objPropsAssertionAxioms);
		// save refactorings in ocean
		OWLDocumentFormat format = new RDFXMLDocumentFormat();
		manager.saveOntology(ocean, format);
		System.out.println(numOfRefactorings + " refactorings recommended!");

		return this;
	}

	/**
	 * Create a new Recommendation individual
	 * 
	 * @return an ontological instance of a new Recommendation
	 */
	private OWLNamedIndividual addRecommendation() {
		// create a new recommendation class
		OWLClass recommendationClazz = factory.getOWLClass(OntosIRI.OSORE_IRI.getIri() + "#Recommendation");
		OWLNamedIndividual recommendationInd = factory.getOWLNamedIndividual(Util.generateUid(),
				OntosIRI.OSORE_IRI.getPrefix());
		OWLClassAssertionAxiom recommendationAssertion = factory.getOWLClassAssertionAxiom(recommendationClazz,
				recommendationInd);
		manager.addAxiom(ocean, recommendationAssertion);
		// add the date of the recommendation
		OWLDataProperty dateValue = factory.getOWLDataProperty("datetime", OntosIRI.OSORE_IRI.getPrefix());
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String recommendationDate = dateFormat.format(new Date());
		OWLDataPropertyAssertionAxiom dateValueAssertion = factory.getOWLDataPropertyAssertionAxiom(dateValue,
				recommendationInd, recommendationDate);
		manager.addAxiom(ocean, dateValueAssertion);

		return recommendationInd;
	}

	/**
	 * Create a object property to link Codesmell and Refactoring
	 * <p>
	 * The relationship follows the pattern: Codesmell --> refactoredBy -->
	 * Refactoring
	 * 
	 * @return an ontological instance of the property
	 */
	private OWLObjectProperty addRefactoredByProperty() {
		OWLEntity entity = factory.getOWLEntity(EntityType.OBJECT_PROPERTY,
				IRI.create(OntosIRI.OCEAN_IRI.getIri() + "#refactoredBy"));
		OWLAxiom objPropAxiom = factory.getOWLDeclarationAxiom(entity);
		manager.addAxiom(ocean, objPropAxiom);
		// retrieve the new property to set its domain and range
		OWLObjectProperty refactoredBy = factory
				.getOWLObjectProperty(IRI.create(OntosIRI.OCEAN_IRI.getIri() + "#refactoredBy"));
		// domain
		OWLClass codeSmellClazz = factory.getOWLClass(OntosIRI.SMELLS_IRI.getIri() + "#Codesmell");
		OWLObjectPropertyDomainAxiom domainAxiom = factory.getOWLObjectPropertyDomainAxiom(refactoredBy,
				codeSmellClazz);
		manager.addAxiom(ocean, domainAxiom);
		// range
		OWLClass refactoringClazz = factory.getOWLClass(OntosIRI.OSORE_IRI.getIri() + "#Refactoring");
		OWLObjectPropertyRangeAxiom rangeAxiom = factory.getOWLObjectPropertyRangeAxiom(refactoredBy, refactoringClazz);
		manager.addAxiom(ocean, rangeAxiom);

		return refactoredBy;
	}

	/**
	 * Create a object property to link Recommendation and Codesmell
	 * <p>
	 * The relationship follows the pattern: Recommendation --> hasRecommendedFor -->
	 * Codesmell
	 * 
	 * @return an ontological instance of the property
	 */
	private OWLObjectProperty addHasRecommendedForProperty() {
		OWLEntity entity = factory.getOWLEntity(EntityType.OBJECT_PROPERTY,
				IRI.create(OntosIRI.OCEAN_IRI.getIri() + "#hasRecommendedFor"));
		OWLAxiom objPropAxiom = factory.getOWLDeclarationAxiom(entity);
		manager.addAxiom(ocean, objPropAxiom);
		// retrieve the new property to set its domain and range
		OWLObjectProperty recommendedFor = factory
				.getOWLObjectProperty(IRI.create(OntosIRI.OCEAN_IRI.getIri() + "#hasRecommendedFor"));
		// domain
		OWLClass recommendationClazz = factory.getOWLClass(OntosIRI.OSORE_IRI.getIri() + "#Recommendation");
		OWLObjectPropertyDomainAxiom domainAxiom = factory.getOWLObjectPropertyDomainAxiom(recommendedFor,
				recommendationClazz);
		manager.addAxiom(ocean, domainAxiom);
		// range
		OWLClass codeSmellClazz = factory.getOWLClass(OntosIRI.SMELLS_IRI.getIri() + "#Codesmell");
		OWLObjectPropertyRangeAxiom rangeAxiom = factory.getOWLObjectPropertyRangeAxiom(recommendedFor, codeSmellClazz);
		manager.addAxiom(ocean, rangeAxiom);

		return recommendedFor;
	}

	/**
	 * copy an instance of ocean from the input to the output path.
	 * <p>
	 * All manipulations will be performed on the copy stored in the output
	 * path.
	 * 
	 * @param ontology
	 *            instance of ocean to be copied
	 * @return the name of the new file
	 * @throws IOException
	 *             if it fails to handle the file's IO operations
	 */
	private String replicate(String ontology) throws IOException {
		File input = new File(inputPath + "/" + ontology);

		String newOnto = "ocean_" + Util.generateUid() + ".owl";
		File output = new File(outputPath + "/" + newOnto);

		FileUtils.copyFile(input, output);

		return newOnto;
	}

	/**
	 * Compress the resulting ontology and dependencies
	 * <p>
	 * The size of the resulting ontology can be quite large. To facilitate the
	 * download of the ontologies, they are added to a zip-compressed file.
	 * 
	 * @param ontology
	 *            the ontology to be compressed
	 * @return the name of the new zip file
	 * @throws IOException
	 *             if it fails to hangle the zip file's IO operations
	 */
	public String zip(String ontology) throws IOException {
		byte[] buffer = new byte[1024];

		String newZip = "ocean_" + Util.generateUid() + ".zip";

		FileOutputStream fileStream = new FileOutputStream(outputPath + "/" + newZip);
		ZipOutputStream zipStream = new ZipOutputStream(fileStream);

		String[] files = { outputPath + "/metrics.owl", outputPath + "/codesmells.owl",
				outputPath + "/repositories.owl", outputPath + "/osore.owl", outputPath + "/" + ontology };

		for (String fileName : files) {
			File file = new File(fileName);
			FileInputStream stream = new FileInputStream(file);

			ZipEntry entry = new ZipEntry(file.getName());
			zipStream.putNextEntry(entry);

			int length;
			while ((length = stream.read(buffer)) > 0) {
				zipStream.write(buffer, 0, length);
			}

			zipStream.closeEntry();
			stream.close();
		}

		zipStream.close();

		return newZip;
	}

}
