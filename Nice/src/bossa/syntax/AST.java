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

    findElements();
  }

  public List definitions()
  {
    return children;
  }

  private void findElements()
  {
    ArrayList classes = new ArrayList(children.size());
    ArrayList methods = new ArrayList(children.size());
    ArrayList globals = new ArrayList(10);

    for(Iterator i = children.iterator(); i.hasNext();)
      {
	Object node = i.next();
	if (node instanceof ClassDefinition)
	  classes.add(node);
        else if (node instanceof MethodDeclaration)
          methods.add(node);
        else if (node instanceof EnumDefinition)
          classes.add(((EnumDefinition)node).classDef);
        else if (node instanceof GlobalVarDeclaration)
          globals.add(node);
        else if (node instanceof DefaultMethodImplementation)
          methods.add(((DefaultMethodImplementation) node).getDeclaration());
      }

    this.classes = (ClassDefinition[]) 
      classes.toArray(new ClassDefinition[classes.size()]);

    this.methods = (MethodDeclaration[]) 
      methods.toArray(new MethodDeclaration[methods.size()]);

    this.globals = (GlobalVarDeclaration[])
      globals.toArray(new GlobalVarDeclaration[globals.size()]);
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
      module.compilation().error(ex);
    }
  }

  public void resolveScoping()
  {
    Node.setModule(module);

    // Classes are resolved first, since code can depend on them
    for(int i = 0; i < classes.length; i++)
      resolve(classes[i]);

    for(Iterator i = children.iterator();i.hasNext();)
      {
	Node n = (Node) i.next();
	if (!(n instanceof ClassDefinition))
	  resolve(n);
      }

    module.compilation().exitIfErrors();
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
            module.compilation().error(ex);
	  }
      }

    for (int i = 0; i < methods.length; i++)
      try{
        methods[i].typedResolve();
      }
      catch(UserError ex){
        module.compilation().error(ex);
      }

    module.compilation().exitIfErrors();
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
          module.compilation().error(ex);
	}
      }

    module.compilation().exitIfErrors();

    for (int i = 0; i < classes.length; i++)
      classes[i].precompile();
  }

  public void typechecking(boolean compiling)
  {
    Node.setModule(module);

    // Classes are typechecked first, since code can depend on them.
    for (int i = 0; i < classes.length; i++)
      classes[i].typecheckClass();

    if (! compiling)
      {
        for (int i = 0; i < methods.length; i++)
          methods[i].typecheckCompiled();

        return;
      }

    doTypecheck();

    module.compilation().exitIfErrors();
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
        // Globals are compiled first, so that we can find out their 
        // dependencies, and initialize them in the right order.
	for (int i = 0; i < globals.length; i++)
	  globals[i].precompile();

	for(Iterator i = children.iterator();i.hasNext();)
	  ((Definition)i.next()).compile();
      }
  }
  
  public String toString()
  {
    return "Abstract Syntax Tree (" + numberOfDeclarations() +" declarations)";
  }

  public int numberOfDeclarations()
  {
    return children.size();
  }

  private Module module;
  private ClassDefinition[] classes;
  private MethodDeclaration[] methods;
  private GlobalVarDeclaration[] globals;
}

