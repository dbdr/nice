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
//$Modified: Mon Feb 14 15:30:34 2000 by Daniel Bonniot $

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
  public static void open(File file, 
			  List definitions,
			  List imports,
			  List importStars)
  {
    Location.currentFile=file.getName();

    Reader r=null;
    try{ r=new BufferedReader(new FileReader(file)); }
    catch(FileNotFoundException e){
      User.error("File "+file.getName()+" not found");
    }
    
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
