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

package nice.tools.typing;

/**
   Utilities for Nice types.

   @author Daniel Bonniot (bonniots@users.sf.net)
 */

import mlsub.typing.*;
import bossa.syntax.PrimitiveType;

public final class Types
{
  /****************************************************************
   * Predicates
   ****************************************************************/

  public static boolean isVoid(mlsub.typing.Monotype m)
  {
    return equivalent(m).head() == PrimitiveType.voidTC;
  }

  public static boolean isVoid(mlsub.typing.Polytype t)
  {
    return isVoid(t.getMonotype());
  }

  public static boolean isPrimitive(TypeConstructor tc)
  {
    return nice.tools.code.Types.javaType(tc) instanceof gnu.bytecode.PrimType;
  }

  public static boolean isPrimitive(Monotype t)
  {
    return nice.tools.code.Types.javaType(t) instanceof gnu.bytecode.PrimType;
  }

  public static boolean isPrimitive(Polytype t)
  {
    return nice.tools.code.Types.javaType(t) instanceof gnu.bytecode.PrimType;
  }

  public static boolean isMaybe(Monotype m)
  {
    // This is prob. laxist, since getTC() might be different but equivalent to maybeTC (?)
    return (m instanceof MonotypeConstructor)
      && ((MonotypeConstructor) m).getTC() == PrimitiveType.maybeTC;
  }

  public static boolean isSure(Monotype m)
  {
    // see comment by isMaybe (e?)
    return (m instanceof MonotypeConstructor)
      && ((MonotypeConstructor) m).getTC() == PrimitiveType.sureTC;
  }

  public static boolean isDispatchable(Monotype m)
  {
    // Functional types are not dispatchable
    if (parameters(m) != null)
      return false;

    return ! isPrimitive(m);
  }

  /****************************************************************
   * Handling of option types
   ****************************************************************/

  public static Monotype equivalent(Monotype m)
  {
    return rawType(m).equivalent();
  }

  public static void setMarkedKind(Monotype m)
  {
    m.setKind(NullnessKind.instance);
  }

  public static void makeMarkedType(MonotypeVar m)
  {
    m.setPersistentKind(NullnessKind.instance);
  }

  /** return the type with nullness markers removed */
  public static Monotype rawType(Monotype m)
  {
    m = m.equivalent();
    if (! (m.getKind() == NullnessKind.instance))
      return m;

    return ((MonotypeConstructor) m).getTP()[0];
  }

  /** return the type with nullness markers removed */
  public static Monotype rawType(MonotypeConstructor mc)
  {
    return mc.getTP()[0];
  }

  /****************************************************************
   * Functional types
   ****************************************************************/

  /** @return the domain of a functional monotype with nullness marker */
  public static Monotype[] parameters(Monotype type)
  {
    return rawType(type).domain();
  }

  /** @return the domain of a functional polytype with nullness marker */
  public static Monotype[] parameters(Polytype type)
  {
    return rawType(type.getMonotype()).domain();
  }

  /** @return the codomain of a functional polytype with nullness marker */
  public static Monotype result(Polytype type)
  {
    return ((FunType) rawType(type.getMonotype())).codomain();
  }

  /** @return the <code>rank</code>th type parameter of this type, or null. */
  public static Monotype getTypeParameter(Polytype type, int rank)
  {
    // This can only help
    type.simplify();

    return getTypeParameter(type.getMonotype(), rank);
  }

  /** 
      Transforms \forall T:K.U into \forall T:K.sure<U>
  */
  public static Polytype addSure(Polytype type)
  {
    return new Polytype
      (type.getConstraint(), 
       bossa.syntax.Monotype.sure(type.getMonotype()));
  }

  /****************************************************************
   * Constructor
   ****************************************************************/

  public static TypeConstructor constructor(Monotype type)
  {
    return equivalent(type).head();
  }

  /**
     Return a concrete type constructor that represents as closely as
     possible the given type. 

     The constraint that introduces type variables occuring in the type must be
     entered in the context before calling this method.
  */
  public static TypeConstructor concreteConstructor(Monotype type)
  {
    TypeConstructor res = constructor(type);

    if (res == null || res.isConcrete())
      return res;

    return Typing.lowestInstance(res);
  }

  public static Monotype zeroArgMonotype(TypeConstructor tc)
    throws BadSizeEx
  {
    // Handle 'Class' as 'Class<?>'
    if (tc == PrimitiveType.classTC)
      return new MonotypeConstructor
        (tc, new mlsub.typing.Monotype[]{ UnknownMonotype.instance });

    return new MonotypeConstructor(tc, null);
  }

  /****************************************************************
   * Type parameters
   ****************************************************************/

  /** @return the <code>rank</code>th type parameter of this type, or null. */
  public static Monotype getTypeParameter(Monotype type, int rank)
  {
    // get rid of the nullness part
    type = rawType(type);

    if (! (type instanceof MonotypeConstructor))
      return null;

    Monotype[] parameters = ((MonotypeConstructor) type).getTP();

    if (parameters.length <= rank)
      return null;
    else
      return parameters[rank];
  }

  /****************************************************************
   * Domains
   ****************************************************************/

  public static Domain domain(Polytype t)
  {
    // remove nullness marker
    Monotype[] m = parameters(t.getMonotype());

    return new Domain(t.getConstraint(), m);
  }

  /****************************************************************
   * Covariant specialization
   ****************************************************************/

  /**
     @returns true if the spec type is a covariant specialization of origin,
              false if the return type is not a subtype of the original subtype
  */
  public static boolean covariantSpecialization(Polytype spec, Polytype origin)
  {
    boolean entered = false;

    if (Constraint.hasBinders(spec.getConstraint()) || 
        Constraint.hasBinders(origin.getConstraint()))
      {
        entered = true;
        Typing.enter();
      }

    try {

      try {
        if (entered) {
          Polytype clonedSpec = spec.cloneType();

          Constraint.enter(origin.getConstraint());
          Constraint.enter(clonedSpec.getConstraint());

          // For all argument types ...
          Monotype[] args = MonotypeVar.news(parameters(origin).length);
          Typing.introduce(args);

          // ... that can be used for both methods ...
          Typing.leq(args, parameters(origin));
          Typing.leq(args, parameters(clonedSpec));

          Typing.implies();

          // ... apply those args to the 'specialized' method ...
          Constraint.enter(spec.getConstraint());
          Typing.leq(args, parameters(spec));
        }

        // ... and check that the result is indeed more precise.
        Typing.leq(result(spec), result(origin));
      }
      finally {
        if (entered)
          Typing.leave();
      }
    }
    catch (TypingEx ex) {
      return false;
    }

    // OK, spec is a covariant specialization
    return true;
  }

  /**
     @returns true if the spec type specializes type parameters of the original
       type (which can not be checked at runtime during dispatch, and therefore
       should not count as overriding).
  */
  public static boolean typeParameterDispatch(Polytype spec, Polytype origin)
  {
    Monotype[] originalParams = parameters(origin);

    if (originalParams.length == 0)
      return false;

    Typing.enter();
    try {

      try {
        Polytype clonedSpec = spec.cloneType();

        Constraint.enter(origin.getConstraint());
        Constraint.enter(clonedSpec.getConstraint());

        // For all argument types ...
        Monotype[] args = MonotypeVar.news(originalParams.length);
        Typing.introduce(args);

        // ... that can be used for the first method ...
        Typing.leq(args, originalParams);

        // ... and that will be dispatched to the specialized method ...
        Typing.leqHead(args, parameters(clonedSpec));

        Typing.implies();

        // ... check that those args fit in the 'specialized' method ...
        Constraint.enter(spec.getConstraint());
        Typing.leq(args, parameters(spec));
      }
      finally {
        Typing.leave();
      }
    }
    catch (TypingEx ex) {
      return true;
    }

    // OK, no covariant dispatch
    return false;
  }
}
