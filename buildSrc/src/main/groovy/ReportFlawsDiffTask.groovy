import au.com.bytecode.opencsv.CSVReader
import au.com.bytecode.opencsv.CSVWriter

class ReportFlawsDiffTask extends VeracodeTask {
	static final String NAME = 'reportFlawsDiff'

	ReportFlawsDiffTask() {
		description = 'Compares veracode report for two builds'
		requiredArguments << 'buildId1' << 'buildId2'
	}

	void run() {
		File build1CsvReport = fromXmlToCsv(
				getXmlReport(project.buildId1).getPath(), "build/scan-result-${project.buildId1}.csv")
		File build2CsvReport = fromXmlToCsv(
				getXmlReport(project.buildId2).getPath(), "build/scan-result-${project.buildId2}.csv")

		List<String> build1FlawIdList = []
		CSVReader build1CsvReader = new CSVReader(new FileReader(build1CsvReport))
		List<String[]> build1CsvContent = build1CsvReader.readAll()
		build1CsvReader.close()
		String[] header = build1CsvContent.get(0)
		int issueIdColumnIndex = header.findIndexOf() {
			return it.equalsIgnoreCase('issue id')
		}
		build1CsvContent.remove(0)
		build1CsvContent.each() {
			build1FlawIdList.add(it[issueIdColumnIndex])
		}

		List<String[]> reportDifferenceList = [header]
		CSVReader build2CsvReader = new CSVReader(new FileReader(build2CsvReport))
		List<String[]> build2CsvContent = build2CsvReader.readAll()
		build2CsvContent.remove(0)
		build2CsvContent.each() {
			if (!build1FlawIdList.contains(it[issueIdColumnIndex])) {
				reportDifferenceList << it
			}
		}
		build2CsvReader.close()

		CSVWriter reportDifferenceListCsvWriter = new CSVWriter(new FileWriter(new File("build/${project.buildId1}-${project.buildId2}-differences")))
		reportDifferenceListCsvWriter.writeAll(reportDifferenceList)
		reportDifferenceListCsvWriter.flush()
		reportDifferenceListCsvWriter.close()
	}

	private File getXmlReport(String buildId) {
		String xmlResponse = loginResults().detailedReport(buildId)
		String buildReportFilename = "build/scan-results-${buildId}.xml"
		writeXml(buildReportFilename, xmlResponse)

		return new File(buildReportFilename)
	}

	private File fromXmlToCsv(String xmlFilename, String csvFilename) {
		File csvFile = new File(csvFilename)
		csvFile.newWriter()
		csvFile << ["Issue Id",
					"Severity",
					"Exploit Level",
					"CWE Id",
					"CWE Name",
					"Module",
					"Source",
					"Source File Path",
					"Line",
					"Remediation Status",
					"Mitigation Status",
					"Mitigation Action",
					"Mitigation Description",
					"Mitigation Date"].join(",") + "\n"

		readXml(xmlFilename).severity.each() { severity ->
			severity.category.each() { category ->
				category.cwe.each() { cwe ->
					cwe.staticflaws.flaw.each() { flaw ->
						def row = [flaw.@issueid,
								   flaw.@severity,
								   flaw.@exploitLevel,
								   cwe.@cweid,
								   cwe.@cwename,
								   flaw.@module,
								   flaw.@sourcefile,
								   flaw.@sourcefilepath,
								   flaw.@line,
								   flaw.@remediation_status,
								   flaw.@mitigation_status_desc,
								   flaw.mitigations?.mitigation[0]?.@action,
								   flaw.mitigations?.mitigation[0]?.@description,
								   flaw.mitigations?.mitigation[0]?.@date]
								.collect { '"' + (it == null ? "" : it.replace('"', '""')) + '"' }
						def rowString = row.join(',')
						csvFile << rowString + "\n"
					}
				}
			}
		}

		return csvFile
	}
}