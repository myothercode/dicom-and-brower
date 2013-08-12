package appcenter.pasc.handle;

import appcenter.dicom.domain.DicomQueue;
import appcenter.logHelper.LogForError;
import appcenter.logHelper.LogForInfo;
import appcenter.pasc.domain.ShareFile;
import appcenter.pasc.domain.SharedQueue;
import appcenter.pasc.util.BaseConfig;

import java.io.File;
import java.lang.reflect.Method;

/**
 * @author mark.yang
 *         13-5-19
 *         pasc
 */
public class FileSendHandel extends Thread {

	private File file = null;
	private String patientID = null;
	private ShareFile shareFile = null;

	public FileSendHandel(ShareFile shareFile) {
		this.shareFile = shareFile;
		this.file = shareFile.getFile();
		this.patientID = shareFile.getPatientID();
	}

	@Override
	public void run() {
		if(this.file.isFile()) {
			//处于共享队列中的文件不需要进行是否读写判断，队列中的文件都是未操作的,未上传成功文件将不会从共享队列中移除。
			try {
				Method method = BaseConfig.serverFileAnalyze
						.getMethod(BaseConfig.analyzeFileSendMethod, File.class, String.class, ShareFile.class);
				if((Boolean) method.invoke(null, this.file, this.patientID, this.shareFile)) {
					LogForInfo.logInfo(
							"检查申请单号:[" + this.shareFile.getPatientID() + "],报告文件:[" + this.shareFile.getFile().getName()
									+ "]上传成功!");
					if("true".equalsIgnoreCase(BaseConfig.runDicomService))
						DicomQueue.DQueue.put(this.patientID);

				} else {
					this.shareFile.getFile().getParentFile().renameTo(
							new File(this.shareFile.getFile().getParentFile().getAbsolutePath() + "_sendError"));
				}
				//不管是否上传成功都要移除共享队列，未上传成功的会被重新加载进入，避免阻塞文件提取。
				SharedQueue.sendFiles.remove(this.shareFile);
			} catch(Exception e) {
				LogForError.logError(e);
			}
		}
	}
}
