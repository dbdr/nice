package nice.tools.ant;



import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.*;
import org.apache.tools.ant.types.Commandline;
import java.io.*;


/**

		<h2><a name="java">Nicec</a></h2>
		<h3>Description</h3>
		<p>Runs the Nice compiler.</p>
		This Task extends the Java Task because calling the Nicec compiler from the command line looks like
		<pre>java -classpath ../nice.jar -Dnice.systemJar=../nice.jar -Dnice.runtime=../nice.jar     nice.tools.compiler.package &quot;$@&quot;</pre>
		<p>Allmost all parameters of the Java Task can be uses in this task with the following restrictions</p>
		<ul>
			<li>the nested &lt;arg&gt; attribute is ignored, the Nice package is automatically the argument to the Java command
			<li>running the Nice compiler task automatically forks a new VM, otherwise we could not use additional jvm arguments
			<li>the classname parameter is ignored, because the main class of the Nice compiler is always the Java class to execute.
		</ul>
		<p>All arguments to the Nice compiler Task has to be placed as attributes in the nicec xml-element. Additional parameters to the Java task can be nested in the nicec element or also put as an attribute in the nicec element itself. All Parameters of the Nice compiler that are already used by the Java task are prefixed with &quot;nice_&quot; or &quot;nicec_&quot;.</p>
		<h3>Parameters</h3>
		<table border="1" cellpadding="2" cellspacing="0">
			<tr>
				<td valign="top"><b>Attribute</b></td>
				<td valign="top"><b>Description</b></td>
				<td align="center" valign="top"><b>Required</b></td>
			</tr>
			<tr bgcolor="#dedede">
				<td colspan="3" valign="top"><b>Java parameters</b></td>
			</tr>
			<tr>
				<td valign="top">classname</td>
				<td valign="top">the Java class to execute.<br>
					<font color="red">Ignored, because the main class of the Nice compiler is always the Java class to execute.</font></td>
				<td align="center" valign="top">Either jar or classname</td>
			</tr>
			<tr>
				<td valign="top">jar</td>
				<td valign="top">the location of the jar file to execute (must have a Main-Class entry in the manifest). Fork must be set to true if this option is selected.</td>
				<td align="center" valign="top">Either jar or classname</td>
			</tr>
			<tr>
				<td valign="top">args</td>
				<td valign="top">the arguments for the class that is executed. <b>deprecated, use nested <code>&lt;arg&gt;</code> elements instead.<br>
					</b><font color="red">Ignored, because the Nicec package is the only argument.</font></td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">classpath</td>
				<td valign="top">the classpath to use.<br>
					<font color="red">The nice.jar file must be included.</font></td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">classpathref</td>
				<td valign="top">the classpath to use, given as <a href="../using.html#references">reference</a> to a PATH defined elsewhere.</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">fork</td>
				<td valign="top">if enabled triggers the class execution in another VM (disabled by default).<br>
					<font color="red">Ignored, because the Nice compiler always sets this flag to true.</font></td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">jvm</td>
				<td valign="top">the command used to invoke the Java Virtual Machine, default is 'java'. The command is resolved by java.lang.Runtime.exec(). Ignored if fork is disabled.</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">jvmargs</td>
				<td valign="top">the arguments to pass to the forked VM (ignored if fork is disabled). <b>deprecated, use nested <code>&lt;jvmarg&gt;</code> elements instead.<br>
					</b><font color="red">The values of the following jvm arguments must be supported:</font>
					<ul>
						<li><font color="red">nice.systemJar</font>
						<li><font color="red">nice.runtime</font>
					</ul>
				</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">maxmemory</td>
				<td valign="top">Max amount of memory to allocate to the forked VM (ignored if fork is disabled)</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">failonerror</td>
				<td valign="top">Stop the buildprocess if the command exits with a returncode other than 0. Only available if fork is true.</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">dir</td>
				<td valign="top">The directory to invoke the VM in. (ignored if fork is disabled)</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">output</td>
				<td valign="top">Name of a file to write the output to.</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr bgcolor="#dedede">
				<td colspan="3" valign="top"><b>Nicec parameters</b></td>
			</tr>
			<tr>
				<td valign="top">package</td>
				<td valign="top">The Nice package to compile.</td>
				<td align="center" valign="top">Yes</td>
			</tr>
			<tr>
				<td valign="top">nice_jar</td>
				<td valign="top">Compile the Nice sources to archive.</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">sourcepath</td>
				<td valign="top">Lookup path for source packages. Is a list of directories and .jar archives.</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">destination</td>
				<td valign="top">Destination directory for compiled packages.</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">packagepath</td>
				<td valign="top">Additional lookup path for compiled packages. Is a list of directories and .jar archives</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">nicec_classpath</td>
				<td valign="top">Lookup path for external classes.</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">nicec_output</td>
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
				<td valign="top">Force recompilation of all dependant packages</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">exclude_runtime</td>
				<td valign="top">Avoid inclusion of the runtime in the archive.</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">help</td>
				<td valign="top">Print help message and exit.</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">man</td>
				<td valign="top">Print man page to stdout.</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">version</td>
				<td valign="top">Print version info and exit.</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">usage</td>
				<td valign="top">Print usage information and exit.</td>
				<td align="center" valign="top">No</td>
			</tr>
			<tr>
				<td valign="top">memory</td>
				<td valign="top">Print memory usage information after compilation.</td>
				<td align="center" valign="top">No</td>
			</tr>
		</table>
		<h3>Examples</h3>
		<pre>  		&lt;property name=&quot;nice.jar&quot; value=&quot;../nice.jar&quot;/&gt;

    <b>&lt;nicec package=&quot;test&quot; nice_jar=&quot;test.jar&quot; memory=&quot;true&quot;&gt;</b>
        &lt;classpath&gt;
    				    &lt;pathelement location=&quot;${nice.jar}&quot;/&gt;
     			&lt;/classpath&gt;
			     &lt;jvmarg value=&quot;<b>-Dnice.systemJar</b>=${nice.jar}&quot;/&gt;
    			 &lt;jvmarg value=&quot;<b>-Dnice.runtime</b>=${nice.jar}&quot;/&gt;
  		&lt;/nicec&gt;
</pre>





 * @author Alex Greif <a href="mailto:alex.greif@web.de">alex.greif@web.de</a>
 */

public class Nicec extends Java {



	/**	Lookup path for source packages
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


	/**	Additional lookup path for compiled packages
	 PATH is a list of directories and .jar archives
	 */
	private String packagepath;

	public void setPackagepath(String packagepath)
	{
		this.packagepath = packagepath;
	}


	/**	Lookup path for external classes
	 */
	private String nicec_classpath;

	public void setNicec_classpath(String nicec_classpath)
	{
		this.nicec_classpath = nicec_classpath;
	}

	/**	Compile to archive
	 You can then run the program with 'java -jar FILE'
	 */
	private String nice_jar;

	public void setNice_jar(String nice_jar)
	{
		this.nice_jar = nice_jar;
	}


	/**	Generate native executable
	 */
	private String nicec_output;

	public void setOutput(String nicec_output)
	{
		this.nicec_output = nicec_output;
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


	/**	Print help message and exit
	 */
	private boolean help;

	public void setHelp(boolean help)
	{
		this.help = help;
	}


	/**	Print man page to stdout
	 */
	private boolean man;

	public void setMan(boolean man)
	{
		this.man = man;
	}


	/**	Print version info and exit
	 */
	private boolean version;

	public void setVersion(boolean version)
	{
		this.version = version;
	}


	/**	Print usage information and exit
	 */
	private boolean usage;

	public void setUsage(boolean usage)
	{
		this.usage = usage;
	}


	/**	Print memory usage information after compilation
	 */
	private boolean memory;

	public void setMemory(boolean memory)
	{
		this.memory = memory;
	}

	/**	The package to compile
	 */
	private String pack;

	public void setPackage(String pack)
	{
		this.pack = pack;
	}















	/**	Flag whether the java argument is specified in the buildfile.
	 In this case this message will be logged.
	 */
	private boolean java_arg_specified;

	/**
	 * Creates a nested arg element.
	 */
	public Commandline.Argument createArg() {
		java_arg_specified = true;

		return super.createArg();
	}


	/**
	 * Set the forking flag.
	 */
	public void setFork(boolean s) {
		if (!s)
			log("The Nice compiler allways forks another VM. " +
					"The value of the fork attribute is ignored.");
		super.setFork(true);
	}



	/**	The main class of the Nice compiler.
	 */
	private static final String NICEC_CLASS = "nice.tools.compiler.package";



	/**
	 * Set the class name.
	 */
	public void setClassname(String s) throws BuildException {
		log("The Nice compiler runs the main class of the Nice compiler.");
		super.setClassname(NICEC_CLASS);
	}



	/**	Error message in the case that the arg of the java command
	 should be specified.
	 */
	private static final String JAVA_ARG_SPECIFIED_MSG =
		"The nested arg element is not supported. " +
		"Please use the package attribute instead.";




	/**	Executes the ant Nice compiler. Calls the Java task with the
	 appropriate parameters and arguments.
	 */
	public void execute() throws BuildException {

		if (java_arg_specified) {
			log(JAVA_ARG_SPECIFIED_MSG, Project.MSG_ERR);
			throw new BuildException(JAVA_ARG_SPECIFIED_MSG, location);
		}
		setClassname(NICEC_CLASS);
		setFork(true);

		if (sourcepath != null) {
			createArg().setValue("--sourcepath");
			createArg().setValue(sourcepath);
		}
		if (destination != null) {
			createArg().setValue("--destination");
			createArg().setValue(destination.getAbsolutePath());
		}
		if (packagepath != null) {
			createArg().setValue("--packagepath");
			createArg().setValue(packagepath);
		}
		if (nicec_classpath != null) {
			createArg().setValue("--classpath");
			createArg().setValue(nicec_classpath);
		}
		if (nice_jar != null) {
			createArg().setValue("--jar");
			createArg().setValue(nice_jar);
		}
		if (nicec_output != null) {
			createArg().setValue("--output");
			createArg().setValue(nicec_output);
		}
		if (recompile) {
			createArg().setValue("--recompile");
		}
		if (recompile_all) {
			createArg().setValue("--recompile-all");
		}
		if (compile) {
			createArg().setValue("--compile");
		}
		if (exclude_runtime) {
			createArg().setValue("--exclude-runtime");
		}
		if (help) {
			createArg().setValue("--help");
		}
		if (man) {
			createArg().setValue("--man");
		}
		if (version) {
			createArg().setValue("--version");
		}
		if (usage) {
			createArg().setValue("--usage");
		}
		if (memory) {
			createArg().setValue("--memory");
		}
		if (pack != null) {
			createArg().setValue(pack);
		}
		super.execute();
	}



}


