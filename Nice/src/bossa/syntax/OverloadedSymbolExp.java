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
import mlsub.typing.Polytype;
import mlsub.typing.FunType;
import mlsub.typing.TupleType;
import mlsub.typing.Monotype;

import nice.tools.code.Types;

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

  private OverloadedSymbolExp(List symbols, LocatedString ident, 
                              boolean noImplicitThis)
  {
    this(symbols, ident);
    this.noImplicitThis = noImplicitThis;
  }

  OverloadedSymbolExp(VarSymbol symbol, LocatedString ident)
  {
    this.symbols = new LinkedList();
    this.symbols.add(symbol);
    this.ident = ident;
    setLocation(ident.location());
  }

  public boolean isAssignable()
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
    SymbolExp res = new SymbolExp(sym, location());
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
    
    // remembers removed symbols, 
    // to list possibilities if none matches
    LinkedList removed = new LinkedList();

    // Useful in case of failure, to try adding an implicit 'this'.
    List fieldAccesses = filterFieldAccesses();

    for(Iterator i = symbols.iterator(); i.hasNext();)
      {
	VarSymbol s = (VarSymbol) i.next();
	
        if (s.isIgnored())
          {
            removed.add(s);
            i.remove();
            continue;
          }

	switch (s.match(arguments)) {
	case 0 : // Doesn't match 
	  removed.add(s);
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
      {
        if (! noImplicitThis)
          {
            Expression res = givePriorityToFields(fieldAccesses);
            if (res != null)
              return res;
          }

        User.error(this, noMatchError(removed, arguments));
      }

    // SECOND PASS: check argument types

    removed.clear();

    for (Iterator i = symbols.iterator(); i.hasNext(); )
      {
	VarSymbol s = (VarSymbol) i.next();
	
	if(Debug.overloading) 
	  Debug.println("Overloading: Trying with "+s);

	Polytype[] argsType = 
	  Expression.getType(arguments.getExpressions(s));

	// we clone the type to avoid clashes with another use
	// of the same symbol
	// the cloned type is stored in the VarSymbol
	// and we check that cloneType() is not called twice
	// before the clone type is released
	s.makeClonedType(argsType, arguments.getUsedArguments(s));
	Polytype t = CallExp.wellTyped(s.getClonedType(), argsType);

	if (t == null)
	  {
	    removed.add(s);
	    i.remove();
	    s.releaseClonedType();
	  }
	else
	  {
	    arguments.types.put(s, t);
	  }
      }

    if (symbols.size() == 0)
      {
        if (! noImplicitThis)
          {
            Expression res = givePriorityToFields(fieldAccesses);
            if (res != null)
              return res;
          }

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
      }

    removeNonMinimal(symbols, arguments);
    
    if (symbols.size() == 1)
      {
	VarSymbol res = (VarSymbol) symbols.get(0);
	// store the formal argument types for later use together with the type
	callExp.argTypes = nice.tools.code.Types.domain(res.getClonedType());
	res.releaseClonedType();

	callExp.type = (Polytype) arguments.types.get(res);
	// store the expression (including default arguments)
	callExp.arguments.computedExpressions = arguments.getExpressions(res);
	//callExp.arguments = null; // free memory
	return uniqueExpression();
      }

    releaseAllClonedTypes();
    throw new AmbiguityError();
  }

  Expression resolveOverloading(Polytype expectedType)
  {
    if(Debug.overloading) 
      Debug.println("Overloading resolution (expected type " + expectedType +
		    ") for " + this);

    // remembers removed symbols, 
    // to list possibilities if none matches
    LinkedList removed = new LinkedList();

    // Useful in case of failure.
    List fieldAccesses = filterFieldAccesses();

    for (Iterator i = symbols.iterator(); i.hasNext();)
      {
	VarSymbol s = (VarSymbol) i.next();
	s.makeClonedType(null, null);
	try{
	  Typing.leq(s.getClonedType(), expectedType);
	  if(Debug.overloading)
	    Debug.println(s + "(" + s.location() + ") of type " +
			  s.getClonedType() + " matches");
	}
	catch(TypingEx e){
	  removed.add(s);
	  i.remove();
	  s.releaseClonedType();
	  if(Debug.overloading) 
	    Debug.println("Not "+s+" of type\n" + s.getClonedType() + 
			  "\nbecause "+e);
	}
      }

    if(symbols.size()==1)
      {
	VarSymbol s = (VarSymbol) symbols.get(0);
	Polytype symType = s.getClonedType();	
	s.releaseClonedType();
	return uniqueExpression(s, symType);
      }

    try {
      Expression res = givePriorityToFields(fieldAccesses);
      if (res != null)
        return res;

      if (Types.domain(expectedType) != null)
        { // in case of function objects symbols find the most precise match
	  List nonMin = removeNonMinimal(symbols);
	  if (symbols.size() == 1)
            {
 	      VarSymbol s = (VarSymbol) symbols.get(0);
	      Polytype symType = s.getClonedType();	
	      s.releaseClonedType();
              symbols = nonMin;
	      return uniqueExpression(s, symType);
            }
          else
            symbols.addAll(nonMin);
        }

      if (symbols.size() != 0)
        throw new AmbiguityError();

      throw User.error(this, noMatchError(removed, expectedType));
    }
    finally {
      releaseAllClonedTypes();
    }
  }

  private void releaseAllClonedTypes()
  {
    for(Iterator i = symbols.iterator(); i.hasNext();)
      {
	VarSymbol s = (VarSymbol) i.next();
	s.releaseClonedType();
      }
  }

  Expression noOverloading()
  {
    if(Debug.overloading) 
      Debug.println("(no)Overloading resolution for "+this);

    if(symbols.size()==1)
      return uniqueExpression();

    Expression res = givePriorityToFields(filterFieldAccesses());

    if (res != null)
      return res;

    LinkedList globalvars = new LinkedList();
    for(Iterator i = symbols.iterator(); i.hasNext();)
      {
	VarSymbol sym = (VarSymbol) i.next();
	if (sym instanceof GlobalVarDeclaration.GlobalVarSymbol)
	  globalvars.add(sym);
      }

    if (globalvars.size() > 0)
      return new OverloadedSymbolExp(globalvars, ident).noOverloading();

    if (symbols.size() != 0)
      throw new AmbiguityError();

    throw User.error(this, 
                     "No variable or field in this class has name " + ident);
  }

  /**
     @return null if there is no solution.
  */
  private Expression givePriorityToFields (List fieldAccesses)
  {
    if (fieldAccesses.size() != 0)
      {
        if (Node.thisExp != null)
          try {
            CallExp res = new CallExp
              (new OverloadedSymbolExp(fieldAccesses, ident, true),
               //Arguments.noArguments());
               new Arguments(new Arguments.Argument[]{new Arguments.Argument(Node.thisExp)}));

            res.resolveOverloading();
            return res;
          }
          catch (UserError e) {}

        symbols.removeAll(filterFieldAccesses());

        if (symbols.size() == 1)
          return uniqueExpression();
      }

    // There is no solution.
    return null;
  }

  private List filterFieldAccesses()
  {
    LinkedList res = new LinkedList();
    for(Iterator i = symbols.iterator(); i.hasNext();)
      {
	VarSymbol sym = (VarSymbol) i.next();
	if (sym.isFieldAccess())
	  res.add(sym);
      }
    return res;
  }

  static List removeNonMinimal(List symbols)
  {
    List removed = new ArrayList();
    // optimization
    if(symbols.size()<2)
      return removed;
    
    int len = symbols.size();
    VarSymbol[] syms = (VarSymbol[])
      symbols.toArray(new VarSymbol[len]);
    boolean[] remove = new boolean[len];
    
    for(int s1 = 0; s1<len; s1++) {

      Domain d1 = domain(syms[s1].getType());
      
      for(int s2 = 0; s2<len; s2++)
	/*
	  Look for symbols s1 and s2 such that
	    d2 <: d1 and not d1 <: d2
	  In that case s1 can be removed, since it is less specific than s2.

	  Optimizations:
	    Skip the diagonal.
	    If s2 was removed, then there is s3 below s2.
	    Therefore s1 will be removed anyway.
	*/
	if (s1 != s2 && !remove[s2]) {

	  Domain d2 = domain(syms[s2].getType());

	  try{
	    Typing.leq(d2, d1);
	    try {
	      Typing.leq(d1, d2);
	    }
	    catch (TypingEx e) {
	      remove[s1] = true;
	      break;
	    }
	  }
	  catch(TypingEx e){
	  }
	}
    }

    for(int i = 0; i<len; i++)
      if(remove[i])
	{
	  if (Debug.overloading)
	    Debug.println("Removing " + syms[i] + " since it is not minimal");
	  
          removed.add(syms[i]);
	  symbols.remove(syms[i]);
	}

    return removed;
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
  private static void removeNonMinimal(List symbols, Arguments arguments)
  {
    // optimization
    if(symbols.size()<2)
      return;
    
    int len = symbols.size();
    VarSymbol[] syms = (VarSymbol[])
      symbols.toArray(new VarSymbol[len]);
    boolean[] remove = new boolean[len];
    
    for(int s1 = 0; s1<len; s1++) {

      Domain d1 = domain(syms[s1].getClonedType(), 
			 arguments.getUsedArguments(syms[s1]));
      
      for(int s2 = 0; s2<len; s2++)
	/*
	  Look for symbols s1 and s2 such that
	    d2 <: d1 and not d1 <: d2
	  In that case s1 can be removed, since it is less specific than s2.

	  Optimizations:
	    Skip the diagonal.
	    If s2 was removed, then there is s3 below s2.
	    Therefore s1 will be removed anyway.
	*/
	if (s1 != s2 && !remove[s2]) {

	  Domain d2 = domain(syms[s2].getClonedType(), 
			     arguments.getUsedArguments(syms[s2]));

	  try{
	    Typing.leq(d2, d1);
	    try {
	      Typing.leq(d1, d2);
	    }
	    catch (TypingEx e) {
	      remove[s1] = true;
	      break;
	    }
	  }
	  catch(TypingEx e){
	  }
	}
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

  private static Domain domain(Polytype t, int[] usedArguments)
  {
    // remove nullness marker
    Monotype[] m = Types.domain(t.getMonotype());

    Monotype[] dom;

    if (usedArguments == null)
      dom = m;
    else
      {
	int n = 0;
	for (int i = 0; i < usedArguments.length; i++)
	  if (usedArguments[i] != 0)
	    n++;

	dom = new Monotype[n];

	for (int i = 0; i < usedArguments.length; i++)
	  if (usedArguments[i] != 0)
	    dom[usedArguments[i] - 1] = m[i];

      }

    return new Domain(t.getConstraint(), new TupleType(dom));
  }

  private static Domain domain(Polytype t)
  {
    // remove nullness marker
    Monotype[] m = Types.domain(t.getMonotype());

    return new Domain(t.getConstraint(), new TupleType(m));
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

    return "\n[" + Util.map("", "\n|", "", symbols) + "]";
  }

  List symbols;
  LocatedString ident;

  /** Do not try to add an implicit 'this' access. */
  private boolean noImplicitThis;

  /****************************************************************
   * Error messages
   ****************************************************************/

  /** No method in removed matched these arguments. */
  private String noMatchError(List removed, Arguments arguments)
  {
    switch(removed.size())
      {
      case 0:
	return "No method has name " + ident;

      case 1:
	VarSymbol sym = (VarSymbol) removed.get(0);

        if (sym.isIgnored())
          return sym.getName() + 
            " cannot be used because it has been ignored.\n" +
            "See above for the reason why it has been ignored";

	return sym.explainWhyMatchFails(arguments);

      default:
	return "No method with name " + ident + 
		arguments.explainNoMatch(removed);
      }
  }

  /** No symbol in removed had the expected type. */
  private String noMatchError(List removed, Polytype expectedType)
  {
    switch (removed.size())
      {
      case 0:
	return "No symbol has name " + ident;

      case 1:
	VarSymbol sym = (VarSymbol) removed.get(0);
	return ident + " has type " + sym.getType();

      default:
	return "No symbol with name " + ident + 
		" has type " + expectedType + ":\n" +
                Util.map("", "\n", "", removed);
      }
  }

    

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

