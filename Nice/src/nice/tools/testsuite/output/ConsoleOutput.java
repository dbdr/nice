package nice.tools.testsuite.output;


import java.io.*;
import java.util.*;

import nice.tools.testsuite.*;

/**
 * Output logs the statements to System.out.
 * 
 * @author	Alex Greif <a href="mailto:alex.greif@web.de">alex.greif@web.de</a>
 * @version	$Id$
 */
public class ConsoleOutput extends TextOutput {




	/**
	 * TODO
	 * 
	 */
	public ConsoleOutput() {
		super(new OutputStreamWriter(System.out));
	}


	
	

}



