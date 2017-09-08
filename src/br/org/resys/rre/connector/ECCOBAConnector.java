package br.org.resys.rre.connector;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Pattern;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * Connector that manipulates datasets produced by ECCOBA
 * <p>
 * ECCOBA is a short for Effort Codesmells COrrelations Breakdown Algorithm.
 * ECCOBA was developed to indicate periods of time, during the development, in
 * which the incidence of code smells (IoC) correlates significantly with the
 * effort applied in the making of a software system.
 * 
 * @author Luis Paulo
 */
public class ECCOBAConnector {
	private static ECCOBAConnector instance;

	/**
	 * @return singleton instance of this connector
	 */
	public static ECCOBAConnector getInstance() {
		if (instance == null) {
			instance = new ECCOBAConnector();
		}

		return instance;
	}

	// pool of correlated commits by date
	private Table<Date, String, Double> correlationsByDateAndCommit;
	private String inputPath;

	/**
	 * Initialization routine. It must be executed first, prior to manipulating
	 * ECCOBA's datasets.
	 * 
	 * @param properties
	 *            props to configure ECCOBA's path+dataset.
	 * @return
	 */
	public ECCOBAConnector init(Properties properties) {
		correlationsByDateAndCommit = HashBasedTable.create();
		inputPath = properties.getProperty("csv.input.path");

		return this;
	}

	/**
	 * Load the correlation from a ECCOBA's dataset file.
	 * <p>
	 * The dataset must be generated first and placed in the csv input path.
	 * Accepted format must contain the following columns:
	 * <ul>
	 * <li>Date -> date of the correlation</li>
	 * <li>Corr -> value of the correlation</li>
	 * <li>Commits -> commits that correlated with effort</li>
	 * </ul>
	 * <p>
	 * It produces a table representing the aforementioned information. Table's
	 * rows are populated with the date of the correlations. Columns contain
	 * correlated commits' ids. Cells enclose the value of the correlations.
	 * <p>
	 * The resulting table can be accessed by {@link #getCorrelationsByDate()}
	 * 
	 * 
	 * @param dataset
	 *            a dataset containing the correlations.
	 * @return instance of #ECCOBAConnector()
	 * @throws IOException
	 *             if it fails to read the dataset.
	 * @throws ParseException
	 *             if it fails to parse data from the dataset.
	 */
	public ECCOBAConnector loadCorrelations(String dataset) throws IOException, ParseException {
		correlationsByDateAndCommit.clear();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		// create bufferedreader to help reading the dataset
		BufferedReader reader = new BufferedReader(new FileReader(inputPath + "/" + dataset));

		String line;
		int countLines = 0;
		while ((line = reader.readLine()) != null) {
			// dismiss header line
			if (++countLines > 1) {
				String[] data = line.split(",");
				if (data.length == 3) { // avoiding malformed lines
					Date date = dateFormat.parse(data[0]);
					Double correlation = Double.valueOf(data[1]);
					// the commits are listed in the third row (the pipe must be
					// escaped prior to the splitting)
					String[] commits = data[2].split(Pattern.quote("|"));
					for (String commit : commits) {
						correlationsByDateAndCommit.put(date, commit, correlation);
					}
				}
			}
		}

		reader.close();

		return this;
	}

	/**
	 * {@link #loadCorrelations(String)} must be executed prior to using this
	 * method.
	 * 
	 * @return a table representing a mapping of correlations for pairs of date
	 *         and commit
	 */
	public Table<Date, String, Double> getCorrelationsByDateAndCommit() {
		return correlationsByDateAndCommit;
	}

}
