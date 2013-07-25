package appcenter.pasc.handle;

import appcenter.logHelper.LogForError;
import appcenter.logHelper.LogForInfo;
import appcenter.logHelper.LogForValidFileError;
import appcenter.pasc.domain.ShareFile;
import appcenter.pasc.domain.SharedQueue;
import appcenter.pasc.util.BaseConfig;
import appcenter.pasc.util.CommonUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mark.yang
 *         13-5-20
 *         pasc
 *         验证文件是否能够
 */
public class FileValidThread extends Thread  {

	private File file = null;

	public FileValidThread(File file) {
		this.file = file;
	}

	@Override
	public void run() {
		if(file != null) {
			boolean runHandel=true;
			for(ShareFile file : SharedQueue.sendFiles){
				if(file.getPatientID().equals(this.file.getName())){
					runHandel=false;
					return;
				}
			}
			if(runHandel) {
				doHandel();
			}
		}
	}

	private void doHandel() {

		List<File> fileList=new ArrayList<File>();
		CommonUtil.findFiles(this.file, "*.doc", fileList);

		if(fileList.size() >= 1) {

			String fileUrl = BaseConfig.fileToDir + "//" + this.file.getName();//创建相对应的copy文件夹

			File sendFile = CommonUtil.getTheLastCreateFile(fileList);
			try {
				if(sendFile.isFile()) {
					int i = 200;//当有文件被使用时候，循环200次，以保证文件能被复制
					while(--i > 0) {
						long firstRecordLength = sendFile.length();
						CommonUtil.pauseTime( 10 * 1000);//暂停3S 用于间隔下次取文件大小以便比较文件是否在操作
						long lastRecordLength = sendFile.length();
						if(firstRecordLength == lastRecordLength) {//判断文件间隔3S时候是否任然在被修改，如果任然在修改，则继续循环
							File newFile = new File(fileUrl);//当判断成功后再创建文件夹
							newFile.mkdir();

							File copyToFile= new File(fileUrl + "//" + sendFile.getName());
							CommonUtil.copyFile(sendFile, copyToFile);

							ShareFile shareFile=new ShareFile();
							shareFile.setFile(copyToFile);
							shareFile.setPatientID(newFile.getName());
							SharedQueue.sendFiles.add(shareFile);
							LogForInfo.logInfo("检查申请单号:["+this.file.getName()+"],报告文件:[" + sendFile.getName() + "]复制成功!");
							break;
						} else {
							LogForInfo.logInfo(
									"检查申请单号:[" + this.file.getName() + "],报告文件:[" + sendFile.getName() + "]正在写入，无法操作!");
						}
					}

					if(i==0) { //当copy文件200次任然时候后，记录失败信息
						LogForInfo.logInfo("检查申请单号:[" + this.file.getName() + "],报告文件:[" + sendFile.getName() + "]未复制!");
					}
				}
			} catch(IOException e) {
				LogForError.logError(e);
			}
		} else if(fileList.size() == 0) {
			LogForValidFileError.logInfo(file.getName());//当验证失败后，将失败的文件夹记录
			LogForInfo.logInfo("申请单号为[" + file.getName() + "]的申请单没有找到相对应的检查报告！");
		}
	}
}
