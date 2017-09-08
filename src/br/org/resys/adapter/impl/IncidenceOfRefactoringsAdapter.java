package br.org.resys.adapter.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.jena.query.QuerySolution;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;

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
public class IncidenceOfRefactoringsAdapter implements ISparqlProcessingAdapter {

	private Table<Date, String, Integer> incidenceTable;
	private DateFormat dateFormat;

	private BufferedWriter csvWriter;
	private String csvFileName;

	@Override
	public String getSparql() {
		return Sparqls.SPARQL_INCIDENCE_OF_REFACTORINGS.getStatement();
	}

	@Override
	public ISparqlProcessingAdapter init(Properties properties) throws Exception {
		incidenceTable = HashBasedTable.create();
		dateFormat = new SimpleDateFormat("yyyy-MM");

		csvFileName = "incidence_" + Util.generateUid() + ".csv";

		File fout = new File(properties.getProperty("csv.output.path") + "/" + csvFileName);
		FileOutputStream fos = new FileOutputStream(fout);

		csvWriter = new BufferedWriter(new OutputStreamWriter(fos));
		csvWriter.write("date,refactoring,qt");
		
		return this;
	}

	@Override
	public ISparqlProcessingAdapter processing(QuerySolution row) throws Exception {
		Date datetime = dateFormat.parse(row.getLiteral("datetime").getString());
		String refactoring = row.getResource("refactoring").getLocalName();

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
