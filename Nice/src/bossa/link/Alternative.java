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

import bossa.util.*;

import mlsub.typing.*;

import bossa.syntax.MethodDeclaration;
import bossa.syntax.Pattern;
import bossa.syntax.LocatedString;
import bossa.syntax.Node;
import bossa.syntax.ConstantExp;

import gnu.bytecode.*;
import gnu.expr.*;

import nice.tools.code.Gen;
import nice.tools.typing.Types;

import java.util.*;

/**
 * Represents a method alternative in the link.
 *
 * It can be build either from information in a Nice source,
 * or read from a compiled bytecode file.
 *
 * @author bonniot
 */

public class Alternative implements Located
{
  /**
     The full name uniquely identifies the method by including the 
     complete type.
  */
  public Alternative(String methodName, Pattern[] patterns)
  {
    this.methodName = methodName;
    this.patterns = patterns;
  }

  /****************************************************************
   * Order on alternatives
   ****************************************************************/

  /**
   * Returns true iff 'a' is more precise than 'b'.
   */
  public static boolean leq(Alternative a, Alternative b)
  {
    for(int i = 0; i<a.patterns.length; i++)
      if (! bossa.syntax.dispatch.leq(a.patterns[i], b.patterns[i]))
	return false;
    return true;
  }

  /**
   * Returns true iff 'a' is strictly more precise than 'b'.
   */
  public static boolean less(Alternative a, Alternative b)
  {
    boolean strictly = false;

    for (int i = 0; i < a.patterns.length; i++)
      if (! bossa.syntax.dispatch.leq(a.patterns[i], b.patterns[i]))
	return false;
      else if (! bossa.syntax.dispatch.leq(b.patterns[i], a.patterns[i]))
	strictly = true;

    return strictly;
  }

  public static boolean disjoint(Alternative a, Alternative b)
  {
    for(int i = 0; i<a.patterns.length; i++)
      if (bossa.syntax.dispatch.disjoint(a.patterns[i], b.patterns[i]))
	return true;

    return false;
  }

  public static Alternative greatestLowerBound(Alternative a, Alternative b)
  {
    Pattern[] pats = new Pattern[a.patterns.length];
    for (int i = 0; i < a.patterns.length; i++)
      {
        if (bossa.syntax.dispatch.leq(a.patterns[i], b.patterns[i]))
          pats[i] = a.patterns[i];
        else if (bossa.syntax.dispatch.leq(b.patterns[i], a.patterns[i]))
          pats[i] = b.patterns[i];
        else
          return null;
      }

    return new Alternative(a.methodName, pats);
  }

  /**
   * Tests the matching of tags against a method alternative.
   */
 public boolean matches(TypeConstructor[] tags)
  {
    for(int i = 0; i < patterns.length; i++)
      if (!patterns[i].matches(tags[i]))
	return false;

    return true;
  }

  public boolean matchesTypePart(TypeConstructor[] tags, boolean[] isValue)
  {
    for(int i = 0; i < patterns.length; i++)
      if (!isValue[i] && !patterns[i].matches(tags[i]))
	return false;

    return true;
  }

  public boolean matchesValuePart(ConstantExp[] values, boolean[] isValue)
  {
    for(int i = 0; i < patterns.length; i++)
      if (isValue[i] && !patterns[i].matchesValue(values[i]))
	return false;

    return true;
  }

  public boolean containsTypeMatchingValue()
  {
    for(int i = 0; i < patterns.length; i++)
      if (patterns[i].atTypeMatchingValue())
	return true;

    return false;
  }

  public boolean allAtAny()
  {
    for (int i = 0; i<patterns.length; i++)
      if (! patterns[i].atAny())
	return false;

    return true;
  }

  /****************************************************************
   * Code generation
   ****************************************************************/

  /**
   * @return the expression that represents the method body.
   */
  public Expression methodExp()
  {
    Internal.error("methodExp called in " + getClass());
    return null;
  }

  /**
     @return the expression that tests if this alternative matches
     the tuple <code>parameters</code>.
   */
  Expression matchTest(Expression[] parameters, boolean skipFirst)
  {
    if (parameters.length != patterns.length)
      Internal.error("Incorrect parameters "+
		     Util.map("",", ","",parameters)+
		     " for " + this);

    Expression result = QuoteExp.trueExp;

    for (int index = 0; index < parameters.length; index++)
      result = Gen.shortCircuitAnd
        (result,
         patterns[index].matchTest(parameters[index], index == 0 && skipFirst));

    return result;
  }

  public String toString()
  {
    return methodName + Util.map("(", ", ", ")", patterns);
  }

  public String printLocated()
  {
    return toString();
  }

  String methodName;
  public Pattern[] patterns;

  public Pattern[] getPatterns() { return patterns; }

  public bossa.util.Location location() { return bossa.util.Location.nowhere(); }

  /****************************************************************
   * Regrouping alternatives per method
   ****************************************************************/

  private static HashMap alternatives;
  public static void reset()
  {
    alternatives = new HashMap();
  }
  
  public void add(String fullName)
  {
    List l = (List) alternatives.get(fullName);
    if (l == null)
      {
	// XXX change to LinkedList
	l = new ArrayList();
	alternatives.put(fullName,l);
      }
    // Dispatch.sort(final List alternatives) assumes that new alternatives
    // are added at the end
    l.add(this);
  }
  
  public void add(String[] fullNames)
  {
    for (int i = 0; i < fullNames.length; i++)
      add(fullNames[i]);
  }

  public static void addAll(MethodDeclaration from, MethodDeclaration to)
  {
    String fullName = to.getFullName();

    if (from.isJavaMethod())
      bossa.syntax.dispatch.createJavaAlternative(from).add(fullName);

    List list = (List) alternatives.get(from.getFullName());

    if (list == null)
      return;

    for(Iterator i = list.iterator(); i.hasNext();)
      {
        Alternative a = (Alternative) i.next();
        a.addDefaultPatterns(from).add(fullName);
      }
  }

  Alternative addDefaultPatterns(MethodDeclaration def)
  {
    Monotype[] parameters = Types.parameters(def.getType());
    Pattern[] newPatterns = null;

    for (int i = 0; i < patterns.length; i++)
      if (patterns[i].getTC() == null)
        {
          if (newPatterns == null)
            {
              newPatterns = new Pattern[patterns.length];
              System.arraycopy(patterns, 0, newPatterns, 0, i);
            }
          newPatterns[i] = bossa.syntax.dispatch.createPattern
            (patterns[i].getName(),
             Types.concreteConstructor(parameters[i]),
             Types.isSure(parameters[i]));
        }

    if (newPatterns == null)
      return this;
    else
      return new Alternative(methodName, newPatterns) {
          public Expression methodExp()
          {
            return Alternative.this.methodExp();
          }
        };
  }

  public static Stack sortedAlternatives(MethodDeclaration m)
  {
    List list = (List) alternatives.get(m.getFullName());
    
    if (list == null)
      // It's not an error for a method to have no alternative
      // as long as its domain is empty.
      // this will be checked later
      return new Stack();
    
    return sort(list);
  }

  /****************************************************************
   * Graph traversal
   ****************************************************************/

  /** Marks the state of this node in the graph traversal. */
  private int mark = 0;

  private static int currentVisitedMark = 0;

  /****************************************************************
   * Sorting alternatives by generality
   ****************************************************************/

  /**
     Computes a topological sorting of the list of alternatives.
     
     Uses a postfix travsersal of the graph.
  */
  private static Stack sort(final List alternatives)
  {
    Stack sortedAlternatives = new Stack();
    
    if (alternatives.size() == 0)
      return sortedAlternatives;

    // We used a different mark for each sort.
    // Useful since an alternative can belong to several methods because of
    // overriding, and SuperExp also lists alternatives.
    int visited = ++currentVisitedMark;

    for(Iterator i = alternatives.iterator(); i.hasNext();)
      {
	Alternative a = (Alternative) i.next();
	if (a.mark != visited)
	  visit(a, alternatives, sortedAlternatives, visited);
      }

    return sortedAlternatives;
  }
  
  private final static void visit
    (final Alternative a, 
     final List alternatives,
     final Stack sortedAlternatives,
     final int visited
     )
  {
    a.mark = visited;
    
    for(Iterator i = alternatives.iterator();
	i.hasNext();)
      {
	Alternative daughter = (Alternative) i.next();
	if (daughter.mark != visited 
            && Alternative.leq(daughter,a))
	  visit(daughter, alternatives, sortedAlternatives, visited);
      }

    sortedAlternatives.push(a);
  }
}
