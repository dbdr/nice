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

	/**
	 * TODO
	 * 
	 */
	public GlobalSourceFile() {
		setPackage("global");
	}


	/**
	 * TODO
	 * 
	 * @param	line	TODO
	 */
	public void consumeLine(String line) {
		addToTopLevel(line);
	}

	public boolean isEmpty() {
		return false;
	}

}
