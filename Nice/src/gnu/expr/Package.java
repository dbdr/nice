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
  public Package (String name)
  {
    this.name = name;
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
  
  public void addMainClass(ClassExp classe)
  {
    addClass(classe);
    mainClass = classe;
  }
  
  ClassExp firstClass;

  /** A special class... */
  ClassExp mainClass;

  public void addClass(ClassType c)
  {
    classes.add(c);
  }

  private java.util.Vector classes = new java.util.Vector(10);

  public Package next;

  /** Qualified name, e.g. "my.package". */
  public String name; 

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
	      throw new Error("Class generate in the wrong package: " + 
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
