package nice.tools.testsuite;


import java.io.*;



/**
 * Example:
 * ///	pass
 * ///	package a dontcompile
 * int x = 3;
 * System.out.println("hallo world");
 * ///	toplevel
 * void f() {
 * System.out.println("f()");
 * }
 * ///	package b import a
 * int z = 5;
 * f();
 * 
 * @author	Alex Greif <a href="mailto:alex.greif@web.de">alex.greif@web.de</a>
 * @version	$Id$	TODO
 */

public class TestNice {

	/**
	 * TODO
	 * 
	 */
	static final String KEYWORD_SIGN = "///";

	/**
	 * TODO
	 * 
	 */
	static final String KEYWORD_GLOBAL = "global";

	/**
	 * TODO
	 * 
	 */
	static final String KEYWORD_TOPLEVEL = "toplevel";

	/**
	 * TODO
	 * 
	 */
	static final String KEYWORD_PACKAGE = "package";

	/**
	 * TODO
	 * 
	 */
	static final String KEYWORD_IMPORT = "import";

	/**
	 * TODO
	 * 
	 */
	static final String KEYWORD_DONTCOMPILE = "dontcompile";





	/**
	 * TODO
	 * 
	 */
	private static File _tempFolder = new File("temp-nice-testsuite");
	
	/**
	 * TODO
	 * 
	 */
	private static int _fileCounter = 0;

	/**
	 * TODO
	 * 
	 */
	private static GlobalSourceFile _globalSource = new GlobalSourceFile();







	/**
	 * TODO
	 * 
	 * @param	args	TODO
	 * @exception	Exception	TODO
	 */
	static public void main(String[] args) {
		if (_tempFolder.exists())
			deleteFolder(_tempFolder);
			
		_tempFolder.mkdir();
		
		try {
			new TestNice().performTests(args[0]);
		} catch(TestSuiteException e) {
			e.printStackTrace();
		}
	}



	static int getFileCounter() {
		return ++_fileCounter;
	}


	/**
	 * TODO
	 * 
	 * @param	folder	TODO
	 */
	private static void deleteFolder(File folder) {
		File[] files = folder.listFiles();
		for(int i = 0; i < files.length; i++) {
			File file = files[i];
			if (file.isFile())
				file.delete();
			else
				deleteFolder(file);
		}
		folder.delete();
	}



	/**
	 * This method could collect testsuite files and run the testcases inside of them.
	 * 
	 * @exception	TestSuiteException	TODO
	 * @param	testSuitePath	TODO
	 * @exception	TestSuiteException	TODO
	 */
	private void performTests(String testSuitePath) throws TestSuiteException {
		//TestSuite testsuite = new TestSuite(new File("/Users/agreif/projects/Nice/sf/testsuite/compiler/first.testsuite"));
		TestSuite testsuite = new TestSuite(new File(testSuitePath));
	
	}


	
	/**
	 * Returns the temporary folder.
	 * 
	 */
	static File getTempFolder() {
		return _tempFolder;
	}
	


	static GlobalSourceFile getGlobalSource() {
		return _globalSource;
	}




}


