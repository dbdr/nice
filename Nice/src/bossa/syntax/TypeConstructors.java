/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2000                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package bossa.syntax;

import bossa.util.*;
import java.util.*;

import mlsub.typing.TypeConstructor;

/**
   Tools for classes and type constructors.

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/

public final class TypeConstructors
{
  /****************************************************************
   * Constructors
   ****************************************************************/
  
  /**
     The list can be modified by the caller. It should thus be cloned 
     each time.
     
     @return a list of the MethodDefinition.Symbols of each constructor of
     this class.
   */
  static LinkedList getConstructors(TypeConstructor tc)
  {
    return (LinkedList) constructors.get(tc);
  }

  static void addConstructor(TypeConstructor tc, MethodDeclaration m)
  {
    List l = (List) constructors.get(tc);
    if (l==null)
      {
	l = new LinkedList();
	constructors.put(tc, l);
      }
    l.add(m.getSymbol());
  }

  private static HashMap constructors;
  public static void reset() { constructors = new HashMap(); }

  /****************************************************************
   * Accessors
   ****************************************************************/

  /**
     Test if a type constructor can be instantiated.
     That is, if <code>new</code> is valid on this tc.
   */
  static boolean instantiable(TypeConstructor tc)
  {
    if(tc.isConcrete())
      return true;

    ClassDefinition definition = ClassDefinition.get(tc);
    return definition!=null &&
      !(definition.isAbstract || definition.isInterface);
  }
  
  /**
     Test if a type constructor is a # class.
  */
  static boolean isSharp(TypeConstructor tc)
  {
    ClassDefinition definition = ClassDefinition.get(tc);
    
    return definition!=null && !definition.isConcrete();
  }
  
  /**
     Test if a type constructor denotes a constant class,
     in opposition to a type constructor variable.
   */
  static boolean constant(TypeConstructor tc)
  {
    if(tc.isConcrete())
      return true;

    ClassDefinition definition = ClassDefinition.get(tc);
    
    return definition!=null;
  }

  
}
