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
//$Modified: Thu Dec 02 11:55:22 1999 by bonniot $
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

  public void append(String suffix)
  {
    this.content=this.content+suffix;
  }
  
  public void prepend(String prefix)
  {
    this.content=prefix+this.content;
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
    boolean res=content.equals(s.content);
    return res;
  }

  public int hashCode()
  {
    return content.hashCode();
  }

  public LocatedString cloneLS()
  {
    return new LocatedString(new String(content),location);
  }
  
  public String content;
  Location location;
}
