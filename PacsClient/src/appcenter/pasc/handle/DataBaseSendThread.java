package appcenter.pasc.handle;

import appcenter.logHelper.LogForError;
import appcenter.logHelper.LogForInfo;
import appcenter.pasc.domain.DBResource;
import appcenter.pasc.domain.ShareDBResource;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @author mark.yang
 *         13-5-18
 *         pasc
 */
public class DataBaseSendThread extends Thread {
	public static final Log logger = LogFactory.getLog(DataBaseSendThread.class);

	private List<DBResource> dbResources = null;

	public DataBaseSendThread(List<DBResource> dbResources) {
		this.dbResources = dbResources;
	}

	@Override

	public void run() {
		if(dbResources != null && !dbResources.isEmpty()) {
			for(int i = 0;i < dbResources.size();i++) {
				DBResource resource = dbResources.get(i);
				if(!doSend(resource.getReportStr(), resource.getPatientID())) {
					ShareDBResource.errorDbResources.add(resource);
				}else{
					LogForInfo.logInfo("检查申请单号:[" + resource.getPatientID() + "]X光报告文件上传成功!");
				}
				if(!ShareDBResource.dbResources.isEmpty()){
					for(DBResource dbResources : ShareDBResource.dbResources) {
						if(resource.getPatientID().equals(dbResources.getPatientID())) {
							ShareDBResource.dbResources.remove(dbResources);
						}
					}
				}
			}
		}
	}

	private boolean doSend(final String reportStr, final String patientID) {
		UploadPacsFileRequestTO uploadPacsFileRequestTO = new UploadPacsFileRequestTO();
		uploadPacsFileRequestTO.setHeader(new ServiceRequestHeader());
		uploadPacsFileRequestTO.setType(BaseConfig.pacsType);
		uploadPacsFileRequestTO.setOrgCode(BaseConfig.code);
		uploadPacsFileRequestTO.setFileName(patientID + "." + BaseConfig.fileType);
		uploadPacsFileRequestTO.setDataHandler(new DataHandler(new DataSource() {
			@Override
			public InputStream getInputStream() throws IOException {
				ByteArrayOutputStream arrayInputStream=new ByteArrayOutputStream();
				try {
					Method method = BaseConfig.serverAnalyze.getMethod(BaseConfig.analyzeMethod, String.class,String.class);
					arrayInputStream=(ByteArrayOutputStream)method.invoke(null, reportStr,patientID);//调用解析的配置方法
				} catch(NoSuchMethodException e) {
					LogForError.logError(e);
				} catch(InvocationTargetException e) {
					LogForError.logError(e);
				} catch(IllegalAccessException e) {
					LogForError.logError(e);
				}
				return new ByteArrayInputStream(arrayInputStream.toByteArray());
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
			LogForInfo.logInfo("cause by:检查申请单号:[" + patientID + "]X光报告文件上传失败!" + errorMsg);
			return false;
		}

		InsertExamReportRequestTO insertExamReportRequestTO = new InsertExamReportRequestTO();
		insertExamReportRequestTO.setHeader(new ServiceRequestHeader());

		ExamReportVO examReportVO = new ExamReportVO();
		examReportVO.setExamNO(patientID);
		examReportVO.setReportFilePath(uploadPacsFileResponseTO.getFilePath());
		examReportVO.setPicPath(uploadPacsFileResponseTO.getFilePath());
		examReportVO.setReportFileType(BaseConfig.fileType);
		examReportVO.setOrgCode(BaseConfig.code);
		examReportVO.setExamReportNO(patientID);
		examReportVO.setCheckDate(new Date());
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
			LogForInfo.logInfo("cause by:检查申请单号:[" + patientID + "]X光报告文件上传信息失败!" + errorMsg);
			return false;
		}
		return true;
	}
}
