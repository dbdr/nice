package nice.tools.testsuite;


import java.io.*;
import java.util.*;



/**
 * Class represents a testsuite file with its testcases.
 * 
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



	private File _testSuiteFile;
	private List _testCases = new ArrayList();

	/**
		Constructor.
	*/
	public TestSuite(File testSuiteFile) throws TestSuiteException {
		_testSuiteFile = testSuiteFile;
		if (! _testSuiteFile.exists())
			throw new TestSuiteException("TestSuite file not found. File: " + _testSuiteFile);
			
		System.out.println("testsuite " + _testSuiteFile);
		readTestCases();
		performTests();
	}


	/**
		Reads the testsuite file line by line and creates the appropriate testcases.
	*/
	private void readTestCases() throws TestSuiteException {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(_testSuiteFile));
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
					TestNice.getGlobalSource().consumeLine(line);
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
			throw new TestSuiteException("Exception while reading file: " + _testSuiteFile, e);
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch(IOException e) {
					throw new TestSuiteException("could not close file: " + _testSuiteFile);
				}
		}
	}


	/**
		Creates and returns a specific TestCase object depending upon
		the keyword
	*/
	private TestCase createTestCase(String line) throws TestSuiteException {
		String type = line.substring(TestNice.KEYWORD_SIGN.length()).trim();
		//System.out.println("testcase type: " + type);
		
		if (TESTCASE_TYPE_PASS.equalsIgnoreCase(type))
			return new PassTestCase();
		else if (TESTCASE_TYPE_FAIL.equalsIgnoreCase(type))
			return new FailTestCase();
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

}


