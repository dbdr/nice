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

// File    : Constraint.java
// Created : Fri Jul 02 17:51:35 1999 by bonniot
//$Modified: Fri Jul 16 18:59:50 1999 by bonniot $
// Description : Syntaxic constraint

package bossa.syntax;

import java.util.*;
import bossa.util.*;

public class Constraint
{
  public Constraint(Collection binders)
  {
    this.binders=binders;
  }

  public static final Constraint emptyConstraint()
  { 
    return new Constraint(new ArrayList());
  }

  Constraint instantiate(TypeParameters typeParameters)
  { 
    Constraint res;
    int nb1,nb2;
    User.error((nb1=binders.size())!=
	       (nb2=typeParameters.size()),
	       nb1+" type parameters expected "+
	       ", not "+nb2);
    //TODO
    return emptyConstraint();
  }

  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return Util.map("<",", ",">",binders);
  }

  Collection /* of TypeSymbol */ binders;
}
