package nice.tools.testsuite;


/**
 * TestCase class fot the case that the test should pass
 * 
 */
public class PassTestCase extends TestCase {

	/**
		Performs the test for this testcase.
		Compilation and the execution of the main() method
		should be successfully.
	*/
	public void performTest() throws TestSuiteException {
		compilePackages();
		runMain();
	}

}

