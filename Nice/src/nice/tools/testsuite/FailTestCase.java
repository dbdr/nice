package nice.tools.testsuite;



/**
 * TestCase class fot the case that the test should fail
 * 
 * @author	Alex Greif <a href="mailto:alex.greif@web.de">alex.greif@web.de</a>
 * @version	$Id$
 */
public class FailTestCase extends TestCase {

	/**
		Performs the test for this testcase.
		Compilation should fail, otherwise throw exception.
	*/
	public void performTest() throws TestSuiteException {
		super.performTest();
		try {
			compilePackages();
		} catch(TestSuiteException e) {
			pass();
			return;
		}
		
		fail();
		throw new TestSuiteException("Compilation was expected to fail, but it succeeded.");
	}

}

