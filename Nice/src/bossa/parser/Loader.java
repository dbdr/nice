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
//$Modified: Thu Nov 04 15:16:05 1999 by bonniot $

package bossa.parser;

import java.util.*;
import java.io.*;

import bossa.util.*;
import bossa.modules.Module;

/** Static class for loading and parsing files
 * 
 * 
 * @author bonniot
 */

public abstract class Loader
{
  public static Module open(String filename)
  {
    if(Debug.modules) Debug.println("Parsing "+filename+" ...");
    Location.currentFile=filename;

    Reader r=null;
    try{ r=new BufferedReader(new FileReader(filename)); }
    catch(FileNotFoundException e){
      User.error(filename+" not found");
    }
    
    if(parser==null)
      parser=new Parser(r);
    else
      parser.ReInit(r);
    
    Module res=null;
    
    try{
      res=parser.module(filename);
    }
    catch(ParseException e){
      if(e.currentToken!=null)
	User.error(new Location(e.currentToken.next),e.getMessage());
      else
	User.error(e.getMessage());
    }
    
    return res;
  } 

  static Parser parser = null;
}
