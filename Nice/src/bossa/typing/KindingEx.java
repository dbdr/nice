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
//$Modified: Thu Jul 22 19:35:24 1999 by bonniot $

package bossa.typing;

import bossa.util.*;
import bossa.syntax.*;

/**
 * Reports that two types that have to be compared 
 * do not have the same kind
 * 
 * @author bonniot
 */

class KindingEx extends TypingEx
{
  KindingEx(Type t1, Type t2)
  {
    super(t1+" and "+t2+" do not have the same kind : "+
	  t1.getClass()+" and "+t2.getClass());
  }
}
