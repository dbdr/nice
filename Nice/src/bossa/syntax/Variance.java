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

// File    : Variance.java
// Created : Fri Jul 23 12:15:46 1999 by bonniot
//$Modified: Tue Jul 27 18:29:16 1999 by bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;

/**
 * Variance of a type constructor
 * 
 * @author bonniot
 */

public class Variance
{
  public Variance(int n)
  {
    this.size=n;
  }

  /**
   * Asserts the inequalities between type parameters belonging to this variance
   *
   * @param tp1 a least Collection of Monotypes
   * @param tp2 a greatest Collection of Monotypes
   */
  public void assertLeq(TypeParameters tp1, TypeParameters tp2)
    throws BadSizeEx,bossa.typing.TypingEx
  {
    if(tp1.size()!=size)
      throw new BadSizeEx(size,tp1.size());
    if(tp2.size()!=size)
      throw new BadSizeEx(size,tp2.size());
    Iterator i1=tp1.iterator();
    Iterator i2=tp2.iterator();
    for(int i=1;i<=size;i++)
      {
	Monotype m1=(Monotype)i1.next();
	Monotype m2=(Monotype)i2.next();
	//Non-variant
	bossa.typing.Typing.leq(m1,m2);
	bossa.typing.Typing.leq(m2,m1);
      }
  }
  
  int size;
}
