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
    
    this.module = module;
    this.definitions = defs;
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
    if (children == null)
      return;

    // Classes are resolved first, since code can depend on them
    for(Iterator i = children.iterator();i.hasNext();)
      {
	Node n = (Node) i.next();
	if (n instanceof ClassDefinition)
	  resolve(n);
      }
    for(Iterator i = children.iterator();i.hasNext();)
      {
	Node n = (Node) i.next();
	if (!(n instanceof ClassDefinition))
	  resolve(n);
      }
    nice.tools.compiler.OutputMessages.exitIfErrors();
  }
  
  public void typechecking()
  {
    for(Iterator i = definitions.iterator(); i.hasNext();)
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

    module.unfreezeGlobalContext();
    for(Iterator i = definitions.iterator(); i.hasNext();)
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
    module.freezeGlobalContext();
    doTypecheck();
  }

  public void printInterface(java.io.PrintWriter s)
  {
    for(Iterator i = definitions.iterator(); i.hasNext();)
      ((Definition) i.next()).printInterface(s);
  }

  public void compile()
  {
    for(Iterator i = definitions.iterator();i.hasNext();)
      ((Definition)i.next()).compile();
  }
  
  public String toString()
  {
    return "Abstract Syntax Tree ("+definitions.size()+" definitions)";
  }

  private Module module;
  private List /* of Definition */ definitions;
}

