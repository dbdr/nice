package nice.tools.testsuite;

import java.io.*;

/**
 * Classloader that loads the newly created and compiled classes for a testcase.
 * 
 * @author	Alex Greif <a href="mailto:alex.greif@web.de">alex.greif@web.de</a>
 * @version	$Id$
 */
public class TestSuiteClassLoader extends ClassLoader {
	
	
	/**
	 * Constructor
	 * 
	 * @param	parent	the parent classloader
	 */
	public TestSuiteClassLoader(ClassLoader parent) {
		super(parent);
	}

	/**
	 * This is the method where the task of class loading
	 * is delegated to our custom loader.
	 * 
	 * @param	name the name of the class
	 * @return	the resulting <code>Class</code> object
	 * @exception	ClassNotFoundException if the class could not be found
	 */
	protected Class findClass(String name) throws ClassNotFoundException
	{
		FileInputStream fi = null;

		try {
			String path = name.replace('.', '/');
			fi = new FileInputStream(new File(TestNice.getTempFolder(), path + ".class"));
			byte[] classBytes = new byte[fi.available()];
			fi.read(classBytes);
			return defineClass(name, classBytes, 0, classBytes.length);
		} catch (Exception e) {
			// We could not find the class, so indicate the problem with an exception
			throw new ClassNotFoundException(name);
		} finally {
			if ( null != fi )
				try {
					fi.close();
				} catch (Exception e){}
		}
	}
}

