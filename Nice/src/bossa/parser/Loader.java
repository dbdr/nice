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

package bossa.parser;

import java.util.*;
import java.io.*;

import bossa.syntax.LocatedString;
import bossa.util.*;

/** 
    Static class for loading and parsing files.
    
    @version $Date$
    @author bonniot
 */

public abstract class Loader
{
  public static LocatedString open(Reader r, 
				   List definitions,
				   List imports,
				   Collection importStars)
  {
    if(parser==null)
      parser = new Parser(r);
    else
      parser.ReInit(r);
    
    try{
      return parser.module(definitions,imports,importStars);
    }
    catch(ParseException e){
      if(e.currentToken!=null)
	User.error(new Location(e.currentToken.next), 
		   removeLocation(e.getMessage()));
      else
	User.error(e.getMessage());
      return null;
    }
  } 

  /**
     Remove the "at line L, column C." in the error message,
     since it is already in the location.
  */
  private static String removeLocation(String message)
  {
    int start = message.indexOf(" at line ");
    if (start == -1)
      return message;
    int end = message.indexOf('.', start);
    return 
      message.substring(0, start) + 
      message.substring(end, message.length());
  }
  
  static Parser parser = null;
}
