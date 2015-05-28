package umu.sakai.umutests;

import org.springframework.test.AbstractTransactionalSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class UMUTest extends AbstractTransactionalSpringContextTests {

	// The following two methods are for converting this into a TestNG class
	
	@BeforeMethod
	protected final void nGSetup() throws Exception {
		this.setPopulateProtectedVariables(true);
		super.setUp();
	}

	@AfterMethod
	protected final void nGTearDown() throws Exception {
		super.tearDown();
	}
	
}
