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

package nice.tools.testsuite.output;


import java.io.*;
import java.util.*;

import nice.tools.testsuite.*;

/**
 * Output logs the statements in html Format.
 * 
 * @author	Alex Greif <a href="mailto:alex.greif@web.de">alex.greif@web.de</a>
 * @version	$Id$
 */
public class HtmlOutput extends AbstractOutput {

	/**
	 * Creates an instance of HtmlFormat where the formatted output
	 should be written to the specified writer.
	 * 
	 * @param	out	TODO
	 */
	public HtmlOutput(Writer out) {
		super(out);
	}



	/**
	 * Called when the application starts up.
	 * 
	 */
	public void startApplication() {
		log("<html><head><title>Nice Testsuite</title></head><body>");
		log("<h2>");
		log(new Date().toString());
		log("</h2>");
		log("<table border=1>");
	}
	
	
	/**
	 * Called when the application terminates.
	 * 
	 */
	public void endApplication() {
		log("</table>");
		log("<table><tr><td colspan=2 nowrap>");
		log("number of testcases:</td>");
		log("<td>" + (TestNice.getTestCasesSucceeded() + TestNice.getTestCasesFailed()) + "<td>");
		log("</tr><tr>");
		log("<td width=30>&nbsp;</td>");
		log("<td>succeeded:</td><td>" + TestNice.getTestCasesSucceeded() + "</td>");
		log("</tr><tr>");
		log("<td width=30>&nbsp;</td>");
		log("<td>failed:</td><td>" + TestNice.getTestCasesFailed() + "</td>");
		log("</tr><tr>");
		log("<td width=30>&nbsp;</td>");
		log("<td>warnings:</td><td>" + TestNice.getTestCasesWarning() + "</td>");
		log("</tr></table>");

		log("</body></html>");
	}
	
	
	
	/**
	 * Called when the testsuite starts to perform its testcases..
	 * 
	 * @param	testSuite	TODO
	 */
	public void startTestSuite(TestSuite testSuite) {
		log("<tr><td>testsuite: " + testSuite.getFile() + "</td></tr>");
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
		log("<tr><td><pre>");
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
			log("</pre></td></tr>");
	}
	
	
	/**
	 * Returns the line break that is specific to this output.
	 * 
	 */	protected String getLineBreak() {
		return "<br>";
	}


	
	

}



