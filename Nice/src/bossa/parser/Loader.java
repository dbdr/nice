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
//$Modified: Tue Aug 17 17:43:26 1999 by bonniot $

package bossa.parser;

import java.util.*;
import java.io.*;

import bossa.util.*;

/** Static class for loading and parsing files
 * 
 * 
 * @author bonniot
 */

public abstract class Loader
{
  public static List open(String filename)
  {
    Debug.println("Parsing "+filename+"...");
    bossa.util.Location.currentFile=filename;

    Reader r=null;
    try{ r=new BufferedReader(new FileReader(filename)); }
    catch(FileNotFoundException e){
      User.error(filename+" not found");
    }
    
    if(parser==null)
      parser=new Parser(r);
    else
      parser.ReInit(r);
    
    List res=null;
    
    try{ res=parser.definitions(); }
    catch(ParseException e){
      User.error(new Location(e.currentToken.next),e.getMessage());
    }
    
    return res;
  } 

  static Parser parser = null;
}
