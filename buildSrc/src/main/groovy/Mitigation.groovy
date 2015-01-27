import au.com.bytecode.opencsv.CSVReader
import au.com.bytecode.opencsv.CSVWriter

/**
 * Created by yaoyangt on 27/1/2015.
 */
public class Mitigation {
    private String action
    private String comment
    private String[] flawIdArray

    public Mitigation(String action, String comment, String[] flawIdArray) {
        this.action = action
        this.comment = comment
        this.flawIdArray = flawIdArray
    }

    public static enum MitigationDetails {
        ACTION('action'),
        COMMENT('comment'),
        FLAW_ID_LIST('flawIdList')

        private final String name

        MitigationDetails(String name) {
            this.name = name
        }
    }

    public static List<Mitigation> fromCsv(File file, String delimiter) {
        CSVReader csvReader = new CSVReader(new FileReader(file))
        def rows = csvReader.readAll()
        def columnMapping = [:]

        rows.get(0).eachWithIndex() { String header, int i ->
            Mitigation.MitigationDetails.values().each() {
                if (header.equalsIgnoreCase(it.name)) {
                    columnMapping.put(it.name, i)
                }
            }
        }

        Mitigation.MitigationDetails.values().each() {
            if (columnMapping[it.name] == null) {
                println "Missing column: ${it.name}"
            }
        }

        def mitigationList = []
        rows.remove(0)
        rows.each() {
            mitigationList << new Mitigation(
                    it[columnMapping[Mitigation.MitigationDetails.ACTION.name]],
                    it[columnMapping[Mitigation.MitigationDetails.COMMENT.name]],
                    it[columnMapping[Mitigation.MitigationDetails.FLAW_ID_LIST.name]].split(delimiter)
            )
        }

        csvReader.close()

        return mitigationList
    }

    public static void toCsv(File file, List<String> issueIdList) {
        List<String[]> csvFileContent = new ArrayList<String[]>()
        int columnSize = MitigationDetails.values().size()
        String[] header = new String[columnSize]

        MitigationDetails.values().eachWithIndex() { MitigationDetails entry, int i ->
            header[i] = entry.name
        }
        csvFileContent.add(header)

        issueIdList.each() {
            String[] row = new String[columnSize]
            row[columnSize - 1] = it
            csvFileContent.add(row)
        }

        CSVWriter csvWriter = new CSVWriter(new FileWriter(file))
        csvWriter.writeAll(csvFileContent)
        csvWriter.flush()
        csvWriter.close()
    }
}
