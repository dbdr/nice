package nice.tools.testsuite;



/**
 * TestCase class fot the case that the test should fail
 * 
 */
public class FailTestCase extends TestCase {

	/**
		Performs the test for this testcase.
		Compilation should fail, otherwise throw exception.
	*/
	public void performTest() throws TestSuiteException {
		try {
			compilePackages();
		} catch(TestSuiteException e) {
			TestNice.increaseSucceeded();
			return;
		}
		
		TestNice.increaseFailed();
		throw new TestSuiteException("Compilation was expected to fail, but it succeeded.");
	}

}

