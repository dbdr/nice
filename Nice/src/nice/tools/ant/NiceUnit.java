/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2004                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package nice.tools.ant;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import java.io.File;
import java.util.Vector;
import bossa.modules.Compilation;

/**
		<h2><a name="java">NiceUnit</a></h2>
		<h3>Description</h3>
		<p>Runs unit tests for a Nice package.</p>
		All arguments to the NiceUnit task have to be placed as attributes in the niceunit xml-element.
		<h3>Parameters</h3>
		<table border="1" cellpadding="2" cellspacing="0">
			<tr>
				<td valign="top"><b>Attribute</b></td>
				<td valign="top"><b>Description</b></td>
				<td align="center" valign="top"><b>Required</b></td>
			</tr>
			<tr>
				<td valign="top">package</td>
				<td valign="top">The Nice package to test.</td>
				<td align="center" valign="top">Yes</td>
			</tr>
			<tr>
				<td valign="top">classpath</td>
				<td valign="top">Search path for compiled packages and libraries.</td>
				<td align="center" valign="top">No</td>
			</tr>
		</table>

		<h4>classpath</h4>
		<p><code>NiceUnit</code>'s <i>classpath</i> attribute is a PATH like structure and can also be set via a nested
		<i>classpath</i> element. This is very reasonable if you want to make your build script's pathes platform
		independent. </p>
		<h5>Example</h5>
<pre>
	&lt;niceunit package=&quot;test&quot; &gt;
	  &lt;classpath&gt;
	    &lt;pathelement location=&quot;\test.jar&quot;/&gt;
	    &lt;pathelement path=&quot;${java.class.path}&quot;/&gt;
	  &lt;/classpath&gt;
	&lt;/niceunit&gt;
</pre>
		<p>It is possible to use the <i>classpath</i> attribute together with the
		<i>classpath<i> nested tag. In this case the result is a concatenated path.</p>
		<p>It is highly recommended to use the nested version!<p>


		<h3>Examples</h3>
<pre>
	&lt;taskdef name=&quot;niceunit&quot; classname=&quot;nice.tools.ant.NiceUnit&quot;/&gt;
	&lt;target name=&quot;nice-tests&quot;&gt;
	  &lt;niceunit package=&quot;test&quot; /&gt;
	&lt;/target&gt;
</pre>


 * @author Daniel Bonniot
 */

public class NiceUnit extends Task {


	/**	Search path for compiled packages and libraries.
	 */
	private String classpath = "";

	public void setClasspath(String classpath)
	{
		this.classpath = classpath;
	}

	/**	Location of nice.jar
	 */
	private String runtime = null;

	public void setRuntime(String runtime)
	{
		this.runtime = runtime;
	}

	/**	The package to test.
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


	/**	Executes niceunit.
	 */
	public void execute() throws BuildException {

		String oldUserDir = System.getProperty("user.dir");
		try {
			System.setProperty("user.dir", 
												 project.getBaseDir().getAbsolutePath());

			TestListener listener = new TestListener(this);

			String classpath = this.classpath + 
				(nestedClasspath != null ? File.pathSeparator+nestedClasspath : "");

			if (! nice.tools.unit.fun.runTests(pack, listener, classpath, null))
				throw new BuildException("Package " + pack + " was not found");

			listener.printSummary();
		}
		finally {
			System.setProperty("user.dir", oldUserDir);
		}
	}

}


// Setting for Emacs
// Local variables:
// tab-width:2
// indent-tabs-mode:t
// End:
