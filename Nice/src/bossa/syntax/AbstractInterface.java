/**************************************************************************/
/*                             N I C E                                    */
/*        A simple imperative object-oriented research language           */
/*                   (c)  Daniel Bonniot 1999                             */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

// File    : AbstractInterface.java
// Created : Thu Sep 21 16:28:17 2000 by Daniel Bonniot

package bossa.syntax;

import bossa.util.*;
import java.util.*;

import mlsub.typing.Interface;
import mlsub.typing.Variance;
import mlsub.typing.Typing;

/**
   Declaration of an abstract interface (i.e. a ML-Sub interface).

   @version $Date$
   @author Daniel Bonniot
 */
public class AbstractInterface extends Definition
  implements MethodContainer
{
  public AbstractInterface(LocatedString name, 
			   List typeParameters, 
			   List typeParametersVariances,
			   List extensions
			  )
  {
    super(name, Node.global);

    this.parameters = typeParameters;
    this.variance = ClassDefinition.makeVariance(typeParametersVariances);

    this.extensions = extensions;

    itf = new Interface(variance, name.toString());
    addTypeSymbol(itf);
  }

  public Collection associatedDefinitions()
  {
    return null;
  }

  public void addMethod(NiceMethod m)
  {
    addChild(m);
  }
  
  public mlsub.typing.TypeSymbol getTypeSymbol()
  {
    return itf;
  }
  
  public mlsub.typing.MonotypeVar[] createSameTypeParameters()
  {
    return ClassDefinition.createSameTypeParameters(parameters);
  }
  
  /****************************************************************
   * Resolution
   ****************************************************************/

  public void resolve()
  {
    surinterfaces = TypeIdent.resolveToItf(typeScope, extensions);
    extensions = null;
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
  }
   
  /****************************************************************
   * Module interface
   ****************************************************************/

  public void printInterface(java.io.PrintWriter s)
  {
    s.print("abstract interface "
            +name
            +Util.map("<",", ",">",parameters)
            +Util.map(" extends ",", ","",extensions)
            +"{}\n");
  }
  
  /****************************************************************
   * Code generation
   ****************************************************************/

  public void compile()
  {
    // Nothing
  }

  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return name.toString();
  }

  List /* of TypeSymbol */ parameters;
  private Variance variance;
  public Variance variance()
  {
    return variance;
  }

  /** ML-Sub interface. */
  private Interface itf;
  
  private List /* of Interface */ extensions; // the surinterfaces
  private Interface[] surinterfaces; // resolved surinterfaces
}
