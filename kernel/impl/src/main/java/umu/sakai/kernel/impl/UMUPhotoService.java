/**********************************************************************************
*
* $Id: SakaiPersonPhotoService.java 60514 2009-04-21 22:05:56Z arwhyte@umich.edu $
*
***********************************************************************************
*
 * Copyright (c) 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/

package umu.sakai.kernel.impl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Blob;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.user.api.UserDirectoryService;

import umu.sakai.kernel.api.IUMUPhotoService;


/**
 * By default, roster photos come from the Profile service.
 */
public class UMUPhotoService implements IUMUPhotoService {

	private static final Log log = LogFactory.getLog(UMUPhotoService.class);
	
	@PersistenceContext(unitName="umuphoto-jpa")
	protected EntityManager entityManager;

	protected UserDirectoryService userDirectoryService;
	protected String userType;
	
	public UserDirectoryService getUserDirectoryService() { return userDirectoryService; }
	public void setUserDirectoryService(UserDirectoryService userDirectoryService) { this.userDirectoryService = userDirectoryService; }

	public String getUserType() { return userType; }
	public void setUserType(String userType) { this.userType = userType; }
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.api.app.roster.PhotoService#getPhotoAsByteArray(java.lang.String)
	 */
	public byte[] getPhotoAsByteArray(String userId,boolean forzar) {
		ByteArrayOutputStream bout = null;
		try {
			String email = userDirectoryService.getUser(userId).getEid();
			String paraEmail = userDirectoryService.getCurrentUser().getEid();
			String tipoFoto = "UserOfficialImage";
			if (!forzar){tipoFoto=tipoFoto+"Permitida";}
			log.debug("Extrae Foto de ["+email+"] para ["+paraEmail+"] :: "+tipoFoto);
			if (userType.equals(userDirectoryService.getUser(userId).getType())) {
				InputStream miFoto = null;
				// Saca la foto de BBDD
				Blob image = (Blob)entityManager.createNamedQuery(tipoFoto).setParameter("email",email).setParameter("observador", paraEmail).getSingleResult();
				if (image==null) {
					// Si no hay en BBDD imagen se pone una por defecto.
					miFoto = getClass().getResource("sinfoto.jpg").openStream();
				} else {
					miFoto = image.getBinaryStream();
				}
				bout = new ByteArrayOutputStream();
				byte [] b = new byte[1024];
				int leido = miFoto.read(b);
				while (leido!=-1) {
					bout.write(b,0,leido);
					leido = miFoto.read(b);
				}
				return bout.toByteArray();
			}
		} catch (Exception ex) {
			log.error("Error extrayendo imagen: "+ex);
		}
		return null;
	}
	
	public byte[] getPhotoAsByteArray(String userId){
		return getPhotoAsByteArray(userId,true);
	}
	
	public void savePhoto(byte[] data, String userId) {
		// Nothing to do
	}

	public boolean overRidesDefault() {
		return true;
	}
	
	public String getDirectorioUMU(String eid){
		if (eid == null) {
			String nombre = userDirectoryService.getCurrentUser().getDisplayName();
			return "http://www.um.es/atica/directorio/index.php?nivel=&lang=0&vista=&search=" + nombre;
		}
		
		String filiaciones = (String) entityManager.createNamedQuery("GetDirectorioUMU")
				.setParameter("email", eid).getSingleResult();

		if (filiaciones.equals("#"))
			return "";
		else {
			String filsOrden[] = {"PDI","RC","BEC1","BEC2","PAS1","BEC3","ALU","EXT","CRG"};
			String filsOrdenN[] = {"a004","a006","a005","a002","a003","a010","a001","a008","a009"};
			
			int i;
			for (i=0;i<filsOrdenN.length && filiaciones.indexOf("#"+filsOrdenN[i]+"#")==-1;i++);
			if (i!=filsOrdenN.length)
				return "http://www.um.es/atica/directorio/index.php?usuario="+ eid.substring(0,eid.indexOf("@"))+"."+filsOrden[i];
			else
				return "";
		}
	}

}
