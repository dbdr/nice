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

package bossa.link;

import bossa.syntax.*;
import bossa.util.*;

import mlsub.typing.*;
import mlsub.typing.Polytype;
import mlsub.typing.TupleType;
import mlsub.typing.Monotype;
import mlsub.typing.MonotypeConstructor;

import java.util.*;

import bossa.util.Debug;

/**
   Static class performing the coverage and non-ambiguity test.
   
   @version $Date$
   @author Daniel Bonniot
 */

public final class Dispatch
{
  // Non instantiable
  private Dispatch() { }

  public static void register(NiceMethod m)
  {
    methods.add(m);
  }
  
  private static Collection methods = new ArrayList();

  public static void test(bossa.modules.Package module)
  {
    for(Iterator i = methods.iterator(); i.hasNext();)
      test((NiceMethod) i.next(), module);
  }
  
  private static void test(NiceMethod m, bossa.modules.Package module)
  {
    Stack sortedAlternatives = Alternative.sortedAlternatives(m);
    
    if (!(m.isMain() || trivialTestOK(sortedAlternatives)))
      test(m, sortedAlternatives);
    
    if(Debug.codeGeneration)
      Debug.println("Generating dispatch function for "+m);
    
    Compilation.compile(m, sortedAlternatives, module);
  }

  private static boolean trivialTestOK(Stack alternatives)
  {
    // return true iff
    //   there is only one alternative and it does not constrain its arguments
    if(alternatives.size()!=1)
      return false;
    
    Alternative a = (Alternative) alternatives.peek();
    for(int i = 0; i<a.patterns.length; i++)
      if(a.patterns[i]!=null)
	  return false;
    return true;
  }

  private static void test(NiceMethod method,
			   final Stack sortedAlternatives)
  {
    if(Debug.linkTests)
      {
	Debug.println("\nLink test for "+method);
	for(Iterator i = sortedAlternatives.iterator(); i.hasNext();)
	  Debug.println("Alternative: "+i.next().toString());
      }
    

    mlsub.typing.Polytype type = method.getType();
    if (type == null)
      {
	User.warning(method + " is not in a proper state. Ignoring.");
	return;
      }
    List multitags = enumerate(type);

    int nb_errors = 0;
    for(Iterator i = multitags.iterator(); i.hasNext();)
      {
	TypeConstructor[] tags = (TypeConstructor[]) i.next();
	
	if (test(method,tags,sortedAlternatives))
	  if (++nb_errors > 9)
	    break;
      }
    if (nb_errors > 0)
      System.exit(2);
  }
  
  /**
     Tests that the 'tags' tuple has a best-match in alternatives
     
     @param method the method being tested
     @param tags a tuple of TypeConstructors
     @param alternatives a list of Alternatives
     
     @return true if the test failed
   */
  private static boolean test(NiceMethod method, 
			      TypeConstructor[] tags, 
			      final Stack sortedAlternatives)
  {
    boolean failed = false;

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
	  else if(!Alternative.leq(first,a))
	    {
	      failed = true;
	      User.warning
		(method,
		 "Ambiguity for method "+method+
		 "For parameters of type "+Util.map("(",", ",")",tags)+
		 "\nboth "+first+" and "+a+" match");
	    }
      }
    if(first==null)
      {
	failed = true;
	if(sortedAlternatives.size()==0)
	  User.error
	    (method, "Method " + method + " is declared but never defined",
	     "\nBytecode name: " + method.getBytecodeName());
	else
	  User.warning(method,
		       "Method "+method+" is not exhaustive:\n"+
		       "no alternative matches "+Util.map("(",", ",")",tags));
	  //FIXME: write 'null' for the tags that code null values (maybeTC)
      }
    return failed;
  }

  /**
     Enumerate all the tuples of tags in the domain of a polytype.
     
     @return a List of TypeConstructor[]
       an element of an array is set to:
         null if it cannot be matched (e.g. a function type)
	 ConstantExp.maybeTC if it is matche by @null
  **/
  private static List enumerate(Polytype type)
  {
    Monotype[] domains = type.domain();
    Monotype[] types = new Monotype[domains.length];
    boolean[] maybeNull = new boolean[domains.length];

    for (int i = 0; i < domains.length; i++)
      { 
	Monotype arg = domains[i];

	if (!(arg instanceof MonotypeConstructor))
	  {
	    types[i] = arg;
	    continue;
	  }

	MonotypeConstructor mc = (MonotypeConstructor) arg;
	TypeConstructor tc = mc.getTC();
	if (tc != ConstantExp.sureTC)
	  maybeNull[i] = true;

	types[i] = mc.getTP()[0];
      }

    Domain domain = new Domain(type.getConstraint(), new TupleType(types));
    List res = mlsub.typing.Enumeration.enumerate(domain);
    return addNullCases(res, maybeNull);
  }

  /** Extend the list of tuples to add the case where some tags 
      must be matches by @null
  */
  private static List addNullCases(List tuples, boolean[] maybeNull)
  {
    // FIXME
    // This implementation creates duplicate tuples

    for(int index = 0; index < maybeNull.length; index++)
      if (maybeNull[index])
	{
	  int size = tuples.size();

	  // duplicate all entries
	  for(int i = 0; i < size; i++)
	    tuples.add(((Object[]) tuples.get(i)).clone());

	  // put the @null tag for the first half
	  for(int i = 0; i < size; i++)
	    ((TypeConstructor[]) tuples.get(i))[index] = ConstantExp.nullTC;
	}
    return tuples;
  }
}
