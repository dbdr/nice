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
//$Modified: Mon Apr 03 17:55:28 2000 by Daniel Bonniot $

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
   * @param originalScope the scope where the symbol has been looked for.
   */
  OverloadedSymbolExp(Collection symbols, LocatedString ident,
		      VarScope originalScope)
  {
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
    return new SymbolExp((VarSymbol) symbols.iterator().next(),location());
  }
  
  Expression resolveOverloading(List /* of Polytype */ parameters,
				CallExp callExp)
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
      if(notFound(removedSomething ?
		  "No method of arity "+arity+" has name "+ident :
		  "No method has name "+ident))
	return resolveOverloading(parameters, callExp);

    // we check even if there is only one candidate
    // if we have not refined yet,
    // since if that one does not fit, maybe an outer one does...
    if(refinementInProgress && symbols.size()==1)
      return uniqueExpression();
    
    Polytype[] types = new Polytype[symbols.size()];
    VarSymbol[] syms = new VarSymbol[symbols.size()];
    int symNum = 0;
    for(Iterator i=symbols.iterator();i.hasNext();)
      {
	VarSymbol s = (VarSymbol) i.next();
	
	if(Debug.overloading) 
	  Debug.println("Overloading: Trying with "+s);
	
	Polytype t = CallExp.wellTyped(new SymbolExp(s,location()),parameters);
	if(t==null)
	  i.remove();
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
	VarSymbol res = (VarSymbol) symbols.iterator().next();
	for(int i=0;;i++)
	  if(syms[i]==res)
	    {
	      callExp.type = types[i];
	      return uniqueExpression();
	    }
      }
    
    if(symbols.size()==0)
      if(notFound("No alternative matches the parameters while resolving overloading for "+ident))
	return resolveOverloading(parameters, callExp);

    //There is ambiguity
    User.error(this,"Ambiguity for symbol "+ident+". Possibilities are :\n"+
	       Util.map("","\n","",symbols));
    return null;
  }

  Expression resolveOverloading(Polytype expectedType)
  {
    if(Debug.overloading) 
      Debug.println("Overloading resolution (expected type "+expectedType+
		    ") for "+this);
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
      if(notFound("No alternative has expected type "+expectedType))
	return resolveOverloading(expectedType);

    //There is ambiguity
    User.error(this,"Ambiguity for symbol "+ident+".\nPossibilities are :\n"+
	       Util.map("","\n","",symbols));
    return null;    
  }
  
  Expression noOverloading()
  {
    if(Debug.overloading) 
      Debug.println("(no)Overloading resolution for "+this);

    if(ident.content.equals("action"))
      Internal.printStackTrace();
    
    if(symbols.size()==1)
      return uniqueExpression();

    if(symbols.size()==0)
      {
	if(notFound())
	  return noOverloading();
	else
	  {
	    TypeConstructor tc = (TypeConstructor)
	      Node.getGlobalTypeScope().lookup(ident.toString());
	    if(tc!=null && tc instanceof JavaTypeConstructor)
	      return new ClassExp((gnu.bytecode.ClassType) 
				  ((JavaTypeConstructor) tc).getJavaType());
	  }
	
	User.error(ident, ident + " is not defined");
      }
    
    User.error(this,"Ambiguity for symbol "+ident+". Possibilities are :\n"+
	       Util.map("","\n","",symbols));
    return null;
  }

  /** Recollects the symbols without stopping at the frontiers */
  private boolean notFound()
  {
    if(refinementInProgress)
      return false;
    symbols = scope.lookupGlobal(ident);
    refinementInProgress = true;
    return true;
  }
  
  private boolean notFound(String errorMessage)
  {
    boolean res = notFound();
    if(!res)
      User.error(this,errorMessage," in "+scope);
    return res;
  }
  
  /**
   * Removes the symbols that do not have minimal types.
   *
   * For instance, if the set of symbols is {s1, s2}
   * with s1:A->C and s2:B->C with B<:A,
   * removoNonMinimal will remove s1.
   *
   * This allows for java-style overloading, 
   * where the most precise method is choosen at compile-time.
   */
  private void removeNonMinimal()
  {
    removeNonMinimal(symbols);
  }
  
  static void removeNonMinimal(Collection symbols)
  {
    // optimization
    if(symbols.size()<2)
      return;
    
    VarSymbol[] syms = (VarSymbol[])
      symbols.toArray(new VarSymbol[symbols.size()]);
    boolean[] remove = new boolean[syms.length];
    
    for(int s1 = 0; s1<syms.length; s1++)
      for(int s2 = s1+1; s2<syms.length; s2++)
	try{
	  bossa.typing.Typing.leq(syms[s2].getType(), syms[s1].getType());
	  remove[s1]=true;
	  break;
	}
	catch(bossa.typing.TypingEx e){
	}
    for(int i = 0; i<syms.length; i++)
      if(remove[i])
	symbols.remove(syms[i]);
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

  Collection symbols;
  LocatedString ident;
  private boolean refinementInProgress;
}
