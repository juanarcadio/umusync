package umu.sakai.umusync.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityNotFoundException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockito.Mockito;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.RoleAlreadyDefinedException;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import umu.sakai.umusync.api.ISyncManager;
import umu.sakai.umusync.api.dao.ICriteria;
import umu.sakai.umusync.api.dao.IListString;
import umu.sakai.umusync.api.dao.IPage;
import umu.sakai.umusync.api.dao.ITask;

// Test components are in src/test/resources
@ContextConfiguration(locations={"classpath:umu/sakai/umusync/test/components.xml", "file:../pack/src/webapp/WEB-INF/components.xml"})
public class TestSyncManager extends AbstractTransactionalTestNGSpringContextTests {
	
	private static Log log = LogFactory.getLog(TestSyncManager.class);
	
	@Autowired
	@Qualifier("umu.sakai.umusync.api.ISyncManager")
	protected ISyncManager testSecureSyncManager;
	
	@Autowired
	@Qualifier("umu.sakai.umusync.api.ISyncManagerTarget")
	protected ISyncManager testSyncManager;

	@Autowired
	@Qualifier("org.sakaiproject.site.api.SiteService")
	protected SiteService ss;
	
	@Autowired
	@Qualifier("org.sakaiproject.tool.api.ToolManager")
	protected ToolManager tm;
	
	@Autowired
	@Qualifier("org.sakaiproject.component.api.ServerConfigurationService")
	protected ServerConfigurationService serverConfigurationServiceMock;
	
	@Autowired
	@Qualifier("org.sakaiproject.authz.api.AuthzGroupService")
	protected AuthzGroupService ags;
	
	@Autowired
	@Qualifier("org.sakaiproject.authz.api.FunctionManager")
	protected FunctionManager fm;
	
	@BeforeClass
	public void init() {
		try {
			Mockito.when(ss.getSite(Mockito.anyString())).thenReturn(null);
			Mockito.when(ss.getSite("mercury")).thenThrow(new IdUnusedException(null));
			
			/* Tools que tiene sakai version mock */
			Set<Tool> toolsDeSakai = new HashSet<Tool>();
			Tool resources = Mockito.mock(Tool.class);
			Mockito.when(resources.getId()).thenReturn("sakai.resources");
			toolsDeSakai.add(resources);
			
			Tool umusuma = Mockito.mock(Tool.class);
			Mockito.when(umusuma.getId()).thenReturn("sakai.umusuma");
			toolsDeSakai.add(umusuma);
				
			Tool syllabus = Mockito.mock(Tool.class);
			Mockito.when(syllabus.getId()).thenReturn("sakai.syllabus");
			toolsDeSakai.add(syllabus);
					
			Tool blogger = Mockito.mock(Tool.class);
			Mockito.when(blogger.getId()).thenReturn("blogger");
			toolsDeSakai.add(blogger);
			
			Tool ospassign = Mockito.mock(Tool.class);
			Mockito.when(ospassign.getId()).thenReturn("osp.assign");
			toolsDeSakai.add(ospassign);
			
			Tool usermembership = Mockito.mock(Tool.class);
			Mockito.when(usermembership.getId()).thenReturn("sakai.usermembership");
			toolsDeSakai.add(usermembership);
		
			Mockito.when(tm.findTools(null, null)).thenReturn(toolsDeSakai);
			
			/* Tipos de sitio que hay en sakai */
			List<String> tipos = new ArrayList<String>();
			tipos.add("asignaturaGrado");
			tipos.add("asignaturaGrado+");
			tipos.add("asignaturaMaster");
			tipos.add("ayuda");
			tipos.add("bienvenida");
			tipos.add("grado");
			tipos.add("master");
			tipos.add("myWorkSpaceExternoUM");
			tipos.add("myWorkSpaceOficialUM");
			tipos.add("portfolio");
			tipos.add("portfolioAdmin");
			tipos.add("project");	
			
			Mockito.when(ss.getSiteTypes()).thenReturn(tipos);
			
			/* Realms de sitio que hay en sakai */
			List<AuthzGroup> siterealm = new ArrayList<AuthzGroup>();
			
			AuthzGroup template = Mockito.mock(AuthzGroup.class);
			Mockito.when(template.getId()).thenReturn("!site.template");
			siterealm.add(template);
			
			AuthzGroup asigG = Mockito.mock(AuthzGroup.class);
			Mockito.when(asigG.getId()).thenReturn("!site.template.asignaturaGrado");
			siterealm.add(asigG);
			
			AuthzGroup asigGmas = Mockito.mock(AuthzGroup.class);
			Mockito.when(asigGmas.getId()).thenReturn("!site.template.asignaturaGrado+");
			siterealm.add(asigGmas);
			
			AuthzGroup asigM = Mockito.mock(AuthzGroup.class);
			Mockito.when(asigM.getId()).thenReturn("!site.template.asignaturaMaster");
			siterealm.add(asigM);
			
			AuthzGroup help = Mockito.mock(AuthzGroup.class);
			Mockito.when(help.getId()).thenReturn("!site.template.ayuda");
			siterealm.add(help);
			
			AuthzGroup welcome = Mockito.mock(AuthzGroup.class);
			Mockito.when(welcome.getId()).thenReturn("!site.template.bienvenida");
			siterealm.add(welcome);
			
			AuthzGroup course = Mockito.mock(AuthzGroup.class);
			Mockito.when(course.getId()).thenReturn("!site.template.course");
			siterealm.add(course);
			
			AuthzGroup grado = Mockito.mock(AuthzGroup.class);
			Mockito.when(grado.getId()).thenReturn("!site.template.grado");
			siterealm.add(grado);
			
			AuthzGroup master = Mockito.mock(AuthzGroup.class);
			Mockito.when(master.getId()).thenReturn("!site.template.master");
			siterealm.add(master);
			
			AuthzGroup portfolio = Mockito.mock(AuthzGroup.class);
			Mockito.when(portfolio.getId()).thenReturn("!site.template.portfolio");
			siterealm.add(portfolio);
			
			AuthzGroup portfolioadmin = Mockito.mock(AuthzGroup.class);
			Mockito.when(portfolioadmin.getId()).thenReturn("!site.template.portfolioAdmin");
			siterealm.add(portfolioadmin);
			
			
			Mockito.when(ags.getAuthzGroups("!site.template", null)).thenReturn(siterealm);
			Mockito.when(ags.getAuthzGroup("!site.template.asignaturaGrado")).thenReturn(asigG);
			
			Mockito.when(ags.getAuthzGroup(null)).thenThrow(new GroupNotDefinedException("MOCK throws exception"));
					
			/* Relams de section que hay en sakai */
			List<AuthzGroup> sectionrealm = new ArrayList<AuthzGroup>();
		
			AuthzGroup templateg = Mockito.mock(AuthzGroup.class);
			Mockito.when(templateg.getId()).thenReturn("!group.template");
			sectionrealm.add(templateg);
			
			AuthzGroup asigGg = Mockito.mock(AuthzGroup.class);
			Mockito.when(asigGg.getId()).thenReturn("!group.template.asignaturaGrado");
			sectionrealm.add(asigGg);
			
			AuthzGroup asigGmasg = Mockito.mock(AuthzGroup.class);
			Mockito.when(asigGmasg.getId()).thenReturn("!group.template.asignaturaGrado+");
			sectionrealm.add(asigGmasg);
			
			AuthzGroup asigMg = Mockito.mock(AuthzGroup.class);
			Mockito.when(asigMg.getId()).thenReturn("!group.template.asignaturaMaster");
			sectionrealm.add(asigMg);
			
			AuthzGroup courseg = Mockito.mock(AuthzGroup.class);
			Mockito.when(courseg.getId()).thenReturn("!group.template.course");
			sectionrealm.add(courseg);
			
			AuthzGroup gradog = Mockito.mock(AuthzGroup.class);
			Mockito.when(gradog.getId()).thenReturn("!group.template.grado");
			sectionrealm.add(gradog);
			
			AuthzGroup masterg = Mockito.mock(AuthzGroup.class);
			Mockito.when(masterg.getId()).thenReturn("!group.template.master");
			sectionrealm.add(masterg);
			
			AuthzGroup portfoliog = Mockito.mock(AuthzGroup.class);
			Mockito.when(portfoliog.getId()).thenReturn("!group.template.portfolio");
			sectionrealm.add(portfoliog);
					
			Mockito.when(ags.getAuthzGroups("!group.template", null)).thenReturn(sectionrealm);
			Mockito.when(ags.getAuthzGroup("!group.template.asignaturaGrado")).thenReturn(asigGg);
			
			// ROLES
			Role rolUno = Mockito.mock(Role.class);
			Role rolDos = Mockito.mock(Role.class);
			Role rolTres = Mockito.mock(Role.class);
			Mockito.when(rolUno.getId()).thenReturn("uno");
			Mockito.when(rolDos.getId()).thenReturn("dos");
			Mockito.when(rolTres.getId()).thenReturn("tres");
			Set<String> permisosVacios = new HashSet<String>();
			Mockito.when(rolUno.getAllowedFunctions()).thenReturn(permisosVacios);
			Mockito.when(rolDos.getAllowedFunctions()).thenReturn(permisosVacios);
			Mockito.when(rolTres.getAllowedFunctions()).thenReturn(permisosVacios);
			
			AuthzGroup soloRolDos = Mockito.mock(AuthzGroup.class);
			Set<Role> soloRolDosSet = new HashSet<Role>();
			soloRolDosSet.add(rolDos);
			Mockito.when(ags.getAuthzGroup("!site.template.soloRolDos")).thenReturn(soloRolDos);			
			Mockito.when(soloRolDos.getRoles()).thenReturn(soloRolDosSet);
			Mockito.when(soloRolDos.getId()).thenReturn("!site.template.soloRolDos");

			AuthzGroup dosRoles = Mockito.mock(AuthzGroup.class);
			Set<Role> dosRolesSet = new HashSet<Role>();
			dosRolesSet.add(rolUno);
			dosRolesSet.add(rolDos);
			Mockito.when(ags.getAuthzGroup("!site.template.dosRoles")).thenReturn(dosRoles);			
			Mockito.when(dosRoles.getRoles()).thenReturn(dosRolesSet);
			Mockito.when(dosRoles.getId()).thenReturn("!site.template.dosRoles");


			List<String> listaFunciones = new ArrayList<String>();
			listaFunciones.add("umusync.VIEW");
			listaFunciones.add("annc.new");
			listaFunciones.add("annc.read");
			listaFunciones.add("calendar.new");
			listaFunciones.add("calendar.read");
			listaFunciones.add("chat.new");
			listaFunciones.add("chat.read");
			listaFunciones.add("content.new");
			listaFunciones.add("content.read");
			listaFunciones.add("mail.new");
			listaFunciones.add("mail.read");
			Mockito.when(fm.getRegisteredFunctions()).thenReturn(listaFunciones);
			
			
			final String IGNORE_FUNCTIONS_PROPERTY = "umusync.ignore.functions";
			final String DEFAULT_IGNORE_FUNCTIONS_VALUE = "annc.all.groups,annc.delete.any,annc.delete.own,annc.new,annc.read,annc.read.drafts,annc.revise.any,annc.revise.own,asn.all.groups,asn.delete,asn.grade,asn.new,asn.read,asn.receive.notifications,asn.revise,asn.share.drafts,asn.submit,calendar.all.groups,calendar.delete.any,calendar.delete.own,calendar.import,calendar.new,calendar.read,calendar.revise.any,calendar.revise.own,calendar.subscribe,chat.delete.any,chat.delete.channel,chat.delete.own,chat.new,chat.new.channel,chat.read,chat.revise.channel,content.all.groups,content.delete.any,content.delete.own,content.hidden,content.new,content.read,content.revise.any,content.revise.own,poll.add,poll.deleteAny,poll.deleteOwn,poll.editAny,poll.editOwn,poll.vote,rwiki.admin,rwiki.create,rwiki.read,rwiki.superadmin,rwiki.update";
			Mockito.when(serverConfigurationServiceMock.getString(IGNORE_FUNCTIONS_PROPERTY, DEFAULT_IGNORE_FUNCTIONS_VALUE)).thenReturn(DEFAULT_IGNORE_FUNCTIONS_VALUE);

			PagingPosition ppnull = null;
			
			SitePage mockSitePage = Mockito.mock(SitePage.class);
			ToolConfiguration mockToolConfiguration = Mockito.mock(ToolConfiguration.class);
			Mockito.when(mockSitePage.addTool(Mockito.anyString())).thenReturn(mockToolConfiguration);
			Mockito.when(mockSitePage.addTool("noDisponible")).thenReturn(null);
			Mockito.when(mockSitePage.addTool("noExiste")).thenReturn(null);
			
			
			Site propertyOff = Mockito.mock(Site.class);
			Mockito.when(propertyOff.addPage()).thenReturn(mockSitePage);
			Mockito.when(propertyOff.getType()).thenReturn("asignaturaGrado");
			Mockito.when(propertyOff.getId()).thenReturn("propertyOff");
			Mockito.when(ss.getSite("propertyOff")).thenReturn(propertyOff);
			ResourceProperties rpPropertyOff = Mockito.mock(ResourceProperties.class);
			Mockito.when(rpPropertyOff.get("synchronize")).thenReturn("off");
			Mockito.when(rpPropertyOff.get("id")).thenReturn("propertyOff");
			Mockito.when(propertyOff.getProperties()).thenReturn(rpPropertyOff);
						
			
			Site sincronizado = Mockito.mock(Site.class);
			Mockito.when(sincronizado.addPage()).thenReturn(mockSitePage);
			Mockito.when(sincronizado.getType()).thenReturn("asignaturaGrado");
			Mockito.when(sincronizado.getId()).thenReturn("sincronizado");
			Mockito.when(ss.getSite("sincronizado")).thenReturn(sincronizado);
			ResourceProperties rpSincronizado = Mockito.mock(ResourceProperties.class);
			Mockito.when(rpSincronizado.get("synchronize")).thenReturn(null);
			Mockito.when(rpSincronizado.get("id")).thenReturn("sincronizado");
			Mockito.when(rpSincronizado.get("term")).thenReturn("2009");
			Mockito.when(sincronizado.getProperties()).thenReturn(rpSincronizado);
			SitePage paginaPK = Mockito.mock(SitePage.class);
			List<SitePage> pagesSincronizado = new ArrayList<SitePage>();
			pagesSincronizado.add(paginaPK);
			Mockito.when(sincronizado.getPages()).thenReturn(pagesSincronizado);
			ResourceProperties rpPaginaPK = Mockito.mock(ResourceProperties.class);
			Mockito.when(paginaPK.getProperties()).thenReturn(rpPaginaPK);
			Mockito.when(rpPaginaPK.getBooleanProperty(SitePage.IS_HOME_PAGE)).thenThrow(new EntityPropertyNotDefinedException());
			List<ToolConfiguration> toolsSincronizado = new ArrayList<ToolConfiguration>();
			ToolConfiguration tcSincronizado = Mockito.mock(ToolConfiguration.class);
			toolsSincronizado.add(tcSincronizado);
			Mockito.when(paginaPK.getTools()).thenReturn(toolsSincronizado);
			Mockito.when(tcSincronizado.getToolId()).thenReturn("sakai.umusync");
			Mockito.when(paginaPK.getTitle()).thenReturn("paginaPK");

	
			List<Site> sitiosAsignaturaGrado = new ArrayList<Site>();
			sitiosAsignaturaGrado.add(propertyOff);
			sitiosAsignaturaGrado.add(sincronizado);
			
			List<Site> sitiosAsignaturaGrado2009 = new ArrayList<Site>();
			sitiosAsignaturaGrado2009.add(sincronizado);
			
			Site withoutAR = Mockito.mock(Site.class);
			Mockito.when(withoutAR.addPage()).thenReturn(mockSitePage);
			Mockito.when(withoutAR.getType()).thenReturn("withoutAdminRole");
			Mockito.when(withoutAR.getId()).thenReturn("withoutAdminRole");
			Mockito.when(ss.getSite("withoutAdminRole")).thenReturn(withoutAR);
			ResourceProperties rpWoutAR = Mockito.mock(ResourceProperties.class);
			Mockito.when(rpWoutAR.get("synchronize")).thenReturn(null);
			Mockito.when(rpWoutAR.get("id")).thenReturn("withoutAdminRole");
			Mockito.when(rpWoutAR.get("term")).thenReturn("2009");
			Mockito.when(withoutAR.getProperties()).thenReturn(rpWoutAR);
			SitePage paginaWoutAR = Mockito.mock(SitePage.class);
			List<SitePage> pagesWoutAR = new ArrayList<SitePage>();
			pagesWoutAR.add(paginaWoutAR);
			Mockito.when(withoutAR.getPages()).thenReturn(pagesWoutAR);
			ResourceProperties rpPaginaWoutAR = Mockito.mock(ResourceProperties.class);
			Mockito.when(paginaWoutAR.getProperties()).thenReturn(rpPaginaWoutAR);
			Mockito.when(rpPaginaWoutAR.getBooleanProperty(SitePage.IS_HOME_PAGE)).thenThrow(new EntityPropertyNotDefinedException());
			List<ToolConfiguration> toolsWoutAR = new ArrayList<ToolConfiguration>();
			ToolConfiguration tcWoutAR = Mockito.mock(ToolConfiguration.class);
			toolsWoutAR.add(tcWoutAR);
			Mockito.when(paginaWoutAR.getTools()).thenReturn(toolsWoutAR);
			Mockito.when(tcWoutAR.getToolId()).thenReturn("sakai.umusync");
			Mockito.when(paginaWoutAR.getTitle()).thenReturn("paginaPK");
			
			ArrayList<Site> sitiosWAR = new ArrayList<Site>();
			sitiosWAR.add(withoutAR);
			Mockito.doThrow(new PermissionException("Mock no admin", "save site", "criteriaOk site")).when(ss).save(withoutAR);

			
			Site removedSite = Mockito.mock(Site.class);
			Mockito.when(removedSite.addPage()).thenReturn(mockSitePage);
			Mockito.when(removedSite.getType()).thenReturn("removedSite");
			Mockito.when(removedSite.getId()).thenReturn("removedSite");
			Mockito.when(ss.getSite("removedSite")).thenReturn(removedSite);
			ResourceProperties rpRS = Mockito.mock(ResourceProperties.class);
			Mockito.when(rpRS.get("synchronize")).thenReturn(null);
			Mockito.when(rpRS.get("id")).thenReturn("removedSite");
			Mockito.when(rpRS.get("term")).thenReturn("2009");
			Mockito.when(removedSite.getProperties()).thenReturn(rpRS);
			SitePage sitepageRS = Mockito.mock(SitePage.class);
			List<SitePage> pagesRS = new ArrayList<SitePage>();
			pagesRS.add(sitepageRS);
			Mockito.when(removedSite.getPages()).thenReturn(pagesRS);
			ResourceProperties rpPageRS = Mockito.mock(ResourceProperties.class);
			Mockito.when(sitepageRS.getProperties()).thenReturn(rpPageRS);
			Mockito.when(rpPageRS.getBooleanProperty(SitePage.IS_HOME_PAGE)).thenThrow(new EntityPropertyNotDefinedException());
			List<ToolConfiguration> toolCRS = new ArrayList<ToolConfiguration>();
			ToolConfiguration tcRS = Mockito.mock(ToolConfiguration.class);
			toolCRS.add(tcRS);
			Mockito.when(sitepageRS.getTools()).thenReturn(toolCRS);
			Mockito.when(tcRS.getToolId()).thenReturn("sakai.umusync");
			Mockito.when(sitepageRS.getTitle()).thenReturn("paginaPK");
			
			ArrayList<Site> sitiosRemoved = new ArrayList<Site>();
			sitiosRemoved.add(removedSite);
			Mockito.doThrow(new IdUnusedException("Exception By Mockito")).when(ss).save(removedSite);

			
			
			Site defaultUserSite = Mockito.mock(Site.class);
			Mockito.when(defaultUserSite.addPage()).thenReturn(mockSitePage);
			Mockito.when(defaultUserSite.getType()).thenReturn(null);
			Mockito.when(defaultUserSite.getId()).thenReturn("defaultUserSite");
			Mockito.when(ss.getSite("defaultUserSite")).thenReturn(defaultUserSite);
			ResourceProperties rpDefaultUserSite = Mockito.mock(ResourceProperties.class);
			Mockito.when(rpDefaultUserSite.get("synchronize")).thenReturn(null);
			Mockito.when(rpDefaultUserSite.get("id")).thenReturn("defaultUserSite");
			Mockito.when(rpDefaultUserSite.get("numerito")).thenReturn("6");
			Mockito.when(defaultUserSite.getProperties()).thenReturn(rpDefaultUserSite);
			Mockito.when(ss.isUserSite("defaultUserSite")).thenReturn(true);

			Site allUserSite = Mockito.mock(Site.class);
			Mockito.when(allUserSite.addPage()).thenReturn(mockSitePage);
			Mockito.when(allUserSite.getType()).thenReturn("customUserSite");
			Mockito.when(allUserSite.getId()).thenReturn("allUserSite");
			Mockito.when(ss.getSite("allUserSite")).thenReturn(allUserSite);
			ResourceProperties rpAllUserSite = Mockito.mock(ResourceProperties.class);
			Mockito.when(rpAllUserSite.get("synchronize")).thenReturn(null);
			Mockito.when(rpAllUserSite.get("id")).thenReturn("allUserSite");
			Mockito.when(rpAllUserSite.get("numerito")).thenReturn("7");
			Mockito.when(allUserSite.getProperties()).thenReturn(rpAllUserSite);
			Mockito.when(ss.isUserSite("allUserSite")).thenReturn(true);
			

			
			
			Site siteWithTwoRoles = Mockito.mock(Site.class);
			Mockito.when(siteWithTwoRoles.addPage()).thenReturn(mockSitePage);
			Mockito.when(siteWithTwoRoles.getType()).thenReturn("permissionTest");
			Mockito.when(siteWithTwoRoles.getId()).thenReturn("siteWithTwoRoles");
			Mockito.when(ss.getSite("siteWithTwoRoles")).thenReturn(siteWithTwoRoles);
			ResourceProperties rpsw2r = Mockito.mock(ResourceProperties.class);
			Mockito.when(rpsw2r.get("synchronize")).thenReturn(null);
			Mockito.when(rpsw2r.get("id")).thenReturn("siteWithTwoRoles");
			Mockito.when(rpsw2r.get("term")).thenReturn("2009");
			Mockito.when(siteWithTwoRoles.getProperties()).thenReturn(rpsw2r);
			SitePage paginaPK1sw2r = Mockito.mock(SitePage.class);
			List<SitePage> pagesSw2r = new ArrayList<SitePage>();
			pagesSw2r.add(paginaPK1sw2r);
			Mockito.when(siteWithTwoRoles.getPages()).thenReturn(pagesSw2r);
			ResourceProperties rpPaginaPK1sw2r = Mockito.mock(ResourceProperties.class);
			Mockito.when(paginaPK1sw2r.getProperties()).thenReturn(rpPaginaPK1sw2r);
			Mockito.when(rpPaginaPK1sw2r.getBooleanProperty(SitePage.IS_HOME_PAGE)).thenThrow(new EntityPropertyNotDefinedException());
			List<ToolConfiguration> toolsSw2r = new ArrayList<ToolConfiguration>();
			ToolConfiguration tcSw2r = Mockito.mock(ToolConfiguration.class);
			toolsSw2r.add(tcSw2r);
			Mockito.when(paginaPK1sw2r.getTools()).thenReturn(toolsSw2r);
			Mockito.when(tcSw2r.getToolId()).thenReturn("sakai.umusync");
			Mockito.when(paginaPK1sw2r.getTitle()).thenReturn("paginaPK");
			
			Mockito.when(siteWithTwoRoles.addRole("uno", rolUno)).thenThrow(new RoleAlreadyDefinedException("Mockito Exception"));
			Mockito.when(siteWithTwoRoles.addRole("dos", rolDos)).thenThrow(new RoleAlreadyDefinedException("Mockito Exception"));
			Role rolUnoEnSw2r = Mockito.mock(Role.class);
			Role rolDosEnSw2r = Mockito.mock(Role.class);
			Mockito.when(siteWithTwoRoles.getRole("uno")).thenReturn(rolUnoEnSw2r);
			Mockito.when(siteWithTwoRoles.getRole("dos")).thenReturn(rolDosEnSw2r);
			Mockito.when(rolUnoEnSw2r.getAllowedFunctions()).thenReturn(permisosVacios);
			Mockito.when(rolDosEnSw2r.getAllowedFunctions()).thenReturn(permisosVacios);
			Set<Role> rolesSw2r = new HashSet<Role>();
			rolesSw2r.add(rolUnoEnSw2r);
			rolesSw2r.add(rolDosEnSw2r);
			Mockito.when(siteWithTwoRoles.getRoles()).thenReturn(rolesSw2r);
			Mockito.when(rolUnoEnSw2r.getId()).thenReturn("uno");
			Mockito.when(rolDosEnSw2r.getId()).thenReturn("dos");

			
			Site siteWithOneRole = Mockito.mock(Site.class);
			Mockito.when(siteWithOneRole.addPage()).thenReturn(mockSitePage);
			Mockito.when(siteWithOneRole.getType()).thenReturn("permissionTest");
			Mockito.when(siteWithOneRole.getId()).thenReturn("siteWithOneRole");
			Mockito.when(ss.getSite("siteWithOneRole")).thenReturn(siteWithOneRole);
			ResourceProperties rpsw1r = Mockito.mock(ResourceProperties.class);
			Mockito.when(rpsw1r.get("synchronize")).thenReturn(null);
			Mockito.when(rpsw1r.get("id")).thenReturn("siteWithOneRole");
			Mockito.when(rpsw1r.get("term")).thenReturn("2009");
			Mockito.when(siteWithOneRole.getProperties()).thenReturn(rpsw1r);
			SitePage paginaPKsw1r = Mockito.mock(SitePage.class);
			List<SitePage> pagesSw1r = new ArrayList<SitePage>();
			pagesSw1r.add(paginaPKsw1r);
			Mockito.when(siteWithOneRole.getPages()).thenReturn(pagesSw1r);
			ResourceProperties rpPaginaPK1sw1r = Mockito.mock(ResourceProperties.class);
			Mockito.when(paginaPKsw1r.getProperties()).thenReturn(rpPaginaPK1sw1r);
			Mockito.when(rpPaginaPK1sw1r.getBooleanProperty(SitePage.IS_HOME_PAGE)).thenThrow(new EntityPropertyNotDefinedException());
			List<ToolConfiguration> toolsSw1r = new ArrayList<ToolConfiguration>();
			ToolConfiguration tcSw1r = Mockito.mock(ToolConfiguration.class);
			toolsSw1r.add(tcSw1r);
			Mockito.when(paginaPKsw1r.getTools()).thenReturn(toolsSw1r);
			Mockito.when(tcSw1r.getToolId()).thenReturn("sakai.umusync");
			Mockito.when(paginaPKsw1r.getTitle()).thenReturn("paginaPK");
			
			Mockito.when(siteWithOneRole.addRole("dos", rolDos)).thenThrow(new RoleAlreadyDefinedException("Mockito Exception"));
			Role rolDosEnSw1r = Mockito.mock(Role.class);
			Mockito.when(siteWithOneRole.getRole("dos")).thenReturn(rolDosEnSw1r);
			Mockito.when(rolDosEnSw1r.getAllowedFunctions()).thenReturn(permisosVacios);
			Set<Role> rolesSw1r = new HashSet<Role>();
			rolesSw1r.add(rolDosEnSw1r);
			Mockito.when(siteWithTwoRoles.getRoles()).thenReturn(rolesSw1r);
			Mockito.when(rolDosEnSw1r.getId()).thenReturn("dos");
			
			
			List<Site> permissionTestSites = new ArrayList<Site>();
			permissionTestSites.add(propertyOff);
			permissionTestSites.add(siteWithOneRole);
			permissionTestSites.add(siteWithTwoRoles);
			
			List<Site> permissionTestSites2009 = new ArrayList<Site>();
			permissionTestSites2009.add(siteWithOneRole);
			permissionTestSites2009.add(siteWithTwoRoles);

			
			List<Site> allSites = new ArrayList<Site>();
			allSites.add(propertyOff);
			allSites.add(sincronizado);
			allSites.add(defaultUserSite);
			allSites.add(allUserSite);
			
			List<Site> allSites2009 = new ArrayList<Site>();
			allSites2009.add(sincronizado);
			
			Mockito.when(ss.getSites(Mockito.eq(SiteService.SelectionType.ANY), Mockito.anyString(), Mockito.anyString(), Mockito.anyMap(), Mockito.eq(SiteService.SortType.NONE), Mockito.eq(ppnull))).thenReturn(allSites);
			Mockito.when(ss.getSites(Mockito.eq(SiteService.SelectionType.ANY), Mockito.eq("removedSite"), Mockito.anyString(), Mockito.anyMap(), Mockito.eq(SiteService.SortType.NONE), Mockito.eq(ppnull))).thenReturn(sitiosRemoved);
			Mockito.when(ss.getSites(Mockito.eq(SiteService.SelectionType.ANY), Mockito.eq("withoutAdminRole"), Mockito.anyString(), Mockito.anyMap(), Mockito.eq(SiteService.SortType.NONE), Mockito.eq(ppnull))).thenReturn(sitiosWAR);			
			Mockito.when(ss.getSites(Mockito.eq(SiteService.SelectionType.ANY), Mockito.eq("asignaturaGrado"), Mockito.anyString(), Mockito.anyMap(), Mockito.eq(SiteService.SortType.NONE), Mockito.eq(ppnull))).thenReturn(sitiosAsignaturaGrado);
			Mockito.when(ss.getSites(Mockito.eq(SiteService.SelectionType.ANY), Mockito.eq("permissionTest"), Mockito.anyString(), Mockito.anyMap(), Mockito.eq(SiteService.SortType.NONE), Mockito.eq(ppnull))).thenReturn(permissionTestSites);

			Mockito.when(ss.getSites(SiteService.SelectionType.ANY, "removedSite", null, null, SiteService.SortType.NONE, null)).thenReturn(sitiosRemoved);
			Mockito.when(ss.getSites(SiteService.SelectionType.ANY, "withoutAdminRole", null, null, SiteService.SortType.NONE, null)).thenReturn(sitiosWAR);		
			Mockito.when(ss.getSites(SiteService.SelectionType.ANY, "asignaturaGrado", null, null, SiteService.SortType.NONE, null)).thenReturn(sitiosAsignaturaGrado);
			Mockito.when(ss.getSites(SiteService.SelectionType.ANY, "permissionTest", null, null, SiteService.SortType.NONE, null)).thenReturn(permissionTestSites);
			Mockito.when(ss.getSites(SiteService.SelectionType.ANY, null, null, null, SiteService.SortType.NONE, null)).thenReturn(allSites);
			
		} catch (Exception e) {
			log.error("init!!!:"+e);
			e.printStackTrace();
		}
	}

	private boolean sameCollection(String string, List<IListString> iListString) {
		if (iListString.isEmpty()) return string==null || "".equals(string);
		
		ArrayList<String> lista = new ArrayList<String>();
		for (String s:string.split(", ")) lista.add(s);
		for (IListString i:iListString) {
			if (!lista.remove(i.getString())) return false;
		}
		return lista.isEmpty();
	}
	
	private long insertTestTask() throws Throwable {
		ICriteria condicion = testSyncManager.createCriteria();
		condicion.setName("criteriaOK");
		condicion.setProperty("term");
		condicion.setComparador(ISyncManager.COMPARATOR_EQUALS);
		condicion.setValor("2009");
		long idcriteria = testSyncManager.addCriteria(condicion);
		condicion.setId(idcriteria);
		List<ICriteria> filtro = new ArrayList<ICriteria>();
		filtro.add(condicion);
		
		IPage pagina = testSyncManager.createPage();
		pagina.setName("paginaPK");
		pagina.setColumns("1");
		pagina.addToolInLeftColumn(0, "sakai.umusync");
		testSyncManager.addNewPage(pagina);
		
		IPage paginadel = testSyncManager.createPage();
		paginadel.setName("paginadel");	
		testSyncManager.addNewPage(paginadel);		
		
		ITask completa = testSyncManager.createTask();
		completa.setSyncInto(false);
		completa.setSyncHome("sakai.properties");
		completa.changeAvailable();
		completa.setTipo("asignaturaGrado");
		completa.setRealmSite("!site.template.asignaturaGrado");
		completa.setRealmSection("!group.template.asignaturaGrado");
		completa.setIgnoreSitesById("~admin, existente");
		completa.addToolToAdd("sakai.resources");
		completa.addToolToAdd("sakai.umusuma");
		completa.addToolToAdd("sakai.syllabus");
		completa.addToolToDel("blogger");
		completa.addToolToDel("osp.assign");
		completa.addToolToDel("sakai.usermembership");
		completa.addPageToAdd(pagina);
		completa.addPageToDel("paginadel");
		completa.setCriteria(filtro);					
		
		testSyncManager.addTask(completa);		
		return idcriteria;
	}
	
	private long getTestTaskId() {
		for (ITask t:testSyncManager.getTasks())
				if (t.getAvailable() && 
						"asignaturaGrado".equals(t.getTipo()) &&
						"!site.template.asignaturaGrado".equals(t.getRealmSite()) &&
						"!group.template.asignaturaGrado".equals(t.getRealmSection()) &&
						"~admin, existente".equals(t.getIgnoreSitesById()) &&
						sameCollection("sakai.resources, sakai.umusuma, sakai.syllabus", t.getToolsToAdd()) &&
						sameCollection("blogger, osp.assign, sakai.usermembership", t.getToolsToDel()) &&
						t.getPagesToAdd().size() == 1 &&
						"paginaPK".equals(t.getPagesToAdd().get(0).getName()) &&
						sameCollection("paginadel", t.getPagesToDel()) &&
						t.getCriteria().size() == 1 &&
						"criteriaOK".equals(t.getCriteria().get(0).getName()) &&
						"term".equals(t.getCriteria().get(0).getProperty()) &&
						ISyncManager.COMPARATOR_EQUALS.equals(t.getCriteria().get(0).getComparador()) &&
						"2009".equals(t.getCriteria().get(0).getValor())
				) return t.getId();
		
		return 0;		
	}
			
	@DataProvider(name="sites")
	public Object[][] getSites() {
		return new Object[][]{{"~admin"}, {"existente"}};
	}
	
	@Test(dataProvider="sites")
	public void testCheckSiteIdEncontrado(String siteId) {
		assert testSyncManager.checkSiteId(siteId);
	}
	
	@Test
	public void testCheckSiteIdNoEncontrado() {
		assert !testSyncManager.checkSiteId("mercury");
	}
	
	
	/**
	 *    TESTS DE DAO
	 */
	
	@Test
	public void testCreateTask() {
		assert testSyncManager.createTask() instanceof ITask;
	}
	
	@Test
	public void testCreateCriteria() {
		assert testSyncManager.createCriteria() instanceof ICriteria;
	}
	
	@Test
	public void testCreatePage() {
		assert testSyncManager.createPage() instanceof IPage;
	}	
		
	@Test
	public void testAddTaskVacia() throws Throwable {
		testSyncManager.addTask(testSyncManager.createTask());		
	}
	
	@Test(expectedExceptions={java.lang.Throwable.class})
	public void testAddCriteriaVacia() throws Throwable {
		testSyncManager.addCriteria(testSyncManager.createCriteria());
	}
	
	@Test(expectedExceptions={java.lang.Throwable.class})
	public void testAddPageVacia() throws Throwable {
		testSyncManager.addNewPage(testSyncManager.createPage());	
	}
	
	
	@Test
	public void testAddCriteriaCorrecta() throws Throwable {
		ICriteria correcta = testSyncManager.createCriteria();
		correcta.setName("criteriaOK");
		correcta.setProperty("term");
		correcta.setComparador(ISyncManager.COMPARATOR_EQUALS);
		correcta.setValor("2009");
		testSyncManager.addCriteria(correcta);
	}
	
	@Test
	public void testAddPageCorrecta() throws Throwable {
		IPage correcta = testSyncManager.createPage();
		correcta.setName("paginaPK");			
		testSyncManager.addNewPage(correcta);
	}
	
	@Test
	public void testGetTasks() throws Throwable {
		ITask correcta = testSyncManager.createTask();
		correcta.changeAvailable();
		correcta.setComments("tests");
		testSyncManager.addTask(correcta);
			
		for (ITask t : testSyncManager.getTasks())
		{
			if ("tests".equals(t.getComments()) 
				&& t.getAvailable()) {
					return;				
			}
		}
		assert false;
	}
	
	
	@Test
	public void testGetUpdatedTasks() throws Throwable {
		ITask correcta = testSyncManager.createTask();
		correcta.changeAvailable();
		correcta.setComments("tests");
		testSyncManager.addTask(correcta);
			
		for (ITask t : testSyncManager.getTasks())
		{
			if ("tests".equals(t.getComments()) 
				&& t.getAvailable()) {
					t.changeAvailable();
					testSyncManager.addTask(t);
					break;
			}
		}
		for (ITask t : testSyncManager.getTasks())
		{
			if ("tests".equals(t.getComments()) 
				&& !t.getAvailable()) {
					return;				
			}
		}
		assert false;
	}

	
	@Test
	public void testDelTask() throws Throwable {
		ITask correcta = testSyncManager.createTask();
		correcta.setSyncInto(false);
		correcta.changeAvailable();			
		testSyncManager.addTask(correcta);
		for (ITask t : testSyncManager.getTasks())
		{
			if ("tests".equals(t.getComments()) 
				&& t.getAvailable()) {
					testSyncManager.delTask(t.getId());
					break;
			}
		}
						
		for (ITask t : testSyncManager.getTasks())
		{
			if ("tests".equals(t.getComments()) 
					&& t.getAvailable()) {
				assert false;
				return;
			}
		}
	}
	
	@Test
	public void testGetCriteria() {
		ICriteria correcta = testSyncManager.createCriteria();
		correcta.setName("criteriaOK");
		correcta.setProperty("term");
		correcta.setComparador(ISyncManager.COMPARATOR_EQUALS);
		correcta.setValor("2009");
		long id = testSyncManager.addCriteria(correcta);
		for (ICriteria i : testSyncManager.getCriteria())
		{
			if (i.getId()==id) {
				if ("criteriaOK".equals(i.getName()) &&
						"term".equals(i.getProperty()) &&
						ISyncManager.COMPARATOR_EQUALS.equals(i.getComparador()) &&
						"2009".equals(i.getValor()))
				{
					return;
				}
				else {	
					assert false;
					return;
				}
			}
		}
			
		assert false;
	}
	
	@Test
	public void testDelCriteria() {
		ICriteria correcta = testSyncManager.createCriteria();
		correcta.setName("criteriaOK");
		correcta.setProperty("term");
		correcta.setComparador(ISyncManager.COMPARATOR_EQUALS);
		correcta.setValor("2009");
		long id = testSyncManager.addCriteria(correcta);
		testSyncManager.delCriteria(id);
		for (ICriteria i : testSyncManager.getCriteria())
		{
			if (i.getId()==id) {
				assert false;
				return;
			}
		}	
	}
	
	@Test
	public void testGetPages() {
		try {
			IPage correcta = testSyncManager.createPage();
			correcta.setName("paginaPK");			
			testSyncManager.addNewPage(correcta);
			for (IPage p: testSyncManager.getPages()) {
				if ("paginaPK".equals(p.getName())) {
					assert true;
					return;
				}
			}
		} catch (Throwable t) {			
			assert false;
		}		
		assert false;
	}
	
	@Test
	public void testDelPage() {
		try {
			IPage correcta = testSyncManager.createPage();
			correcta.setName("paginaPK");			
			testSyncManager.addNewPage(correcta);
			testSyncManager.delPage("paginaPK");
			for (IPage p: testSyncManager.getPages()) {
				if ("paginaPK".equals(p.getName())) {
					assert false;
					return;
				}
			}
		} catch (Throwable t) {			
			assert false;
			return;
		}		
		assert true;
	}
	
	@Test
	public void testAddTaskCompleta() throws Throwable {
		insertTestTask();
	}

	
	
	
	@Test
	public void testGetTaskLazyCorrect() throws Throwable {
		insertTestTask();
			
		for (ITask l : testSyncManager.getTasks())
		{			
			// couldn't simulate lazy load in tests
			ITask t = testSyncManager.getTaskAndRelatedPages(l.getId());
			
			if (t.getAvailable() && 
					"asignaturaGrado".equals(t.getTipo()) &&
					"!site.template.asignaturaGrado".equals(t.getRealmSite()) &&
					"!group.template.asignaturaGrado".equals(t.getRealmSection()) &&
					"~admin, existente".equals(t.getIgnoreSitesById()) &&
					sameCollection("sakai.resources, sakai.umusuma, sakai.syllabus", t.getToolsToAdd()) &&
					sameCollection("blogger, osp.assign, sakai.usermembership", t.getToolsToDel()) &&
					t.getPagesToAdd().size() == 1 &&
					"paginaPK".equals(t.getPagesToAdd().get(0).getName()) &&
					sameCollection("paginadel", t.getPagesToDel()) &&
					t.getCriteria().size() == 1 &&
					"criteriaOK".equals(t.getCriteria().get(0).getName()) &&
					"term".equals(t.getCriteria().get(0).getProperty()) &&
					ISyncManager.COMPARATOR_EQUALS.equals(t.getCriteria().get(0).getComparador()) &&
					"2009".equals(t.getCriteria().get(0).getValor())
				) {
						return;
				}
		}
		assert false;
	}

	@Test
	public void testCriteriaDependsOnTask() throws Throwable {
		long idcriteria = insertTestTask();
		long idtask = getTestTaskId();
		testSyncManager.delTask(idtask);
			
		for (ITask t : testSyncManager.getTasks())
		{
			if (t.getId()==idtask) {
				assert false;
				return;
			}
		}
			
		for (ICriteria c : testSyncManager.getCriteria())
		{
			if (c.getId()==idcriteria) {
				return;
			}
		}
			
		
		assert false;
	}
	
	
	//@Test
	public void testTaskDependsOnCriteria() throws Throwable {
		long idcriteria = insertTestTask();
		long idtask = getTestTaskId();
		testSyncManager.delCriteria(idcriteria);
			
		for (ICriteria c : testSyncManager.getCriteria())
		{
			if (c.getId()==idcriteria) {
				assert false;
				return;
			}
		}
			
		for (ITask t : testSyncManager.getTasks())
		{
			if (t.getId()==idtask) {
				return;
			}
		}
		assert false;
	}
	
	
	@Test
	public void testAddTaskWithOrphan() throws Throwable {
		insertTestTask();
		long idTask = getTestTaskId();
		ITask completaWithId = testSyncManager.getTaskAndRelatedPages(idTask);		
		IListString tta = null, ttd = null, ptd = null;
		for (IListString i : completaWithId.getToolsToAdd()) {
			tta = i;
		}
		for (IListString i : completaWithId.getToolsToDel()) {
			ttd = i;
		}
		for (IListString i : completaWithId.getPagesToDel()) {
			ptd = i;
		}
		completaWithId.delToolToAdd(tta);
		completaWithId.delToolToDel(ttd);
		completaWithId.delPageToDel(ptd);

		
		testSyncManager.addTask(completaWithId);
		
		ITask completa = testSyncManager.getTaskAndRelatedPages(idTask);		
		completa.addToolToAdd(tta.getString());
		completa.addToolToDel(ttd.getString());
		completa.addPageToDel(ptd.getString());
		testSyncManager.addTask(completa);
		
		boolean matches1=false;
		for (IListString i : completa.getToolsToAdd()) {
			if (i.getString().equals(tta.getString())) {
				if (!matches1) matches1=true;
				else assert false;
			}
		}
		boolean matches2=false;
		for (IListString i : completa.getToolsToDel()) {
			if (i.getString().equals(ttd.getString())) {
				if (!matches2) matches2=true;
				else assert false;
			}
		}
		boolean matches3=false;
		for (IListString i : completa.getPagesToDel()) {
			if (i.getString().equals(ptd.getString())) {
				if (!matches3) matches3=true;
				else assert false;
			}
		}
		assert (matches1 && matches2 && matches3);
	}
		
	@Test
	public void testAddPageWithOrphan() throws Throwable {
		IPage pagina = testSyncManager.createPage();
		pagina.setName("withOrphan");	
		pagina.setColumns("2");
		pagina.addToolInLeftColumn(0, "sakai.umusync");		
		pagina.addToolInRightColumn(0, "sakai.calendar");
		pagina.addToolInRightColumn(1, "sakai.chat");
		testSyncManager.addNewPage(pagina);
		
		IPage paginaFull = testSyncManager.getPage("withOrphan");
		paginaFull.delToolFromRightColumn(pagina.getRightColumn().get(1));
		paginaFull.delToolFromLeftColumn(pagina.getLeftColumn().get(0));
		testSyncManager.addUpdPage(paginaFull);
		
		IPage paginaModified = testSyncManager.getPage("withOrphan");
		assert (paginaModified.getLeftColumn().isEmpty());
		Assert.assertEquals( 1, paginaModified.getRightColumn().size());
		Assert.assertEquals( "sakai.calendar", paginaModified.getRightColumn().get(0).getString());
	}
	
	@Test(expectedExceptions={java.lang.Throwable.class})
	public void testAddTaskWithInvalidValues() throws Throwable {
		ICriteria condicion = testSyncManager.createCriteria();
		condicion.setName("criteriaOK");
		condicion.setProperty("term");
		condicion.setComparador(ISyncManager.COMPARATOR_EQUALS);
		condicion.setValor("2009");
		long idcriteria = testSyncManager.addCriteria(condicion);
		condicion.setId(idcriteria);
		List<ICriteria> filtro = new ArrayList<ICriteria>();
		filtro.add(condicion);
		
		IPage pagina = testSyncManager.createPage();
		pagina.setName("paginaPK");	
		testSyncManager.addNewPage(pagina);
		
		IPage paginadel = testSyncManager.createPage();
		paginadel.setName("paginadel");	
		testSyncManager.addNewPage(paginadel);		
		
		ITask completa = testSyncManager.createTask();
		completa.setSyncInto(false);
		completa.setSyncHome("sakai.properties");
		completa.changeAvailable();
		completa.setTipo("asignaturaGradoasignaturaGradoasignaturaGrado");
		completa.setRealmSite("!site.template.asignaturaGrado");
		completa.setRealmSection("!group.template.asignaturaGrado");
		completa.setIgnoreSitesById("~admin, existente");
		completa.addToolToAdd("sakai.resources");
		completa.addToolToAdd("sakai.umusuma");
		completa.addToolToAdd("sakai.syllabus");
		completa.addToolToDel("blogger");
		completa.addToolToDel("osp.assign");
		completa.addToolToDel("sakai.usermembership");
		completa.addPageToAdd(pagina);
		completa.addPageToDel("paginadel");
		completa.setCriteria(filtro);					
		
		testSyncManager.addTask(completa);
	}
	
	@Test(expectedExceptions={java.lang.Throwable.class})
	public void testAddPageWithColumInvalidValue() throws Throwable {
		IPage pagina = testSyncManager.createPage();
		pagina.setName("paginaPK");	
		pagina.setColumns("100");
		testSyncManager.addNewPage(pagina);		
	}
	
	@Test(expectedExceptions={java.lang.Throwable.class})
	public void testUpdPageWithInvalidValue() throws Throwable {
		IPage pagina = testSyncManager.createPage();
		pagina.setName("pageOK");
		testSyncManager.addNewPage(pagina);
		pagina = testSyncManager.getPage("pageOK");
		pagina.setColumns("100");
		testSyncManager.addUpdPage(pagina);
	}
	
	@Test(expectedExceptions={java.lang.Throwable.class})
	public void testAddPageWithNameInvalidValue() throws Throwable {
		IPage pagina = testSyncManager.createPage();
		pagina.setName("overflowoverflowoverflowoverflowoverflowoverflowoverflowoverflow");
		testSyncManager.addNewPage(pagina);		
	}
	
	@Test
	public void testDelTaskInexistente() throws Exception {
		long idTaskInexistente = 1;
		try {
			while (true) {
				testSyncManager.getTask(idTaskInexistente);
				idTaskInexistente++;
			}
		} catch (EntityNotFoundException enfe) {
			testSyncManager.delTask(idTaskInexistente);
		}
		
	}

	
	@Test
	public void testDelCriteriaInexistente() throws Exception {
		boolean continuar = true;
		long idInexistente = 1;
		while (continuar) {
			continuar = false;
			for (ICriteria i : testSyncManager.getCriteria()) {
				if (i.getId()==idInexistente) {
					idInexistente++;
					continuar = true;
					break;
				}
			}
		}
		testSyncManager.delCriteria(idInexistente);
	}

	@Test
	public void testDelPageInexistente() throws Exception {
		String nombre = "test";
		try {
			while (true) {
				log.debug("nombre: "+nombre);
				testSyncManager.getPage(nombre);
				nombre = new String (nombre+this.hashCode());
				if (nombre.length()>50) nombre=nombre.substring(nombre.length()-50, nombre.length());
			}
		} catch (EntityNotFoundException enfe) {			
			testSyncManager.delPage(nombre);
		}
		
	}
	

	/**
	 *  TEST DE JOB
	 * @throws Throwable 
	 */
	private void disableAllTask() throws Throwable {	
		for (ITask t:testSyncManager.getTasks()) {
			if (t.getAvailable()) {
				t.changeAvailable();
				testSyncManager.addTask(t);
			}
		}
	}
	
	
	@Test
	public void testJobName() throws Exception {
		Assert.assertEquals("SyncSites Job", testSyncManager.jobName());
	}
	
	@Test
	public void testJobWithoutTasks() throws Throwable {
		disableAllTask();		
		testSyncManager.doit();		
	}

	@Test
	public void testJobWithTasks() throws Throwable {
		disableAllTask();
		this.insertTestTask();
		testSyncManager.doit();
	}

	/**
	 * TESTS SECURE COMPONENT (used in tool)
	 */
	
	@Test
	public void testSecureComponentInitialized() {
		Assert.assertNotNull(testSecureSyncManager);
	}
	
	@Test (expectedExceptions = {java.lang.SecurityException.class})
	public void testSecureComponentUnauthorized() {
		testSecureSyncManager.getTasks();
	}
	
	@Test (expectedExceptions = {java.lang.SecurityException.class})
	public void testSecureComponentJobUnauthorized() throws Throwable {
		testSecureSyncManager.doit();
	}
	
	/**
	 *  TESTS DE GETS FROM SERVICES
	 */
	
	
	@Test
	public void testGetTools() {
		Collection<String> tools = testSyncManager.getTools();
		assert (tools.size()==6 &&
				tools.contains("sakai.resources") &&
				tools.contains("sakai.umusuma") &&
				tools.contains("sakai.syllabus") &&
				tools.contains("blogger") &&
				tools.contains("osp.assign") &&
				tools.contains("sakai.usermembership"));
	}
	
	@Test
	public void testGetSiteTypes() {
		Collection<String> tipos = testSyncManager.getSiteTypes();
		assert (tipos.size() == 12 && tipos.contains("asignaturaGrado")
				&& tipos.contains("asignaturaGrado+")
				&& tipos.contains("asignaturaMaster")
				&& tipos.contains("ayuda") && tipos.contains("bienvenida")
				&& tipos.contains("grado") && tipos.contains("master")
				&& tipos.contains("myWorkSpaceExternoUM")
				&& tipos.contains("myWorkSpaceOficialUM")
				&& tipos.contains("portfolio")
				&& tipos.contains("portfolioAdmin") && tipos
				.contains("project"));
	}
	
	@Test
	public void testGetSiteRealms() {
		Collection<String> realms = testSyncManager.getSiteRealms();
		assert (realms.size() == 11
				&& realms.contains("!site.template")
				&& realms.contains("!site.template.asignaturaGrado")
				&& realms.contains("!site.template.asignaturaGrado+") 
				&& realms.contains("!site.template.asignaturaMaster")
				&& realms.contains("!site.template.ayuda")
				&& realms.contains("!site.template.bienvenida")
				&& realms.contains("!site.template.course")
				&& realms.contains("!site.template.grado")
				&& realms.contains("!site.template.master") 
				&& realms.contains("!site.template.portfolio") 
				&& realms.contains("!site.template.portfolioAdmin"));
	}
	
	
	@Test
	public void testGetSectionRealms() {
		Collection<String> realms = testSyncManager.getSectionRealms();
		assert (realms.size() == 8 
				&& realms.contains("!group.template")
				&& realms.contains("!group.template.asignaturaGrado")
				&& realms.contains("!group.template.asignaturaGrado+") 
				&& realms.contains("!group.template.asignaturaMaster")
				&& realms.contains("!group.template.course")
				&& realms.contains("!group.template.grado")
				&& realms.contains("!group.template.master") 
				&& realms.contains("!group.template.portfolio"));
	}

	@Test
	public void testGetRegisterdFunctions() {
		Collection<String> listaFunciones = testSyncManager.getRegisteredFunctions();
		Assert.assertEquals(11, listaFunciones.size());
		assert (listaFunciones.contains("umusync.VIEW") && 
				listaFunciones.contains("annc.new") && 
				listaFunciones.contains("annc.read") && 
				listaFunciones.contains("calendar.new") && 
				listaFunciones.contains("calendar.read") && 
				listaFunciones.contains("chat.new") && 
				listaFunciones.contains("chat.read") && 
				listaFunciones.contains("content.new") && 
				listaFunciones.contains("content.read") && 
				listaFunciones.contains("mail.new") && 
				listaFunciones.contains("mail.read")); 
	}	


	/**
	 *  TEST DE EJECUCION DE TASK
	 */
	@Test
	public void testExecuteTestTask() throws Throwable {
		insertTestTask();
		ITask t = testSyncManager.getTaskAndRelatedPages(getTestTaskId());
		Assert.assertEquals("0/1", testSyncManager.syncSites(t));
	}
	
	@Test
	public void testExecuteTaskDefaultUser() throws Throwable {
		ITask defaultUser = testSyncManager.createTask();
		defaultUser.setSyncHome(ITask.NOT_CHANGE_HOME_PAGE);
		defaultUser.setTipo(ITask.DEFAULT_USER_SITE);
		defaultUser.setComments("testExecuteTaskDefaultUser");
		
		testSyncManager.addTask(defaultUser);
		
		long idTask = 0;
		for (ITask t:testSyncManager.getTasks()) {
			if (!t.getAvailable() && 
					"testExecuteTaskDefaultUser".equals(t.getComments()) &&
					ITask.DEFAULT_USER_SITE.equals(t.getTipo())) {
				idTask = t.getId();
				break;
			}
		}
		
		defaultUser = testSyncManager.getTaskAndRelatedPages(idTask);
		Assert.assertEquals("0/1", testSyncManager.syncSites(defaultUser));
	}

	@Test
	public void testExecuteTaskAllUsers() throws Throwable {
		ITask defaultUser = testSyncManager.createTask();
		defaultUser.setSyncHome(ITask.NOT_CHANGE_HOME_PAGE);
		defaultUser.setTipo(ITask.ALL_USER_SITE);
		defaultUser.setComments("testExecuteTaskAllUsers");
		
		testSyncManager.addTask(defaultUser);
		
		long idTask = 0;
		for (ITask t:testSyncManager.getTasks()) {
			if (!t.getAvailable() && 
					"testExecuteTaskAllUsers".equals(t.getComments()) &&
					ITask.ALL_USER_SITE.equals(t.getTipo())) {
				idTask = t.getId();
				break;
			}
		}
		
		defaultUser = testSyncManager.getTaskAndRelatedPages(idTask);
		Assert.assertEquals("0/2", testSyncManager.syncSites(defaultUser));
	}
	
	@Test
	public void testExecuteTaskAllUsersIgnoreById() throws Throwable {
		ITask defaultUser = testSyncManager.createTask();
		defaultUser.setSyncHome(ITask.NOT_CHANGE_HOME_PAGE);
		defaultUser.setTipo(ITask.ALL_USER_SITE);
		defaultUser.setIgnoreSitesById("allUserSite");
		defaultUser.setComments("testExecuteTaskAllUsersIgnoreById");
		
		testSyncManager.addTask(defaultUser);
		
		long idTask = 0;
		for (ITask t:testSyncManager.getTasks()) {
			if (!t.getAvailable() && 
					"testExecuteTaskAllUsersIgnoreById".equals(t.getComments()) &&
					"allUserSite".equals(t.getIgnoreSitesById()) &&
					ITask.ALL_USER_SITE.equals(t.getTipo())) {
				idTask = t.getId();
				break;
			}
		}
		
		defaultUser = testSyncManager.getTaskAndRelatedPages(idTask);
		Assert.assertEquals("0/1", testSyncManager.syncSites(defaultUser));
	}
	
	@Test
	public void testExecuteTaskNotEquals() throws Throwable {
		
		ICriteria condicion = testSyncManager.createCriteria();
		condicion.setName("criteriaX");
		condicion.setProperty("term");
		condicion.setComparador(ISyncManager.COMPARATOR_NOT_EQUALS);
		condicion.setValor("2009");
		long idcriteria = testSyncManager.addCriteria(condicion);
		condicion.setId(idcriteria);
		List<ICriteria> filtro = new ArrayList<ICriteria>();
		filtro.add(condicion);
		
		ITask defaultUser = testSyncManager.createTask();
		defaultUser.setSyncHome(ITask.NOT_CHANGE_HOME_PAGE);
		defaultUser.setTipo(null);
		defaultUser.changeAvailable();
		defaultUser.setComments("testExecuteTaskNotEquals");
		
		testSyncManager.addTask(defaultUser);
		
		long idTask = 0;
		for (ITask t:testSyncManager.getTasks()) {
			if (t.getAvailable() && t.getTipo()==null 
					&& t.getComments().equals("testExecuteTaskNotEquals")) {
				idTask = t.getId();
				break;
			}
		}
		
		defaultUser = testSyncManager.getTaskAndRelatedPages(idTask);
		Assert.assertEquals("0/3", testSyncManager.syncSites(defaultUser));
	}
	
	@Test
	public void testExecuteTaskExistsAndMatches() throws Throwable {
		
		ICriteria condicion1 = testSyncManager.createCriteria();
		condicion1.setName("criteriaX");
		condicion1.setProperty("term");
		condicion1.setComparador(ISyncManager.COMPARATOR_NOT_EXISTS);
		condicion1.setId(testSyncManager.addCriteria(condicion1));
		ICriteria condicion2 = testSyncManager.createCriteria();
		condicion2.setName("criteriaX");
		condicion2.setProperty("id");
		condicion2.setComparador(ISyncManager.COMPARATOR_EXISTS);
		condicion2.setId(testSyncManager.addCriteria(condicion2));
		ICriteria condicion3 = testSyncManager.createCriteria();
		condicion3.setName("criteriaX");
		condicion3.setProperty("id");
		condicion3.setComparador(ISyncManager.COMPARATOR_MATCHES);
		condicion3.setValor(".*User.*");
		condicion3.setId(testSyncManager.addCriteria(condicion3));
		ICriteria condicion4 = testSyncManager.createCriteria();
		condicion4.setName("criteriaX");
		condicion4.setProperty("numerito");
		condicion4.setComparador(ISyncManager.COMPARATOR_GRATHER_THAN);
		condicion4.setValor("5");
		condicion4.setId(testSyncManager.addCriteria(condicion4));
		ICriteria condicion5 = testSyncManager.createCriteria();
		condicion5.setName("criteriaX");
		condicion5.setProperty("numerito");
		condicion5.setComparador(ISyncManager.COMPARATOR_LESS_THAN);
		condicion5.setValor("7");
		condicion5.setId(testSyncManager.addCriteria(condicion5));
		List<ICriteria> filtro = new ArrayList<ICriteria>();
		filtro.add(condicion1);
		filtro.add(condicion2);
		filtro.add(condicion3);
		filtro.add(condicion4);
		filtro.add(condicion5);

		
		ITask defaultUser = testSyncManager.createTask();
		defaultUser.setSyncHome(ITask.NOT_CHANGE_HOME_PAGE);
		defaultUser.setIgnoreFunctionsMode(ITask.PROPERTIES_FUNCTION_MODE);
		defaultUser.setTipo(null);
		defaultUser.changeAvailable();
		defaultUser.setComments("testExecuteTaskExistsAndMatches");
		defaultUser.setCriteria(filtro);
		
		testSyncManager.addTask(defaultUser);
		
		long idTask = 0;
		for (ITask t:testSyncManager.getTasks()) {
			if (t.getAvailable() && t.getTipo()==null 
					&& t.getComments().equals("testExecuteTaskExistsAndMatches")) {
				idTask = t.getId();
				break;
			}
		}
		
		defaultUser = testSyncManager.getTaskAndRelatedPages(idTask);
		Assert.assertEquals("0/1", testSyncManager.syncSites(defaultUser));
	}

	@Test (expectedExceptions = {java.util.regex.PatternSyntaxException.class})
	public void testExecuteTaskBadRegex() throws Throwable {
		
		ICriteria condicion = testSyncManager.createCriteria();
		condicion.setName("criteriaX");
		condicion.setProperty("term");
		condicion.setComparador(ISyncManager.COMPARATOR_MATCHES);
		condicion.setValor("*.bad");
		condicion.setId(testSyncManager.addCriteria(condicion));

		List<ICriteria> filtro = new ArrayList<ICriteria>();
		filtro.add(condicion);
		
		ITask defaultUser = testSyncManager.createTask();
		defaultUser.setSyncHome(ITask.NOT_CHANGE_HOME_PAGE);
		defaultUser.setTipo(null);
		defaultUser.changeAvailable();
		defaultUser.setComments("testExecuteTaskBadRegex");
		defaultUser.setCriteria(filtro);
		
		testSyncManager.addTask(defaultUser);
		
		long idTask = 0;
		for (ITask t:testSyncManager.getTasks()) {
			if (t.getAvailable() && t.getTipo()==null 
					&& t.getComments().equals("testExecuteTaskBadRegex")) {
				idTask = t.getId();
				break;
			}
		}
		
		defaultUser = testSyncManager.getTaskAndRelatedPages(idTask);
		testSyncManager.syncSites(defaultUser);
	}
	
	@Test
	public void testExecuteTestTaskAndModify() throws Throwable {
		insertTestTask();
		long id = getTestTaskId();
		ITask t = testSyncManager.getTaskAndRelatedPages(id);
		IPage pToAdd = testSyncManager.createPage();
		pToAdd.setName("pToAdd");
		pToAdd.setColumns("1");
		pToAdd.addToolInLeftColumn(0, "una");
		pToAdd.addToolInLeftColumn(1, "dos");
		testSyncManager.addNewPage(pToAdd);
		t.addPageToAdd(pToAdd);
		testSyncManager.addTask(t);
		Assert.assertEquals("1/1", testSyncManager.syncSites(testSyncManager.getTaskAndRelatedPages(id)));
	}
	
	
	@Test
	public void testExecuteTestTaskAndModifyOneRemovedSite() throws Throwable {
		insertTestTask();
		long id = getTestTaskId();
		ITask t = testSyncManager.getTaskAndRelatedPages(id);
		IPage pToAdd = testSyncManager.createPage();
		pToAdd.setName("pToAdd");
		pToAdd.setColumns("1");
		pToAdd.addToolInLeftColumn(0, "una");
		pToAdd.addToolInLeftColumn(1, "dos");		
		testSyncManager.addNewPage(pToAdd);
		t.addPageToAdd(pToAdd);
		t.setTipo("removedSite");
		testSyncManager.addTask(t);
		Assert.assertEquals("0/1", testSyncManager.syncSites(testSyncManager.getTaskAndRelatedPages(id)));
	}
	
	
	@Test
	public void testExecuteTestTaskAndModifyWithoutAdminRole() throws Throwable {
		insertTestTask();
		long id = getTestTaskId();
		ITask t = testSyncManager.getTaskAndRelatedPages(id);
		IPage pToAdd = testSyncManager.createPage();
		pToAdd.setName("pToAdd");
		pToAdd.setColumns("1");
		pToAdd.addToolInLeftColumn(0, "una");
		pToAdd.addToolInLeftColumn(1, "dos");
		testSyncManager.addNewPage(pToAdd);
		t.addPageToAdd(pToAdd);
		t.setTipo("withoutAdminRole");
		testSyncManager.addTask(t);
		Assert.assertEquals("0/1", testSyncManager.syncSites(testSyncManager.getTaskAndRelatedPages(id)));
	}
	
	@Test
	public void testExecuteTaskMoreConditionals() throws Throwable {
		
		ICriteria condicion1 = testSyncManager.createCriteria();
		condicion1.setName("criteriaX");
		condicion1.setProperty("invent");
		condicion1.setComparador(ISyncManager.COMPARATOR_NOT_EQUALS);
		condicion1.setValor("invent");
		condicion1.setId(testSyncManager.addCriteria(condicion1));
		ICriteria condicion2 = testSyncManager.createCriteria();
		condicion2.setName("criteriaX");
		condicion2.setProperty("numerito");
		condicion2.setComparador(ISyncManager.COMPARATOR_LESS_THAN);
		condicion2.setValor("10");
		condicion2.setId(testSyncManager.addCriteria(condicion2));
		ICriteria condicion3 = testSyncManager.createCriteria();
		condicion3.setName("criteriaX");
		condicion3.setProperty("id");
		condicion3.setComparador(ISyncManager.COMPARATOR_MATCHES);
		condicion3.setValor("a.*");
		condicion3.setId(testSyncManager.addCriteria(condicion3));
		ICriteria condicion4 = testSyncManager.createCriteria();
		condicion4.setName("criteriaX");
		condicion4.setProperty("numerito");
		condicion4.setComparador(ISyncManager.COMPARATOR_NOT_EQUALS);
		condicion4.setValor("9");
		condicion4.setId(testSyncManager.addCriteria(condicion4));
		ICriteria condicion5 = testSyncManager.createCriteria();
		condicion5.setName("criteriaX");
		condicion5.setProperty("numerito");
		condicion5.setComparador(ISyncManager.COMPARATOR_GRATHER_THAN);
		condicion5.setValor("9");
		condicion5.setId(testSyncManager.addCriteria(condicion5));
		List<ICriteria> filtro = new ArrayList<ICriteria>();
		filtro.add(condicion1);
		filtro.add(condicion2);
		filtro.add(condicion3);
		filtro.add(condicion4);
		filtro.add(condicion5);

		
		ITask defaultUser = testSyncManager.createTask();
		defaultUser.setSyncHome(ITask.NOT_CHANGE_HOME_PAGE);
		defaultUser.setIgnoreFunctionsMode(ITask.PROPERTIES_FUNCTION_MODE);
		defaultUser.setTipo(null);
		defaultUser.changeAvailable();
		defaultUser.setComments("testExecuteTaskMoreConditionals");
		defaultUser.setCriteria(filtro);
		
		testSyncManager.addTask(defaultUser);
		
		long idTask = 0;
		for (ITask t:testSyncManager.getTasks()) {
			if (t.getAvailable() && t.getTipo()==null 
					&& t.getComments().equals("testExecuteTaskMoreConditionals")) {
				idTask = t.getId();
				break;
			}
		}
		
		defaultUser = testSyncManager.getTaskAndRelatedPages(idTask);
		Assert.assertEquals("0/0", testSyncManager.syncSites(defaultUser));
	}
	
	/* 
	 * TESTS SINCRONIZANDO PERMISOS 
	 */
	@Test
	public void testRemoveRole() throws Throwable {
		insertTestTask();
		ITask t = testSyncManager.getTaskAndRelatedPages(getTestTaskId());
		t.setTipo("permissionTest");
		t.setRealmSite("!site.template.soloRolDos");
		testSyncManager.addTask(t);
		Assert.assertEquals("1/2", testSyncManager.syncSites(t));
	}

	@Test
	public void testAddNewRole() throws Throwable {
		insertTestTask();
		ITask t = testSyncManager.getTaskAndRelatedPages(getTestTaskId());
		t.setTipo("permissionTest");
		t.setRealmSite("!site.template.dosRoles");
		t.setRealmSection(null);
		testSyncManager.addTask(t);
		//assertEquals("1/2", testSyncManager.syncSites(t)); equals cannot stub
		Assert.assertEquals("2/2", testSyncManager.syncSites(t));
	}
	
	
	// ITask.CUSTOMLIST_FUNCTION_MODE
	
	

	/*
	 *  TEST SINCRONIZANDO HERRAMIENTAS 
	 */
	@Test
	public void testNoAddingTool() throws Throwable {
		ITask task = testSyncManager.createTask();
		task.setSyncInto(false);
		task.setSyncHome(ITask.NOT_CHANGE_HOME_PAGE);
		task.setTipo("permissionTest");
		task.setRealmSite(null);
		task.setRealmSection(null);
		task.addToolToAdd("sakai.umusync");
		testSyncManager.addTask(task);
		Assert.assertEquals("0/2", testSyncManager.syncSites(task));
	}
	
	/*
	@Test
	public void testAddingTool() throws Throwable {		
		ITask task = testSyncManager.createTask();
		task.setSyncInto(false);
		task.setSyncHome(ITask.NOT_CHANGE_HOME_PAGE);
		task.setTipo("permissionTest");
		task.setRealmSite(null);
		task.setRealmSection(null);
		task.addToolToAdd("sakai.umusync2");
		testSyncManager.addTask(task);
		Assert.assertEquals("2/2", testSyncManager.syncSites(task));
	}*/

}

