package umu.sakai.umusync.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockito.Mockito;

public class SakaiMockService {

	private static Log log = LogFactory.getLog(SakaiMockService.class);
	
	public static Object createMock(String className) {
		try {
			Class c = Class.forName(className);
			return Mockito.mock(c);
		} catch (ClassNotFoundException cnfe) {
			log.error(cnfe);
		}
		return null;
	}
	
}
