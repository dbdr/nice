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





