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

import gnu.bytecode.ClassType;

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
  
  public static void register(JavaMethod m)
  {
    javaMethods.add(m);
  }
  
  private static Collection methods, javaMethods;
  public static void reset() 
  { 
    methods = new ArrayList(); 
    javaMethods = new ArrayList();
  }

  private static Chronometer chrono = Chronometer.make("Dispatch tests");

  public static void test(bossa.modules.Package module)
  {
    chrono.start();
    try {
      for(Iterator i = methods.iterator(); i.hasNext();)
	test((NiceMethod) i.next(), module);

      for (Iterator i = javaMethods.iterator(); i.hasNext();)
	test((JavaMethod) i.next(), module);
    } 
    finally {
      chrono.stop();
    }
  }
  
  private static void test(NiceMethod m, bossa.modules.Package module)
  {
    Stack sortedAlternatives = Alternative.sortedAlternatives(m);
    
    if (!(m.isMain() || trivialTestOK(sortedAlternatives)))
      test(m, sortedAlternatives, false);
    
    if(Debug.codeGeneration)
      Debug.println("Generating dispatch function for "+m);
    
    Compilation.compile(m, sortedAlternatives, module);
  }

  private static void test(JavaMethod m, bossa.modules.Package module)
  {
    Stack sortedAlternatives = Alternative.sortedAlternatives(m);
    
    if (! trivialTestOK(sortedAlternatives))
      test(m, sortedAlternatives, true);
    
    if(Debug.codeGeneration)
      Debug.println("Generating dispatch function for " + m);
    
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

  private static void test(MethodDeclaration method,
			   final Stack sortedAlternatives,
			   boolean isJavaMethod)
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

    boolean[] isValue = new boolean[method.getArity()];
    List values = generateValues(sortedAlternatives, isValue);
    boolean hasValues = values.size() > 0;  
   
    int nb_errors = 0;
    for(Iterator i = multitags.iterator(); i.hasNext();)
      {
	TypeConstructor[] tags = (TypeConstructor[]) i.next();

	// For java methods, we are only concerned with cases
	// where the first argument is a Nice class.
	ClassType firstArg = null;
	if (isJavaMethod)
	  {
	    firstArg = classTypeOfNiceClass(tags[0]);
	    if (firstArg == null)
	      continue;
	  }
	
	if (test(method, tags, sortedAlternatives, firstArg))
	{
	  if (++nb_errors > 9)
	    break;
        }
	else if (hasValues && 
	      testValues(method, tags, values, isValue, sortedAlternatives) )
	  if (++nb_errors > 9)
	    break;

      }
    if (nb_errors > 0)
      User.error(method, "The implementation test failed for method " + 
		 method.getName());
  }

  private static ClassType classTypeOfNiceClass(TypeConstructor tc)
  {
    ClassDefinition def = ClassDefinition.get(tc);

    if (def == null || ! (def.getImplementation() instanceof NiceClass))
      return null;

    return ((NiceClass) def.getImplementation()).getClassExp().getClassType();
  }

  /**
     Tests that the 'tags' tuple has a best-match in alternatives
     
     @param method the method being tested
     @param tags a tuple of TypeConstructors
     @param alternatives a list of Alternatives
     
     @return true if the test failed
   */
  private static boolean test(MethodDeclaration method, 
			      TypeConstructor[] tags, 
			      final Stack sortedAlternatives,
			      ClassType firstArg)
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
    if (first == null)
      {
	if (firstArg != null)
	  {
	    gnu.bytecode.Method superImplementation = SuperExp.getImplementationAbove
	      ((JavaMethod) method, firstArg);
	    if (superImplementation != null &&
		superImplementation.isAbstract() == false)
	      // It's OK, this case is covered by a Java implementation.
	      return false;
	  }

	failed = true;
	if(sortedAlternatives.size()==0)
	  User.error
	    (method, "Method " + method + " is declared but never implemented:\n" +
	     "no alternative matches " + toString(tags));
	else
	  User.warning(method,
		       "Method " + method + " is not completely covered:\n" + 
		       "no alternative matches " + 
		       toString(tags));
      }
    return failed;
  }

  /**
     Special version of above that tests tags with all combinations of integer values.
   */
  private static boolean testValues
    (MethodDeclaration method, 
     TypeConstructor[] tags,
     List valueCombis,
     boolean[] isValue, 
     final Stack sortedAlternatives)
  {
    boolean failed = false;
    List sortedTypeMatches = new ArrayList();
    for (Iterator i = sortedAlternatives.iterator(); i.hasNext(); )
    {
      Alternative a = (Alternative) i.next();
      if (a.matchesTypePart(tags, isValue))
	sortedTypeMatches.add(a);
    }
  
    for (Iterator valit = valueCombis.iterator(); valit.hasNext(); )
    {
      Alternative first = null;
      long[] values = (long[]) valit.next();
      for (Iterator i = sortedTypeMatches.iterator(); i.hasNext();)
      {
	Alternative a = (Alternative) i.next();
	if (a.matchesValuePart(values, isValue))
	  if (first == null)
	    first = a;
	  else if (!Alternative.less(first, a))
	    {
	      failed = true;
	      User.warning
		(method,
		 "Ambiguity for method "+method+
		 "\nFor parameters of type/value " + toString(tags, values, isValue)+
		 "\nboth\n" + first.printLocated() + 
		 "\nand\n" + a.printLocated() + "\nmatch.");
	    }
      }
      if(first==null)
      {
	failed = true;
	User.warning(method,
		       "Method " + method + " is not completely covered:\n" + 
		       "no alternative matches " + 
		       toString(tags, values, isValue));
      }
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

  private static String toString(TypeConstructor[] tags, long[] values, boolean[] isValue)
  {
    StringBuffer res = new StringBuffer();
    res.append('(');
    for (int i = 0, n = 0; i < tags.length; i++)
      {
	if(isValue[n]) 
	  res.append(values[n++]);
	else
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
    res = mergeNullCases(res, domains.length);
    return enumerateBooleans(res, domains.length);
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
    Set tupleSet = new TreeSet(tagComp);
    for (Iterator i = tuples.iterator(); i.hasNext();)
    {
      TypeConstructor[] tags = flatten((TypeConstructor[]) i.next(), length);
      //add only non duplicate tags
      if (tupleSet.add(tags))
        res.add(tags);
    }
    return res;
  }

  private static Comparator tagComp = new TagComparator();

  private static class TagComparator implements Comparator 
  {
    public TagComparator(){}
    
    public int compare(Object o1, Object o2)
    {
      TypeConstructor[] tc1 = (TypeConstructor[])o1;
      TypeConstructor[] tc2 = (TypeConstructor[])o2;

      for(int i = 0; i<tc1.length; i++)
      {
        if (tc1[i] == null)
	{
          if (tc2[i] == null) return 0;
	  return -1;
	}
        if (tc2[i] == null) return 1;
	int a = tc1[i].getId();
        int b = tc2[i].getId();
	if (a<b) return -1;
	if (a>b) return 1;
      }
      return 0;
    }

    public boolean equals(Object obj)
    { 
      return false;
    }

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

  /** Expand the 'boolean' case into 'true' and 'false'.
   */
  private static List enumerateBooleans(List tags, int length)
  {
    if (tags.size() < 1) return tags;

    List res;
    for (int pos = 0; pos < length; pos++)
    {
      res = new ArrayList();  
      for (Iterator i = tags.iterator(); i.hasNext(); ) 
      {
        TypeConstructor[] tc = (TypeConstructor[]) i.next();
        if (tc[pos] == PrimitiveType.boolTC)
          {
	    // Create two copies of this case, one for true and one for false.
            TypeConstructor[] tc2 = new TypeConstructor[tc.length];
            System.arraycopy(tc, 0, tc2, 0, tc.length);
	    tc[pos] = PrimitiveType.trueBoolTC;
            res.add(tc);
            tc2[pos] = PrimitiveType.falseBoolTC;
            res.add(tc2);
          }
	else res.add(tc);
      }
      tags = res;
    }
    return tags;	
  }

  /** Generate all combinations of integer values from the alternatives
   */
  private static List generateValues(List alternatives, boolean[] isValue)
  {
    List values = new ArrayList();
    if (alternatives.size() < 1) return values;
    int len = isValue.length;
    for (int pos = 0; pos < len; pos++)
    {
      long[] valuesAtPos = new long[alternatives.size()];  
      int valueCount = 0;
      for (Iterator i = alternatives.iterator(); i.hasNext(); ) 
      {
	Pattern pat = ((Alternative)i.next()).getPatterns()[pos];
	if (pat.atIntValue) {
	  isValue[pos] = true;
          valuesAtPos[valueCount++] = pat.value;
	}	   
      }
      if (valueCount > 0)
      {
	List res = new ArrayList();
	//remove duplicates
	for (int i = 0; i < valueCount; i++)
	  for (int j = i+1; j < valueCount; j++)
	    if (valuesAtPos[i] == valuesAtPos[j])
	      valuesAtPos[i] = valuesAtPos[--valueCount];	

	if (values.size() == 0)
	  for (int i = 0; i < valueCount; i++)
	  {
	    long[] arr2 = new long[len];
	    arr2[pos] = valuesAtPos[i];
	    res.add(arr2);
	  }

	else
	  for (Iterator it = values.iterator(); it.hasNext(); )
	  {
	    long[] arr = (long[])it.next();
	    for (int i = 0; i < valueCount; i++)
	    {
	      long[] arr2 = new long[len];
	      System.arraycopy(arr,0,arr2,0,len);
	      arr2[pos] = valuesAtPos[i];
	        res.add(arr2);
	    }
	  }

	values = res;
      }
    }
    return values;
  }
  
}
