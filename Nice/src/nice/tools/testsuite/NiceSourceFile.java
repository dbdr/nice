package nice.tools.testsuite;


import java.io.*;
import java.util.*;



/**
 * Class represents a nice source file.
 * 
 */
public class NiceSourceFile {
	public final static int STATUS_MAIN = 0;
	public final static int STATUS_TOPLEVEL = 1;

	private int _status = STATUS_MAIN;

	public static final String DEFAULT_PACKAGE = "defaultpackage";
	
	private String _package = DEFAULT_PACKAGE;
	private StringBuffer _mainMethodContent = new StringBuffer();
	private StringBuffer _topLevelContent = new StringBuffer();
	private Set _imports = new HashSet();
	
	private final String _className = "file_" + TestNice.getFileCounter();
	private final String _fileName = _className + ".nice";


	/**
		Sets the status.
	*/
	public void setStatus(int status) {
		_status = status;
	}


	/**
	 * Consumes a read line from the testsuite file.
	 Sets the status if necessary.
	 * 
	 */
	public void consumeLine(String line) {
		switch(_status) {
			case STATUS_MAIN:
				addToMainMethod(line);
				break;
			case STATUS_TOPLEVEL:
				addToTopLevel(line);
				break;
		}
	}


	/**
		Add a line to the main methods body.
	*/
	public void addToMainMethod(String line) {
		_mainMethodContent.append(line).append('\n');
	}

	/**
		Add a line to the main methods body.
	*/
	public void addToTopLevel(String line) {
		_topLevelContent.append(line).append('\n');
	}


	/**
		TODO
	*/
	public void consumePackageKeyword(String keywordStatement) throws TestSuiteException {
		//	get rid of the dontcompile keyword
		int dontCompilePos = keywordStatement.indexOf(TestNice.KEYWORD_DONTCOMPILE);
		if (dontCompilePos != -1)
			keywordStatement = keywordStatement.substring(0, dontCompilePos)
					+ keywordStatement.substring(dontCompilePos + TestNice.KEYWORD_DONTCOMPILE.length());

		keywordStatement = keywordStatement.substring(TestNice.KEYWORD_PACKAGE.length()).trim();
		//	if contains no space than statement has only package name
		int spacePos = keywordStatement.indexOf(" ");
		if (spacePos == -1) {
			setPackage(keywordStatement);
			return;
		}
		
		setPackage(keywordStatement.substring(0, spacePos));

		//	look for import keyword
		int importStartPos = keywordStatement.indexOf(TestNice.KEYWORD_IMPORT, spacePos);
		if (importStartPos == -1)
			throw new TestSuiteException("unknown keyword statement: " + keywordStatement);
		
		keywordStatement = keywordStatement.substring(importStartPos + TestNice.KEYWORD_IMPORT.length()).trim();
		StringTokenizer tokenizer = new StringTokenizer(keywordStatement, " ,;+");
		for (;tokenizer.hasMoreTokens();)
			_imports.add(tokenizer.nextToken());
	}









	/**
		Sets the package, this file belongs to.
	*/
	public void setPackage(String packageName) {
		_package = packageName;
	}
	
	/**
		Returns the package, this file belongs to.
	*/
	public String getPackage() {
		return _package;
	}
	
	/**
		Returns the file name.
	*/
	public String getFileName() {
		return _fileName;
	}
	
	/**
		Returns the class name.
	*/
	public String getClassName() {
		return _className;
	}
	
	/**
		Writes the nice source file.
	*/
	public void write() throws TestSuiteException {
		File packageFolder = new File(TestNice.getTempFolder(), _package.replace('.', File.separatorChar));
		if (! packageFolder.exists()  &&  ! packageFolder.mkdirs())
			throw new TestSuiteException("could not create folder: " + packageFolder);
			
		File sourceFile = new File(packageFolder, _fileName);
		BufferedWriter writer = null;
		try {
			StringWriter debugWriter = new StringWriter();
			writer = new BufferedWriter(debugWriter);
			writePackage(writer);
			writeImports(writer);
			writeTopLevel(writer);
			writeMainMethod(writer);
			writer.close();
			//System.out.println("#####\n" + debugWriter.toString() + "\n###\n");
			writer = new BufferedWriter(new FileWriter(sourceFile));
			writer.write(debugWriter.toString());
		} catch(IOException e) {
			throw new TestSuiteException("Exception while writing file: " + sourceFile, e);
		} finally {
			if (writer != null)
				try {
					writer.close();
				} catch(IOException e) {
					throw new TestSuiteException("Could not close file: " + sourceFile, e);
				}
		}
	}
	
	
	/**
		Writes the package in the nice source file.
	*/
	private void writePackage(BufferedWriter writer) throws IOException {
		writer.write("package ");
		writer.write(_package);
		writer.write(";");
		writer.newLine();
		writer.newLine();
	}
	
	/**
		Writes the import statements in the nice source file.
	*/
	private void writeImports(BufferedWriter writer) throws IOException {
		for (Iterator iter = _imports.iterator(); iter.hasNext();) {
			writer.write("import ");
			writer.write((String)iter.next());
			writer.write(";");
			writer.newLine();
		}			
	}
	
	/**
		Writes the main() method in the nice source file.
	*/
	private void writeMainMethod(BufferedWriter writer) throws IOException {
		if (! hasMainMethod())
			return;
			
		writer.write("// main method");writer.newLine();
		writer.write("main(args) {\n");
		writer.write(_mainMethodContent.toString());
		writer.write("}\n");
	}
	
	
	/**
		Writes the toplevel source in the nice source file.
	*/
	private void writeTopLevel(BufferedWriter writer) throws IOException {
		writer.write("// Top level code");writer.newLine();
		writer.write(_topLevelContent.toString());
	}
	
	
	/**
		Returns whether this file has sources for the main() method.
	*/
	public boolean hasMainMethod() {
		return _mainMethodContent.length() != 0;
	}
	
	/**
		Return whether the source file contains code or not.
	*/
	public boolean isEmpty() {
		return (_mainMethodContent.length() == 0)
			&&  (_topLevelContent.length() == 0);
	}
}


