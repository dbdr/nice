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
//$Modified: Wed Aug 02 19:12:14 2000 by Daniel Bonniot $

package bossa.util;

import java.util.*;

import bossa.parser.Token;

/**
 * Represents a portion of the input file.
 * Used to report errors to the user.
 *
 * @see Located
 */
public class Location implements Located
{
  /**
   * The file beeing parsed.
   * Enables to set the file name just once,
   * not at each Location construction.
   */
  public static String currentFile = null;

  public Location(String file,
		  int startLine, int startColumn,
		  int endLine, int endColumn)
  {
    this.fileName = file;
    this.startLine = startLine;
    // JavaCC has 1 as first column,
    // and Emacs has 0 !!
    this.startColumn = startColumn;
    this.endLine = endLine;
    this.endColumn = endColumn;
  }

  public Location(
		  int startLine, int startColumn,
		  int endLine, int endColumn)
  {
    this(currentFile, startLine, startColumn, endLine, endColumn);
  }

  public Location(String fileName)
  {
    this(fileName,-1,-1,-1,-1);
  }

  public Location(String abstractLocation, boolean dummy)
  {
    this.abstractLocation = abstractLocation;
  }

  public Location(Token t)
  {
    this(t.beginLine,t.beginColumn,t.endLine,t.endColumn);
  }

  /**
     Return a location that goes from start to end.
   */
  public Location(Token start, Token end)
  {
    this(start.beginColumn, start.beginColumn, end.endLine, end.endColumn);
  }

  /** returns the "invalid" location */
  public static Location nowhere()
  {
    return new Location(-1,-1,-1,-1);
  }

  public static Location nowhereAtAll()
  {
    return new Location(null, -1,-1,-1,-1);
  }

  public String toString()
  {
    if(abstractLocation!= null)
      return "["+abstractLocation+"]";

    String res;

    if(fileName!= null)
      res = fileName;
    else
      res = "";

    if(!isValid())
      if(Debug.powerUser)
	return res+"[no location]";
      else
	return res;
    return res+
      (res.length()>0 ? ": " : "")+
      "line "+startLine+", column "+(startColumn-1);
  }

  public boolean isValid()
  {
    return abstractLocation!=null || startLine>=0;
  }
  
  public Location englobe(Location loc)
  {
    return new Location(startLine, startColumn, loc.endLine, loc.endColumn);
  }

  public Location englobe(Collection /* of Located */ locs)
  {
    List llocs = (List) locs;
    return englobe(((Located)llocs.get(llocs.size()-1)).location());
  }

  public Location location()
  {
    return this;
  }

  public int getLine()
  {
    return startLine;
  }
  
  public int getColumn()
  {
    return startColumn;
  }
  
  private int startLine,startColumn,endLine,endColumn;
  private String fileName;
  private String abstractLocation = null; // if non-null, overseeds everyting
}
