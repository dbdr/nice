/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2002                         */
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

import mlsub.typing.Interface;
import mlsub.typing.Variance;
import mlsub.typing.Typing;

/**
   Declaration of an abstract interface (i.e. a ML-Sub interface).

   @version $Date$
   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */
public class AbstractInterface extends MethodContainer
{
  public AbstractInterface(LocatedString name, 
			   List typeParameters, 
			   List typeParametersVariances,
			   List extensions
			  )
  {
    super(name, Node.global, typeParameters, typeParametersVariances);

    this.extensions = extensions;

    itf = new Interface(variance, name.toString());
    addTypeSymbol(itf);
  }

  public Collection associatedDefinitions()
  {
    return null;
  }

  public void addMethod(MethodDeclaration m)
  {
    addChild(m);
  }
  
  mlsub.typing.TypeSymbol getTypeSymbol()
  {
    return itf;
  }
  
  /****************************************************************
   * Resolution
   ****************************************************************/

  public void resolve()
  {
    surinterfaces = TypeIdent.resolveToItf(typeScope, extensions);
    extensions = null;
  }

  void resolveBody()
  {
    if (children != null)
      for (Iterator i = children.iterator(); i.hasNext();)
	{
	  Object child = i.next();
	  if (child instanceof ToplevelFunction)
	    ((ToplevelFunction) child).resolveBody();
	}
  }

  /****************************************************************
   * Initial Context
   ****************************************************************/

  public void createContext()
  {
    if (surinterfaces != null)
      try{
	Typing.assertLeq(itf, surinterfaces);
      }
      catch(mlsub.typing.KindingEx e){
	User.error(name, name + " cannot extends one of the surinterfaces " +
		   " because they don't have the same number or kind of " +
		   " type parameters");
      }

    if (children != null)
      for(Iterator i = children.iterator(); i.hasNext(); )
	{
	  Object o = i.next();
	  if (o instanceof Definition)
	    ((Definition) o).createContext();
	}
  }
   
  /****************************************************************
   * Module interface
   ****************************************************************/

  public void printInterface(java.io.PrintWriter w)
  {
    w.print("abstract interface "
            + getSimpleName()
            + Util.map("<",", ",">", typeParameters)
            + Util.map(" extends ",", ","", surinterfaces)
            + "{}\n");

    if (children != null)
      for(Iterator i = children.iterator(); i.hasNext(); )
	{
	  Object o = i.next();
	  if (o instanceof Definition)
	    ((Definition) o).printInterface(w);
	}
  }
  
  /****************************************************************
   * Code generation
   ****************************************************************/

  public void compile()
  {
    // Compile children methods and functions.
    if (children != null)
      for(Iterator i = children.iterator(); i.hasNext(); )
	{
	  Object o = i.next();
	  if (o instanceof Definition)
	    ((Definition) o).compile();
	}
  }

  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return name.toString();
  }

  /** ML-Sub interface. */
  private Interface itf;
  
  private List /* of Interface */ extensions; // the surinterfaces
  private Interface[] surinterfaces; // resolved surinterfaces
}
