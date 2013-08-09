package in.raster.oviyam.access;

import com.appcenter.bhml.basic.dto.ServiceRequestHeader;
import com.appcenter.bhml.core.pacs.dto.GetPacsReportPathByMapRequestTO;
import com.appcenter.bhml.core.pacs.dto.GetPacsReportPathByMapResponseTO;
import com.appcenter.bhml.webservice.ExamineFileWebService;
import in.raster.oviyam.model.PacsQueryLogModel;
import in.raster.oviyam.model.SessionModel;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Chace.Cai
 * Date: 13-7-12
 * Time: 下午4:27
 * To change this template use File | Settings | File Templates.
 */

@Service("dataAccess")
public class DataAccessImpl implements DataAccess {
   // @Autowired
    JdbcTemplate jdbcTemplate;

    @Value("${ws.addDicomWSURL}")
    private String addDicomWSURL;

    @Value("${ws.addDicomMethod}")
    private String addDicomMethod;

    @Value("${ws.checkDicomMethod}")
    private String checkDicomMethod;

    /*应用中心接口地址*/
    @Value("${ws.reportAddressWSURL}")
    private String reportAddressWSURL;
    @Value("${ws.reportMethod}")
    private String getPacsReportPathByMap;

    @Override
    public void test(){
        System.out.println("df");
        int i = jdbcTemplate.queryForInt("select count(1) from dual");
    }

    /*查询当前登录人信息*/
    public SessionModel findSessionInfoByLoginId(String loginId) {

        String sql = "select xm xm,loginId loginId,ksId ksId,0 as pid from TESTYGINFO te where te.loginId=?";
        Object[] params = new Object[]{loginId};
        List li = jdbcTemplate.query(sql, params, new DataMapperD());
        if (li.isEmpty()) return null;

        return (SessionModel) li.get(0);
    }

    /*验证当前的登录医生是否有指定的检验id查看权限*/
    @Override
    public boolean checkCheckId(String loginId, String pid) {
        StringBuffer sql = new StringBuffer();
        sql.append("select count(1) from TESTYGINFO te ");
        sql.append("inner join PACS_AUTHORITY pa on te.loginid=pa.target_doctor and pa.application_id=? ");
        sql.append("where te.loginid=?");
        Object[] params = new Object[]{pid,loginId};
        int i = jdbcTemplate.queryForInt(sql.toString(), params);
        return i == 0 ? false : true;
    }

    @Override
    public void addPacsLog(PacsQueryLogModel pl) {
        //StringBuffer sql = new StringBuffer();
        //sql.append("insert into pacs_query_log (application_id, query_doctor_id, query_time) values ");
        String sql = "insert into pacs_query_log (application_id, query_doctor_id, query_time) values (?,?,sysdate)";
        Object[] params = new Object[]{pl.getApplicationId(),pl.getDoctorId()};
        int i=jdbcTemplate.update(sql,params);
    }

    @Override
    public boolean checsIsExist(String pid) {
        /*String sql="select count(1) from pacs.patient pp where pp.pat_id = ?";
        Object[] params = new Object[]{pid};
        int i = jdbcTemplate.queryForInt(sql.toString(), params);
        return i == 0 ? false : true;*/
        Object[] a=null;
        JaxWsDynamicClientFactory jw=JaxWsDynamicClientFactory.newInstance();
        try {
            Client c=jw.createClient(addDicomWSURL);
            a=c.invoke(checkDicomMethod,pid);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return a==null?null:(Boolean)a[0];
    }

    @Override
    public String addDicom(String path){
        Object[] a=null;
        JaxWsDynamicClientFactory jw=JaxWsDynamicClientFactory.newInstance();
        try {
            Client c=jw.createClient(addDicomWSURL);
            a=c.invoke(addDicomMethod,path);
        } catch (Exception e) {
            e.printStackTrace();
            return "获取dcm文件失败!"+e.getMessage();
        }

        return a==null?null:a[0].toString();
    }

    @Override
    public String getReportAddress(String pid) {
        if(pid==null||pid=="")return null;
        String[] s=pid.split("_");
        if(s==null||s.length!=2)return null;
        GetPacsReportPathByMapRequestTO getPacsReportPathByMapRequestTO=new GetPacsReportPathByMapRequestTO();
        getPacsReportPathByMapRequestTO.setHeader(new ServiceRequestHeader());
        getPacsReportPathByMapRequestTO.setAppNO(s[1].toString());
        getPacsReportPathByMapRequestTO.setOrgCode(s[0].toString());

        JaxWsProxyFactoryBean jaxWsProxyFactoryBean=new JaxWsProxyFactoryBean();
        jaxWsProxyFactoryBean.setAddress(reportAddressWSURL);
        jaxWsProxyFactoryBean.setServiceClass(ExamineFileWebService.class);
        ExamineFileWebService examineFileWebService=(ExamineFileWebService)jaxWsProxyFactoryBean.create();


        /*Object[] a=null;
        JaxWsDynamicClientFactory jw=JaxWsDynamicClientFactory.newInstance();
        try {
            Client c=jw.createClient(reportAddressWSURL);
            a=c.invoke(getPacsReportPathByMap,getPacsReportPathByMapRequestTO);
        } catch (Exception e) {
            e.printStackTrace();
            return "调用接口失败!"+e.getMessage();
        }*/

        GetPacsReportPathByMapResponseTO getPacsReportPathByMapResponseTO=examineFileWebService.getPacsReportPathByMap(getPacsReportPathByMapRequestTO);

        if(getPacsReportPathByMapResponseTO.getErrorMessage() != null) {
            Iterator iterator = getPacsReportPathByMapResponseTO.getErrorMessage().iterator();
            StringBuffer errorMsg = new StringBuffer("接口错误返回:");
            while(iterator.hasNext()) {
                errorMsg.append("[" + iterator.next().toString() + "]");
            }
            return "getPacsReportPathByMapResponseTO ERR";
        }
        return  getPacsReportPathByMapResponseTO.getPath();
    }


}


class DataMapperD implements RowMapper{
    public Object mapRow(ResultSet rs,int rowNum) throws SQLException{
        SessionModel sm=new SessionModel();
        sm.setXm(rs.getString("xm"));
        sm.setLoginId(rs.getString("loginId"));
        sm.setKsId(rs.getString("ksId"));
        sm.setPid(rs.getString("pid"));
        sm.setId(12);
        sm.setOrgId(123);
        sm.setSfzjh("w");
        sm.setYgDm("xx");
        sm.setYljgdm("er");
        return sm;
    }
}