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
 * Output logs the statements as text with indentations.
 * 
 * @author	Alex Greif <a href="mailto:alex.greif@web.de">alex.greif@web.de</a>
 * @version	$Id$
 */
public class TextOutput extends AbstractOutput {
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
	 * @param	out	TODO
	 */
	public TextOutput(Writer out) {
		super(out);
	}



	/**
	 * TODO
	 * 
	 */
	private String indent() {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < _indent; i++)
			buf.append(_indentSign);
			
		return buf.toString();
	}


	/**
	 * Called when the application starts up.
	 * 
	 */
	public void startApplication() {
		logAndFlush("run test engine");
		_indent++;
	}
	
	
	/**
	 * Called when the application terminates.
	 * 
	 */
	public void endApplication() {
		_indent--;
		log("test engine finished");
		log("");
		log("number of testcases: " + (TestNice.getTotalTestCases()));
		log("  successes  : " + TestNice.getTestCasesSucceeded());
		log("  regressions: " + TestNice.getTestCasesFailed());
		log("  warnings   : " + TestNice.getTestCasesWarning());
		log("  known bugs : " + TestNice.getTestCasesKnownBug());
		if (TestNice.getTestCasesFixed() > 0)
		  log("  FIXES :-)) : " + TestNice.getTestCasesFixed());
		
		logAndFlush("");
	}
	
	
	
	/**
	 * Called when the testsuite starts to perform its testcases..
	 * 
	 * @param	testSuite	TODO
	 */
	public void startTestSuite(TestSuite testSuite) {
		logAndFlush("testsuite: " + testSuite.getFile());
		_indent++;
	}
	
	
	/**
	 * Called when the testsuite finishes to perform its testcases..
	 * 
	 */
	public void endTestSuite() {
		_indent--;
		//log("finished");

	}
	
	
	
	/**
	 * Called when the testcase starts to perform.
	 * 
	 * @param	testCase	TODO
	 */
	public void startTestCase(TestCase testCase) {
		mark();
		log("testcase");
		_indent++;
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
			logAndFlush("fail");

		_indent--;
	}
	
	



	/**
	 * TODO
	 * 
	 */
	protected String getIndent() {
		return indent();
	}



	
	

}



