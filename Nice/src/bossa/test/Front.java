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

// File    : Front.java
// Created : Thu Jul 01 15:11:18 1999 by bonniot
//$Modified: Sat Dec 04 12:22:13 1999 by bonniot $

package bossa.test;

import java.util.*;

import bossa.parser.Loader;
import bossa.syntax.AST;

/** 
 * Test of the frontend.
 */
public class Front
{
  /** The main function
   * 
   * @exception Exception 
   * @param args Command line arguments
   */
  public static void main(String[] args) throws Exception
  {
    String file;
    if(args.length==0)
      {
	System.out.println("Usage: bossa file.bossa");
	//System.exit(0);
	file="mi.bossa";
      }
    else file=args[0];
    
    try{
      bossa.modules.Module.compile(new bossa.syntax.LocatedString(file,bossa.util.Location.nowhere()));
    }
    catch(Exception e){
      System.out.println("Uncaught exception :");
      e.printStackTrace();
    }
  }
}

