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
//$Modified: Fri Jul 09 20:32:20 1999 by bonniot $
// Description : A block : a list of statements with local variables

package bossa.syntax;

import java.util.*;
import bossa.util.*;

public class Block extends Statement
{
  public Block(Collection statements)
  {
    this.statements=statements;
  }

  void buildScope(VarScope outer, TypeScope typeOuter)
  {
    Collection locals=findLocals(statements);
    this.scope=VarScope.makeScope(outer,locals);
    this.typeScope=typeOuter;
    buildScope(this.scope,this.typeScope,statements);
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
	    res.add(new VarSymbol(decl.name,decl.type));
	  }
      }
    return res;
  }

  void resolveScope()
  {
    resolveScope(statements);
  }

  public String toString()
  {
    return "{\n"
      + Util.map("",";\n",";\n",statements)
      + "}\n";
  }

  private Collection /* of Statement */ statements;
}
