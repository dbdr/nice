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

// File    : Element.java
// Created : Wed Jul 28 10:42:50 1999 by bonniot
//$Modified: Wed Jul 28 17:30:54 1999 by bonniot $

package bossa.engine;

import bossa.util.*;

/** Something that can be constrained in this engine
 * 
 * 
 * @author bonniot
 */

public interface Element
{
  int getId();
  void setId(int value);
  
  Kind getKind();
  void setKind(Kind value);
}
