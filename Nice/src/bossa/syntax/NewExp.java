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
import mlsub.typing.TypeSymbol;
import mlsub.typing.TypeConstructor;

/**
   Call of an object constructor.

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
*/
public class NewExp extends CallExp
{
  public NewExp(TypeIdent ti, Arguments arguments)
  {
    super(null, arguments);
    this.ti = ti;
  }

  void resolve(TypeMap typeScope)
  {
    TypeSymbol sym = ti.resolveToTypeSymbol(typeScope);

    if (sym == mlsub.typing.TopMonotype.instance)
      setObject();
    else if (sym instanceof TypeConstructor)
      setTC((TypeConstructor) sym);
    else
      throw User.error(ti, ti + " is not a class" + sym.getClass());
  }

  private void setTC(TypeConstructor tc)
  {
    this.tc = tc;

    if (! TypeConstructors.instantiable(tc))
      {
	String message;
	if (TypeConstructors.isInterface(tc))
	  message = tc + " is an interface, it can't be instantiated";
	else if (TypeConstructors.isClass(tc))
	  message = tc + " is an abstract class, it can't be instantiated";
	else
	  message = tc + " is a type variable, it can't be instantiated";
	throw User.error(this, message);
      }

    // Make sure that the constructors have been created.
    ClassDefinition definition = ClassDefinition.get(tc);
    if (definition != null)
      definition.resolve();
    
    LinkedList constructors = TypeConstructors.getConstructors(tc);
    if (constructors == null)
      {
	if (tc.arity() > 0)
	  User.error(this, "Class " + tc + " has no constructor with the correct number of " +
		tc.arity() + " type parameters.\nA retyping is needed to use this constructor.");
	else
	  User.error(this, "Class " + tc + " has no constructor");
      }

    // the list of constructors must be cloned, as
    // OverloadedSymbolExp removes elements from it
    constructors = (LinkedList) constructors.clone();

    function = new OverloadedSymbolExp
      (constructors, new LocatedString("new " + tc, location()));
  }
  
  private void setObject()
  {
    JavaMethod method = JavaClasses.getObjectConstructor();

    function = new SymbolExp(method.getSymbol(), ti.location());
  }
  
  /****************************************************************
   * Printing
   ****************************************************************/

  public String toString()
  {
    String cl = ti.toString();
    return "new " + cl + arguments;
  }

  private TypeIdent ti;

  /** Can be null if the class instantiated is Object. */
  TypeConstructor tc = null;
}
