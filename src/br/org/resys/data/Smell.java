package br.org.resys.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Enumeration of all smells refactorings are applicable to.
 * 
 * @author Luis Paulo
 */
public enum Smell {

	LONG_METHOD("Long Method", "LongMethod"), BRAIN_METHOD("Brain Method", "BrainMethod"),

	GOD_CLASS("God Class", "GodClass"), DATA_CLASS("Data Class", "DataClass"), BRAIN_CLASS("Brain Class", "BrainClass"),

	FEATURE_ENVY("Feature Envy", "FeatureEnvy"), REFUSE_PARENT_BEQUEST("Refuse Parent Bequest", "RefuseParentBequest"),

	UNKNOWN("Unknown", "Unknown");

	private String label;
	private String ontoType;

	Smell(String label, String ontoType) {
		this.label = label;
		this.ontoType = ontoType;
	}

	public String getLabel() {
		return label;
	}

	public String getOntoType() {
		return ontoType;
	}

	public static List<String> getAllLabels() {
		List<String> labels = new ArrayList<String>();

		labels.add(LONG_METHOD.getLabel());
		labels.add(BRAIN_METHOD.getLabel());
		labels.add(GOD_CLASS.getLabel());
		labels.add(DATA_CLASS.getLabel());
		labels.add(BRAIN_CLASS.getLabel());
		labels.add(FEATURE_ENVY.getLabel());
		labels.add(REFUSE_PARENT_BEQUEST.getLabel());

		return labels;
	}

	public static Smell fromOntoType(String ontoType) {
		Smell smell = Smell.UNKNOWN;

		if (ontoType.equals(LONG_METHOD.getOntoType())) {
			smell = Smell.LONG_METHOD;
		} else if (ontoType.equals(BRAIN_METHOD.getOntoType())) {
			smell = Smell.BRAIN_METHOD;
		} else if (ontoType.equals(GOD_CLASS.getOntoType())) {
			smell = Smell.GOD_CLASS;
		} else if (ontoType.equals(DATA_CLASS.getOntoType())) {
			smell = Smell.DATA_CLASS;
		} else if (ontoType.equals(BRAIN_CLASS.getOntoType())) {
			smell = Smell.BRAIN_CLASS;
		} else if (ontoType.equals(FEATURE_ENVY.getOntoType())) {
			smell = Smell.FEATURE_ENVY;
		} else if (ontoType.equals(REFUSE_PARENT_BEQUEST.getOntoType())) {
			smell = Smell.REFUSE_PARENT_BEQUEST;
		}

		return smell;
	}

}
