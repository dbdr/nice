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

// File    : Statement.java
// Created : Mon Jul 05 15:48:25 1999 by bonniot
//$Modified: Tue Jul 20 18:54:34 1999 by bonniot $
// Description : Abstract ancestor for all statements
//   Descendants have "Stmt" suffix

package bossa.syntax;

import bossa.util.*;
import java.util.*;

public abstract class Statement extends Node
  implements Located
{
  /****************************************************************
   * Type cheking
   ****************************************************************/
  
  abstract void typecheck();

  void typecheck(Collection c)
  {
    Iterator i=c.iterator();
    while(i.hasNext())
      {
	Statement s=(Statement)i.next();
	s.typecheck();
      }
  }

  public Location location()
  {
    return loc;
  }

  public void setLocation(Location l)
  {
    loc=l;
  }

  Location loc;
}
