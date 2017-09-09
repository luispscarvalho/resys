package br.org.resys.adapter.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.jena.query.QuerySolution;

import br.org.resys.adapter.ISparqlProcessingAdapter;
import br.org.resys.en.Sparqls;
import br.org.resys.util.Util;

/**
 * Select and export data concerning the evolution of the incidence of
 * refactorings over time
 * <p>
 * The dataset is stored in a csv file which can later on be used to analyze the
 * recommendation of refactoring as smells are mined from software projects.
 * 
 * @author Luis Paulo
 */
public class RefactoringsByCommittersAdapter implements ISparqlProcessingAdapter {

	private List<String> lines;
	private DateFormat dateFormat;

	private BufferedWriter csvWriter;
	private String csvFileName;

	@Override
	public String getSparql() {
		return Sparqls.SPARQL_SMELLS_BY_COMMITTER.getStatement();
	}

	@Override
	public ISparqlProcessingAdapter init(Properties properties) throws Exception {
		lines = new ArrayList<String>();
		dateFormat = new SimpleDateFormat("yyyy-MM");

		csvFileName = "refactoringsbycommitter_" + Util.generateUid() + ".csv";

		File fout = new File(properties.getProperty("csv.output.path") + "/" + csvFileName);
		FileOutputStream fos = new FileOutputStream(fout);

		csvWriter = new BufferedWriter(new OutputStreamWriter(fos));
		csvWriter.write("committer,smell,datetime,locatio");

		return this;
	}

	@Override
	public ISparqlProcessingAdapter processing(QuerySolution row) throws Exception {
		String committer = row.getResource("committer").getLocalName();
		String smell = row.getResource("codesmell").getLocalName();
		String date = row.getLiteral("datetime").getString();
		String location = row.getLiteral("location").getString();

		lines.add(committer + "," + smell + "," + date + "," + location);

		return this;
	}

	@Override
	public ISparqlProcessingAdapter conclude() throws Exception {
		for (String line : lines) {
			csvWriter.newLine();
			csvWriter.write(line);
		}

		csvWriter.flush();
		csvWriter.close();

		return this;
	}

	public String getCSVFileName() {
		return this.csvFileName;
	}
}
