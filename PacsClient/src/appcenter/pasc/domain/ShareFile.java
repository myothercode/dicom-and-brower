package appcenter.pasc.domain;

import java.io.File;

/**
 * @author mark.yang
 *         13-5-20
 *         pasc
 */
public class ShareFile {

	private File file=null;

	private String patientID=null;

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getPatientID() {
		return patientID;
	}

	public void setPatientID(String patientID) {
		this.patientID = patientID;
	}
}
