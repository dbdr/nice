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

package gnu.bytecode;

import java.util.Stack;

/**
   A Generic Java / JDK 1.5 type variable.

   @version $Date$
   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */

public class TypeVariable extends ObjectType
{
  TypeVariable(String name, Type bound)
  {
    super(name);
    this.bound = bound;
  }

  public Type bound;

  public String toString()
  {
    return "Type variable " + getName();
  }

  static TypeVariable[] none = new TypeVariable[0];

  static TypeVariable[] parse(String signature, int[] pos)
  {
    // Skip the '<'
    pos[0]++;

    Stack vars = new Stack();
    do {
      int colon = signature.indexOf(':', pos[0]);
      String name = signature.substring(pos[0], colon);

      pos[0] = colon + 1;
      Type bound = Type.fullSignatureToType(signature, pos);
      
      vars.push(new TypeVariable(name, bound));
    }
    while (signature.charAt(pos[0]) != '>');
    // Skip the '>'
    pos[0]++;

    TypeVariable[] res = new TypeVariable[vars.size()];
    for (int i = vars.size();  --i >= 0; )
      res[i] = (TypeVariable) vars.pop();

    return res;
  }

  /****************************************************************
   * Scoping
   ****************************************************************/

  /**
     Type variables are lookup up from at most two scopes.
     The first one is typically a method's type parameters.
     The second one the the englobing class'.
  */
  static TypeVariable[] scope1;
  static TypeVariable[] scope2;

  static TypeVariable lookup(String name)
  {
    TypeVariable res = lookup(name, scope1);
    if (res != null)
      return res;
    return lookup(name, scope2);
  }

  static TypeVariable lookup(String name, TypeVariable[] scope)
  {
    if (scope != null)
      for (int i = 0; i < scope.length; i++)
	if (scope[i].this_name.equals(name))
	  return scope[i];

    return null;
  }
}
