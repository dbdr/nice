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

package gnu.expr;

import gnu.bytecode.*;

import java.io.File;
import java.io.FileOutputStream;

/**
   A set of classes to generate as a java-like package.

   Extends expression to make it possible to walk the package.
   The walk is performed on each class of the package.

   @version $Date$
   @author Daniel Bonniot (Daniel.Bonniot@inria.fr)
 */

public class Package extends Expression
{
  /** Create a Bytecode Package.

      @param name qualified name of the package (e.g. "my.package")
  */
  public Package (String name)
  {
    this.name = name;
    this.prefix = name.replace('.', '/') + "/";
  }

  public void print (gnu.mapping.OutPort ps)
  {
    ps.print("Package " + name);
  }

  public void compile (Compilation comp, Target target)
  {
    throw new Error("A package cannot be evaluated");
  }

  protected void walkChildren (ExpWalker walker)
  {
    for (LambdaExp c = firstClass; c != null; c = c.nextSibling)
      c.walk(walker);
  }

  public void addClass(ClassExp classe)
  {
    classe.nextSibling = firstClass;
    firstClass = classe;
  }
  
  ClassExp firstClass;

  void addClass(ClassType c)
  {
    classes.add(c);
  }

  /** Search this package for a class with a given name.
   * @param name the name of the class desired
   * @return the matching ClassType, or null if none is found */
  public ClassType findNamedClass (String name)
  {
    for (int i = classes.size();  --i >= 0;)
      {
	if (name.equals (((ClassType) classes.get(i)).getName ()))
	  return (ClassType) classes.get(i);
      }
    return null;
  }

  private java.util.Vector classes = new java.util.Vector(10);

  public Package next;

  /** Qualified name, e.g. "my.package". */
  public final String name; 

  /** "my/package/" */
  public final String prefix;

  public File directory;

  public void compileToFiles ()
    throws java.io.IOException
  {
    Compilation comp = new Compilation();
    comp.compile(this);

    for (Package p = this; p != null; p = p.next)
      p.writeClasses();
  }

  private void writeClasses()
    throws java.io.IOException
  {
    for(java.util.Iterator i = classes.iterator(); i.hasNext();)
      {
	ClassType clas = (ClassType) i.next();
	String name = clas.getName();
	int lastDot = name.lastIndexOf('.');
	if (lastDot != -1)
	  {
	    if (!name.substring(0, lastDot).equals(this.name))
	      throw new Error("Class generated in the wrong package: " + 
			      name + 
			      " was found in package " + 
			      this.name);

	    name = name.substring(lastDot + 1);
	  }

	File out = new File(directory, name + ".class");
	out.getParentFile().mkdirs();
	clas.writeToFile(out);
      }
  }
}
