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
//$Modified: Fri Sep 01 16:42:12 2000 by Daniel Bonniot $

package bossa.syntax;

import java.util.*;
import bossa.util.*;

import bossa.util.Debug;

import mlsub.typing.*;
import mlsub.typing.lowlevel.Kind;
import mlsub.typing.Polytype;

/**
 * A symbol, for which overloading resolution has yet to be done.
 */
public class OverloadedSymbolExp extends Expression
{
  /**
   * @param symbols All the possible VarSymbols
   * @param ident the original identifier
   * @param originalScope the scope where the symbol has been looked for.
   */
  OverloadedSymbolExp(List symbols, LocatedString ident,
		      VarScope originalScope)
  {
    if(symbols == null)
      Internal.error("No symbols");
    
    this.symbols = symbols;
    this.ident = ident;
    this.scope = originalScope;
    setLocation(ident.location());
  }

  boolean isAssignable()
  {
    Internal.error("Overloading resolution should be done before this.");
    return false;
  }

  private Expression uniqueExpression()
  {
    return new SymbolExp((VarSymbol) symbols.get(0), location());
  }
  
  private Expression uniqueExpression(VarSymbol sym, Polytype t)
  {
    SymbolExp res = new SymbolExp(sym);
    res.type = t;
    return res;
  }
  
  Expression resolveOverloading(Polytype[] parameters,
				CallExp callExp)
  {
    if(Debug.overloading) 
      Debug.println("Overloading resolution for "+this +
		    "\nwith parameters ("+Util.map("", ", ", "", parameters)+")");
    
    //addJavaFieldAccess(ident,parameters,symbols);
    
    final int arity = parameters.length;

    // We remember if some method was discarded
    // in order to enhance the error message
    boolean removedSomething = false;
    
    for(Iterator i = symbols.iterator(); i.hasNext();)
      {
	Object s = i.next();

	if(s instanceof MonoSymbol)
	  {
	    Kind k = ((MonoSymbol) s).type.getKind();
	    if(k instanceof FunTypeKind)
	      if(((FunTypeKind) k).domainArity!=arity)
		{ i.remove(); removedSomething = true; continue; }
	      else ;  
	    else { i.remove(); continue; }
	  }
	else if(s instanceof MethodDefinition.Symbol)
	  {
	    if(((MethodDefinition.Symbol)s).definition.getArity()!=arity)
	      { i.remove(); removedSomething = true; continue; }
	  }
	else
	  { Debug.println(s.getClass()+""); i.remove(); continue; }
      }

    if(symbols.size()==0)
      User.error(this, 
		 removedSomething ?
		 "No method of arity "+arity+" has name "+ident :
		 "No method has name "+ident);

    if(symbols.size()==1)
      return uniqueExpression();
    
    Polytype[] types = new Polytype[symbols.size()];
    VarSymbol[] syms = new VarSymbol[symbols.size()];
    int symNum = 0;
    for(Iterator i = symbols.iterator();i.hasNext();)
      {
	VarSymbol s = (VarSymbol) i.next();
	
	if(Debug.overloading) 
	  Debug.println("Overloading: Trying with "+s);

	// we clone the type to avoid clashes with another use
	// of the same symbol
	// the cloned type is stored in the VarSymbol
	// and we check that cloneType() is not called twice
	// before the clone type is released
	s.makeClonedType();
	Polytype t = CallExp.wellTyped(s.getClonedType(), parameters);
	if(t==null)
	  {
	    i.remove();
	    s.releaseClonedType();
	  }
	else
	  {
	    types[symNum] = t;
	    syms[symNum] = s;
	    symNum++;
	  }
      }

    removeNonMinimal();
    
    if(symbols.size()==1)
      {
	VarSymbol res = (VarSymbol) symbols.get(0);
	res.releaseClonedType();

	for(int i = 0;;i++)
	  if(syms[i]==res)
	    {
	      callExp.type = types[i];
	      return uniqueExpression();
	    }
      }
    
    if(symbols.size()==0)
      User.error(this, 
		 "No alternative matches the parameters while resolving overloading for "+ident);

    //There is ambiguity
    User.error(this,"Ambiguity for symbol "+ident+". Possibilities are :\n"+
	       Util.map("","\n","",symbols));
    return null;
  }

  Expression resolveOverloading(Polytype expectedType)
  {
    if(Debug.overloading) 
      Debug.println("Overloading resolution (expected type " + expectedType +
		    ") for " + this);

    VarSymbol s = null;
    Polytype symType = null;
    for(Iterator i = symbols.iterator(); i.hasNext();)
      {
	s = (VarSymbol) i.next();
	s.makeClonedType();
	symType = s.getClonedType();
	try{
	  Typing.leq(symType, expectedType);
	  if(Debug.overloading)
	    Debug.println(s+"("+s.location()+") of type "+symType+" matches");
	}
	catch(TypingEx e){
	  i.remove();
	  s.releaseClonedType();
	  if(Debug.overloading) 
	    Debug.println("Not "+s+" of type\n" + symType + "\nbecause "+e);
	}
      }

    if(symbols.size()==1)
      {
	s.releaseClonedType();
	return uniqueExpression(s, symType);
      }
    
    if(symbols.size()==0)
      User.error(this, 
		 "No alternative has expected type "+expectedType);
    
    //There is ambiguity
    User.error(this,"Ambiguity for symbol "+ident+".\nPossibilities are :\n"+
	       Util.map("","\n","",symbols));
    return null;
  }
  
  Expression noOverloading()
  {
    if(Debug.overloading) 
      Debug.println("(no)Overloading resolution for "+this);

    if(symbols.size()==1)
      return uniqueExpression();

    if(symbols.size()==0)
      return ClassExp.create(ident);
    
    User.error(this,"Ambiguity for symbol "+ident+". Possibilities are :\n"+
	       Util.map("","\n","",symbols));
    return null;
  }

  /**
   * Removes the symbols that do not have minimal domain types.
   *
   * For instance, if the set of symbols is {s1, s2}
   * with s1:A->C and s2:B->D with B<:A,
   * removoNonMinimal will remove s1.
   *
   * This allows for java-style overloading, 
   * where the most precise method is choosen at compile-time.
   */
  private void removeNonMinimal()
  {
    removeNonMinimal(symbols);
  }
  
  private static void removeNonMinimal(List symbols)
  {
    // optimization
    if(symbols.size()<2)
      return;
    
    int len = symbols.size();
    VarSymbol[] syms = (VarSymbol[])
      symbols.toArray(new VarSymbol[len]);
    boolean[] remove = new boolean[len];
    
    for(int s1 = 0; s1<len; s1++)
      for(int s2 = 0; s2<len; s2++)
	if (s1 != s2)
	  try{
	    Debug.println("TRYING " + 
			  syms[s2].getClonedType() + " << " +
			  syms[s1].getClonedType());
	    Typing.leq(syms[s2].getClonedType().getDomain(), 
		       syms[s1].getClonedType().getDomain());
	    Debug.println("SUCCESS");
	    remove[s1] = true;
	    break;
	  }
	  catch(TypingEx e){
	  }
    for(int i = 0; i<len; i++)
      if(remove[i])
	{
	  syms[i].releaseClonedType();
	  symbols.remove(syms[i]);
	}
  }
  
  void computeType()
  {
    Internal.error(this,ident+" has not been resolved yet.\n"+
		   "Possibilities are :"+this);
  }

  /****************************************************************
   * Code generation
   ****************************************************************/

  public gnu.expr.Expression compile()
  {
    Internal.error("compile in "+getClass()+" "+this);
    return null;
  }
  
  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    return "["+Util.map(""," | ","",symbols)+"]";
  }

  List symbols;
  LocatedString ident;
}
