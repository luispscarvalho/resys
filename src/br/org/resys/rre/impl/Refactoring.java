package br.org.resys.rre.impl;

import org.semanticweb.owlapi.model.OWLNamedIndividual;

import br.org.resys.rre.IRefactoring;
import br.org.resys.rre.ITemplate;

/**
 * @see IRefactoring
 * 
 * @author Luis Paulo
 *
 */
public class Refactoring implements IRefactoring {

	private String acronym;
	private String name;
	private String description;
	private ITemplate template;
	private OWLNamedIndividual owlRefactoring;

	@Override
	public IRefactoring setAcronym(String acronym) {
		this.acronym = acronym;

		return this;
	}

	@Override
	public String getAcronym() {
		return acronym;
	}

	@Override
	public IRefactoring setName(String name) {
		this.name = name;

		return this;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public IRefactoring setDescription(String description) {
		this.description = description;

		return this;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public IRefactoring setTemplate(ITemplate template) {
		this.template = template;

		return this;
	}

	@Override
	public ITemplate getTemplate() {
		return template;
	}

	@Override
	public OWLNamedIndividual getOwlRefactoring() {
		return owlRefactoring;
	}

	@Override
	public void setOwlRefactoring(OWLNamedIndividual owlRefactoring) {
		this.owlRefactoring = owlRefactoring;
	}
	
	@Override
	public String toString() {
		return name + "(" + acronym + ")";
	}
	
	@Override
	public String toJson() {
		return "{name: '" + name + "', description: '" + description + "', template: " + template.toJson() + "}";
	}

}
