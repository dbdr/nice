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
