/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2002                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package bossa.parser;

import java.util.*;
import java.io.*;

import bossa.syntax.LocatedString;
import bossa.util.*;
import nice.tools.util.Chronometer;

/** 
    Static class for loading and parsing files.
    
    @version $Date$
    @author bonniot
 */

public abstract class Loader
{
  public static LocatedString readImports(Reader r, 
					  List imports, Collection opens)
  {
    chrono.start();
    try {
      if(parser==null)
	parser = new Parser(r);
      else
	parser.ReInit(r);

      // set to 0 (required when recovered from error)
      ParserTokenManager.nestingLevel = 0;

      try{
	return parser.readImports(imports, opens);
      }
      catch(ParseException e){
	if(e.currentToken!=null)
	  User.error(Location.make(e.currentToken.next), 
		     removeLocation(e.getMessage()));
	else
	  User.error(e.getMessage());
	return null;
      }
    }
    finally {
      chrono.stop();
    }
  }

  public static void open(Reader r, List definitions)
  {
    chrono.start();
    try {
      if(parser==null)
	parser = new Parser(r);
      else
	parser.ReInit(r);

      // set to 0 (required when recovered from error)
      ParserTokenManager.nestingLevel = 0;
  
      boolean storeDocStrings = false;

      try{
	parser.module(definitions, storeDocStrings);
      }
      catch(ParseException e){
	if(e.currentToken!=null)
	  User.error(Location.make(e.currentToken.next), 
		     removeLocation(e.getMessage()));
	else
	  User.error(e.getMessage());
      }
      catch(TokenMgrError e) {
	String message = e.getMessage();
	if (message.indexOf("<EOF>") != -1)
	  message = "Unexpected end of file";
	User.error(Location.nowhere(), message);
      }
    }
    finally {
      chrono.stop();
    }
  }

  private static Chronometer chrono = Chronometer.make("Parsing");

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
  
  public static Parser getParser(String toParse)
  {
    Reader r = new StringReader(toParse);
    Parser parser = new Parser(r);
    return parser;
  }

  static Parser parser = null;
}
