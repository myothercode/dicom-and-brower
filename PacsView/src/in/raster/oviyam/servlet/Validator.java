

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
package in.raster.oviyam.servlet;





import in.raster.oviyam.EchoService;
import in.raster.oviyam.access.DataAccess;

import in.raster.oviyam.config.ServerConfiguration;
import in.raster.oviyam.model.PacsQueryLogModel;
import in.raster.oviyam.utils.AE;

import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import org.apache.log4j.Logger;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Checks whether the configured AeTitle is available in the configured server or not.
 * It uses the EchoService to find whether the machine is available or not.
 * @see in.raster.oviyam.EchoService
 *   
 * @author bharathi
 * @version 0.7
 *
 */
@Controller("oviyam")
@Scope("prototype")
public class Validator extends HttpServlet{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Initialize the Logger.
	 */
	Logger log = Logger.getLogger(Validator.class);

    @Autowired
    DataAccess dataAccess;

    @Override
    public void init()throws ServletException{
        //super.init();
        //ServletContext servletContext=this.getServletContext();
        //WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(servletContext);
        //DataAccessImpl a=(DataAccessImpl)ctx.getBean("aa");

    }

	@Override
	public void doGet(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
        /*判断当前登录人是否有权限查看指定的检验id*/
        AttributePrincipal principal = (AttributePrincipal) request.getUserPrincipal();
        String loginId=principal.getName();
        String pid=(String)request.getParameter("PatientID");
        if(pid==null || "".equals(pid))return;
        boolean b=dataAccess.checkCheckId(loginId,pid);
        if(!b)return;
        PacsQueryLogModel pqlm = new PacsQueryLogModel();
        pqlm.setApplicationId(pid);
        pqlm.setDoctorId(loginId);
        dataAccess.addPacsLog(pqlm);

		AE ae;
		ServletContext servletContext=getServletContext();
		ServerConfiguration serverConfiguration;
		EchoService echoService;
		
		String agree = request.getParameter("agree");
		
		if(agree!=null && agree.equals("agree")){
			Cookie agreeCookie = new Cookie("agree","agree");
			agreeCookie.setMaxAge(60*60*24*365);
			response.addCookie(agreeCookie);		
		}
		
		try{			
			ae = new AE();
			//assigns the serverConfiguration instance.			
			serverConfiguration = ae.getServerConfiguration();
			/*
			 * writes the serverConfiguration instance in the servletContext (application scope).
			 * So all the SERVLET classes and JSP pages can access the serverConfig attribute.
			 * User can use either <jsp:useBean> tag or ${applicationScope.serverConfig} EL 
			 * to access the serverConfig attribute. From SERVLET classes the User can use
			 * the getServletContext().getAttribute("serverConfig") to access the serverConfiguration attribute.
			 */
			servletContext.setAttribute("serverConfig", serverConfiguration);		
			
			echoService = new EchoService();
			echoService.checkEcho();			
			/*If the status of EchoService is failed then the request will be forwarded to 
			 * EchoFailed.jsp. Otherwise, request is forwarded to oviyam7.jsp
			 * 
			 */
			if(echoService.getStatus().equals("Echo failed")){
				/*
				 * writes the echoURL(dcmProtocol://aeTitle@hostName:port) attribute in request instance.
				 * and forwards the request and response object to EchoFailed.jsp .
				 * echoFailed attribute can be accessed through either ${request.echoURL} or 
				 * <% request.getAttribute("echoURL")%>
				 */
				request.setAttribute("echoURL",ae.toString());
				request.getRequestDispatcher("EchoFailed.jsp").forward(request, response);
			}else{
				// forwards the request and response to oviyam7.jsp
				String studyUID = request.getParameter("studyUID");
				String seriesUID = request.getParameter("seriesUID");
				String patientID = request.getParameter("patientID");
				if(studyUID!=null && studyUID.length()<=0){
					request.setAttribute("param", "studyUID");
					request.getRequestDispatcher("InvalidParam.jsp").forward(request, response);
					log.error("Invalid studyUID parameter for Oviyam.");
				}else if(seriesUID!=null && seriesUID.length()<=0){
					request.setAttribute("param", "seriesUID");
					request.getRequestDispatcher("InvalidParam.jsp").forward(request, response);
					log.error("Invalid seriesUID parameter for Oviyam.");
				}else if(patientID!=null && patientID.length()<=0){
					request.setAttribute("param", "patientID");
					request.getRequestDispatcher("InvalidParam.jsp").forward(request, response);
					log.error("Invalid patientID parameter for Oviyam.");
				}else{
					request.getRequestDispatcher("oviyam7.jsp").forward(request, response);
				}
			}	
		
		}catch(Exception e){
			log.error(e.getMessage());
			
		}
	}
		
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
