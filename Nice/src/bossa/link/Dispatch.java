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
    // main does not have to be implemented if the module is not runnable
    if (m.isMain() && !module.isRunnable())
      return;
    
    Stack sortedAlternatives = Alternative.sortedAlternatives(m);
    
    if(!(trivialTestOK(sortedAlternatives)))
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
      }
    return failed;
  }

  private static List enumerate(Polytype type)
  {
    Monotype[] domains = type.domain();
    Monotype[] types = new Monotype[domains.length];

    for (int i = 0; i < domains.length; i++)
      { 
	if (!(domains[i] instanceof MonotypeConstructor))
	  // Unconstrained type variable
	  {
	    types[i] = domains[i];
	    continue;
	  }

	MonotypeConstructor mc = (MonotypeConstructor) domains[i];
	TypeConstructor tc = mc.getTC();
	if (tc == ConstantExp.sureTC)
	  types[i] = mc.getTP()[0];
	else
	  Internal.error("Not implemented: " + tc);
      }

    Domain domain = new Domain(type.getConstraint(), new TupleType(types));
    return mlsub.typing.Enumeration.enumerate(domain);
  }
}
