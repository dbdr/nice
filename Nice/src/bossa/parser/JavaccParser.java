/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2004                         */
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
import bossa.syntax.FormalParameters;
import bossa.util.*;
import nice.tools.util.Chronometer;

/**
    Nice parser using the JavaCC-generated parser.

    @author Daniel Bonniot
 */

public class JavaccParser implements bossa.modules.Parser
{
  public final boolean storeDocStrings;

  public JavaccParser(boolean storeDocStrings)
  {
    this.storeDocStrings = storeDocStrings;
  }

  public JavaccParser()
  {
    this(false);
  }


  public LocatedString readImports(Reader r, List imports, Collection opens)
  {
    chrono.start();
    try {
      Parser parser = new Parser(r);

      try{
	return parser.readImports(imports, opens);
      }
      catch(ParseException e){
        throw reportError(e);
      }
    }
    finally {
      chrono.stop();
    }
  }

  public void read(Reader r, List definitions)
  {
    chrono.start();
    try {

      Parser parser = new Parser(r);

      try{
	parser.module(definitions, storeDocStrings);
      }
      catch(ParseException e){
        throw reportError(e);
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


  public /*bossa.syntax.FormalParameters*/Object formalParameters(String parameters)
  {
    try {
      return getParser(parameters).formalParameters(false, null);
    }
    catch(ParseException ex) {
      return null;
    }
  }

  private static Error reportError(ParseException e)
  {
    if(e.currentToken!=null)
      {
        Token token = e.currentToken;

        /* For errors generated by JavaCC, currentToken is the last
           successfull token, so we want to point to the next one.
           For errors handled by us for specific reporting, currentToken
           is the offending token (which was parsed), and there is no next.
        */
        if (token.next != null)
          token = token.next;

        throw User.error(Parser.makeLocation(token),
                         removeLocation(e.getMessage()));
      }
    else
      throw User.error(e.getMessage());
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

  private static Parser getParser(String toParse)
  {
    Reader r = new StringReader(toParse);
    Parser parser = new Parser(r);
    return parser;
  }
}
