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
 * 
 * @author Luis Paulo
 */
public class EffortCorrelationConnector {
	private static EffortCorrelationConnector instance;

	/**
	 * @return singleton instance of this connector
	 */
	public static EffortCorrelationConnector getInstance() {
		if (instance == null) {
			instance = new EffortCorrelationConnector();
		}

		return instance;
	}

	// pool of correlated commits by date
	private Table<Date, String, Double> correlationsByDate;
	private String inputPath;

	public EffortCorrelationConnector init(Properties properties) {
		correlationsByDate = HashBasedTable.create();
		inputPath = properties.getProperty("csv.input.path");

		return this;
	}

	public EffortCorrelationConnector loadCorrelations(String file) throws IOException, ParseException {
		correlationsByDate.clear();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		BufferedReader reader = new BufferedReader(new FileReader(inputPath + "/" + file));

		String line;
		int countLines = 0;
		while ((line = reader.readLine()) != null) {
			// dismiss header
			if (++countLines > 1) {
				String[] data = line.split(",");
				if (data.length == 3) {
					Date date = dateFormat.parse(data[0]);
					Double correlation = Double.valueOf(data[1]);

					String[] commits = data[2].split(Pattern.quote("|"));
					for (String commit : commits) {
						correlationsByDate.put(date, commit, correlation);
					}
				}
			}
		}

		reader.close();

		return this;
	}

	public Table<Date, String, Double> getCorrelationsByDate() {
		return correlationsByDate;
	}

}
