
/* ***** BEGIN LICENSE BLOCK *****
* Version: MPL 1.1/GPL 2.0/LGPL 2.1
*
* The contents of this file are subject to the Mozilla Public License Version
* 1.1 (the "License"); you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
* http://www.mozilla.org/MPL/
*
* Software distributed under the License is distributed on an "AS IS" basis,
* WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
* for the specific language governing rights and limitations under the
* License.
*
* The Original Code is part of Oviyam, an web viewer for DICOM(TM) images
* hosted at http://skshospital.net/pacs/webviewer/oviyam_0.6-src.zip 
*
* The Initial Developer of the Original Code is
* Raster Images
* Portions created by the Initial Developer are Copyright (C) 2007
* the Initial Developer. All Rights Reserved.
*
* Contributor(s):
* Babu Hussain A
* Bharathi B
* Manikandan P
* Meer Asgar Hussain B
* Prakash J
* Prakasam V
* Suresh V
*
* Alternatively, the contents of this file may be used under the terms of
* either the GNU General Public License Version 2 or later (the "GPL"), or
* the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
* in which case the provisions of the GPL or the LGPL are applicable instead
* of those above. If you wish to allow use of your version of this file only
* under the terms of either the GPL or the LGPL, and not to allow others to
* use your version of this file under the terms of the MPL, indicate your
* decision by deleting the provisions above and replace them with the notice
* and other provisions required by the GPL or the LGPL. If you do not delete
* the provisions above, a recipient may use your version of this file under
* the terms of any one of the MPL, the GPL or the LGPL.
*
* ***** END LICENSE BLOCK ***** */ 
package in.raster.oviyam.handler;


import in.raster.oviyam.PatientInfo;
import in.raster.oviyam.model.PatientModel;
import in.raster.oviyam.model.StudyModel;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.log4j.Logger;


/**
 * Tag handler class generates the informations of the Studies.
 * 
 * @author bharathi
 * @version 0.7
 *
 * Example: The Jsp code that uses the StudyInfoHadler class as Tag.
 * <%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>    
 * <%@ page isELIgnored="false"%> *	
 * <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
 * <%@ taglib prefix="study" uri="StudyInfo" %>  
 * <HTML>
 * 	<HEAD>
 * 	</HEAD>
 * 		<BODY> 
 * 			<TABLE>
 * 				<study:Study patientId="${param.patientId}" modality="${param.modality}">
 * 					<TR>
 * 						<TD>${studyId}</TD>
 * 						<TD>${modality}</TD>
 *  					<TD>${studyDescription}</TD>
 *  					<TD>${studyDates}</TD>
 *  				</TR>					
 * 				</study:Study>
 * 			</TABLE>
 * 		 </BODY>
 * </HTML>
 *
 */
public class StudyInfoHandler extends SimpleTagSupport{
	
	private static Logger log = Logger.getLogger(StudyInfoHandler.class);
	
	// Attribute variables of this tag -------------------------------------------------------
	private String patientId;
	
	private String modality;
	
	public static int count;

	/**
	 * Setter for property patientId.
	 * @param patientId String object registers the patientId.
	 */
	public void setPatientId(String patientId){
		this.patientId = patientId;		
	}	
	
	/**
	 * Setter for property modality.
	 * @param modality String object registers the modality.
	 */
	public void setModality(String modality){
		this.modality = modality;
	}
	
	/**
	 * Overridden Tag handler method.Default processing of the tag.
	 * This method will send the Study information to generate a Html page during its execution. 	
	 * 
	 * @see javax.servlet.jsp.tagext.SimpleTagSupport#doTag()
	 */
	@Override
	public void doTag() throws JspException, IOException {
		if (this.modality == null) {
			this.modality = "";
		}
		try {
			//ArrayList contains the PatientModels.
			ArrayList<PatientModel> patientList = PatientInfo.patientList;
			for (int i = 0; i < patientList.size(); i++) {
				PatientModel pat = patientList.get(i);
				if (pat.getPatientID().equals(patientId)) {
					// Writes the study informations to the response.
					for (int j = 0; j < pat.getStudis().size(); j++) {
						
						/* StudyModel instance contains the study informations such as studyId,
						 * studyDescription, modality etc.
						 * @see in.raster.oviyam.StudyModel.
						 */
						StudyModel study = pat.getStudis().get(j);					
						
						/*
						 * Tag handler sets the attribute values such as studyId, studyDescription, modality etc.
						 * Example : User can access it form the JSP file using the Expression Language of JSP such as
						 * 	${studyId}
						 * 	${studyDescription}
						 *  ${modality}
						 */
						getJspContext().setAttribute("no", ++count);

						getJspContext().setAttribute("studyId",
								study.getStudyID());
						getJspContext().setAttribute("modalityInStudy",
								study.getModalitiesInStudy());
						getJspContext().setAttribute("studyDescription",
								study.getStudyDescription());
						getJspContext().setAttribute("studyDates",
								study.getStudyDate());
						getJspContext().setAttribute("accessionNumber",
								study.getAccessionNumber());
						/*
						 * Process the body of the tag and print it to the response. The null argument
						 * means the output goes to the response rather than some other writer. 
						 */
						getJspBody().invoke(null);

					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		}
	}

}
