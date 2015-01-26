import au.com.bytecode.opencsv.CSVReader

/**
 * Created by yaoyangt on 23/1/2015.
 */
class VeracodeMitigationTask extends VeracodeTask {
    static final String NAME = 'veracodeMitigation'

    VeracodeMitigationTask() {
        description = 'Perform mitigation based on a csv file in the build folder for the flaws found in the Veracode Scan'
        requiredArguments << 'buildId' << "mitigationCsv${VeracodeTask.OPTIONAL}"
    }

    @Override
    void run() {
        def mitigationCsvFile = 'build/sample-mitigation.csv'
        if (project.hasProperty('mitigationCsv')) {
            mitigationCsvFile = "build/${project.mitigationCsv}"
        }

        parseCsv(new File(mitigationCsvFile), ',').each() {
            loginMitigation().updateMitigationInfo(project.buildId, it.action, it.comment, it.flawIdArray.join(','))
        }
    }

    private List<Mitigation> parseCsv(File file, String delimiter) {
        def rows = new CSVReader(new FileReader(file)).readAll()

        rows.get(0).eachWithIndex() { String header, int i ->
            MitigationDetails.values().each() {
                if (header.equalsIgnoreCase(it.name)) {
                    it.column = i
                }
            }
        }

        MitigationDetails.values().each() {
            if (it.column < 0) {
                println "Missing column: ${it.name}"
            }
        }

        def mitigationList = []
        rows.remove(0)
        rows.each() {
            mitigationList << new Mitigation(
                    it[MitigationDetails.ACTION.column],
                    it[MitigationDetails.COMMENT.column],
                    it[MitigationDetails.FLAW_ID_LIST.column].split(delimiter)
            )
        }

        return mitigationList
    }

    private class Mitigation {
        private String action
        private String comment
        private String[] flawIdArray

        public Mitigation(String action, String comment, String[] flawIdArray) {
            this.action = action
            this.comment = comment
            this.flawIdArray = flawIdArray
        }
    }

    private enum MitigationDetails {
        ACTION('action'),
        COMMENT('comment'),
        FLAW_ID_LIST('flawIdList')

        private final String name
        private int column = -1

        MitigationDetails(String name) {
            this.name = name
        }
    }
}
