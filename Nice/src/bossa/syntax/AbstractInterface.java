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
import mlsub.typing.Typing;

/**
   Declaration of an abstract interface (i.e. a ML-Sub interface).

   @version $Date$
   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */
public class AbstractInterface extends MethodContainer
{
  public AbstractInterface(LocatedString name, 
			   MethodContainer.Constraint typeParameters, 
			   List typeParametersVariances,
			   List extensions
			  )
  {
    super(name, Node.global, typeParameters, typeParametersVariances);

    this.extensions = extensions;

    itf = new Interface(variance, name.toString());
    addTypeSymbol(itf);
  }

  mlsub.typing.TypeSymbol getTypeSymbol()
  {
    return itf;
  }

  /****************************************************************
   * Resolution
   ****************************************************************/

  static Interface[] resolve(TypeMap scope, List types)
  {
    if (types == null || types.size() == 0) return null;

    Interface[] res = new Interface[types.size()];

    int n = 0;
    for (Iterator i = types.iterator(); i.hasNext(); n++)
      {
        MonotypeConstructor parent = (MonotypeConstructor) i.next();
        res[n++] = parent.tc.resolveToItf(scope);
      }

    return res;
  }

  void resolve()
  {
    super.resolve();

    superInterfaces = resolve(typeScope, extensions);
    extensions = null;

    createContext();
  }

  /****************************************************************
   * Initial Context
   ****************************************************************/

  private void createContext()
  {
    if (superInterfaces != null)
      try{
	Typing.assertLeq(itf, superInterfaces);
      }
      catch(mlsub.typing.KindingEx e){
	User.error(name, name + " cannot extends one of the interfaces " +
		   " because they don't have the same number or kind of " +
		   " type parameters");
      }
  }

  /****************************************************************
   * Module interface
   ****************************************************************/

  public void printInterface(java.io.PrintWriter w)
  {
    super.printInterface(w);
    w.print("abstract interface "
            + getSimpleName()
            + printTypeParameters()
            + Util.map(" extends ",", ","", superInterfaces)
            + "{}\n");
  }

  /****************************************************************
   * Code generation
   ****************************************************************/
  public void compile()
  {
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

  private List /* of Interface */ extensions; // the super-interfaces
  private Interface[] superInterfaces; // resolved super-interfaces
}
