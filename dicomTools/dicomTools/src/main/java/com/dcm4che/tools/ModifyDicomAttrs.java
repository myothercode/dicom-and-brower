package com.dcm4che.tools;

import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Chace.Cai
 * Date: 13-7-22
 * Time: 上午10:58
 * To change this template use File | Settings | File Templates.
 */
public class ModifyDicomAttrs extends ModifyDcm {
    public void ModifyAttrs(String sourcePath,String destPath,Map attrs){
        /*attrs支持字符集、patientID的修改*/
        //Tag.SpecificCharacterSet   VR.CS;  GB18030
        //Tag.PatientID VR.LO
       // Tag.PatientName, VR.PN,"dfdf"
        doModify(sourcePath, destPath, attrs);
    }
}
