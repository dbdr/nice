package nice.tools.testsuite;


/**
 * TestCase class fot the case that the test should pass
 * 
 * @author	Alex Greif <a href="mailto:alex.greif@web.de">alex.greif@web.de</a>
 * @version	$Id$
 */
public class PassTestCase extends TestCase {

	/**
		Performs the test for this testcase.
		Compilation and the execution of the main() method
		should be successfully.
	*/
	public void performTest() throws TestSuiteException {
		super.performTest();
		try {
			compilePackages();
			runMain();
		} catch(TestSuiteException e) {
			fail();
			//e.printStackTrace();
			return;
		}
		
		pass();
	}

}

