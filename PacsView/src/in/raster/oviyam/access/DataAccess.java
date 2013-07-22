package in.raster.oviyam.access;

import in.raster.oviyam.model.PacsQueryLogModel;
import in.raster.oviyam.model.SessionModel;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Chace.Cai
 * Date: 13-7-12
 * Time: 下午4:36
 * To change this template use File | Settings | File Templates.
 */
public interface DataAccess {
    void test();
    public SessionModel findSessionInfoByLoginId(String loginId);
    public boolean checkCheckId(String loginId,String pid);
    public void addPacsLog(PacsQueryLogModel pl);
}
