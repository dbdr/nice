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
//$Modified: Sat Jul 24 18:48:49 1999 by bonniot $
// Description : Front-end test

package bossa.test;

import java.util.*;
import java.io.*;
import bossa.parser.Parser;
import bossa.syntax.AST;

/** Test of the frontend
 * 
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
    try{
      bossa.util.Location.currentFile="stdlib.bossa";
      Reader r = new FileReader("stdlib.bossa");
      Parser p=new Parser(r);
      Collection defs=p.definitions();

      String file;
      if(args.length==0)
	{
	  System.out.println("Usage: bossa file.bossa");
	  //System.exit(0);
	  file="t.bossa";
	}
      else file=args[0];
      bossa.util.Location.currentFile=file;
      r = new FileReader(file);
      p.ReInit(r);
      defs.addAll(p.definitions());

      AST ast=new AST(defs);
      //System.out.print(ast);
    }
    catch(bossa.parser.ParseException e){
      System.out.println(e);
    }
    catch(Exception e){
      System.out.println("Uncaught exception :");
      System.out.println(e);
      e.printStackTrace();
    }
  }
}

