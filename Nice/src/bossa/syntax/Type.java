/**************************************************************************/
/*                           B O S S A                                    */
/*        A simple imperative object-oriented research language           */
/*                   (c)  Daniel Bonniot 1999                             */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

// File    : Type.java
// Created : Mon Jul 19 17:45:27 1999 by bonniot
//$Modified: Thu Jul 22 19:27:31 1999 by bonniot $
// Description : Any type. 
//   Can either be a Polytype or a PolytypeConstructor

package bossa.syntax;

import java.util.*;
import bossa.util.*;

public abstract class Type
{
  /**
   * Constructs a type of the correct kind
   *
   * @param typeParameters a collection of TypeSymbol
   * @param p the type body
   * @return the new type
   */
  public static Type newType(Collection typeParameters, Polytype p)
  {
    if(typeParameters==null || 
       typeParameters.size()==0) 
      return p; 
    else 
      return new PolytypeConstructor(typeParameters,p);
  }

  //the get* methods are used to construct new Type. 
  //See FunExp for instance
  public abstract Collection getTypeParameters();
  public abstract Constraint getConstraint();
  public abstract Monotype   getMonotype();


  VarScope memberScope()
  {
    return null;
  }

  /** overrided in PolytypeConstructor */
  Polytype instantiate(TypeParameters typeParameters) 
    throws BadSizeEx
  {
    return null;
  }

  /*******************************************************************
   * Scoping
   *******************************************************************/

  TypeScope typeScope;

  abstract void buildScope(TypeScope ts);

  abstract void resolve();

  /****************************************************************
   * Functional types
   ****************************************************************/

  /** both methods are overrided in Polytype */

  Collection /* of Monotype */ domain()
  {
    return null;
  }

  Monotype codomain()
  {
    return null;
  }

}
