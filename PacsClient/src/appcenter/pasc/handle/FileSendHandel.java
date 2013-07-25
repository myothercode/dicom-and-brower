package appcenter.pasc.handle;

import appcenter.logHelper.LogForError;
import appcenter.logHelper.LogForInfo;
import appcenter.pasc.domain.ShareFile;
import appcenter.pasc.domain.SharedQueue;
import appcenter.pasc.util.BaseConfig;
import appcenter.pasc.util.CreateServiceConUtil;
import com.appcenter.bhml.basic.dto.ServiceRequestHeader;
import com.appcenter.bhml.core.pacs.domain.ExamReportVO;
import com.appcenter.bhml.core.pacs.dto.InsertExamReportRequestTO;
import com.appcenter.bhml.core.pacs.dto.InsertExamReportResponseTO;
import com.appcenter.bhml.core.pacs.dto.UploadPacsFileRequestTO;
import com.appcenter.bhml.core.pacs.dto.UploadPacsFileResponseTO;
import com.appcenter.bhml.webservice.ExamInfoWebService;
import com.appcenter.bhml.webservice.UploadFileWebService;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import java.io.*;
import java.util.Date;
import java.util.Iterator;

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
				if(doSend(this.file)) {
					LogForInfo.logInfo(
							"检查申请单号:[" + this.shareFile.getPatientID() + "],报告文件:[" + this.shareFile.getFile().getName()
									+ "]上传成功!");

				}else{
					this.shareFile.getFile().getParentFile().renameTo(new File(this.shareFile.getFile().getParentFile().getAbsolutePath()+"_sendError"));
				}
				//不管是否上传成功都要移除共享队列，未上传成功的会被重新加载进入，避免阻塞文件提取。
				SharedQueue.sendFiles.remove(this.shareFile);
			} catch(Exception e) {
				LogForError.logError(e);
			}
		}
	}

	private boolean doSend(final File file) throws Exception {
		UploadPacsFileRequestTO uploadPacsFileRequestTO = new UploadPacsFileRequestTO();
		uploadPacsFileRequestTO.setHeader(new ServiceRequestHeader());
		uploadPacsFileRequestTO.setType(BaseConfig.pacsType);
		uploadPacsFileRequestTO.setOrgCode(BaseConfig.code);
		uploadPacsFileRequestTO.setFileName(this.patientID + "." + BaseConfig.fileType);
		uploadPacsFileRequestTO.setDataHandler(new DataHandler(new DataSource() {
			@Override
			public InputStream getInputStream() throws IOException {
				return new FileInputStream(file);
			}
			@Override
			public OutputStream getOutputStream() throws IOException {
				return null;
			}
			@Override
			public String getContentType() {
				return null;
			}
			@Override
			public String getName() {
				return null;
			}
		}));
		UploadFileWebService uploadFileWebService = (UploadFileWebService) CreateServiceConUtil
				.getJaxWsProxyFactoryBean(BaseConfig.upLoadFileUrl);
		UploadPacsFileResponseTO uploadPacsFileResponseTO = uploadFileWebService
				.uploadPacsFile(uploadPacsFileRequestTO);
		if(uploadPacsFileResponseTO.getErrorMessage() != null) {
			Iterator iterator = uploadPacsFileResponseTO.getErrorMessage().iterator();
			StringBuffer errorMsg = new StringBuffer("接口错误返回:");
			while(iterator.hasNext()) {
				errorMsg.append("[" + iterator.next().toString() + "]");
			}
			LogForInfo.logInfo(
					"cause by:检查申请单号:[" + this.shareFile.getPatientID() + "],报告文件:[" + this.shareFile.getFile()
							.getName() + "]报告文件上传失败!" + errorMsg);
			return false;
		}

		InsertExamReportRequestTO insertExamReportRequestTO = new InsertExamReportRequestTO();
		insertExamReportRequestTO.setHeader(new ServiceRequestHeader());

		ExamReportVO examReportVO = new ExamReportVO();
		examReportVO.setExamNO(this.patientID);
		examReportVO.setReportFilePath(uploadPacsFileResponseTO.getFilePath());
		examReportVO.setPicPath(uploadPacsFileResponseTO.getFilePath());
		examReportVO.setReportFileType(BaseConfig.fileType);
		examReportVO.setCheckDate(new Date());
		examReportVO.setOrgCode(BaseConfig.code);
		examReportVO.setExamReportNO(this.patientID);
		insertExamReportRequestTO.setExamReportVO(examReportVO);

		ExamInfoWebService examInfoWebService = (ExamInfoWebService) CreateServiceConUtil
				.getJaxWsProxyFactoryBean(BaseConfig.upLoadInfoUrl);
		InsertExamReportResponseTO insertExamReportResponseTO = examInfoWebService
				.insertExamReport(insertExamReportRequestTO);
		if(insertExamReportResponseTO.getErrorMessage() != null) {
			Iterator iterator = insertExamReportResponseTO.getErrorMessage().iterator();
			StringBuffer errorMsg = new StringBuffer("接口错误返回:");
			while(iterator.hasNext()) {
				errorMsg.append("[" + iterator.next().toString() + "]");
			}
			LogForInfo.logInfo(
					"cause by:检查申请单号:[" + this.shareFile.getPatientID() + "],报告文件:[" + this.shareFile.getFile()
							.getName() + "]报告信息上传失败!" + errorMsg);
			return false;
		}
		file.delete();//上传文件成功后删除
		new File(BaseConfig.fileToDir + "/" + this.patientID).delete();
		return true;
	}
}
