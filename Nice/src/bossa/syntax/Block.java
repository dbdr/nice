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

// File    : Block.java
// Created : Wed Jul 07 17:42:15 1999 by bonniot
//$Modified: Mon Oct 25 19:37:33 1999 by bonniot $
// Description : A block : a list of statements with local variables

package bossa.syntax;

import java.util.*;
import bossa.util.*;

public class Block extends Statement
{
  public Block(List statements)
  {
    super(Node.down);
    this.statements=addChildren(statements);
  }

  private Collection findLocals(Collection statements)
  {
    Collection res=new ArrayList();
    Iterator i=statements.iterator();
    while(i.hasNext())
      {
	// TODO : optimize using Object s=... (one cast less)
	Statement s=(Statement)i.next();
	if(s instanceof LocalDeclarationStmt)
	  { 
	    LocalDeclarationStmt decl=(LocalDeclarationStmt)s;
	    res.add(new PolySymbol(decl.left));
	  }
      }
    return res;
  }

  /****************************************************************
   * Type checking
   ****************************************************************/

  Polytype getType()
  {
    if(statements.size()>0)
      {
	Object o=statements.get(statements.size()-1);
	if(o instanceof ReturnStmt)
	  return ((ReturnStmt)o).value.getType();
      }
    return Polytype.voidType(typeScope);
  }

  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return "{\n"
      + Util.map("",";\n",";\n",statements)
      + "}\n";
  }

  private List /* of Statement */ statements;
}
