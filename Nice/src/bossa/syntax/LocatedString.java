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
//$Modified: Thu Dec 09 13:49:30 1999 by bonniot $
// Description : A string + location information

package bossa.syntax;

import bossa.util.*;

public class LocatedString 
  implements Located
{
  public LocatedString(String content, Location loc)
  {
    this(content,loc,false);
  }

  /**
   * @param content the underlying raw string
   * @param loc the location of this string in the source
   * @param quoted true if this string must be quoted (operators like "+")
   */
  public LocatedString(String content, Location loc, boolean quoted)
  {
    this.content=content;
    this.location=loc;
    this.quoted = quoted;
  }

  public String toQuotedString()
  {
    if(quoted)
      return "\""+content+"\"";
    else
      return content;
  }

  public String toString()
  {
    return content;
  }

  public Location location()
  {
    return location;
  }

  /****************************************************************
   * Wrapper for string functions
   ****************************************************************/

  public void append(String suffix)
  {
    this.content=this.content+suffix;
  }
  
  public void prepend(String prefix)
  {
    this.content=prefix+this.content;
  }
  
  public LocatedString substring(int beginIndex, int endIndex)
  {
    return new LocatedString(content.substring(beginIndex,endIndex),location);
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
  private boolean quoted;
}
