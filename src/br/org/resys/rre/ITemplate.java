package br.org.resys.rre;

/**
 * 
 * 
 * @author Luis Paulo
 */
public interface ITemplate {

	/**
	 * Every template has a "before" sample. It shows the source code before the
	 * application of the refactoring technique.
	 * 
	 * @param before
	 *            the "before" sample
	 * @return concrete instance of ITemplate
	 */
	public ITemplate setBefore(String before);

	public String getBefore();

	/**
	 * Every template has a "after" sample. It shows the source code after the
	 * application of the refactoring technique.
	 * 
	 * @param after
	 *            the "after" sample
	 * @return concrete instance of ITemplate
	 */
	public ITemplate setAfter(String after);

	public String getAfter();

	/**
	 * Pretty-print json document representing the template
	 * 
	 * @return a json doc
	 */
	public String toJson();

}
