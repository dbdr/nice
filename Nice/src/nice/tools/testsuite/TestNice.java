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
import java.net.URL;

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
	 * Keyword.
	 * 
	 */
	static final String KEYWORD_FAILHERE = "fail here";





	/**
	 * The temporary folder where nice sources and the compiled bytecod is placed.
	 * 
	 */
	private static File _tempFolder = new File("testsuite-temp-folder");
	
	/**
	 * The temporary folder where failed testcases are placed.
	 * 
	 */
	private static File _failFolder = new File("testsuite-fail-folder");
	
	/**
	 * TODO
	 * 
	 */
	private static int _fileCounter = 0;

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
	 * Number of testcases with warnings. These are ahndled as failed testcases.
	 * 
	 */
	private static int _testCasesWarning = 0;

	private static int _testCasesKnownBug = 0;

	private static int _testCasesFixed = 0;

	/**
	 * The Output where log statements should be written.
	 * ConsoleOutput is the default Output.
	 * 
	 */
	private static Output _output = new ConsoleOutput();


	/**
		 The <b>home</b> directory of gcc.
		 If you want to use the system-wide version of gcc on a Unix machine,
		 this should likely be set to "/usr".
	*/
	private static String _gcc = null;


	/**
		 A JVM with which to run generated code (in a separate process).
		 Useful for testing alternative JVMs like Kaffe without using them for
		 running the testengine and the compilation itself.
	*/
	private static String _jvm = null;


	/**
	 * TODO
	 * 
	 */
	private static List _testSuites = new ArrayList();


	/**
	 * Flag whether comments should be included in the output or not.
	 * 
	 */
	private static boolean _writeComments;


	/**
		 Whether to wait at the end of the run.
		 This is useful for profiling memory after all the work is done,
		 using a debugger/memory profiler.
	*/ 
	private static boolean _wait;

	/**
	 * Classpath entry that contains the Nice standard library.
	 * 
	 * This is only needed when the testsuite is not run from a JVM
   * (for instance after native compilation), or if the standard library
	 * is not in the classpath.
	 * 
	 */
	private static String _runtime;



	/**
	 * Main method of the application.
	 *
	 * Returns to the system with the code:
	 *   0 if there are no regressions;
	 *   1 if there was at least one regression;
	 *   2 if there was at least one warning on no regression;
	 * 
	 * @param	args	console arguments
	 */
	static public void main(String[] args) {

		_runtime = nice.tools.compiler.dispatch.getNiceRuntime();

		if (!processArgs(args)) {
			usage();
			System.exit(1);
		}
		
		/*try {
			_output = new HtmlOutput(new FileWriter(new File(TestNice.getTempFolder().getParent(), "testsuite_output.html")));
		} catch(IOException e) {
			e.printStackTrace();
		}
		_output = new ConsoleOutput();
		*/
		
		_output.startApplication();
		cleanupTempFolder();
		cleanupFailFolder();
		
		try {
			//	iterate through testsuites
			for (Iterator iter = _testSuites.iterator(); iter.hasNext();)
				new TestNice().performTests((String)iter.next());
		} catch(TestSuiteException e) {
			e.printStackTrace();
		}

		if (_wait)
			{
				reclaimMemory(true);

				System.out.println("Test finished.\nPress return to terminate:");
				try {
					new DataInputStream(System.in).readLine();
				}
				catch (IOException e) {}
			}
        
		_output.endApplication();
		
		//	close writer
		_output.close();

		if (getTestCasesFailed() > 0)
			System.exit(1);

		if (getTestCasesWarning() > 0)
			System.exit(2);
	}

	private static void reclaimMemory(boolean clear)
	{
		// Reclaim memory.
		bossa.modules.Package.startNewCompilation();
		mlsub.typing.NullnessKind.setSure(null);
		mlsub.typing.NullnessKind.setMaybe(null);

		// Create an OutOfMemoryError, so that soft references are also let loose.
		if (clear)
			try {
				int[] l = new int[1000];
				for (;;)
					{
						l = new int[l.length * 2];
					}
			}
			catch (OutOfMemoryError e) {}

		System.gc();

		nice.tools.compiler.console.fun.printMemoryUsage();
	}

	/**
	 * Processes the command line arguments. Returns true if everything is ok.
	 * 
	 * @param	args	TODO
	 */
	private static boolean processArgs(String[] args) {
		for(int i = 0; i < args.length;) {
			String s = args[i];
			i++;
			if(s.startsWith("-")) {
				if ("-output".equalsIgnoreCase(s))
					setOutput(args[i++]);
				else if ("-gcc".equalsIgnoreCase(s))
					setGcc(args[i++]);
				else if ("-jvm".equalsIgnoreCase(s))
					setJvm(args[i++]);
				else if ("-comment".equalsIgnoreCase(s))
					_writeComments = true;
				else if ("-runtime".equalsIgnoreCase(s))
					_runtime = args[i++];
				else if ("-wait".equalsIgnoreCase(s))
					_wait = true;
				else
					return false;
			} else
				_testSuites.add(s);
		}
		return true;
	}

	/**
	 * Sets the output of the test engine
	 *
	 * @param	output	TODO
	 */
	private static void setOutput(String output) {
		if ("console".equalsIgnoreCase(output)) {
			_output = new ConsoleOutput();
			return;
		}

		output = output.toLowerCase();
		if (output.endsWith(".html")  ||  output.endsWith(".htm"))
			try {
				_output = new HtmlOutput(new FileWriter(new File(output)));
			} catch(IOException e) {
				e.printStackTrace();
			}
	}



	private static void setGcc(String gcc) {
		_gcc = gcc;

		// When doing native compilation, we need a jarred runtime
		if (_runtime == null || ! _runtime.endsWith(".jar"))
			_runtime = "share/java/nice.jar";
	}


	private static void setJvm(String jvm) {
		_jvm = jvm;
	}


	/**
	 * Prints the usage of this application.
	 * 
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
	 * Deletes the fail folder with all its contents and
	 * creates a new empty one.
	 * 
	 */
	static private void cleanupFailFolder() {
		if (_failFolder.exists())
			deleteFolder(_failFolder);
			
		_failFolder.mkdir();
	}



	/**
	 * Moves all files and folders of the temp folder to a
	 * newly created folder inside the fail folder
	 * 
	 */
	static void moveFilesToFailFolder() {
		File folder = new File(_failFolder, "" + 
													 (_testCasesFailed + _testCasesKnownBug));
		_tempFolder.renameTo(folder);
		
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
			if (file.isDirectory())
				deleteFolder(file);
			else
				file.delete();
		}
		folder.delete();
	}



	/**
	 * Performs the tests in a single testsuite file or in all testsuite files in 
	 * a folder
	 * 
	 * @param	testSuitePath	TODO
	 * @exception	TestSuiteException	TODO
	 * @exception	TestSuiteException	TODO
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

                // sort the files on last modification time.
                List files = new ArrayList(testSuiteFiles);
                Collections.sort(files, fileComp);

		for (Iterator iter = files.iterator(); iter.hasNext();)
			new TestSuite((File)iter.next());
	}

	private static Comparator fileComp = new FileComparator();

	private static class FileComparator implements Comparator 
	{
		public FileComparator(){}

		public int compare(Object o1, Object o2)
    		{
      			File file1 = (File)o1;
      			File file2 = (File)o2;
                        if (file1.lastModified() < file2.lastModified())
				return 1;

                        if (file1.lastModified() > file2.lastModified())
				return -1;

			return 0;
    		}

    		public boolean equals(Object obj)
    		{ 
      			return false;
    		}
  	}


	/**
	 * Collects all testsuite files in the specified folder in the given collection.
	 * 
	 * @param	folder	TODO
	 * @param	testSuiteFiles	TODO
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
	 * Returns the temporary folder.
	 * 
	 */
	static public File getFailFolder() {
		return _failFolder;
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
	 * Increases the number of warning testcases.
	 * 
	 */
	public static void increaseWarning() {
		++_testCasesWarning;
	}

	public static void increaseKnownBug() {
		++_testCasesKnownBug;
	}

	public static void increaseFixed() {
		++_testCasesFixed;
	}



	/**
	 * Returns the Output.
	 * 
	 */
	static Output getOutput() {
		return _output;
	}



	static String getGcc() {
		return _gcc;
	}


	static String getJVM() {
		return _jvm;
	}



	/**
	 * Returns whether comments should be written to output
	 * 
	 */
	static boolean getWriteComments() {
		return _writeComments;
	}


	/**
	 * Returns the runtime, where the Nice standard library is.
	 * 
	 */
	static String getRuntime() {
		return _runtime;
	}


	/**
		 @return the classloader used to run generated code.
	*/
	static ClassLoader getClassLoader() {

		File[] dirs = { getTempFolder(), new File(getRuntime()) };
		ClassLoader res = new nice.tools.util.DirectoryClassLoader(dirs, null);
		try {
			nice.tools.util.JDK.setDefaultAssertionStatus(res, true);
		}
		catch(java.lang.reflect.InvocationTargetException e) {
			System.out.println("WARNING: could not enable assertions");
		}
		return res;
	}

	/**
	 * Returns the total number of testcases.
	 * 
	 */
	static public int getTotalTestCases() {
		return _testCasesSucceeded + _testCasesFailed + _testCasesWarning
			+ _testCasesKnownBug + _testCasesFixed;
	}


	/**
	 * Returns the number of succeded testcases.
	 * 
	 */
	static public int getTestCasesSucceeded() {
		return _testCasesSucceeded;
	}


	/**
	 * Returns the number of failed testcases.
	 * 
	 */
	static public int getTestCasesFailed() {
		return _testCasesFailed;
	}

	/**
	 * Returns the number of warning testcases.
	 * 
	 */
	static public int getTestCasesWarning() {
		return _testCasesWarning;
	}

	static public int getTestCasesKnownBug() {
		return _testCasesKnownBug;
	}

	static public int getTestCasesFixed() {
		return _testCasesFixed;
	}

}

// Local Variables:
// tab-width: 2
// indent-tabs-mode: t
// End:
