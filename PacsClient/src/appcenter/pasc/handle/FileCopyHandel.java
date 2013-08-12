package appcenter.pasc.handle;

import appcenter.logHelper.LogForError;
import appcenter.logHelper.LogForInfo;
import appcenter.pasc.domain.ShareFile;
import appcenter.pasc.domain.SharedQueue;
import appcenter.pasc.util.BaseConfig;
import appcenter.pasc.util.CommonUtil;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author mark.yang
 *         13-5-19
 *         pasc
 */
public class FileCopyHandel extends Thread {

	private static boolean firstRun = true;

	private static long systemTime=0;

	@Override
	public void run() {
		LogForInfo.logInfo("从文件夹" + BaseConfig.fileDir + "复制文件到" + BaseConfig.fileToDir + "线程已经启动!");
		while(true) {//根据file处理主线程的标记判断时候需要继续执行文件copy
			try{
				systemTime=System.currentTimeMillis();
				doOldFileHandel(); //处理copy文件中未加载到共享队列中的久文件
				doHandel();//处理源文件的copy目录中数据
			}catch(Exception e){
				LogForInfo.logInfo("复制文件出现错误!");
				LogForError.logError(e);
			}
			CommonUtil.pauseTime(Integer.parseInt(BaseConfig.runTime) * 60 * 1000, true, "终止文件复制线程运行!");//通过时间配置BaseConfig.runTime分钟执行一次copy线程。
			BaseConfig.updateConfig();
		}
	}

	private void doOldFileHandel(){
		if(SharedQueue.sendFiles.isEmpty()){//当队列为空时候再执行加载
			OldFileHandel.loadOldFileTOShare();
		}
	}

	private void doHandel() {
		File file = new File(BaseConfig.fileDir);
		if(file.exists()) {
			if(file.isDirectory()) {
				File[] children = this.getConfigFiles(file);//获得所有本地文件夹
				if(children != null) {
					for(File fi : children) {
						boolean runHandel=true;
						for(ShareFile shareFile : SharedQueue.sendFiles){
							if(shareFile.getPatientID().equals(fi.getName())){
								runHandel=false;
								break;
							}
						}
						if(runHandel) {
							Method method = null;
							try {
								method = BaseConfig.serverFileAnalyze.getMethod(BaseConfig.analyzeFileCopyMethod, File.class);
								method.invoke(null,fi);
							} catch(NoSuchMethodException e) {
								LogForError.logError(e);
							} catch(InvocationTargetException e) {
								LogForError.logError(e);
							} catch(IllegalAccessException e) {
								LogForError.logError(e);
							}
						}
					}
				}
			} else {
				LogForInfo.logInfo("文件BaseConfig.fileDir=" + BaseConfig.fileDir + "不是文件夹类型!");
			}
		} else {
			LogForInfo.logInfo("没有读取到BaseConfig.fileDir=" + BaseConfig.fileDir + "!");
		}
	}

	//获得需要解析的文件夹
	private File[] getConfigFiles(File file) {
		File[] tFile = null;
		FileFilter fileFilter = null;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = null;
		try {
			date = format.parse(BaseConfig.startTime);
		} catch(ParseException e) {
			LogForError.logError(e);
		}
		if(firstRun) {
			final Date finalDate = date;
			fileFilter = new FileFilter() {
				@Override
				public boolean accept(File file) {
					long last = file.lastModified();//获得文件最后一次修改时间=
					long config = finalDate.getTime();//获得配置比较时间
					return (last > config ? true : false) && file.getName().length()==8 && file.isDirectory() && last <= systemTime;
				}
			};
			tFile = file.listFiles(fileFilter);
			firstRun = false;
		} else {
			fileFilter = new FileFilter() {
				@Override
				public boolean accept(File file) {
					long last = file.lastModified();//获得文件最后一次修改时间=
					long config = systemTime - Integer.parseInt(BaseConfig.runTime) * 60 * 1000;//获得配置时间
					return (last > config ? true : false) && file.getName().length()==8 && file.isDirectory() && last <= systemTime;
				}
			};
			tFile = file.listFiles(fileFilter);
		}
		return tFile;
	}
}
