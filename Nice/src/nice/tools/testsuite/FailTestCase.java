package nice.tools.testsuite;



/**
 * TestCase class fot the case that the test should fail
 * 
 * @author	Alex Greif <a href="mailto:alex.greif@web.de">alex.greif@web.de</a>
 * @version	$Id$
 */
public class FailTestCase extends TestCase {

	/**
	 * Constructor.
	 * 
	 * @param	suite	The testsuite this testcase belongs to
	 */
	public FailTestCase(TestSuite suite) {
		super(suite);
	}


	/**
		Performs the test for this testcase.
		Compilation should fail, otherwise throw exception.
	*/
	public void performTest() {
		super.performTest();
		try {
			compilePackages();
		} catch(TestSuiteException e) {
			pass();
			return;
		} catch(CompilerBugException e) {
			fail();
			return;
		}
		
		TestNice.getOutput().log("Compilation was expected to fail, but it succeeded.");
		fail();
	}

}

