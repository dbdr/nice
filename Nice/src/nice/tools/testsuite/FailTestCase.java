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

import java.util.Iterator;

/**
 * TestCase class for test cases that should fail.
 * 
 * @author	Alex Greif <a href="mailto:alex.greif@web.de">alex.greif@web.de</a>
 * @version	$Id$
 */
public class FailTestCase extends TestCase {

	/**
	 * Constructor.
	 * 
	 * @param	suite	The testsuite this testcase belongs to
	 */
	public FailTestCase(TestSuite suite) {
		super(suite);
	}


	/**
	 * Performs the test for this testcase.
	 * Compilation should fail, otherwise throw exception.
	 * 
	 */
	public void performTest() {
		super.performTest();
		try {
			compilePackages();
		} catch(TestSuiteException e) {
			checkFailPositions();
			if (getFailPositions().isEmpty())
				pass();
			else
				warning();
			return;
		} catch(CompilerBugException e) {
			fail();
			return;
		}
		
		TestNice.getOutput().log("Compilation was expected to fail, but it succeeded.");
		fail();
	}
	
	
	
	/**
	 * Parses the compiler messages for line, column, filename and compares them with the
	 * expected failure positions that the user defined. If the user defined more expected 
	 * failure positions than the compiler recognized then a warning is shown.
	 * A warning is also shown when the user expected a failure at another position than the
	 * compiler recognized.
	 * 
	 */
	private void checkFailPositions() {
		String compilerMessages = getCompilerMessages();
		//System.out.println(">" + compilerMessages + "<");
		
		for (Iterator iter = getFailPositions().iterator(); iter.hasNext();) {
			FailPosition failPosition = (FailPosition)iter.next();
			String s = failPosition.getFileName() + ": line " + failPosition.getLine() + ", column " + failPosition.getColumn() + ":";
			//System.out.println(">" + s + "<");
			if (compilerMessages.indexOf(s) == -1)
				TestNice.getOutput().log("warning", "Failure not at expected position (" +
						failPosition.getFileName() + ": line " + 
						failPosition.getLine() + ", column " + failPosition.getColumn() + ")");
			else
				iter.remove();
		
		}
		
		if (! getFailPositions().isEmpty())
			TestNice.getOutput().log("warning", "more expected failures than compiler failures");
		
		/*
		int failPositionCounter = 0;
		int pos = 0;
		
		
		while(true) {
			int linePos = compilerMessages.indexOf(": line ", pos);
			
			if (linePos == -1)
				break;
			
			pos = linePos + 7;
			int columnPos = compilerMessages.indexOf(", column ");
			
			int line = Integer.parseInt(compilerMessages.substring(pos, columnPos).trim());
			
			pos = columnPos + 9;
			int colonPos = compilerMessages.indexOf(":", pos);

			int column = Integer.parseInt(compilerMessages.substring(pos, colonPos).trim());
						
			FailPosition failPosition = (FailPosition)getFailPositions().get(failPositionCounter++);

			System.out.println("comp-line: " + line + "     comp-column: " + column);
			System.out.println("exp-line: " + failPosition.getLine() + "     exp-column: " + failPosition.getColumn());


			if (line != failPosition.getLine()  ||  column != failPosition.getColumn())
				TestNice.getOutput().log("warning", "Failure not at expected position (" +
						failPosition.getLine() + "," + failPosition.getColumn() +
						") but at (" + line + "," + column + ")");
		}
		*/
	}
	
	

}

// Local Variables:
// tab-width: 2
// End:
