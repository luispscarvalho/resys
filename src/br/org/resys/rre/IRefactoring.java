package br.org.resys.rre;

import org.semanticweb.owlapi.model.OWLNamedIndividual;

/**
 * Refactoring bean/pojo
 * <p>
 * To be used to list and map refactorings after they are mined from an instance
 * of osore.
 * 
 * @author Luis Paulo
 */
public interface IRefactoring {

	/**
	 * Usually, every refactoring technique has an acronym. For instance,
	 * "Consolidate Conditional Expression" is a technique and CCE is its
	 * acronym.
	 * 
	 * @param acronym
	 * @return concrete instance of IRefactoring
	 */
	public IRefactoring setAcronym(String acronym);

	public String getAcronym();

	/**
	 * Refactoring techniques have a very characteristic name, such as
	 * "Consolidate Conditional Expression" and "Decompose Conditional".
	 * 
	 * @param name
	 * @return concrete instance of IRefactoring
	 */
	public IRefactoring setName(String name);

	public String getName();

	/**
	 * The description describes the cases in which the refactoring is
	 * applicable or gives a hint about to use it.
	 * 
	 * @param description
	 * @return concrete instance of IRefactoring
	 */
	public IRefactoring setDescription(String description);

	public String getDescription();

	/**
	 * Refactoring techniques have templates that describe "before" and "after"
	 * situations as a specific refactoring is applied.
	 * 
	 * @param template
	 * @return concrete instance of IRefactoring
	 */
	public IRefactoring setTemplate(ITemplate template);

	public ITemplate getTemplate();

	/**
	 * The refactoring techniques are obtained from osore, this get/set pair is
	 * intended to keep track of the former OWLNamedIndividual which a
	 * refactoring was mined from.
	 * 
	 * @return former OWLNamedIndividual mined from osore
	 */
	public OWLNamedIndividual getOwlRefactoring();

	public void setOwlRefactoring(OWLNamedIndividual owlRefactoring);

	/**
	 * Pretty-print json document representing the refactoring
	 * 
	 * @return a json doc
	 */
	public String toJson();

}
