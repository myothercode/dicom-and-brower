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

	public static Properties p;

	public static String code;//init.org.code 机构编码

	public static String type;//init.run.type  拉取数据方式 （dataBase,file）

	public static String startTime;//init.run.startTime  拉取文件起始时间（格式 yyyy-mm-dd）

	public static String upLoadFileUrl;//init.run.upLoadFileUrl  上传文件数据WebService Url

	public static String upLoadInfoUrl;//init.run.upLoadInfoUrl  上传信息数据WebService Url

	public static String pacsType;//init.run.pacsType  检查类型

	public static String fileType;//init.run.fileType  上传文件类型

	//init.run.serviceClass  用于初始化保存接口访问对象，key 为接口URL，value 为 Class
	public static Map<String, Class> serviceClass = new HashMap<String, Class>();

	public static String runMonitor;//init.run.runMonitor 是否启动监控线程

	public static String monitorName;//init.run.monitorName  监控程序名称

	public static String monitorTime;//init.run.monitorTime  监控程序名称

	public static String dbUrl;//init.db.dbUrl 数据库连接IP及端口

	public static String dbType;//init.db.dbType 数据库分类只能有如下几种方式（oracle、sqlServer、mysql、access）

	public static String dbName;//init.db.dbName 数据库名字

	public static String userName;//init.db.userName 数据库用户名

	public static String passWord;//init.db.passWord 数据库密码

	public static String sql;//init.db.sql  拉取数据方式为dataBase时候配置读取数据的SQL

	public static String otherSql;//init.db.otherSql  拉取数据方式为dataBase时候配置读取数据的SQL

	public static String fileDir;//init.copy.fileDir 原始文件目录例如（e://source）

	public static String fileToDir;//init.copy.fileToDir 拷贝文件目录例如（e://back）

	public static String runTime;//init.copy.runTime  拷贝平率（单位 min）

	public static String addOldFile;// init.copy.addOldFile 是否读取旧数据

	public static String checkUpdateTime;//init.copy.checkUpdateTime 设置对有修改的报告进行重新上传的时间

	public static String dbConnectType;//init.db.dbConnectType 数据库连接类型 odbc jdbc

	public static void initConfig() {
		File file = new File("config/serviceConfig.properties");
		try {
			if(!file.exists()) {
				file.createNewFile();
			}
			InputStream in = new BufferedInputStream(new FileInputStream("config/serviceConfig.properties"));
			p = new Properties();
			p.load(in);
			BaseConfig.code = p.getProperty("init.org.code");
			BaseConfig.type = p.getProperty("init.run.type");
			BaseConfig.startTime = p.getProperty("init.run.startTime");
			BaseConfig.upLoadFileUrl = p.getProperty("init.run.upLoadFileUrl");
			BaseConfig.upLoadInfoUrl = p.getProperty("init.run.upLoadInfoUrl");
			BaseConfig.pacsType = p.getProperty("init.run.pacsType");
			BaseConfig.fileType = p.getProperty("init.run.fileType");
			BaseConfig.runTime = p.getProperty("init.run.runTime");
			BaseConfig.runMonitor = p.getProperty("init.run.runMonitor");
			BaseConfig.monitorName = p.getProperty("init.run.monitorName");
			BaseConfig.monitorTime = p.getProperty("init.run.monitorTime");
			BaseConfig.dbUrl = p.getProperty("init.db.dbUrl");
			BaseConfig.dbType = p.getProperty("init.db.dbType");
			BaseConfig.dbName = p.getProperty("init.db.dbName");
			BaseConfig.userName = p.getProperty("init.db.userName");
			BaseConfig.passWord = p.getProperty("init.db.passWord");
			BaseConfig.sql = p.getProperty("init.db.sql");
			BaseConfig.otherSql = p.getProperty("init.db.otherSql");
			BaseConfig.fileDir = p.getProperty("init.copy.fileDir");
			BaseConfig.fileToDir = p.getProperty("init.copy.fileToDir");
			BaseConfig.addOldFile=p.getProperty("init.copy.addOldFile");
			BaseConfig.checkUpdateTime = p.getProperty("init.copy.checkUpdateTime");
			BaseConfig.dbConnectType=p.getProperty("init.db.dbConnectType");
			String serviceConfig = p.getProperty("init.run.serviceClass");
			String[] strClassInfos = serviceConfig.split("\\|");
			if(strClassInfos != null && strClassInfos.length > 0) {
				ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
				for(String strClassInfo : strClassInfos) {
					String[] info = strClassInfo.split("\\$");
					Class cl = Class.forName(info[0], true, classLoader);
					BaseConfig.serviceClass.put(info[1], cl);
				}
			}
		} catch(Exception e) {
			LogForError.logError(e);
		}
	}

	public static void updateConfig(){
		if(BaseConfig.p!=null){
			try {
				OutputStream out = new FileOutputStream(new File("config/serviceConfig.properties"));
				BaseConfig.p.setProperty("init.run.startTime",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
				BaseConfig.p.store(out,"change init.run.startTime");
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
