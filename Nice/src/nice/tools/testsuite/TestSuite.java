package nice.tools.testsuite;


import java.io.*;
import java.util.*;



/**
 * Class represents a testsuite file with its testcases.
 * 
 * @author	Alex Greif <a href="mailto:alex.greif@web.de">alex.greif@web.de</a>
 * @version	$Id$
 */
public class TestSuite {

	/**
	 * TODO
	 * 
	 */
	private final String TESTCASE_TYPE_PASS = "pass";

	/**
	 * TODO
	 * 
	 */
	private final String TESTCASE_TYPE_FAIL = "fail";

	/**
	 * TODO
	 * 
	 */
	private final String KEYWORD_GLOBAL = "global";


	/**
		The testsuite file on the disc
	*/
	private File _file;
	private List _testCases = new ArrayList();
	
	/**
	 * Nice source file that contains the gathered global sources.
	 * These sources are available for all testcases.
	 * 
	 */
	private GlobalSourceFile _globalSource = new GlobalSourceFile();



	/**
		Constructor. Reads the testcases and performs the tests in the testsuite.
	*/
	public TestSuite(File testSuiteFile) throws TestSuiteException {
		_file = testSuiteFile;			
		
		TestNice.getOutput().startTestSuite(this);
		readTestCases();
		performTests();
		TestNice.getOutput().endTestSuite();
	}


	/**
		Reads the testsuite file line by line and creates the appropriate testcases.
	*/
	private void readTestCases() throws TestSuiteException {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(_file));
			String line;
			TestCase testCase = null;
			boolean isGlobalSource = false;
			while((line = reader.readLine()) != null) {
				if (line.trim().length() == 0)	//	ignore empty lines
					continue;

				if (line.startsWith(TestNice.KEYWORD_SIGN)) {
					if (line.toLowerCase().indexOf(KEYWORD_GLOBAL) != -1)
						isGlobalSource = true;
					else isGlobalSource = false;
				}
				
				if (isGlobalSource) {
					getGlobalSource().consumeLine(line);
					continue;
				}
				
				if (line.startsWith(TestNice.KEYWORD_SIGN)) {
					testCase = createTestCase(line);
					_testCases.add(testCase);
					continue;
				}
					
				testCase.consumeLine(line);
			}
		} catch(IOException e) {
			throw new TestSuiteException("Exception while reading file: " + _file, e);
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch(IOException e) {
					throw new TestSuiteException("could not close file: " + _file);
				}
		}
	}


	/**
		Creates and returns a specific TestCase object depending upon
		the keyword.
	*/
	private TestCase createTestCase(String line) throws TestSuiteException {
		String type = line.substring(TestNice.KEYWORD_SIGN.length()).trim();
		
		if (TESTCASE_TYPE_PASS.equalsIgnoreCase(type))
			return new PassTestCase(this);
		else if (TESTCASE_TYPE_FAIL.equalsIgnoreCase(type))
			return new FailTestCase(this);
		throw new TestSuiteException("Unknown testcase type: " + type);
	}


	/**
		Performs tests on all testcases in this testsuite.
		Writes the files of the testcase, and calls the specific 
		TestCase class to perform the test.
	*/
	private void performTests() throws TestSuiteException {
		for(Iterator iter = _testCases.iterator(); iter.hasNext(); ) {
			TestNice.cleanupTempFolder();
			TestCase testCase = (TestCase)iter.next();
			testCase.writeFiles();
			testCase.performTest();
		}
	}

	/**
	 * Returns the testsuite file.
	 */
	public File getFile() {
		return _file;
	}

	/**
	 * Returns the global source file.
	 * 
	 */
	GlobalSourceFile getGlobalSource() {
		return _globalSource;
	}


	/**
	 * Returns whether we have a global source file.
	 * 
	 */
	boolean hasGlobalSource() {
		return ! _globalSource.isEmpty();
	}






}


