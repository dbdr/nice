package nice.tools.testsuite.output;


import java.io.*;
import java.util.*;

import nice.tools.testsuite.*;

/**
 * The output Interface. The start..() and end..() methods are sort of
 * events callbacks, that can be called while runtime. Classes that implement tis interface
 * can decide which events to implement and in what format the messages should be logged.
 * 
 * @author	Alex Greif <a href="mailto:alex.greif@web.de">alex.greif@web.de</a>
 * @version	$Id$
 */
public interface Output {

	/**
	 * Called when the application starts up.
	 * 
	 */
	public void startApplication();


	/**
	 * Called when the application terminates.
	 * 
	 */
	public void endApplication();
	

	/**
	 * Called when the testsuite starts to perform its testcases..
	 * 
	 * @param	testSuite	TODO
	 */
	public void startTestSuite(TestSuite testSuite);


	/**
	 * Called when the testsuite finishes to perform its testcases..
	 * 
	 */
	public void endTestSuite();

	
	/**
	 * Called when the testcase starts to perform.
	 * 
	 * @param	testCase	TODO
	 */
	public void startTestCase(TestCase testCase);


	/**
	 * Called when the testcase is performed.
	 * 
	 * @param	succeeded	TODO
	 */
	public void endTestCase(boolean succeeded);


	/**
	 * Logs a statement to this Appender.
	 * 
	 * @param	statement	TODO
	 */
	public void log(String statement);


	/**
	 * Logs a statement with the given prefix in angled braces.
	 * 
	 * @param	prefix	TODO
	 * @param	statement	TODO
	 */
	public void log(String prefix, String statement);

	/**
	 * Logs a statement to this output and flushes the writer.
	 * 
	 * @param	statement	TODO
	 */
	public void logAndFlush(String statement);
	
	
	/**
	 * Logs a statement with the given prefix in angled braces and flushes the writer.
	 * 
	 * @param	prefix	TODO
	 * @param	statement	TODO
	 */
	public void logAndFlush(String prefix, String statement);

	
	/**
	 * Closes the underlying writer.
	 * 
	 */
	public void close();

}



