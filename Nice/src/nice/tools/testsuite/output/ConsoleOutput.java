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
	 * Creates an instance of ConsoleOutput that uses the System.out to
	 * write the messages.
	 * 
	 */
	public ConsoleOutput() {
		super(new OutputStreamWriter(System.out));
	}


	
	

}



