/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                         (c) Alex Greif 2002                            */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package nice.tools.testsuite;


/**
 * TestCase class fot the case that the test should pass
 * 
 * @author	Alex Greif <a href="mailto:alex.greif@web.de">alex.greif@web.de</a>
 * @version	$Id$
 */
public class PassTestCase extends TestCase {


	/**
	 * Constructor.
	 * 
	 * @param	suite	The testsuite this testcase belongs to
	 */
	public PassTestCase(TestSuite suite) {
		super(suite);
	}


	/**
	 * Performs the test for this testcase.
	 * Compilation and the execution of the main() method
	 * should be successfully.
	 * 
	 * @exception	TestSuiteException	TODO
	 */
	public void performTest() {
		super.performTest();
		try {
			compilePackages();
			runMain();
		} catch(TestSuiteException e) {
			fail();
			return;
		} catch(CompilerBugException e) {
			fail();
			return;
		}
		
		pass();
	}

}

