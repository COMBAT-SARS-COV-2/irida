package ca.corefacility.bioinformatics.irida.ria.web.services;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.corefacility.bioinformatics.irida.exceptions.MetadataImportFileTypeNotSupportedError;
import ca.corefacility.bioinformatics.irida.model.project.Project;
import ca.corefacility.bioinformatics.irida.ria.utilities.SampleMetadataStorage;
import ca.corefacility.bioinformatics.irida.service.ProjectService;
import ca.corefacility.bioinformatics.irida.service.sample.SampleService;

import com.google.common.base.Strings;

/**
 * UI service to handle parsing metadata files so they can be saved to the session.
 */
@Component
public class UIMetadataImportService {

	private final ProjectService projectService;
	private final SampleService sampleService;

	@Autowired
	public UIMetadataImportService(ProjectService projectService, SampleService sampleService) {
		this.projectService = projectService;
		this.sampleService = sampleService;
	}

	/**
	 * Parse metadata from an csv file.
	 *
	 * @param inputStream The inputStream of the csv file.
	 * @return {@link SampleMetadataStorage} contains the metadata from file.
	 * @throws IOException thrown if the extension does not exist.
	 */
	public SampleMetadataStorage parseCSV(Long projectId, InputStream inputStream) throws IOException {
		SampleMetadataStorage storage = new SampleMetadataStorage();

		CSVParser parser = CSVParser.parse(inputStream, StandardCharsets.UTF_8,
				CSVFormat.RFC4180.withFirstRecordAsHeader()
						.withTrim()
						.withIgnoreEmptyLines());
		List<Map<String, String>> rows = new ArrayList<>();

		// save headers
		Map<String, Integer> headers_set = parser.getHeaderMap();
		List<String> headers_list = new ArrayList<>(headers_set.keySet());
		storage.saveHeaders(headers_list);

		// save data
		for (CSVRecord row : parser) {
			Map<String, String> rowMap = new HashMap<>();
			for (String key : row.toMap()
					.keySet()) {
				String value = row.toMap()
						.get(key);
				rowMap.put(key, value);
			}
			rows.add(rowMap);
		}

		storage.saveRows(rows);
		storage.setSampleNameColumn(findColumnName(projectId, rows.get(0)));
		parser.close();

		return storage;
	}

	/**
	 * Parse metadata from an excel file.
	 *
	 * @param inputStream The inputStream of the excel file.
	 * @param extension   The extension of the excel file.
	 * @return {@link SampleMetadataStorage} contains the metadata from file.
	 * @throws IOException thrown if the extension does not exist.
	 */
	public SampleMetadataStorage parseExcel(Long projectId, InputStream inputStream, String extension)
			throws IOException {
		SampleMetadataStorage storage = new SampleMetadataStorage();
		Workbook workbook = null;

		// Check the type of workbook
		switch (extension) {
		case "xlsx":
			workbook = new XSSFWorkbook(inputStream);
			break;
		case "xls":
			workbook = new HSSFWorkbook(inputStream);
			break;
		default:
			// Should never reach here as the uploader limits to .csv, .xlsx and .xlx files.
			throw new MetadataImportFileTypeNotSupportedError(extension);
		}

		// Only look at the first sheet in the workbook as this should be the file we want.
		Sheet sheet = workbook.getSheetAt(0);
		Iterator<Row> rowIterator = sheet.iterator();

		List<String> headers = getWorkbookHeaders(rowIterator.next());
		storage.saveHeaders(headers);

		// Get the metadata out of the table.
		List<Map<String, String>> rows = new ArrayList<>();
		while (rowIterator.hasNext()) {
			Map<String, String> rowMap = new HashMap<>();
			Row row = rowIterator.next();
			Iterator<Cell> cellIterator = row.cellIterator();
			while (cellIterator.hasNext()) {
				Cell cell = cellIterator.next();

				int columnIndex = cell.getColumnIndex();
				if (columnIndex < headers.size()) {
					String header = headers.get(columnIndex);

					if (!Strings.isNullOrEmpty(header)) {
						// Need to ignore empty headers.
						if (cell.getCellTypeEnum()
								.equals(CellType.NUMERIC)) {
								/*
								This is a special handler for number cells.  It was requested that numbers
								keep their formatting from their excel files.  E.g. 2.222222 with formatting
								for 2 decimal places will be saved as 2.22.
								 */
							DataFormatter formatter = new DataFormatter();
							String value = formatter.formatCellValue(cell);
							rowMap.put(header, value);
						} else {
							cell.setCellType(CellType.STRING);
							rowMap.put(header, cell.getStringCellValue());
						}
					}
				}
			}
			rows.add(rowMap);
		}
		storage.saveRows(rows);
		storage.setSampleNameColumn(findColumnName(projectId, rows.get(0)));

		return storage;
	}

	/**
	 * Extract the headers from an excel file.
	 *
	 * @param row {@link Row} First row from the excel file.
	 * @return {@link List} of {@link String} header values.
	 */
	private List<String> getWorkbookHeaders(Row row) {
		// We want to return a list of the table headers back to the UI.
		List<String> headers = new ArrayList<>();

		// Get the column headers
		Iterator<Cell> headerIterator = row.cellIterator();
		while (headerIterator.hasNext()) {
			Cell headerCell = headerIterator.next();
			CellType cellType = headerCell.getCellTypeEnum();

			String headerValue;
			if (cellType.equals(CellType.STRING)) {
				headerValue = headerCell.getStringCellValue()
						.trim();
			} else {
				headerValue = String.valueOf(headerCell.getNumericCellValue())
						.trim();
			}

			// Leave empty headers for now, we will remove those columns later.
			headers.add(headerValue);
		}
		return headers;
	}

	/**
	 * Extract the headers from an excel file.
	 *
	 * @param row {@link Row} First row from the excel file.
	 * @return {@link String} column name.
	 */
	private String findColumnName(Long projectId, Map<String, String> row) {
		String columnName = null;
		Project project = projectService.read(projectId);
		Iterator<Map.Entry<String, String>> iterator = row.entrySet()
				.iterator();

		while (iterator.hasNext() && columnName != null) {
			Map.Entry<String, String> entry = iterator.next();
			String key = entry.getKey();
			String value = entry.getValue();
			System.out.println("key = " + key + ", value = " + value);
			if (sampleService.getSampleBySampleName(project, value) != null) {
				columnName = key;
			}
		}

		return columnName;
	}
}