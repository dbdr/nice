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


import java.io.*;
import java.util.*;




/**
 * TODO
 * 
 * @author	Alex Greif <a href="mailto:alex.greif@web.de">alex.greif@web.de</a>
 * @version	$Id$
 */
public class GlobalSourceFile extends NiceSourceFile {

	final static String PACKAGE_GLOBAL = "global";


	/**
	 * TODO
	 * 
	 */
	public GlobalSourceFile() {
		setPackage(PACKAGE_GLOBAL);
	}


	/**
	 * TODO
	 * 
	 * @param	line	TODO
	 */
	public void consumeLine(String line) {
		addToTopLevel(line);
	}

}

// Local Variables:
// tab-width: 2
// End:
