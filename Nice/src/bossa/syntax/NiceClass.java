/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2004                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package bossa.syntax;

import bossa.util.*;
import mlsub.typing.*;
import mlsub.typing.Constraint;
import mlsub.typing.MonotypeConstructor;
import mlsub.typing.AtomicConstraint;

import java.util.*;

import nice.tools.code.Types;
import nice.tools.code.*;

import gnu.expr.Declaration;
import gnu.expr.QuoteExp;

/**
   Abstract syntax for a class definition.

 */
public abstract class NiceClass extends ClassImplementation
{
  abstract public LocatedString getName();

  abstract public gnu.expr.ClassExp createClassExp();

  /**
     Returns an expression to call a super method from outside a class method.

     This is needed because the JVM restricts call to a specific implementation
     to occur inside a method of the same class. So this generates a stub class
     method that calls the desired super method, and return a reference to this
     stub.
  */
  abstract public Definition importMethod(gnu.bytecode.Method method);
}
