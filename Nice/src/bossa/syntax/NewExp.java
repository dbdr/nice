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

// File    : NewExp.java
// Created : Thu Jul 08 17:15:15 1999 by bonniot
//$Modified: Fri Aug 13 15:00:53 1999 by bonniot $
// Description : Allocation of a new object

package bossa.syntax;

import bossa.util.*;

public class NewExp extends Expression
{
  public NewExp(Monotype type)
  {
    this.type=type;
  }
  
  Expression resolve(VarScope s, TypeScope ts)
  {
    type=type.resolve(ts);
    User.error(!type.isImperative(),
  	       this,"Type parameters in \"New\" expressions must be imperative");
    
    return this;
  }
  
  Type getType()
  {
    return new Polytype(type);
  }

  public String toString()
  {
    return "new "+type;
  }

  Monotype type;
}
