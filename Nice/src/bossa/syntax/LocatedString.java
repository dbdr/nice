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

// File    : LocatedString.java
// Created : Fri Jul 09 19:09:47 1999 by bonniot
//$Modified: Fri Jul 23 18:57:16 1999 by bonniot $
// Description : A string + location information

package bossa.syntax;

import bossa.util.*;

public class LocatedString 
  implements Located
{
  public LocatedString(String content, Location loc)
  {
    this.content=content;
    this.location=loc;
  }

  public String toString()
  {
    return content;
  }

  public Location location()
  {
    return location;
  }

  public boolean equals(Object o)
  {
    if(o instanceof LocatedString)
      return equals((LocatedString) o);
    else
      return false;
  }

  public boolean equals(LocatedString s)
  {
    return content.equals(s.content);
  }

  public int hashCode()
  {
    return content.hashCode();
  }

  String content;
  Location location;
}
