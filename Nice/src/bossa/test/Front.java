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
//$Modified: Fri Jul 09 18:44:58 1999 by bonniot $
// Description : Front-end test

package bossa.test;

import java.util.*;
import java.io.*;
import bossa.parser.Parser;
import bossa.syntax.AST;

public class Front
{
  public static void main(String[] args) throws Exception
  {
    try{
      Reader r = new FileReader("stdlib.bossa");
      Parser p=new Parser(r);
      Collection defs=p.definitions();
      r = new FileReader(args[0]);
      p.ReInit(r);
      defs.addAll(p.definitions());

      AST ast=new AST(defs);
      System.out.print(ast);
    }
    catch(Exception e){
      System.out.println("Uncaught exception :");
      System.out.println(e);
      e.printStackTrace();
    }
  }
}

