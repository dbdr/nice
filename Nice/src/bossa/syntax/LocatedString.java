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
//$Modified: Fri Jul 09 19:35:25 1999 by bonniot $
// Description : A string + location information

package bossa.syntax;

import bossa.util.*;

public class LocatedString
{
  public LocatedString(String content, 
	       int startLine, int startColumn,
	       int endLine, int endColumn)
  {
    this.content=content;
    this.startLine=startLine;
    this.startColumn=startColumn;
    this.endLine=endLine;
    this.endColumn=endColumn;
  }

  public String toString()
  {
    return content/*+":"+startLine+":"+startColumn*/;
  }

  String content;
  int startLine,startColumn,endLine,endColumn;
}
