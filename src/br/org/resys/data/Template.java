package br.org.resys.data;

import br.org.resys.rre.ITemplate;

public class Template implements ITemplate {

	private String before;
	private String after;

	@Override
	public ITemplate setBefore(String before) {
		this.before = before;

		return this;
	}

	@Override
	public String getBefore() {
		return before;
	}

	@Override
	public ITemplate setAfter(String after) {
		this.after = after;

		return this;
	}

	@Override
	public String getAfter() {
		return after;
	}

	@Override
	public String toJson() {
		return "{before: '" + before + "', after: '" + after + "'}";
	}

}
