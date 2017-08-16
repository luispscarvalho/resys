package br.org.resys.export.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.jena.query.QuerySolution;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;

import br.org.resys.en.Sparqls;
import br.org.resys.export.ISparqlProcessingAdapter;
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
public class IncidenceOfRefactoringsThroughTimeExporter implements ISparqlProcessingAdapter {

	private Table<Date, String, Integer> incidenceTable = HashBasedTable.create();
	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM");

	private BufferedWriter csvWriter;
	private String csvFileName;

	@Override
	public String getSparql() {
		return Sparqls.SPARQL_INCIDENCE_REFACTORINGS_THROUGH_TIME.getStatement();
	}

	@Override
	public ISparqlProcessingAdapter init(String outputPath) throws Exception {
		csvFileName = "incidence_" + Util.generateUid() + ".csv";

		File fout = new File(outputPath + "/" + csvFileName);
		FileOutputStream fos = new FileOutputStream(fout);

		csvWriter = new BufferedWriter(new OutputStreamWriter(fos));
		csvWriter.write("date,refactoring,qt");

		return this;
	}

	@Override
	public ISparqlProcessingAdapter processing(QuerySolution result) throws Exception {
		Date datetime = dateFormat.parse(result.getLiteral("datetime").getString());
		String refactoring = result.getResource("refactoring").getLocalName();

		if (incidenceTable.contains(datetime, refactoring)) {
			Integer qt = incidenceTable.get(datetime, refactoring) + 1;
			incidenceTable.put(datetime, refactoring, qt);
		} else {
			incidenceTable.put(datetime, refactoring, 1);
		}

		return this;
	}

	@Override
	public ISparqlProcessingAdapter conclude() throws Exception {
		for (Cell<Date, String, Integer> cell : incidenceTable.cellSet()) {
			csvWriter.newLine();
			csvWriter.write(dateFormat.format(cell.getRowKey()) + "," + cell.getColumnKey() + "," + cell.getValue());
		}

		csvWriter.flush();
		csvWriter.close();

		return this;
	}

	public String getCSVFileName() {
		return this.csvFileName;
	}
}
