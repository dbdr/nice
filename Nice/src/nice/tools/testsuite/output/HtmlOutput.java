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
public class HtmlOutput implements Output {

	StringBuffer _html = new StringBuffer();
	int _mark = 0;


	private void append(String s) {
		_html.append(s);
	}


	private void mark() {
		_mark = _html.length();
	}

	private void reset() {
		_html.delete(_mark, _html.length());
	}


	/**
	 * Called when the application starts up.
	 * 
	 */
	public void startApplication() {
		append("<html><head><title>Nice Testsuite</title></head><body>");
		append("<h2>");
		append(new Date().toString());
		append("</h2>");
		append("<table border=1>");
	}
	
	
	/**
	 * Called when the application terminates.
	 * 
	 */
	public void endApplication() {
		append("</table>");
		append("<table><tr><td colspan=2 nowrap>");
		append("number of testcases:</td>");
		append("<td>" + (TestNice.getTestCasesSucceeded() + TestNice.getTestCasesFailed()) + "<td>");
		append("</tr><tr>");
		append("<td width=30>&nbsp;</td>");
		append("<td>succeeded:</td><td>" + TestNice.getTestCasesSucceeded() + "</td>");
		append("</tr><tr>");
		append("<td width=30>&nbsp;</td>");
		append("<td>failed:</td><td>" + TestNice.getTestCasesFailed() + "</td>");
		append("</tr></table>");

		append("</body></html>");
		
		//write to disc
		try {
			FileWriter writer = new FileWriter(new File(TestNice.getTempFolder(), "output.html"));
			writer.write(_html.toString());
			writer.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * Called when the testsuite starts to perform its testcases..
	 * 
	 * @param	testSuite	TODO
	 */
	public void startTestSuite(TestSuite testSuite) {
		append("<tr><td>testsuite: " + testSuite.getFile() + "</td></tr>");
	}
	
	
	/**
	 * Called when the testsuite finishes to perform its testcases..
	 * 
	 */
	public void endTestSuite() {
	}
	
	
	
	/**
	 * Called when the testcase starts to perform.
	 * 
	 * @param	testCase	TODO
	 */
	public void startTestCase(TestCase testCase) {
		mark();
		append("<tr><td><pre>");
	}
	
	
	/**
	 * Called when the testcase is performed.
	 * 
	 * @param	pass	TODO
	 */
	public void endTestCase(boolean pass) {
		if (pass)
			reset();
		else
			append("</pre></td></tr>");
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
			append((prefix == null ? "" : "["+prefix+"] ") + statement + "<br>");
		
		BufferedReader reader = null;
		String line = "";
		try {
			reader = new BufferedReader(new StringReader(statement));
			while((line = reader.readLine()) != null) {
				append((prefix == null ? "" : "["+prefix+"] ") + line + "<br>");
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
		exception.printStackTrace();
	}
	
	
	/**
	 * Logs a statement and an exception.
	 * 
	 * @param	statement	TODO
	 * @param	exception	TODO
	 */
	public void log(String statement, Throwable exception) {
		append(statement + "<br>");
		exception.printStackTrace();
	}

	

}



