package nice.tools.testsuite.output;


import java.io.*;
import java.util.*;

import nice.tools.testsuite.*;

/**
 * Appender logs the statements to the console.
 * 
 * @author	Alex Greif <a href="mailto:alex.greif@web.de">alex.greif@web.de</a>
 * @version	$Id$
 */
public class ConsoleOutput implements Output {
	/**
	 * TODO
	 * 
	 */
	private String _indentSign = "  ";
	/**
	 * TODO
	 * 
	 */
	private int _indent = 0;


	/**
	 * TODO
	 * 
	 */
	private void indent() {
		for (int i = 0; i < _indent; i++)
			System.out.print(_indentSign);
	}


	/**
	 * Called when the application starts up.
	 * 
	 */
	public void startApplication() {
		indent();
		System.out.println("start test engine");
		_indent++;
	}
	
	
	/**
	 * Called when the application terminates.
	 * 
	 */
	public void endApplication() {
		_indent--;
		System.out.println("end application");
		System.out.println();
		System.out.println("number of testcases: " + (TestNice.getTestCasesSucceeded() + TestNice.getTestCasesFailed()));
		System.out.println("  succeeded: " + TestNice.getTestCasesSucceeded());
		System.out.println("  failed   : " + TestNice.getTestCasesFailed());
		System.out.println();
	}
	
	
	
	/**
	 * Called when the testsuite starts to perform its testcases..
	 * 
	 * @param	testSuite	TODO
	 */
	public void startTestSuite(TestSuite testSuite) {
		indent();
		System.out.println("testsuite: " + testSuite.getFile());
		_indent++;
	}
	
	
	/**
	 * Called when the testsuite finishes to perform its testcases..
	 * 
	 */
	public void endTestSuite() {
		_indent--;
		indent();
		System.out.println("finished");

	}
	
	
	
	/**
	 * Called when the testcase starts to perform.
	 * 
	 * @param	testCase	TODO
	 */
	public void startTestCase(TestCase testCase) {
		indent();
		System.out.println("testcase");
		_indent++;
	}
	
	
	/**
	 * Called when the testcase is performed.
	 * 
	 * @param	pass	TODO
	 */
	public void endTestCase(boolean pass) {
		indent();
		System.out.println((pass ? "pass" : "fail"));
		_indent--;
	}
	
	


	/**
	 * Logs a statement to this Appender.
	 * 
	 * @param	statement	TODO
	 */
	public void log(String statement) {
		log(null, statement);
	}

	/**
	 * Logs a statement with the given prefix in angled braces.
	 * 
	 * @param	prefix	TODO
	 * @param	statement	TODO
	 */
	public void log(String prefix, String statement) {
		if (statement.length() == 0)	//	workaround, reader returns null for ""
			System.out.println((prefix == null ? "" : "["+prefix+"] ") + statement);
		
		BufferedReader reader = null;
		String line = "";
		try {
			reader = new BufferedReader(new StringReader(statement));;
			while((line = reader.readLine()) != null) {
				indent();
				System.out.println((prefix == null ? "" : "["+prefix+"] ") + line);
			}
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch(IOException e) {
					e.printStackTrace();
				}
		}
	}
	
	
	/**
	 * Logs an exception.
	 * 
	 * @param	exception ?statement?	TODO
	 */
	public void log(Throwable exception) {
		indent();
		exception.printStackTrace();
	}
	
	
	/**
	 * Logs a statement and an exception.
	 * 
	 * @param	statement	TODO
	 * @param	exception	TODO
	 */
	public void log(String statement, Throwable exception) {
		indent();
		System.out.println(statement);
		exception.printStackTrace();
	}

	

}



