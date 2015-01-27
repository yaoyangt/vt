/**
 * Created by yaoyangt on 23/1/2015.
 */
class VeracodeMitigationTask extends VeracodeTask {
    static final String NAME = 'veracodeMitigation'

    VeracodeMitigationTask() {
        description = 'Perform mitigation based on a csv file in the build folder for the flaws found in the Veracode Scan'
        requiredArguments << 'buildId' << "csvFileName${VeracodeTask.OPTIONAL}"
    }

    @Override
    void run() {
        def mitigationCsvFilename = "build/${GenerateMitigationCsvTask.MITIGATION_CSV_FILENAME}"
        if (project.hasProperty('csvFileName')) {
            mitigationCsvFilename = "build/${project.csvFileName}"
        }

        Mitigation.fromCsv(new File(mitigationCsvFilename), ',').each() {
            loginMitigation().updateMitigationInfo(project.buildId, it.action, it.comment, it.flawIdArray.join(','))
        }
    }
}
