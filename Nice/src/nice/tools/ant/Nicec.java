package nice.tools.ant;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import java.io.File;
import java.util.Vector;
import bossa.modules.Compilation;

/**
		<h2><a name="java">Nicec</a></h2>
		<h3>Description</h3>
		<p>Runs the Nice compiler.</p>
		All arguments to the Nice compiler Task has to be placed as attributes in the nicec xml-element.
		<h3>Parameters</h3>
		<table border="1" cellpadding="2" cellspacing="0">
			<tr>
				<td valign="top"><b>Attribute</b></td>
				<td valign="top"><b>Description</b></td>
				<td align="center" valign="top"><b>Required</b></td>
			</tr>
			<tr>
				<td valign="top">package</td>
				<td valign="top">The Nice package to compile.</td>
				<td align="center" valign="top">Yes</td>
			</tr>
			<tr>
				<td valign="top">jar</td>
				<td valign="top">Compile the Nice sources to archive.</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">sourcepath</td>
				<td valign="top">Search path for source packages. Is a list of directories and .jar archives.</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">destination</td>
				<td valign="top">Destination directory for compiled packages.</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">classpath</td>
				<td valign="top">Search path for compiled packages and libraries.</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">output</td>
				<td valign="top">Generate native executable.</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">compile</td>
				<td valign="top">Compile packages but do not link them.</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">recompile</td>
				<td valign="top">Force recompilation of package.</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">recompile_all</td>
				<td valign="top">Force recompilation of all dependant packages.</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">exclude_runtime</td>
				<td valign="top">Avoid inclusion of the runtime in the archive.</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">runtime</td>
				<td valign="top">Location of nice.jar.</td>
				<td align="center" valign="top">Yes</td>
			</tr>
			<tr>
				<td valign="top">native_compiler</td>
				<td valign="top">Location of the native compiler binary (gcj).</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">editor</td>
				<td valign="top">Tell nicec that it is called by an editor.</td>
				<td align="center" valign="top">No</td>
			</tr>
		</table>

		<h4>classpath</h4>
		<p><code>Nicec</code>'s <i>classpath</i> attribute is a PATH like structure and can also be set via a nested
		<i>classpath</i> element. This is very reasonable if you want to make your build script's pathes platform
		independent. </p>
		<h5>Example</h5>
<pre>  
	&lt;nicec package=&quot;test&quot; &gt;
	  &lt;classpath&gt;
	    &lt;pathelement location=&quot;\test.jar&quot;/&gt;
	    &lt;pathelement path=&quot;${java.class.path}&quot;/&gt;
	  &lt;/classpath&gt;
	&lt;/java&gt;
</pre>
		<p>It is possible to use the <i>classpath</i> attribute together with the
		<i>classpath<i> nested tag. In this case the result is a concatenated path.</p>
		<p>It is highly recommended to use the nested version!<p>


		<h3>Examples</h3>
<pre>
	&lt;taskdef name=&quot;nicec&quot; classname=&quot;nice.tools.ant.Nicec&quot;/&gt;
	&lt;target name=&quot;nice-compiler&quot;&gt;
	  &lt;nicec package=&quot;test&quot; runtime=&quot;../share/java/nice.jar&quot;/&gt;
	&lt;/target&gt;
</pre>


 * @author Alex Greif <a href="mailto:alex.greif@web.de">alex.greif@web.de</a>
 */

public class Nicec extends Task {

	private static final String
		ERROR_MSG = "Compilation failed with errors.";
	private static final String
		BUG_MSG = "Compilation failed because of a bug in the compiler.";
	private static final String
		WARNING_MSG = "Compilation successful despite warnings.";
	private static final String
		OK_MSG = "Compilation successful.";
		


	/**	Search path for source packages
	 PATH is a list of directories and .jar archives
	 */
	private String sourcepath;

	public void setSourcepath(String sourcepath)
	{
		this.sourcepath = sourcepath;
	}


	/**	Destination directory for compiled packages
	 */
	private File destination;

	public void setDestination(File destination)
	{
		this.destination = destination;
	}


	/**	Search path for compiled packages and libraries
	 */
	private String classpath = "";

	public void setClasspath(String classpath)
	{
		this.classpath = classpath;
	}

	/**	Compile to archive
	 You can then run the program with 'java -jar FILE'
	 */
	private String jar;

	public void setJar(String jar)
	{
		this.jar = jar;
	}


	/**	Generate native executable
	 */
	private String output;

	public void setOutput(String output)
	{
		this.output = output;
	}


	/**	Force recompilation of package
	 */
	private boolean recompile;

	public void setRecompile(boolean recompile)
	{
		this.recompile = recompile;
	}


	/**	Force recompilation of all dependant packages
	 */
	private boolean recompile_all;

	public void setRecompile_all(boolean recompile_all)
	{
		this.recompile_all = recompile_all;
	}


	/**	Compile packages but do not link them
	 */
	private boolean compile;

	public void setCompile(boolean compile)
	{
		this.compile = compile;
	}


	/**	Avoid inclusion of the runtime in the archive
	 */
	private boolean exclude_runtime;

	public void setExclude_runtime(boolean exclude_runtime)
	{
		this.exclude_runtime = exclude_runtime;
	}

	/**	Location of nice.jar
	 */
	private String runtime = null;

	public void setRuntime(String runtime)
	{
		this.runtime = runtime;
	}

	/**	Location of the native compiler binary (gcj)
	 */
	private String native_compiler;

	public void setNative_compiler(String native_compiler)
	{
		this.native_compiler = native_compiler;
	}


	/**	Tell nicec that it is called by an editor.
	 */
	private boolean editor;

	public void setEditor(boolean editor)
	{
		this.editor = editor;
	}


	/**	The package to compile
	 */
	private String pack;

	public void setPackage(String pack)
	{
		this.pack = pack;
	}



	private Path nestedClasspath = null;

    /**
     * Creates a nested classpath element
     */
    public Path createClasspath() {
    	nestedClasspath = new Path(project);
        return nestedClasspath.createPath();
    }










	/**	Executes the ant Nice compiler.
	 */
	public void execute() throws BuildException {
	  log("runtime: " + runtime, Project.MSG_VERBOSE);

	  nice.tools.compiler.console.ConsoleOutput console = 
	    nice.tools.compiler.console.fun.consoleOutput();
	  Compilation compilation = bossa.modules.fun.createCompilation(console);
	  if (sourcepath != null)
	    compilation.sourcePath = sourcepath;
	  if (destination != null)
	    compilation.destinationDir = destination.getAbsolutePath();
	  compilation.packagePath = classpath + (nestedClasspath != null ? File.pathSeparator+nestedClasspath : "");
	  compilation.output = jar;
	  compilation.recompileCommandLine = recompile;
	  compilation.recompileAll = recompile_all;
	  compilation.skipLink = compile;
	  compilation.excludeRuntime = exclude_runtime;
	  compilation.runtimeFile = runtime;
	  nice.tools.compiler.fun.compile
	    (compilation, pack, output, native_compiler, editor);
	  int retval = console.statusCode;

	  switch (retval) {
	  case nice.tools.compiler.console.fun.ERROR:
	    throw new BuildException(ERROR_MSG, location);
	  case nice.tools.compiler.console.fun.BUG:
	    throw new BuildException(BUG_MSG, location);
	  case nice.tools.compiler.console.fun.WARNING:
	    log(WARNING_MSG, Project.MSG_WARN);
	    break;
	  case nice.tools.compiler.console.fun.OK:
	    log(OK_MSG, Project.MSG_INFO);
	    break;
	  }
	}



	/** Only for test usage.
	 */
	public static void main(String[] args) {
		Nicec nicec = new Nicec();
		nicec.setRuntime("../share/java/nice.jar");
		nicec.setPackage("test");
		nicec.execute();
	}



}
