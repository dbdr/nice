package nice.tools.testsuite;


import java.io.*;
import java.util.*;

import nice.tools.testsuite.output.*;

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
 * @version	$Id$
 */

public class TestNice {

	/**
	 * Testsuite file extension.
	 * 
	 */
	static private final String TESTSUITE_FILE_EXTENSION = ".testsuite";

	/**
	 * Keyword indicator.
	 * 
	 */
	static final String KEYWORD_SIGN = "///";

	/**
	 * Keyword.
	 * 
	 */
	static final String KEYWORD_GLOBAL = "global";

	/**
	 * Keyword.
	 * 
	 */
	static final String KEYWORD_TOPLEVEL = "toplevel";

	/**
	 * Keyword.
	 * 
	 */
	static final String KEYWORD_PACKAGE = "package";

	/**
	 * Keyword.
	 * 
	 */
	static final String KEYWORD_IMPORT = "import";

	/**
	 * Keyword.
	 * 
	 */
	static final String KEYWORD_DONTCOMPILE = "dontcompile";





	/**
	 * The temporary folder where nice sources and the compiled bytecod is placed.
	 * 
	 */
	private static File _tempFolder = new File("temp-nice-testsuite");
	
	/**
	 * TODO
	 * 
	 */
	private static int _fileCounter = 0;

	/**
	 * Nice source file that contains the gathered global sources.
	 These sources are available for all testcases.
	 * 
	 */
	private static GlobalSourceFile _globalSource = new GlobalSourceFile();

	/**
	 * Number of successful testcases.
	 * 
	 */
	private static int _testCasesSucceeded = 0;

	/**
	 * Number of failed testcases.
	 * 
	 */
	private static int _testCasesFailed = 0;

	/**
		The Output where log statements should be written.
		ConsoleOutput is the default Output.
	*/
	private static Output _output;



	/**
	 * Main method of the application;
	 * 
	 * @param	args	console arguments
	 */
	static public void main(String[] args) {
		if (args.length != 1) {
			usage();
			System.exit(1);
		}
		
		/*try {
			_output = new HtmlOutput(new FileWriter(new File(TestNice.getTempFolder().getParent(), "testsuite_output.html")));
		} catch(IOException e) {
			e.printStackTrace();
		}*/
		_output = new ConsoleOutput();
		
		
		_output.startApplication();
		cleanupTempFolder();
		
		try {
			new TestNice().performTests(args[0]);
		} catch(TestSuiteException e) {
			e.printStackTrace();
		}
		_output.endApplication();
		
		//	close writer
		_output.close();
	}

	/**
	 * Prints the usage of this application.
	 */
	static private void usage() {
		System.out.println("usage:\n java nice.tools.testsuiteTestNice [testSuiteFile | folder]");
	}


	/**
	 * Deletes the temporary folder with all its contents and
	 * creates a new empty one.
	 * 
	 */
	static void cleanupTempFolder() {
		if (_tempFolder.exists())
			deleteFolder(_tempFolder);
			
		_tempFolder.mkdir();
	}


	/**
	 * TODO
	 * 
	 */
	static int getFileCounter() {
		return ++_fileCounter;
	}


	/**
	 * deletes the specified folder and all its contents.
	 * 
	 * @param	folder	The folder that should be deleted
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
	 * Performs the tests in a single testsuite file or in all testsuite files in 
	 * a folder
	 * 
	 * @param	testSuitePath	TODO
	 * @exception	TestSuiteException	TODO
	 */
	private void performTests(String testSuitePath) throws TestSuiteException {
		File file = new File(testSuitePath);
		if (! file.exists())
			throw new TestSuiteException("Could not find testsuite file or folder: " + file.getAbsolutePath());
		//TestSuite testsuite = new TestSuite(new File("/Users/agreif/projects/Nice/sf/testsuite/compiler/first.testsuite"));
		if (file.isFile()) {
			new TestSuite(file);
			return;
		}
		
		//	collect all testsuite files and perform its tests
		Set testSuiteFiles = new HashSet();
		getTestSuiteFiles(file, testSuiteFiles);
		for (Iterator iter = testSuiteFiles.iterator(); iter.hasNext();)
			new TestSuite((File)iter.next());
	}


	/**
	 * Collects all testsuite files in the specified folder in the given collection.
	 * 
	 */
	private void getTestSuiteFiles(File folder, Set testSuiteFiles) {
		File[] files = folder.listFiles();
		for(int i = 0; i < files.length; i++) {
			File file = files[i];
			if (file.isFile()) {
				if (file.getName().endsWith(TESTSUITE_FILE_EXTENSION))
					testSuiteFiles.add(file);
			} else
				getTestSuiteFiles(file, testSuiteFiles);
		}
	}
	
	/**
	 * Returns the temporary folder.
	 * 
	 */
	static public File getTempFolder() {
		return _tempFolder;
	}
	


	/**
	 * Returns the global source file.
	 * 
	 */
	static GlobalSourceFile getGlobalSource() {
		return _globalSource;
	}



	/**
	 * Increases the number of successful testcases.
	 * 
	 */
	public static void increaseSucceeded() {
		++_testCasesSucceeded;
	}

	/**
	 * Increases the number of failed testcases.
	 * 
	 */
	public static void increaseFailed() {
		++_testCasesFailed;
	}





	/**
	 * Returns the Output.
	 */
	static Output getOutput() {
		return _output;
	}



	/**
	 * Returns the number of succeded testcases.
	 */
	static public int getTestCasesSucceeded() {
		return _testCasesSucceeded;
	}


	/**
	 * Returns the number of failed testcases.
	 */
	static public int getTestCasesFailed() {
		return _testCasesFailed;
	}

















}


