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

// File    : Loader.java
// Created : Thu Jul 29 09:43:50 1999 by bonniot
//$Modified: Fri Apr 21 14:01:44 2000 by Daniel Bonniot $

package bossa.parser;

import java.util.*;
import java.io.*;

import bossa.syntax.LocatedString;
import bossa.util.*;

/** 
 * Static class for loading and parsing files
 * 
 * @author bonniot
 */

public abstract class Loader
{
  public static void open(Reader r, 
			  String filename, // only for locating identifiers
			  List definitions,
			  List imports,
			  List importStars)
  {
    Location.currentFile = filename;

    if(parser==null)
      parser=new Parser(r);
    else
      parser.ReInit(r);
    
    try{
      parser.module(definitions,imports,importStars);
    }
    catch(ParseException e){
      if(e.currentToken!=null)
	User.error(new Location(e.currentToken.next),e.getMessage());
      else
	User.error(e.getMessage());
    }
  } 

  static Parser parser = null;
}
