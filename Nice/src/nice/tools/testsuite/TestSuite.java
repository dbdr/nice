/**************************************************************************/
/*                            NICE Testsuite                              */
/*             A testsuite for the Nice programming language              */
/*                         (c) Alex Greif 2002                            */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package nice.tools.testsuite;


import java.io.*;
import java.util.*;



/**
 * A testsuite file with its testcases.
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
	 * TODO
	 * 
	 */
	private final String KEYWORD_COMMENT = "comment";


	/**
	 * The testsuite file on the disc
	 * 
	 */
	private File _file;
	/**
	 * TODO
	 * 
	 */
	private List _testCases = new ArrayList();
	
	/**
	 * Nice source file that contains the gathered global sources.
	 * These sources are available for all testcases.
	 * 
	 */
	private GlobalSourceFile _globalSource = new GlobalSourceFile();



	/**
	 * Constructor. Reads the testcases and performs the tests in the testsuite.
	 * 
	 * @param	testSuiteFile	TODO
	 * @exception	TestSuiteException	TODO
	 */
	public TestSuite(File testSuiteFile) throws TestSuiteException {
		_file = testSuiteFile;			
		
		TestNice.getOutput().startTestSuite(this);
		readTestCases();
		performTests();
		TestNice.getOutput().endTestSuite();
	}


	/**
	 * Reads the testsuite file line by line and creates the appropriate testcases.
	 * Also handles globals and comments
	 * 
	 * @exception	TestSuiteException	TODO
	 */
	private void readTestCases() throws TestSuiteException {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(_file));
			String line;
			TestCase testCase = null;
			boolean isGlobalSource = false;
			boolean testsuiteKeywordLine;
			while((line = reader.readLine()) != null) {
				if (line.trim().length() == 0)	//	ignore empty lines
					continue;

				testsuiteKeywordLine = line.startsWith(TestNice.KEYWORD_SIGN);
				if (testsuiteKeywordLine)
					isGlobalSource = line.toLowerCase().indexOf(KEYWORD_GLOBAL) != -1;

				if (isGlobalSource) {
					getGlobalSource().consumeLine(line);
					continue;
				}

				if (testsuiteKeywordLine  &&  consumeComment(line))
					continue;
				
				if (testsuiteKeywordLine) {
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
	 * Decides whether the line is a comment or not, and if it is one
	 * then the comment is printed if desired.
	 * 
	 */
	boolean consumeComment(String line) {
		int commentPos = line.toLowerCase().indexOf(KEYWORD_COMMENT);
		if (commentPos == -1)
			return false;
		if (TestNice.getWriteComments()) {
			String comment = line.substring(commentPos + KEYWORD_COMMENT.length()).trim();
			TestNice.getOutput().logAndFlush(KEYWORD_COMMENT, comment);
		}
		
		return true;
	}


	/**
	 * Creates and returns a specific TestCase object depending upon
	 * the keyword.
	 * 
	 * @param	line	TODO
	 * @exception	TestSuiteException	TODO
	 */
	private TestCase createTestCase(String line) throws TestSuiteException {
		String type = line.substring(TestNice.KEYWORD_SIGN.length()).trim();

		boolean skip = false;
		if (type.endsWith(" skip"))
      {
        skip = true;
				type = type.substring(0, type.length() - "skip".length()).trim();
      }

		boolean isKnownBug = false;
		if (type.endsWith(" bug"))
			{
				isKnownBug = true;
				type = type.substring(0, type.length() - "bug".length()).trim();
			}

		TestCase res;
		if (TESTCASE_TYPE_PASS.equalsIgnoreCase(type))
			res = new PassTestCase(this);
		else if (TESTCASE_TYPE_FAIL.equalsIgnoreCase(type))
			res = new FailTestCase(this);
		else
			throw new TestSuiteException("Unknown testcase type: " + type);

		res.skip = skip;
		res.isKnownBug = isKnownBug;
		return res;
	}


	/**
	 * Performs tests on all testcases in this testsuite.
	 * Writes the files of the testcase, and calls the specific 
	 * TestCase class to perform the test.
	 * 
	 * @exception	TestSuiteException	TODO
	 */
	private void performTests() throws TestSuiteException {
		for(Iterator iter = _testCases.iterator(); iter.hasNext(); ) {
			TestNice.cleanupTempFolder();
			TestCase testCase = (TestCase)iter.next();

      if (testCase.skip) {
        testCase.fail();
        continue;
      }

      testCase.writeFiles();
			testCase.performTest();
		}
	}

	/**
	 * Returns the testsuite file.
	 * 
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

// Local Variables:
// tab-width: 2
// End:
