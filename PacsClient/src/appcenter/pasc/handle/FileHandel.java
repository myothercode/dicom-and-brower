package appcenter.pasc.handle;

import appcenter.logHelper.LogForInfo;
import appcenter.pasc.domain.ShareFile;
import appcenter.pasc.domain.SharedQueue;
import appcenter.pasc.util.BaseConfig;
import appcenter.pasc.util.CommonUtil;
import appcenter.pasc.util.CreateServiceConUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mark.yang
 *         13-5-17
 *         pasc
 */
public class FileHandel extends Thread{

	public static boolean runFlag;
	private static File baseFile = null;
	public static FileCopyHandel fileCopyHandel = null;
	public static FileCheckUpdateHandel fileCheckUpdateHandel =null ;
	public static boolean firstLoad = true;

	@Override
	public void run() {
		runFlag = true;

		//1.第一次加载初始化上传接口
		if(firstLoad) {
			if(CreateServiceConUtil.createUtilServiceObj()){
				LogForInfo.logInfo("初始化接口池成功!");
				firstLoad = false;
			}else{
				LogForInfo.logInfo("初始化接口池失败!");
			}
		}

		//2.检查copy目录是否建立
		FileHandel.baseFile = new File(BaseConfig.fileToDir);
		if(!FileHandel.baseFile.exists()) {
			FileHandel.baseFile.mkdir();
		}

		//3.执行copy线程
		doFileCopyHandel();

		//4.执行检查修改的报告重新上传线程
		//doFileCheckUpdateHandel();

		//5.开始上传文件
		int i=0;
		while(runFlag) {
			LogForInfo.logInfo("执行第" + (i++) + "次上传任务");
			LogForInfo.logInfo("当前共享队列中有文件:"+SharedQueue.sendFiles.size()+"个");
			doFileSendHandel();
			CommonUtil.pauseTime(Integer.parseInt(BaseConfig.runTime) * 60 * 1000,true,"终止文件处理主线程运行!");//每次上传数据后暂停BaseConfig.runTime min
		}
		runFlag = true;
	}

	private void doFileCopyHandel() {
		fileCopyHandel = new FileCopyHandel();
		fileCopyHandel.start();
		CommonUtil.pauseTime(5000);//当首次执行 doFileCopyHandel时候，通过休眠5s时间来控制文件共享队列的初始化
	}

	private void doFileCheckUpdateHandel() {
		fileCheckUpdateHandel = new FileCheckUpdateHandel();
		fileCheckUpdateHandel.start();
	}

	private void doFileSendHandel() {
		if(!SharedQueue.sendFiles.isEmpty() && SharedQueue.sendFiles.size()>10){
			List<ShareFile> sendFiles=new ArrayList<ShareFile>();
			for(int i=0;i<10;i++){
				sendFiles.add(SharedQueue.sendFiles.get(i));
			}
			for(ShareFile fi : sendFiles) {
				new FileSendHandel(fi).start();
			}
		}else if(!SharedQueue.sendFiles.isEmpty() && SharedQueue.sendFiles.size()<=10){
			for(ShareFile fi : SharedQueue.sendFiles) {
				new FileSendHandel(fi).start();
			}
		}
	}
}
