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

// File    : DuplicateIdentEx.java
// Created : Wed Jul 21 13:12:08 1999 by bonniot
//$Modified: Fri Jul 23 11:57:31 1999 by bonniot $
// Description : thrown when the same ident is defined twice 
//   in the same scope level

package bossa.syntax;

import bossa.util.*;

public class DuplicateIdentEx extends Exception
{
  public DuplicateIdentEx(LocatedString name)
  {
    this.ident=name;
  }
  
  LocatedString ident;
}
