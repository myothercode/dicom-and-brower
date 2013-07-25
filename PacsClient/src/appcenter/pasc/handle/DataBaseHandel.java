package appcenter.pasc.handle;

import appcenter.logHelper.LogForError;
import appcenter.logHelper.LogForInfo;
import appcenter.pasc.domain.DBResource;
import appcenter.pasc.domain.ShareDBResource;
import appcenter.pasc.util.BaseConfig;
import appcenter.pasc.util.CommonUtil;
import appcenter.pasc.util.CreateServiceConUtil;
import appcenter.pasc.util.DBConnectionPool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** @author mark.yang 13-5-17 pasc */
public class DataBaseHandel extends Thread {
	public static final Log logger = LogFactory.getLog(DataBaseHandel.class);

	public static boolean runFlag;
	private static boolean firstRun = true;
	private static boolean firstLoad = true;

	@Override
	public void run() {
		runFlag = true;
		if(firstLoad) {
			if(CreateServiceConUtil.createUtilServiceObj()) {
				LogForInfo.logInfo("初始化接口池成功!");
				firstLoad = false;
			} else {
				LogForInfo.logInfo("初始化接口池失败!");
				return;
			}
		}
		while(runFlag) {
			doHandel();
			CommonUtil.pauseTime(Integer.parseInt(BaseConfig.runTime) * 60 * 1000, true, "终止数据库上传线程运行!");
		}
		runFlag = true;
	}

	private void doHandel() {
		try {
			LogForInfo.logInfo("数据库连接方式:"+BaseConfig.dbConnectType);
			Connection conn = DBConnectionPool.getConnection();
			LogForInfo.logInfo("取得数据库连接!");
			PreparedStatement st = getConfigPreparedStatement(conn);
			LogForInfo.logInfo("PreparedStatement is "+st==null?"null":"right!");
			if(st != null) {
				ResultSet set = st.executeQuery();
				while(set.next()) {
					DBResource dbResource = new DBResource();
					dbResource.setPatientID(set.getString("PatientID"));
					dbResource.setReportStr(set.getString("Report"));
					ShareDBResource.dbResources.add(dbResource);
					LogForInfo.logInfo("查询出PatientID="+dbResource.getPatientID());
				}
				set.close();
				st.close();
			}
			DBConnectionPool.close(conn);
		} catch(SQLException e) {
			LogForError.logError(e);
		}
		this.doValidResource();
	}

	// todo getConfigSQL 是用来通过配置文件解析SQL的函数，目前只根据固定的数据库解析。
	private PreparedStatement getConfigPreparedStatement(Connection conn) {
		PreparedStatement st = null;
		if(firstRun) {
			LogForInfo.logInfo("首次运行构造参数!");
			try {
				st = conn.prepareStatement(BaseConfig.sql);
				String[] dateStr=BaseConfig.startTime.split(" ");
				st.setString(1, dateStr[0]);
				st.setString(2, dateStr[1]);
			} catch(SQLException e) {
				LogForError.logError(e);
			}
			firstRun = false;
		} else {
			LogForInfo.logInfo("非首次运行构造参数!");
			try {
				st = conn.prepareStatement(BaseConfig.otherSql);
				String[] dateStr=BaseConfig.startTime.split(" ");
				st.setString(1, dateStr[0]);
				st.setInt(2, -Integer.parseInt(BaseConfig.runTime));
			} catch(SQLException e) {
				LogForError.logError(e);
			}
		}
		return st;
	}

	private void doValidResource() {
		List<DBResource> dbResources = new ArrayList<DBResource>();
		this.delMore();
		ShareDBResource.errorDbResources.clear();
		if(ShareDBResource.dbResources.size() > 10) {
			for(int i = 0;i < 10;i++) {
				dbResources.add(ShareDBResource.dbResources.get(i));
			}
		} else {
			for(int i = 0;i < ShareDBResource.dbResources.size();i++) {
				dbResources.add(ShareDBResource.dbResources.get(i));
			}
		}
		if(!dbResources.isEmpty()){
			LogForInfo.logInfo("构造数据解析上传线程!");
			this.doSendToCenter(dbResources);
		}
		BaseConfig.updateConfig();
	}

	private void doSendToCenter(List<DBResource> dbResources) {
		DataBaseSendThread dataBaseSendThread = new DataBaseSendThread(dbResources);
		dataBaseSendThread.start();
	}

	private void delMore(){
		if(!ShareDBResource.errorDbResources.isEmpty()){
			for(DBResource resource : ShareDBResource.errorDbResources){
				boolean result=true;
				for(DBResource resource1 : ShareDBResource.dbResources){
					  if(resource1.getPatientID().equals(resource.getPatientID())){
						  result=false;
					  }
				}
				if(result){
					ShareDBResource.dbResources.add(resource);
				}
			}
		}
	}
}
