package com.dcm4che.tools;

import java.io.File;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Chace.Cai
 * Date: 13-7-10
 * Time: 下午4:50
 * To change this template use File | Settings | File Templates.
 */
public class GetDicomMap extends ReadDcm {
    public Map GetDicomMap(String filePath){
        read(filePath);
        return map;
    }
    public Map GetDicomMap(File filePath){
        read(filePath);
        return map;
    }
}
