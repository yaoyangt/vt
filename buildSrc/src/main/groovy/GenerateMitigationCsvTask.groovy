import au.com.bytecode.opencsv.CSVReader

/**
 * Created by yaoyangt on 26/1/2015.
 */
class GenerateMitigationCsvTask extends VeracodeTask {
    static final String NAME = 'generateMitigationCsv'
    static final String MITIGATION_CSV_FILENAME = 'sample-mitigation.csv'

    GenerateMitigationCsvTask() {
        description = 'Generate a sample mitigation csv file from the scan results csv file. All flaws more than or equal to the severity designated will be included.'
        requiredArguments << 'severity' << "csvFileName${VeracodeTask.OPTIONAL}"
    }

    @Override
    void run() {
        String scanResultsCsvFilename = "build/${VeracodeScanResultsInCsvTask.SCAN_RESULTS_CSV_FILENAME}"
        if (project.hasProperty('csvFileName')) {
            scanResultsCsvFilename = "build/${project.csvFileName}"
        }

        Iterator<String[]> csvFileContents = new CSVReader(new FileReader(new File(scanResultsCsvFilename))).readAll().iterator()
        String[] headers = csvFileContents.next()
        int severityColumnIndex = headers.findIndexOf() {
            return it.equalsIgnoreCase('severity')
        }
        int issueIdColumnIndex = headers.findIndexOf() {
            return it.equalsIgnoreCase('issue id')
        }
        int mitigationStatusColumnIndex = headers.findIndexOf() {
            return it.equalsIgnoreCase('mitigation status')
        }
        List<String> issueIdList = new ArrayList<String>()

        while(csvFileContents.hasNext()) {
            String[] row = csvFileContents.next()
            if (row[severityColumnIndex] >= project.severity && row[mitigationStatusColumnIndex].equals('Not Mitigated')) {
                issueIdList.add(row[issueIdColumnIndex])
            }
        }

        Mitigation.toCsv(new File("build/${MITIGATION_CSV_FILENAME}"), issueIdList)
    }
}
