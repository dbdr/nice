/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2002                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package bossa.link;

import bossa.syntax.*;
import bossa.util.*;

import mlsub.typing.*;
import mlsub.typing.Polytype;
import mlsub.typing.TupleType;
import mlsub.typing.Monotype;
import mlsub.typing.MonotypeConstructor;
import mlsub.typing.Constraint;
import mlsub.typing.MonotypeLeqCst;
import mlsub.typing.lowlevel.Element;

import java.util.*;

import bossa.util.Debug;
import nice.tools.util.Chronometer;

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
  
  private static Collection methods;
  public static void reset() { methods = new ArrayList(); }

  private static Chronometer chrono = Chronometer.make("Dispatch tests");

  public static void test(bossa.modules.Package module)
  {
    chrono.start();
    try {
      for(Iterator i = methods.iterator(); i.hasNext();)
	test((NiceMethod) i.next(), module);
    } 
    finally {
      chrono.stop();
    }
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
    for (int i = 0; i<a.patterns.length; i++)
      if (! a.patterns[i].atAny())
	  return false;
    return true;
  }

  private static boolean[] findUsedPositions(int len, Stack alternatives)
  {
    // We want all solutions for both the nullness marker and the tag,
    // if that position is used.

    boolean[] res = new boolean[len * 2];

    for (Iterator it = alternatives.iterator(); it.hasNext();)
      {
	Alternative a = (Alternative) it.next();
	for (int i = 0; i < len; i++)
	  if (! a.patterns[i].atAny())
	    res[2*i] = res[2*i + 1] = true;
      }

    return res;
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

    boolean[] used = findUsedPositions(method.getArity(), sortedAlternatives);

    mlsub.typing.Polytype type = method.getType();
    if (type == null)
      throw Internal.error(method + " is not in a proper state.");

    List multitags = enumerate(type, used);

    int nb_errors = 0;
    for(Iterator i = multitags.iterator(); i.hasNext();)
      {
	TypeConstructor[] tags = (TypeConstructor[]) i.next();
	
	if (test(method, tags, sortedAlternatives))
	  if (++nb_errors > 9)
	    break;
      }
    if (nb_errors > 0)
      User.error(method, "The implementation test failed for method " + 
		 method.getName());
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
	if (a.matches(tags))
	  if (first == null)
	    first = a;
	  else if (!Alternative.less(first, a))
	    {
	      failed = true;
	      User.warning
		(method,
		 "Ambiguity for method "+method+
		 "\nFor parameters of type " + toString(tags)+
		 "\nboth\n" + first.printLocated() + 
		 "\nand\n" + a.printLocated() + "\nmatch.");
	    }
      }
    if(first==null)
      {
	failed = true;
	if(sortedAlternatives.size()==0)
	  User.error
	    (method, "Method " + method + " is declared but never defined:\n" +
	     "no alternative matches " + toString(tags));
	else
	  User.warning(method,
		       "Method " + method + " is not exhaustive:\n" + 
		       "no alternative matches " + 
		       toString(tags));
      }
    return failed;
  }

  private static String toString(TypeConstructor[] tags)
  {
    StringBuffer res = new StringBuffer();
    res.append('(');
    for (int i = 0, n = 0; i < tags.length; i++)
      {
	res.append(tags[n++]);
	if (i + 1 < tags.length)
	  res.append(", ");
      }
    return res.append(')').toString();
  }

  /**
     Enumerate all the tuples of tags in the domain of a polytype.
     
     @return a List of TypeConstructor[]
       an element of an array is set to:
         null if it cannot be matched (e.g. a function type)
	 PrimtiveType.nullTC if it can be matched by @null
  **/
  private static List enumerate(Polytype type, boolean[] used)
  {
    Monotype[] domains = type.domain();
    Constraint cst = type.getConstraint();

    int len = used.length;

    Element[] types = new Element[len];

    for (int i = domains.length; --i >= 0;)
      {
	Monotype arg = domains[i];

	TypeConstructor marker;
	Monotype raw;
	// We deconstruct 'arg' as 'marker<raw>'
	// This is easy and more efficent if arg is already a constructed type

	if (arg instanceof MonotypeConstructor)
	  {
	    MonotypeConstructor mc = (MonotypeConstructor) arg;
	    marker = mc.getTC();
	    raw = mc.getTP()[0];
	  }
	else
	  {
	    marker = new TypeConstructor(PrimitiveType.maybeTC.variance);
	    MonotypeVar var = new MonotypeVar("dispatchType");
	    raw = var;
	    Monotype t = MonotypeConstructor.apply(marker, raw);
	    cst = Constraint.and(cst, marker, var, 
				 new MonotypeLeqCst(t, arg),
				 new MonotypeLeqCst(arg, t));
	  }

	types[--len] = raw;
	types[--len] = marker;
      }
    if (! (len == 0)) throw new Error();

    List res = mlsub.typing.Enumeration.enumerate(cst, types, used);
    return mergeNullCases(res, domains.length);
  }

  /** 
      Merges all null<*> into null.

      @param tuples a list of tuples.
        Each tuple has 2 * length elements, 
	alternatively a nullness marker TC and a normal TC
  */
  private static List mergeNullCases(List tuples, int length)
  {
    LinkedList res = new LinkedList();

    for (Iterator i = tuples.iterator(); i.hasNext();)
      add(res, flatten((TypeConstructor[]) i.next(), length));

    return res;
  }

  private static void add(List tuples, TypeConstructor[] tags)
  {
    // FIXME
    // This implementation creates duplicate tuples

    tuples.add(tags);
  }

  /** 
      Translates (nullTC, *) into nullTC and (sureTC, tc) into tc

      @param tags a list of tuples.
        Each tuple has 2 * length elements, 
	alternatively a nullness marker TC and a normal TC
  */
  private static TypeConstructor[] flatten(TypeConstructor[] tags, int length)
  {
    TypeConstructor[] res = new TypeConstructor[length];

    for (int i = length; --i >= 0 ;)
      if (tags[2 * i] == PrimitiveType.nullTC)
	res[i] = PrimitiveType.nullTC;
      else
	res[i] = tags[2 * i + 1];

    return res;
  }
}
