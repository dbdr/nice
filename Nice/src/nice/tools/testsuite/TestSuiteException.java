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




/**
 * Nice TestSuite specific exception, that holds a reference to the original exception.
 * 
 * @author	Alex Greif <a href="mailto:alex.greif@web.de">alex.greif@web.de</a>
 * @version	$Id$
 */
public class TestSuiteException extends Exception {
	
	Throwable _cause;
	
	public TestSuiteException(String message) {
		super(message);
	}
	
	public TestSuiteException(String message, Throwable cause) {
		super(message);
		_cause = cause;
	}
	
	public Throwable getCause() {
		return _cause;
	}
	
	public void printStackTrace() {
		super.printStackTrace();
		if (_cause != null)
			_cause.printStackTrace();

	}
	
}





