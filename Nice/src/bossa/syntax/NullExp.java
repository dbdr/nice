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

// File    : NullExp.java
// Created : Thu Feb 03 19:17:15 2000 by Daniel Bonniot
//$Modified: Tue Jun 06 14:29:15 2000 by Daniel Bonniot $

package bossa.syntax;

import bossa.util.*;

/**
 * The 'null' expression.
 * 
 * @author Daniel Bonniot
 */

public class NullExp extends Expression
{
  public NullExp()
  {
  }

  void computeType()
  {
    this.type = mlsub.typing.Polytype.bottom();
  }
  
  protected gnu.expr.Expression compile()
  {
    return gnu.expr.QuoteExp.nullExp;
  }
  
  public String toString()
  {
    return "null";
  }
}
