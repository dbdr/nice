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
//$Modified: Tue Feb 22 15:58:07 2000 by Daniel Bonniot $

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

  /**
   * Adds a method to access an instance field of a java class if applicable.
   *
   * @param ident the name of the field
   * @param parameters the list of the <code>Polytype</code>s 
   *   of the parameters
   * @param to a list to add the access method to
   */
//    private static void addJavaFieldAccess
//      (LocatedString ident,
//       List parameters,
//       Collection to)
//    {
//      // Access to a field may only have one parameters: its owner
//      if(parameters.size()!=1)
//        return;
    
//      Polytype t=(Polytype)parameters.iterator().next();
    
//      String javaClass=t.getJavaClass();
//      if(javaClass==null)
//        return;

//      java.lang.reflect.Field field = StaticFieldAccess.getField(javaClass,ident.content,ident.location());
    
//      if(field==null)
//        return;
    
//      User.error(java.lang.reflect.Modifier.isStatic(field.getModifiers()),
//  	       ident,
//  	       "Static fields should be declared explicitely");
    
//      to.add(new FieldAccessMethod(javaClass,ident));
//    }

  private Expression uniqueExpression()
  {
    return new SymbolExp((VarSymbol)symbols.iterator().next(),location());
  }
  
  Expression resolveOverloading(List /* of Polytype */ parameters)
  {
    if(Debug.overloading) 
      Debug.println("Overloading resolution for "+this);
    
    //addJavaFieldAccess(ident,parameters,symbols);
    
    final int arity = parameters.size();

    // We remember if some method was discarded
    // in order to enhance the error message
    boolean removedSomething = false;
    
    for(Iterator i=symbols.iterator();i.hasNext();)
      {
	Object s=i.next();

	if(s instanceof MonoSymbol)
	  {
	    bossa.engine.Kind k = ((MonoSymbol) s).type.getKind();
	    if(k instanceof bossa.typing.FunTypeKind)
	      if(((bossa.typing.FunTypeKind) k).domainArity!=arity)
		{ i.remove(); removedSomething=true; continue; }
	      else ;  
	    else { i.remove(); continue; }
	  }
	else if(s instanceof MethodDefinition.Symbol)
	  {
	    if(((MethodDefinition.Symbol)s).definition.getArity()!=arity)
	      { i.remove(); removedSomething=true; continue; }
	  }
	else
	  { i.remove(); continue; }
      }

    if(symbols.size()==0)
      if(removedSomething)
	User.error(this,
		   "No method of arity "+arity+" has name "+ident);
      else
	User.error(this,
		   "No method has name "+ident);

    if(symbols.size()>=2)
      {
	for(Iterator i=symbols.iterator();i.hasNext();)
	  {
	    VarSymbol s=(VarSymbol)i.next();

	    if(Debug.overloading) 
	      Debug.println("Overloading: Trying with "+s);
	    
	    if(!CallExp.wellTyped(new SymbolExp(s,location()),parameters))
	      i.remove();
	  }
      }
    
    if(symbols.size()==1)
      return uniqueExpression();

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
      Debug.println("Overloading resolution (expected type "+expectedType+") for "+this);
    Iterator i=symbols.iterator();
    while(i.hasNext())
      {
	VarSymbol s=(VarSymbol)i.next();
	Polytype t=s.getType();
	try{
	  bossa.typing.Typing.leq(t,expectedType);
	  if(Debug.overloading)
	    Debug.println(s+"("+s.location()+") of type "+t+" matches");
	}
	catch(bossa.typing.TypingEx e){
	  i.remove();
	  if(Debug.overloading) 
	    Debug.println("Not "+s+" of type\n"+t+"\nbecause "+e);
	}
      }

    if(symbols.size()==1)
      return uniqueExpression();

    if(symbols.size()==0)
      User.error(this,"No alternative has expected type "+expectedType);

    //There is ambiguity
    User.error(this,"Ambiguity for symbol "+ident+".\nPossibilities are :\n"+
	       Util.map("","\n","",symbols));
    return null;    
  }
  
  Expression noOverloading()
  {
    if(symbols.size()==1)
      return uniqueExpression();

    if(symbols.size()==0)
      User.error(this,ident + " is not defined");

    User.error(this,"Ambiguity for symbol "+ident+". Possibilities are :\n"+
	       Util.map("","\n","",symbols));
    return null;
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
    return Util.map("["," | ","]",symbols);
  }

  Collection symbols;
  LocatedString ident;
}
