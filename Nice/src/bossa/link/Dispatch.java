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

// File    : Dispatch.java
// Created : Mon Nov 15 10:36:41 1999 by bonniot
//$Modified: Fri Jul 28 17:11:21 2000 by Daniel Bonniot $

package bossa.link;

import bossa.syntax.*;
import bossa.util.*;

import mlsub.typing.*;

import gnu.expr.*;
import gnu.bytecode.Access;
import gnu.bytecode.ClassType;

import java.util.*;

import bossa.util.Debug;
import gnu.expr.Expression;

/**
 * Static class performing the coverage and non-ambiguity test.
 * 
 * @author bonniot
 */

public final class Dispatch
{
  // Non instantiable
  private Dispatch() { }

  public static void register(MethodDefinition m)
  {
    methods.add(m);
  }
  
  private static Collection methods = new ArrayList();

  public static void test(bossa.modules.Package module)
  {
    for(Iterator i = methods.iterator();
	i.hasNext();)
      test((MethodDefinition) i.next(), module);
  }
  
  private static void test(MethodDefinition m, bossa.modules.Package module)
  {
    if(m instanceof JavaMethodDefinition || 
       m instanceof StaticFieldAccess ||
       m instanceof FieldAccessMethod
       )
      return;
    
    List alternatives = Alternative.listOfAlternative(m);
    
    if(alternatives==null)
      //User.error(m,"Method "+m+" has no alternative");
      // It's not an error for a method to have no alternative
      // if its domain is void.
      // this will be checked later
      alternatives = new LinkedList();
   
    
    Stack sortedAlternatives = sort(alternatives);
    
    if(!(trivialTestOK(sortedAlternatives)))
      test(m, sortedAlternatives);
    
    if(Debug.codeGeneration)
      Debug.println("Generating dispatch function for "+m);
    
    compile(m, sortedAlternatives, module);
  }

  private static boolean trivialTestOK(Stack alternatives)
  {
    // return true iff
    //   there is only one alternative which does not constrain its arguments
    if(alternatives.size()!=1)
      return false;
    
    Alternative a = (Alternative) alternatives.peek();
    for(int i = 0; i<a.patterns.length; i++)
      if(a.patterns[i]!=null)
	  return false;
    return true;
  }

  /**
   * Computes a topological sorting of the list of alternatives.
   *
   * Uses a postfix travsersal of the graph.
   *
   * pre: alternatives are marked unvisited.
   */
  private static Stack sort(final List alternatives)
  {
    Stack sortedAlternatives = new Stack();
    
    if (alternatives.size() == 0)
      return sortedAlternatives;

    // Test if another sort has been done before.
    // In that case reset the marks.
    // This can happen if several dependant packages are runnable
    if (((Alternative) alternatives.get(0)).mark != Alternative.UNVISITED)
      for(Iterator i = alternatives.iterator(); i.hasNext();)
	((Alternative) i.next()).mark = Alternative.UNVISITED;
	
    for(Iterator i = alternatives.iterator(); i.hasNext();)
      {
	Alternative a = (Alternative) i.next();
	if (a.mark == Alternative.UNVISITED)
	  visit(a, alternatives, sortedAlternatives);
      }

    return sortedAlternatives;
  }
  
  private final static void visit
    (final Alternative a, 
     final List alternatives,
     final Stack sortedAlternatives
     )
  {
    // Cycles are possible
    //   * if two alternatives are identical (same patterns)
    //   * if there is a cyclic subclass relation
    //   * that's all, folks ! :-)
    switch(a.mark)
      {
      case Alternative.VISITING : User.error("Cycle in alternatives: "+a);
      case Alternative.UNVISITED: a.mark = Alternative.VISITING; break;
      case Alternative.VISITED  : return; //should not happen
      }
    
    for(Iterator i = alternatives.iterator();
	i.hasNext();)
      {
	Alternative daughter = (Alternative) i.next();
	if(daughter != a 
	   && daughter.mark == Alternative.UNVISITED 
	   && Alternative.leq(daughter,a))
	  visit(daughter,alternatives,sortedAlternatives);
      }
    a.mark = Alternative.VISITED;
    sortedAlternatives.push(a);
  }

  private static void test(MethodDefinition method,
	    final Stack sortedAlternatives)
  {
    if(Debug.linkTests)
      {
	Debug.println("\nLink test for "+method);
	for(Iterator i = sortedAlternatives.iterator(); i.hasNext();)
	  Debug.println("Alternative: "+i.next().toString());
      }
    

    TupleDomain domain = method.getType().getDomain();
    
    List multitags = Typing.enumerate(domain);

    for(Iterator i = multitags.iterator();
	i.hasNext();)
      {
	TypeConstructor[] tags = (TypeConstructor[]) i.next();
	
	test(method,tags,sortedAlternatives);
      }
  }
  
  /**
   * Tests that the 'tags' tuple has a best-match in alternatives
   *
   * @param method the method being tested
   * @param tags a tuple of TypeConstructors
   * @param alternatives a list of Alternatives
   */
  private static void test(MethodDefinition method, 
			   TypeConstructor[] tags, 
			   final Stack sortedAlternatives)
  {
    if(Debug.linkTests)
      Debug.println(Util.map("Multitag: ",", ","",tags));

    // Tests that a match exists, and that the first match is a "best-match"

    Alternative first = null;

    for(Iterator i = sortedAlternatives.iterator();
	i.hasNext();)
      {
	Alternative a = (Alternative) i.next();
	if(a.matches(tags))
	  if(first==null)
	    first = a;
	  else
	    if(!Alternative.leq(first,a))
	      User.error(method,
			 "Ambiguity for method "+method+
			 "For parameters of type "+Util.map("(",", ",")",tags)+
			 "\nboth "+first+" and "+a+" match");
      }
    if(first==null)
      {
	if(sortedAlternatives.size()==0)
	  User.error(method,
		     "Method "+method+" is declared but never defined");
	else
	  User.error(method,
		     "Method "+method+" is not exhaustive:\n"+
		     "no alternative matches "+Util.map("(",", ",")",tags));
      }
  }

  /****************************************************************
   * Compilation
   ****************************************************************/

  private static void compile(MethodDefinition m, 
			      Stack sortedAlternatives, 
			      bossa.modules.Package module)
  {
    BlockExp block = new BlockExp(m.javaReturnType());
    LambdaExp lexp = new LambdaExp(block);
    
    // parameters of the alternative function are the same in each case, so we compute them just once
    gnu.bytecode.Type[] types = m.javaArgTypes();
    Expression[] params = new Expression[types.length];
    lexp.min_args = lexp.max_args = types.length;
    
    for(int n = 0; n<types.length; n++)
      {
	Declaration param = lexp.addDeclaration("parameter_"+n, types[n]);
	param.setParameter(true);
	params[n] = new ReferenceExp(param);
      }
    
    block.setBody(dispatch(sortedAlternatives.iterator(),
			   m.javaReturnType()==gnu.bytecode.Type.void_type,
			   block,
			   params));

    lexp.setPrimMethod(m.getDispatchPrimMethod());
    
    module.compileDispatchMethod(lexp);
  }
  
  private static final gnu.mapping.Procedure throwProc 
    = new kawa.standard.prim_throw();
  
  private static Expression dispatch(Iterator sortedAlternatives, 
				     boolean voidReturn, 
				     final BlockExp block, 
				     Expression[] params)
  {
    if(!sortedAlternatives.hasNext())
      {
	Expression[] exn = 
	{ new QuoteExp(new Error("Message not understood")) };
	return new ApplyExp(throwProc,exn);
      }
    
    Alternative a = (Alternative) sortedAlternatives.next();
    Expression matchTest = a.matchTest(params);

    Expression matchCase;
    if(voidReturn)
      matchCase = new ApplyExp(a.methodExp(),params);
    else
      matchCase = new ExitExp(new ApplyExp(a.methodExp(),params),block);
    
    boolean optimize = true;
    
    if(optimize && !sortedAlternatives.hasNext())
      return matchCase;
    else
      return new gnu.expr.IfExp(matchTest,matchCase,dispatch(sortedAlternatives,voidReturn,block,params));
  }
}
