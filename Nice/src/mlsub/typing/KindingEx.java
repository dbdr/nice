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

// File    : KindingEx.java
// Created : Thu Jul 22 19:33:34 1999 by bonniot
//$Modified: Sat Jun 03 12:43:53 2000 by Daniel Bonniot $

package mlsub.typing;

/**
 * Reports that two types that have to be compared 
 * do not have the same kind
 * 
 * @author bonniot
 */

public class KindingEx extends TypingEx
{
  KindingEx(Polytype t1, Polytype t2)
  {
    super(t1+" and "+t2+" do not have the same kind : "+
	  t1.getClass()+" and "+t2.getClass());
  }

  KindingEx(Monotype t1, Monotype t2)
  {
    super(t1+" and "+t2+" do not have the same kind : "+
	  t1.getClass()+" and "+t2.getClass());
  }

  KindingEx(TypeConstructor t1, TypeConstructor t2)
  {
    super(t1+" and "+t2+" do not have the same kind : "+
	  t1.getKind()+" and "+t2.getKind());
    this.t1=t1;
    this.t2=t2;
  }

  KindingEx(Interface t1, Interface t2)
  {
    super(t1+" cannot extend "+t2);
    this.t1 = t1;
    this.t2 = t2;
  }
  
  public Object t1, t2;
}
