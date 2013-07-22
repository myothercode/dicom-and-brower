package in.raster.oviyam.access;

import in.raster.oviyam.model.PacsQueryLogModel;
import in.raster.oviyam.model.SessionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
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
    @Autowired
    JdbcTemplate jdbcTemplate;

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