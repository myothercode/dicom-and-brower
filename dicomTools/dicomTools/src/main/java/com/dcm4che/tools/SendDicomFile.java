package com.dcm4che.tools;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Chace.Cai
 * Date: 13-7-12
 * Time: 上午8:56
 * To change this template use File | Settings | File Templates.
 */
public class SendDicomFile extends SendDcm {
    public Map SendDicomFile(String AE,String filePath){
       sendAction(AE, filePath);
        return map;
    }
}
