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

// File    : Location.java
// Created : Tue Jul 13 11:55:08 1999 by bonniot
//$Modified: Thu Jul 22 19:44:52 1999 by bonniot $

package bossa.util;

import java.util.*;

/**
 * Represents a portion of the input file.
 * Used to report errors to the user.
 *
 * @see Located
 */
public class Location
{
  /**
   * The file beeing parsed.
   * Enables to set the file name just once,
   * not at each Location construction.
   */
  public static String currentFile=null;

  public Location(String file,
		  int startLine, int startColumn,
		  int endLine, int endColumn)
  {
    this.fileName=file;
    this.startLine=startLine;
    // substracts 1 since JavaCC has 1 as first column,
    // and Emacs has 0 !!
    this.startColumn=startColumn-1;
    this.endLine=endLine;
    this.endColumn=endColumn-1;
  }

  public Location(
		  int startLine, int startColumn,
		  int endLine, int endColumn)
  {
    this(currentFile,startLine, startColumn, endLine, endColumn);
  }

  public Location(bossa.parser.Token t)
  {
    this(t.beginLine,t.beginColumn,t.endLine,t.endColumn);
  }

  /** returns the "invalid" location */
  public static Location nowhere()
  {
    return new Location(-1,-1,-1,-1);
  }

  public String toString()
  {
    String res;

    if(fileName!=null)
      res=fileName+": ";
    else
      res="";

    if(startLine==-2)
      return res;
    return res+"line "+startLine+", column "+startColumn+": ";
  }

  public Location englobe(Location loc)
  {
    return new Location(startLine, startColumn, loc.endLine, loc.endColumn);
  }

  public Location englobe(Collection /* of Located */ locs)
  {
    List llocs=(List) locs;
    return englobe(((Located)llocs.get(llocs.size()-1)).location());
  }

  private int startLine,startColumn,endLine,endColumn;
  private String fileName;
}
