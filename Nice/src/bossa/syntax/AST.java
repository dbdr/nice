/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2000                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package bossa.syntax;

import bossa.util.*;

import java.util.*;

/**
   The Abstract Syntax Tree : a collection of definitions.

   @see Definition

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
 */
public class AST extends Node
{
  public AST(Module module, List defs)
  {
    super(defs,Node.global);
    if (children == null)
      children = new LinkedList();

    this.module = module;
    findClasses();
  }

  private void findClasses()
  {
    ArrayList c = new ArrayList(children.size());
    for(Iterator i = children.iterator(); i.hasNext();)
      {
	Object node = i.next();
	if (node instanceof ClassDefinition)
	  c.add((ClassDefinition) node);
      }
    classes = (ClassDefinition[]) c.toArray(new ClassDefinition[c.size()]);
  }
  
  public void buildScope()
  {
    buildScope(module);
  }
  
  private void resolve(Node n)
  {
    try{
      n.doResolve();
    }
    catch(UserError ex){
      nice.tools.compiler.OutputMessages.error(ex.getMessage());
    }
  }

  public void resolveScoping()
  {
    Node.setModule(module);
    Location.setCurrentFile(module.toString());
    Location.current = Location.nowhere();

    // Classes are resolved first, since code can depend on them
    for(int i = 0; i < classes.length; i++)
      resolve(classes[i]);

    for(Iterator i = children.iterator();i.hasNext();)
      {
	Node n = (Node) i.next();
	if (!(n instanceof ClassDefinition))
	  resolve(n);
      }

    nice.tools.compiler.OutputMessages.exitIfErrors();
  }

  public void typedResolve()
  {
    Node.setModule(module);

    for(Iterator i = children.iterator(); i.hasNext();)
      {
	Object o = i.next();
	if (o instanceof MethodBodyDefinition)
	  try{
	    ((MethodBodyDefinition) o).lateBuildScope();
	  }
	  catch(UserError ex){
	    nice.tools.compiler.OutputMessages.error(ex.getMessage());
	  }
      }
    nice.tools.compiler.OutputMessages.exitIfErrors();
  }

  public void localResolve()
  {
    Node.setModule(module);

    for (Iterator i = children.iterator(); i.hasNext();)
      {
	Definition d = (Definition) i.next();
	try{
	  d.resolveBody();
	}
	catch(UserError ex){
	  nice.tools.compiler.OutputMessages.error(ex.getMessage());
	}
      }

    nice.tools.compiler.OutputMessages.exitIfErrors();

    for (int i = 0; i < classes.length; i++)
      classes[i].precompile();
  }

  public void typechecking()
  {
    Node.setModule(module);

    // Classes are typechecked first, since code can depend on them.
    for(int i = 0; i < classes.length; i++)
      classes[i].typecheckClass();

    doTypecheck();
  }

  public void printInterface(java.io.PrintWriter s)
  {
    for(Iterator i = children.iterator(); i.hasNext();)
      ((Definition) i.next()).printInterface(s);
  }

  /**
     @param generateCode
       false if the current module was already compiled and up-to-date.
  */
  public void compile(boolean generateCode)
  {
    if (! generateCode)
      {
	for (int i = 0; i < classes.length; i++)
	  classes[i].recompile();
      }
    else
      {
	for(Iterator i = children.iterator();i.hasNext();)
	  ((Definition)i.next()).compile();
      }
  }
  
  public String toString()
  {
    return "Abstract Syntax Tree ("+children.size()+" definitions)";
  }

  private Module module;
  private ClassDefinition[] classes;
}

