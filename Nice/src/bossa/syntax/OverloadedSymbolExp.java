/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2000                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package bossa.syntax;

import java.util.*;
import bossa.util.*;

import bossa.util.Debug;

import mlsub.typing.*;
import mlsub.typing.lowlevel.Kind;
import mlsub.typing.Polytype;

/**
   A symbol, for which overloading resolution has yet to be done.

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/
public class OverloadedSymbolExp extends Expression
{
  /**
     @param symbols All the possible VarSymbols
     @param ident the original identifier
   */
  OverloadedSymbolExp(List symbols, LocatedString ident)
  {
    if(symbols == null)
      Internal.error("No symbols");
    
    this.symbols = symbols;
    this.ident = ident;
    setLocation(ident.location());
  }

  OverloadedSymbolExp(VarSymbol symbol, LocatedString ident)
  {
    this.symbols = new LinkedList();
    this.symbols.add(symbol);
    this.ident = ident;
    setLocation(ident.location());
  }

  OverloadedSymbolExp(VarSymbol[] symbols, LocatedString ident)
  {
    this.symbols = new ArrayList();
    for (int i = 0; i < symbols.length; i++)
      this.symbols.add(symbols[i]);

    this.ident = ident;
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
  
  Expression resolveOverloading(CallExp callExp)
  {
    Arguments arguments = callExp.arguments;
    // It's better to do this know. OR is oriented, arguments first.
    arguments.computeTypes();
    
    if (Debug.overloading) 
      Debug.println("Overloading resolution for " + this +
		    "\nwith parameters " + arguments);

    // FIRST PASS: only checks the number of parameters
    
    // We remember if some method was discarded
    // in order to enhance the error message
    boolean removedSomething = false;
    
    for(Iterator i = symbols.iterator(); i.hasNext();)
      {
	VarSymbol s = (VarSymbol) i.next();
	
	switch (s.match(arguments)) {
	case 0 : // Doesn't match 
	  removedSomething = true;
	  i.remove();
	  break;
	case 1: // Wasn't even a function or method
	  i.remove();
	  break;
	case 2: // Matches
	  break;
	default: // Should not happen
	  Internal.warning("Unknown O.R. case: " + s.getClass()); 
	  i.remove(); 
	  break;
	}
      }

    if (symbols.size() == 0)
      User.error(this, 
		 removedSomething ?
		 "No method with name " + ident + 
		 " matches call " + arguments :
		 "No method has name " + ident);

    // SECOND PASS: check argument types

    // remembers removed symbols, 
    // to list possibilities if none matches
    LinkedList removed = new LinkedList();    

    //if(symbols.size() == 1) return uniqueExpression();

    Polytype[] types = new Polytype[symbols.size()];
    VarSymbol[] syms = new VarSymbol[symbols.size()];
    int sym = 0, good = 0;
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
	Polytype[] argsType = Expression.getType(arguments.getExpressions(sym));
	Polytype t = CallExp.wellTyped(s.getClonedType(), argsType);

	if (t == null)
	  {
	    removed.add(s);
	    i.remove();
	    s.releaseClonedType();
	  }
	else
	  {
	    types[good] = t;
	    syms[good] = s;
	    good++;
	  }
	sym++;
      }

    if (symbols.size() == 0)
      if (removed.size() == 1)
	User.error(this,
		   "Arguments " + arguments.printTypes() +
		   " do not fit: \n" + removed.get(0));
      else
	User.error(this, 
		   "No possible call for " + ident + 
		   ".\nArguments: " + arguments.printTypes() + 
		   "\nPossibilities:\n" + 
		   Util.map("", "\n", "", removed));

    removeNonMinimal();
    
    if (symbols.size() == 1)
      {
	VarSymbol res = (VarSymbol) symbols.get(0);
	res.releaseClonedType();

	for (int i = 0;; i++)
	  if (syms[i] == res)
	    {
	      callExp.type = types[i];
	      // store the expression (including default arguments)
	      callExp.computedExpressions = arguments.getExpressions(i);
	      //callExp.arguments = null; // free memory
	      return uniqueExpression();
	    }
      }
    
    throw new AmbiguityError();
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
    
    throw new AmbiguityError();
  }
  
  Expression noOverloading()
  {
    if(Debug.overloading) 
      Debug.println("(no)Overloading resolution for "+this);

    if(symbols.size()==1)
      return uniqueExpression();

    throw new AmbiguityError();
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
	// avoid the diagonal
	// if s2 was removed, there is s'1 below s2 so we can ignore it
	// makes removal faster
	// funny special case is when too symbols have the same domain,
	// we silently remove only one of them
	// should do something about that 
	// (but maybe only once per package, 
	//  checking all symbols with same name).
	if (s1 != s2 && !remove[s2])
	  try{
	    Typing.leq(syms[s2].getClonedType().getDomain(), 
		       syms[s1].getClonedType().getDomain());
	    remove[s1] = true;
	    break;
	  }
	  catch(TypingEx e){
	  }
    for(int i = 0; i<len; i++)
      if(remove[i])
	{
	  if (Debug.overloading)
	    Debug.println("Removing " + syms[i] + " since it is not minimal");
	  
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
    if (symbols.size() <= 1)
      return "[" + Util.map("", "\n|", "", symbols) + "]";
    else
      return "\n[" + Util.map("", "\n|", "", symbols) + "]";
  }

  List symbols;
  LocatedString ident;

  class AmbiguityError extends UserError
  {
    AmbiguityError()
    {
      super(OverloadedSymbolExp.this, 
	    "Ambiguity for symbol " + OverloadedSymbolExp.this.ident + 
	    ". Possibilities are :\n" + 
	    Util.map("", "\n", "", OverloadedSymbolExp.this.symbols));
    }
  }
}

