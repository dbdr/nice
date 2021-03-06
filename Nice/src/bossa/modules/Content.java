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
import java.util.*;
import java.io.*;
import nice.tools.util.System;
import bossa.syntax.Definition;
import bossa.syntax.LocatedString;

/**
   An abstract package source, where source or interface files 
   and compiled code if applicable can be found.
   
   @version $Date$
   @author Daniel Bonniot
 */

class Content
{
  Content(Package pkg, SourceContent source, CompiledContent compiled)
  {
    this.pkg = pkg;
    this.source = source;
    this.compiled = compiled;

    if (source == null && compiled == null) 
      User.error(pkg, "Package " + pkg.getName() + " is not available." +
		 "\nThe source  path is: " + str(pkg.compilation.sourcePath) + 
		 "\nThe package path is: " + str(pkg.compilation.packagePath));

    // By default, use the last compilation time also as last modification time
    // This is a safe bet, and will be overwritten if source is also available.
    if (compiled != null)
      lastModification = lastCompilation = compiled.lastCompilation;

    if (source != null)
      lastModification = source.lastModification;
  }

  private String str(String s)
  {
    return s == null ? "<empty>" : s;
  }

  List getImports(Set opens, boolean shouldReload)
  {
    Content.Unit[] readers;
    /*
       If the package is up to date, preferably load imports from
       the compiled package, since that involves touching only one file
       instead of possiby several.
    */
    if (source != null &&
	(shouldReload || compiled == null ||
         lastModification > lastCompilation))
      readers = source.getDefinitions();
    else
      {
        pkg.compiledModule =
          new bossa.syntax.Module(pkg, pkg.getName(), pkg.packageScope);
        readers = compiled.getDefinitions();
      }

    LinkedList imports = new LinkedList();

    for (int i = 0; i < readers.length; i++)
      read(readers[i], imports, opens);

    return imports;
  }

  void getDefinitions(List definitions, boolean shouldReload)
  {
    Content.Unit[] readers = getReaders(shouldReload);

    for(int i = 0; i<readers.length; i++)
      try{
        read(readers[i], definitions);
      }
      catch(UserError ex){
        pkg.compilation.error(ex);
      }

    pkg.compilation.exitIfErrors();
  }

  private void read(Content.Unit unit, List imports, Set opens)
  {
    bossa.util.Location.setCurrentFile(unit.file);

    bossa.syntax.LocatedString pkgName =
      pkg.compilation.parser.readImports(unit.reader, imports, opens);

    if (pkgName != null && ! pkgName.equals(pkg.name))
      User.error(pkgName,
		 source + " declares it belongs to package " + pkgName +
		 ", but it was found in package " + pkg.name);
  }

  private void read(Content.Unit unit, List definitions)
  {
    bossa.util.Location.setCurrentFile(unit.file);

    bossa.syntax.Module module;

    if (pkg.compiledModule != null)
      module = pkg.compiledModule;
    else
      {
        nice.tools.visibility.Scope scope =
          new nice.tools.visibility.Scope(unit.getName(), pkg.packageScope);

        module = new bossa.syntax.Module(pkg, unit.name, scope);
      }

    Definition.currentModule = module;

    pkg.compilation.parser.read(unit.reader, module, definitions);

    Definition.currentModule = null;
  }

  /**
     @param shouldReload reload if the source if available.
   **/
  Content.Unit[] getReaders(boolean shouldReload)
  {
    // If no compiled version is available then reload
    shouldReload |= (compiled == null);

    // If the package is out-of-date, then we must recompile
    if (lastModification > lastCompilation)
      {
	if (Debug.modules && !shouldReload)
	  {
	    Debug.println(pkg + " compiled: " + System.date(lastCompilation));
	    Debug.println(pkg + " modified: " + System.date(lastModification));
	    Debug.println("Recompiling " + pkg);
	  }
	shouldReload = true;
      }

    // Reload if we should and can
    if (shouldReload && source != null)
      {
	sourceRead = true;
	return source.getDefinitions();
      }

    // We're not reloading. Did we have to?
    if (lastModification > lastCompilation)
      User.error(pkg, pkg.getName() + " must be compiled, but no source code is available.\nThe source path is: " + pkg.compilation.sourcePath);
    
    if (Debug.modules)
      Debug.println("Loading compiled version of " + pkg);

    return compiled.getDefinitions();
  }

  
  /** return a short name to display this package source
      (typically a file name, an URL, ...) */
  public String getName()
  {
    if (sourceRead)
      return source.getName();
    else if (compiled != null)
      return compiled.getName();
    else
      return "";
  }

  /** return a longer string that identifies the type of package source too. */
  public String toString()
  {
    return "Source  : " + (source == null ? "none" : source.getName()) + 
         "\nCompiled: " + (compiled == null ? "none" : compiled.getName());
  }

  boolean sourceRead;

  /** Date of the last modification of the source of this package. */
  long lastModification;

  /** Date of the last succesful compilation of this package. */
  long lastCompilation;

  void someImportModified(long date)
  {
    if (lastModification < date)
      lastModification = date;
  }

  gnu.bytecode.ClassType getBytecode()
  {
    if (sourceRead)
      return null;
    else
      return compiled.bytecode;
  }

  gnu.bytecode.ClassType getDispatch()
  {
    if (sourceRead)
      return null;
    else
      return compiled.dispatch;
  }

  gnu.bytecode.ClassType readClass(String name)
  {
    if (sourceRead)
      return null;
    else
      return compiled.readClass(name);
  }

  /** @return the directory in which to place generated files 
      of this package. 

      This is used even if the package was not recompiled,
      e.g. to know where to place the 'dispatch' class.
  */
  File getOutputDirectory()
  {
    String destDir = pkg.compilation.destinationDir;

    if (destDir == null)
      // Default: write in the source directory if it exists
      if (source != null)
	return source.getOutputDirectory();
      else
	// Otherwise do as if the destnation is the current directory
	destDir = ".";

    // Return the appropriate subdirectory of the destination dir
    File res = new File(destDir, 
			pkg.getName().replace('.', File.separatorChar));
    // Make sure that we can write there
    res.mkdirs();
    return res;
  }

  /****************************************************************
   * Private
   ****************************************************************/
  
  private Package pkg;
  private SourceContent source;
  private CompiledContent compiled;

  static protected class Unit
  {
    protected Unit(BufferedReader reader, File file)
    {
      this.reader = reader;
      this.file = file;
    }
    protected Unit(BufferedReader reader, String name)
    {
      this.reader = reader;
      this.name = name;
    }

    public String getName()
    {
      if (file != null)
        return file.toString();
      else
        return name;
    }

    BufferedReader reader;
    String name;
    File file;
  }
  
  static protected class Stream
  {
    Stream(InputStream stream, String name)
    {
      this.stream = stream;
      this.name = name;
    }

    public boolean equals(Object o)
    {
      return o instanceof Stream && name.equals(((Stream) o).name);
    }

    public int hashCode() { return name.hashCode(); }

    InputStream stream;
    String name;
  }

  /**
     Returns the compiled classes of this source.
  */
  Stream[] getClasses(boolean linkPerformed)
  {
    /* We first add the generated classes, then all those compiled classes
       that were not regenerated. The Set ensures that the previous version of
       regenerated classes is not included.
    */
    Set res = new HashSet();

    DirectoryCompiledContent.addClasses(res, getOutputDirectory());

    if (!sourceRead)
      compiled.addClasses(res);

    return (Stream[]) res.toArray(new Stream[res.size()]);
  }
}
