package com.dcm4che.test;

import com.dcm4che.tools.GetDicomMap;
import com.dcm4che.tools.ModifyDicomAttrs;
import com.dcm4che.tools.ReadDcm;
import com.dcm4che.tools.SendDicomFile;
import org.dcm4che2.data.Tag;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Chace.Cai
 * Date: 13-7-10
 * Time: 下午4:33
 * To change this template use File | Settings | File Templates.
 */
public class test{
    private ModifyDicomAttrs mda;

    public static void main(String arg[]){
       /* GetDicomMap m=new GetDicomMap();
        Map p=m.GetDicomMap("E:\\MR\\1.2.276.0.7230010.3.0.3.5.1.6189764.4272510569\\1.2.276.0.7230010.3.0.3.5.1.6189767.635433212.dcm");*/

        /*SendDicomFile s=new SendDicomFile();
        Map m= s.SendDicomFile("DCM4CHEE@localhost:11112","E:\\dicom\\dicomfile\\1.2.276.0.7230010.3.0.3.5.1.6189812.569431277");*/

        ModifyDicomAttrs mda = new ModifyDicomAttrs();
        Map map=new HashMap();
        map.put(Tag.PatientID,"123456jjj");
        map.put(Tag.SpecificCharacterSet,"GB18030");

        mda.ModifyAttrs("E:\\dicom\\dicomfile\\1.2.276.0.7230010.3.0.3.5.1.6189812.569431277\\1.2.276.0.7230010.3.0.3.5.1.6189815.224816701.dcm",
                "E:\\dicom\\dicomfile\\1.2.276.0.7230010.3.0.3.5.1.6189812.569431277\\xx.dcm",map);
    }
}
