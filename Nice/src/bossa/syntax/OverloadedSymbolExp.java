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

// File    : OverloadedSymbolExp.java
// Created : Thu Jul 08 12:20:59 1999 by bonniot
//$Modified: Fri Sep 03 15:23:22 1999 by bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;

/**
 * A symbol, for which overloading resolution has yet to be done.
 */
public class OverloadedSymbolExp extends Expression
{
  /**
   * @param symbols All the possible VarSymbols
   * @param ident the original identifier
   */
  OverloadedSymbolExp(Collection symbols, LocatedString ident)
  {
    this.symbols=symbols;
    this.ident=ident;
    setLocation(ident.location());
  }

  boolean isAssignable()
  {
    return false;
  }

  Expression resolveOverloading(List /* of expression */ parameters,
				TypeParameters typeParameters)
  {
    Iterator i=symbols.iterator();
    while(i.hasNext())
      {
	VarSymbol s=(VarSymbol)i.next();
	if(!(s instanceof MethodDefinition))
	  {
	    i.remove();
	    continue;
	  }
	if(((MethodDefinition)s).getArity()!=parameters.size())
	  { i.remove(); continue; }
	Type t=s.getType();
	if(typeParameters==null)
	  if(t instanceof PolytypeConstructor)
	    { i.remove(); continue; }
	  else ;
	else
	  if(!(t instanceof PolytypeConstructor)
	     || ((PolytypeConstructor)t).getTypeParameters().size()!=typeParameters.size())
	    { i.remove(); continue; }
      }

    User.error(symbols.size()==0,this,
	       "No alternative has "+parameters.size()+" parameters");

    if(symbols.size()>=2)
      {
	i=symbols.iterator();
	while(i.hasNext())
	  {
	    if(!CallExp.wellTyped(InstantiateExp.create(new SymbolExp((VarSymbol)i.next(),location()),typeParameters),
				 parameters))
	      i.remove();
	  }
      }
    
    if(symbols.size()==1)
      return new SymbolExp((VarSymbol)symbols.iterator().next(),location());

    User.error(symbols.size()==0,this,
	       "No alternative matches the parameters while resolving overloading for "+ident);
    //There is ambiguity
    User.error(this,"Ambiguity for symbol "+ident+". Possibilities are :\n"+
	       Util.map("","","",symbols));
    return null;
  }
  
  void computeType()
  {
    User.error(this,ident+" is ambiguous");
  }

  public String toString()
  {
    return Util.map("["," | ","]",symbols);
  }

  Collection symbols;
  LocatedString ident;
}
