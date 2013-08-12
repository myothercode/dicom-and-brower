package appcenter.pasc.util;

import appcenter.logHelper.LogForError;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author mark.yang
 *         13-5-17
 *         pasc
 */
public class BaseConfig {

	private static Properties server;

	public static String code;//init.server.code 机构编码

	public static String upLoadFileUrl;//init.server.upLoadFileUrl  上传文件数据WebService Url

	public static String upLoadInfoUrl;//init.server.upLoadInfoUrl  上传信息数据WebService Url

	public static String runMonitor;//init.server.runMonitor 是否启动监控线程

	public static String runTime;//init.server.runTime  拷贝平率（单位 min）

	public static String fileType;//init.server.fileType  上传文件类型

	public static String monitorName;//init.server.monitorName  监控程序名称

	public static String monitorTime;//init.server.monitorTime  监控程序名称

	public static String startTime;//init.server.startTime  拉取文件起始时间（格式 yyyy-mm-dd）

	//init.server.serviceClass  用于初始化保存接口访问对象，key 为接口URL，value 为 Class
	public static Map<String, Class> serviceClass = new HashMap<String, Class>();

	public static String pacsType;//init.server.pacsType  检查类型

	public static String type;//init.server.type  拉取数据方式 （dataBase,file）

	private static Properties file;

	public static String addOldFile;// init.file.addOldFile 是否读取旧数据

	public static String checkUpdateTime;//init.file.checkUpdateTime 设置对有修改的报告进行重新上传的时间

	public static String fileDir;//init.file.fileDir 原始文件目录例如（e://source）

	public static String fileToDir;//init.file.fileToDir 拷贝文件目录例如（e://back）

	private static Properties database;

	public static String dbUrl;//init.database.dbUrl 数据库连接IP及端口

	public static String dbType;//init.database.dbType 数据库分类只能有如下几种方式（oracle、sqlServer、mysql、access）

	public static String dbName;//init.database.dbName 数据库名字

	public static String userName;//init.database.userName 数据库用户名

	public static String passWord;//init.database.passWord 数据库密码

	public static String sql;//init.database.sql  拉取数据方式为dataBase时候配置读取数据的SQL

	public static String otherSql;//init.database.otherSql  拉取数据方式为dataBase时候配置读取数据的SQL

	public static String dbConnectType;//init.database.dbConnectType 数据库连接类型 odbc jdbc

	private static Properties analyze;

	public static Class serverAnalyze;//init.analyze.serverAnalyze 解析字符串类

	public static String analyzeMethod;// 解析字符串函数

	public static Class serverFileAnalyze;//init.analyze.serverFileAnalyze 文件处理类

	public static String analyzeFileCopyMethod;// 文件复制方法

	public static String analyzeFileSendMethod;// 文件传送方法

	public static String analyzeFileUpdateMethod;// 文件传送方法

	public static String stencil;//init.analyze.stencil 基础模版

    public static String runDicomService;//是否上传dicom

	public static void initServerConfig() {
		File file = new File("config/server.properties");
		try {
			if(!file.exists()) {
				file.createNewFile();
			}
			InputStream in = new BufferedInputStream(new FileInputStream("config/server.properties"));
			BaseConfig.server = new Properties();
			BaseConfig.server.load(in);
			BaseConfig.code = BaseConfig.server.getProperty("init.server.code");
			BaseConfig.type = BaseConfig.server.getProperty("init.server.type");
			BaseConfig.startTime = BaseConfig.server.getProperty("init.server.startTime");
			BaseConfig.upLoadFileUrl = BaseConfig.server.getProperty("init.server.upLoadFileUrl");
			BaseConfig.upLoadInfoUrl = BaseConfig.server.getProperty("init.server.upLoadInfoUrl");
			BaseConfig.pacsType = BaseConfig.server.getProperty("init.server.pacsType");
			BaseConfig.fileType = BaseConfig.server.getProperty("init.server.fileType");
			BaseConfig.runTime = BaseConfig.server.getProperty("init.server.runTime");
			BaseConfig.runMonitor = BaseConfig.server.getProperty("init.server.runMonitor");
			BaseConfig.monitorName = BaseConfig.server.getProperty("init.server.monitorName");
			BaseConfig.monitorTime = BaseConfig.server.getProperty("init.server.monitorTime");
            BaseConfig.runDicomService=BaseConfig.server.getProperty("init.server.DicomService","false");
			String serviceConfig = BaseConfig.server.getProperty("init.server.serviceClass");
			String[] classesInfo = serviceConfig.split("\\|");
			if(classesInfo != null && classesInfo.length > 0) {
				ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
				for(String strClassInfo : classesInfo) {
					String[] info = strClassInfo.split("\\$");
					Class cl = Class.forName(info[0], true, classLoader);
					BaseConfig.serviceClass.put(info[1], cl);
				}
			}
		} catch(Exception e) {
			LogForError.logError(e);
		}
	}


	public static void initFileConfig() {
		File file = new File("config/file.properties");
		try {
			if(!file.exists()) {
				file.createNewFile();
			}
			InputStream in = new BufferedInputStream(new FileInputStream("config/file.properties"));
			BaseConfig.file = new Properties();
			BaseConfig.file.load(in);
			BaseConfig.fileDir = BaseConfig.file.getProperty("init.file.fileDir");
			BaseConfig.fileToDir = BaseConfig.file.getProperty("init.file.fileToDir");
			BaseConfig.addOldFile = BaseConfig.file.getProperty("init.file.addOldFile");
			BaseConfig.checkUpdateTime = BaseConfig.file.getProperty("init.file.checkUpdateTime");
		} catch(Exception e) {
			LogForError.logError(e);
		}
	}

	public static void initDatabaseConfig() {
		File file = new File("config/database.properties");
		try {
			if(!file.exists()) {
				file.createNewFile();
			}
			InputStream in = new BufferedInputStream(new FileInputStream("config/database.properties"));
			BaseConfig.database = new Properties();
			BaseConfig.database.load(in);
			BaseConfig.dbUrl = BaseConfig.database.getProperty("init.database.dbUrl");
			BaseConfig.dbType = BaseConfig.database.getProperty("init.database.dbType");
			BaseConfig.dbName = BaseConfig.database.getProperty("init.database.dbName");
			BaseConfig.userName = BaseConfig.database.getProperty("init.database.userName");
			BaseConfig.passWord = BaseConfig.database.getProperty("init.database.passWord");
			BaseConfig.sql = BaseConfig.database.getProperty("init.database.sql");
			BaseConfig.otherSql = BaseConfig.database.getProperty("init.database.otherSql");
			BaseConfig.dbConnectType = BaseConfig.database.getProperty("init.database.dbConnectType");
		} catch(Exception e) {
			LogForError.logError(e);
		}
	}

	public static void initAnalyzeConfig() {
			File file = new File("config/analyze.properties");
			try {
				if(!file.exists()) {
					file.createNewFile();
				}
				ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
				InputStream in = new BufferedInputStream(new FileInputStream("config/analyze.properties"));
				BaseConfig.analyze = new Properties();
				BaseConfig.analyze.load(in);


				String strClass = BaseConfig.analyze.getProperty("init.analyze.serverAnalyze");
				BaseConfig.stencil = BaseConfig.analyze.getProperty("init.analyze.stencil");
				if(strClass != null) {
					String[] info = strClass.split("\\$");
					BaseConfig.serverAnalyze = Class.forName(info[0], true, classLoader);
					BaseConfig.analyzeMethod = info[1];
				}
				String fileClass = BaseConfig.analyze.getProperty("init.analyze.serverFileAnalyze");
				if(fileClass!=null){
					String[] info = fileClass.split("\\$");
					BaseConfig.serverFileAnalyze = Class.forName(info[0], true, classLoader);
					BaseConfig.analyzeFileCopyMethod = info[1];
					BaseConfig.analyzeFileSendMethod = info[2];
					BaseConfig.analyzeFileUpdateMethod = info[3];
				}

			} catch(Exception e) {
				LogForError.logError(e);
			}
		}

	public static void updateConfig() {
		if(BaseConfig.server != null) {
			try {
				OutputStream out = new FileOutputStream(new File("config/server.properties"));
				BaseConfig.server.setProperty("init.server.startTime",
						new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
				BaseConfig.server.store(out, "change init.server.startTime");
				out.flush();
				out.close();
			} catch(FileNotFoundException e) {
				LogForError.logError(e);
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}

}
