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
//$Modified: Mon Jan 24 19:13:12 2000 by Daniel Bonniot $

package bossa.link;

import bossa.syntax.*;
import bossa.util.*;

import gnu.expr.*;

import java.util.*;

/**
 * Static class to make the coverage and non-ambiguity test.
 * 
 * @author bonniot
 */

public class Dispatch
{
  // Non instantiable
  private Dispatch() { }

  public static void register(MethodDefinition m)
  {
    methods.add(m);
  }
  
  private static Collection methods = new ArrayList();

  public static void test(bossa.modules.Module module)
  {
    for(Iterator i=methods.iterator();
	i.hasNext();)
      test((MethodDefinition)i.next(), module);
  }
  
  private static void test(MethodDefinition m, bossa.modules.Module module)
  {
    if(m instanceof JavaMethodDefinition
       || m instanceof StaticFieldAccess
       )
      return;
    
    if(m instanceof FieldAccessMethod
       || m instanceof SetFieldMethod)
      {
	compile(m,null,module);
	return;
      }
    
    List alternatives = Alternative.listOfAlternative(m);
    
    if(alternatives==null)
      //User.error(m,"Method "+m+" has no alternative");
      // It's not an error for a method to have no alternative
      // if its domain is void.
      // this will be checked later
      alternatives=new LinkedList();
    
    Stack sortedAlternatives = sort(alternatives);
    
    if(Debug.linkTests)
      {
	Debug.print("\nLink test for "+m);
	for(Iterator i=sortedAlternatives.iterator();
	    i.hasNext();)
	  Debug.println("Alternative: "+i.next().toString());
      }
    
    Domain domain = m.getType().getDomain();
    
    List multitags = bossa.typing.Typing.enumerate(domain);
    
    for(Iterator i=multitags.iterator();
	i.hasNext();)
      {
	TypeConstructor[] tags = (TypeConstructor[]) i.next();
	
	test(m,tags,sortedAlternatives);
      }

    if(Debug.codeGeneration)
      Debug.print("Generating dispatch function for "+m);
    
    compile(m,sortedAlternatives,module);
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
    
    for(Iterator i = alternatives.iterator();i.hasNext();)
      {
	Alternative a = (Alternative) i.next();
	if(a.mark==Alternative.UNVISITED)
	  visit(a,alternatives,sortedAlternatives);
      }

    return sortedAlternatives;
  }
  
  private final static void visit
    (
     final Alternative a, 
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
      case Alternative.UNVISITED: a.mark=Alternative.VISITING; break;
      case Alternative.VISITED  : return; //should not happen
      }
    
    for(Iterator i=alternatives.iterator();
	i.hasNext();)
      {
	Alternative daughter = (Alternative) i.next();
	if(daughter!=a && daughter.mark==Alternative.UNVISITED && Alternative.leq(daughter,a))
	  visit(daughter,alternatives,sortedAlternatives);
      }
    a.mark=Alternative.VISITED;
    sortedAlternatives.push(a);
  }
    
  /**
   * Tests that the 'tags' tuple has a best-match in alternatives
   *
   * @param method the method being tested
   * @param tags a tuple of TypeConstructors
   * @param alternatives a list of Alternatives
   */
  private static void test(MethodDefinition method, TypeConstructor[] tags, final Stack sortedAlternatives)
  {
    if(Debug.linkTests)
      Debug.println(Util.map("Multitag: ",", ","",tags));

    // Tests that a match exists, and that the first match is a "best-match"

    Alternative first = null;

    for(Iterator i=sortedAlternatives.iterator();
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
			 "\neither "+first+" or "+a+" match");
      }
    if(first==null)
      User.error(method,
		 "Method "+method+" is not exhaustive:\n"+
		 "no alternative matches "+Util.map("(",", ",")",tags));
  }

  /****************************************************************
   * Compilation
   ****************************************************************/

  private static void compile(MethodDefinition m, Stack sortedAlternatives, bossa.modules.Module module)
  {
    if(m instanceof JavaMethodDefinition
       || m instanceof StaticFieldAccess
       )
      return;

    BlockExp block = new BlockExp();
    LambdaExp lexp = new LambdaExp(block);
    
    // parameters of the alternative function are the same in each case, so we compute them just once
    gnu.bytecode.Type[] types = m.javaArgTypes();
    gnu.expr.Expression[] params = new gnu.expr.Expression[types.length];
    lexp.min_args=lexp.max_args=types.length;

    for(int n=0; n<types.length; n++)
      {
	Declaration param = lexp.addDeclaration("parameter_"+n,types[n]);
	param.setParameter(true);
	params[n]=new ReferenceExp(param);
      }
    
    if(m instanceof FieldAccessMethod)
      block.setBody(compileFieldAccess((FieldAccessMethod) m,params[0]));
    else if(m instanceof SetFieldMethod)
      block.setBody(compileSetField((SetFieldMethod) m,params[0],params[1]));
    else
      block.setBody(dispatch(sortedAlternatives.iterator(),
			     m.javaReturnType()==gnu.bytecode.Type.void_type,
			     block,params));

    lexp.setPrimMethod(m.getDispatchPrimMethod());
    
    lexp.compileAsMethod(module.dispatchComp);
  }
  
  private static gnu.expr.Expression compileFieldAccess(FieldAccessMethod method, gnu.expr.Expression value)
  {
    ListIterator types = method.classTC.getJavaInstanceTypes();
    
    gnu.expr.Expression[] param = new gnu.expr.Expression[1];
    param[0] = value;
    
    gnu.expr.Expression res =
      new ApplyExp(new QuoteExp(new kawa.lang.GetFieldProc((gnu.bytecode.ClassType) types.next(),method.fieldName, method.javaReturnType(), gnu.bytecode.Access.PUBLIC)),
		   param);
    
    while(types.hasNext())
      {
	gnu.bytecode.ClassType type = (gnu.bytecode.ClassType) types.next();
	res = new gnu.expr.IfExp(Alternative.instanceOfExp(value,type),
			new ApplyExp(new QuoteExp(new kawa.lang.GetFieldProc(type,method.fieldName)),param),
			res);
      }
    
    return res;
  }

  private static gnu.expr.Expression compileSetField(SetFieldMethod method, gnu.expr.Expression obj, gnu.expr.Expression value)
  {
    ListIterator types = method.classTC.getJavaInstanceTypes();
    
    gnu.expr.Expression[] params = new gnu.expr.Expression[2];
    params[0] = obj;
    params[1] = value;
    
    gnu.expr.Expression res =
      new ApplyExp(new QuoteExp(new kawa.lang.SetFieldProc((gnu.bytecode.ClassType) types.next(),method.fieldName, method.fieldType.getJavaType(), gnu.bytecode.Access.PUBLIC)),
		   params);
    
    while(types.hasNext())
      {
	gnu.bytecode.ClassType type = (gnu.bytecode.ClassType) types.next();
	res = new gnu.expr.IfExp(Alternative.instanceOfExp(value,type),
			new ApplyExp(new QuoteExp(new kawa.lang.SetFieldProc(type,method.fieldName)),params),
			res);
      }
    
    return res;
  }

  private static final gnu.mapping.Procedure throwProc 
    = new kawa.standard.prim_throw();
  
  private static gnu.expr.Expression dispatch(Iterator sortedAlternatives, 
					      boolean voidReturn, 
					      final BlockExp block, 
					      gnu.expr.Expression[] params)
  {
    if(!sortedAlternatives.hasNext())
      {
	gnu.expr.Expression[] exn = new gnu.expr.Expression[1];
	exn[0] = new QuoteExp(new Error("Message not understood"));
	return new ApplyExp(throwProc,exn);
      }
    
    Alternative a = (Alternative) sortedAlternatives.next();
    gnu.expr.Expression matchTest = a.matchTest(params);

    gnu.expr.Expression matchCase;
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
