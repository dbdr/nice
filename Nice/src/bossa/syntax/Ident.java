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

// File    : Ident.java
// Created : Thu Jul 01 15:04:41 1999 by bonniot
//$Modified: Fri Jul 09 19:39:47 1999 by bonniot $
// Description : Identifier
//   any string that comes from the parser should have this type

package bossa.syntax;

public class Ident
{
  public Ident(LocatedString name)
  {
    this.name=name;
  }

  public String toString()
  {
    return name.toString();
  }

  public boolean equals(Ident i)
  {
    return name.content.equals(i.name.content);
  }

  public boolean equals(String s)
  {
    if(name==null)
      return(s==null || s.length()==0);
    return name.content.equals(s);
  }

  LocatedString name;
}

