package in.raster.oviyam.model;

import java.lang.String;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Chace.Cai
 * Date: 13-7-15
 * Time: 下午5:33
 * To change this template use File | Settings | File Templates.
 */
public class PacsQueryLogModel {

    private String doctorId;
    private String applicationId;
    private Date queryDate;

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public Date getQueryDate() {
        return queryDate;
    }

    public void setQueryDate(Date queryDate) {
        this.queryDate = queryDate;
    }
}
