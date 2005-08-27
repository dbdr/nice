/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2001                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package bossa.modules;

import bossa.util.*;
import java.io.*;

import gnu.bytecode.ClassType;

/**
   A package located in a directory.

   @version $Date$
   @author Daniel Bonniot
 */

class DirectorySourceContent extends SourceContent
{
  static DirectorySourceContent create(Package pkg, File directory)
  {
    if (!directory.exists())
      return null;

    DirectorySourceContent res = new DirectorySourceContent(pkg, directory);
    if (res.isValid())
      return res;

    return null;
  }

  static DirectorySourceContent create(Package pkg, java.net.URL url)
  {
    if (! url.getProtocol().equals("file"))
      User.error("Cannot use " + url + " to get sources for package " + pkg);

    return create(pkg, new File(url.getFile()));
  }

  DirectorySourceContent(Package pkg, File directory)
  {
    this.pkg = pkg;
    this.directory = directory;

    this.sources = getSources();
    lastModification = maxLastModification(sources);
  }

  private File[] sources;

  /** @return true if this directory indeed hosts a Nice package. */
  private boolean isValid()
  {
    return sources.length > 0;
  }

  Content.Unit[] getDefinitions()
  {
    if (sources.length == 0)
      User.error(pkg.name, 
		 "Package " + pkg.getName() + 
		 " has no source file in " +
		 nice.tools.util.System.prettyPrint(directory));
    
    sourcesRead = true;
    Content.Unit[] res = new Content.Unit[sources.length];
    for(int i = 0; i<res.length; i++)
      res[i] = new Content.Unit(read(sources[i]), sources[i]);
    
    return res;
  }
  
  private long maxLastModification(File[] files)
  {
    long res = 0;
    for(int i = 0; i<files.length; i++)
      {
	long time = files[i].lastModified();
	if (time>res) res = time;
      }
    return res;
  }
  
  private File[] getSources()
  {
    File[] res = directory.listFiles
      (new FileFilter()
	{ 
	  public boolean accept(File f)
	  { return f.getPath().endsWith(sourceExtension) && f.isFile(); }
	}
       );
    if (res == null)
      User.error(pkg, "Could not list source files in " + getName());
    
    // put nice.lang.prelude first if it exists
    if (pkg.name.toString().equals("nice.lang"))
      for(int i = 0; i<res.length; i++)
	if (res[i].getName().equals("prelude" + sourceExtension))
	  {
	    File tmp = res[i];
	    res[i] = res[0];
	    res[0] = tmp;
	    break;
	  }
    
    return res;
  }

  private BufferedReader read(File f)
  {
    try{
      String encoding = pkg.compilation.sourceEncoding;
      if(encoding != null) try{
        FileInputStream fis = new FileInputStream(f);
        // No need to wrap the FileInputStream with BufferedInputStream here:
        // InputStreamReader already contains an input buffer, both in Sun and GNU implementations.
        InputStreamReader isr = new InputStreamReader(fis, encoding);
        return new BufferedReader(isr);
      }catch(UnsupportedEncodingException badEncoding){
        User.warning("Encoding '" + encoding + "' was rejected while reading " +
                     nice.tools.util.System.prettyPrint(f));
      }
      return new BufferedReader(new FileReader(f));
    }
    catch(FileNotFoundException e){
      User.error(nice.tools.util.System.prettyPrint(f) +
		 " of package " + pkg.getName() + " could not be found");
      return null;
    }
  }
  
  File getOutputDirectory()
  {
    return directory;
  }
  
  public String getName()
  {
    return nice.tools.util.System.prettyPrint(directory);
  }
  
  public String toString()
  {
    return "Source files in: " + getName();
  }
  
  private Package pkg;
  private File directory;

  final static String sourceExtension = ".nice";
}
