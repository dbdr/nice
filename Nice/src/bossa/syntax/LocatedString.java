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
//$Modified: Tue Jul 13 11:59:37 1999 by bonniot $
// Description : A string + location information

package bossa.syntax;

import bossa.util.*;

public class LocatedString
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

  String content;
  Location location;
}
