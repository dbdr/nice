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

package mlsub.typing;

import java.util.*;

/**
   A monomorphic type.

   @version $Date$
   @author Daniel Bonniot (d.bonniot@mail.dotcom.fr)
 */
abstract public class Monotype implements mlsub.typing.lowlevel.Element
{
  /**
     A zero length Monotype array.
     Can be shared, since it's empty, thus immutable.
  */
  static final Monotype[] zeroMonotypes = new Monotype[0];

  final public boolean isConcrete()
  {
    // No Monotype lives at runtime.
    return false;
  }

  /** 
      Returns true if this monotype is only made of
      top-level, rigid type constructors
  */
  public boolean isRigid()
  {
    return false;
  }
  
  public final static boolean isRigid(Monotype[] ms)
  {
    if(ms==null) return true;
    
    for(int i=0; i<ms.length; i++)
      if(!(ms[i].isRigid()))
	return false;

    return true;
  }

  public boolean isExistential()
  {
    // This is a default implementation. Only MonotypeVar need to be
    // considered existential.
    return false;
  }
  
  /****************************************************************
   * Substitution
   ****************************************************************/

  /**
   * Perform type symbol substitution inside the monotype.
   *
   * Does not need to create a new object, 
   * but must not modify the monotype.
   *
   * @param map a map from TypeSymbols to TypeSymbols
   * @return a monotype with substitution performed
   */
  abstract Monotype substitute(Map map);
  
  final static Monotype[] substitute(java.util.Map map, Monotype[] ms)
  {
    if(ms==null) return null;
    
    Monotype[] res = new Monotype[ms.length];
    for(int i=ms.length-1; i>=0; i--)
      res[i] = ms[i].substitute(map);
    return res;
  }

  /****************************************************************
   * Equivalent types
   ****************************************************************/

  /**
     Return the monotype used for type checking.

     Should be <tt>this</tt>, except in class MonotypeVar
     where equivalent is a monotype with the correct Kind
     once the kind is known.
  */
  public Monotype equivalent()
  {
    return this;
  }

  /**
     Return the head type constructor if this monotype is
     of a known variance, or null.
  */
  public TypeConstructor head()
  {
    return null;
  }

  /**
     If this monotype is functional, return its domain.
     Otherwise return null.
  */
  public Monotype[] domain()
  {
    return null;
  }

  boolean isUnknown() { return false; }

  void setUnknown()
    throws mlsub.typing.lowlevel.Unsatisfiable
  {
    mlsub.typing.lowlevel.Engine.leq
      (TopMonotype.instance, nice.tools.typing.Types.rawType(this));
  }

  /****************************************************************
   * Simplification
   ****************************************************************/

  /**
     Propagate information for type simplification. 
     Not public.
  */
  abstract void tag(int variance);

  final static void tag(Monotype[] monotypes, int variance)
  {
    if (monotypes == null) return;
    
    for (int i=0; i<monotypes.length; i++)
      monotypes[i].tag(variance);
  }

  /**
     Return the monotype this one reduces to after simplification.
  */
  abstract Monotype canonify();
  
  final static Monotype[] canonify(Monotype[] monotypes)
  {
    if (monotypes == null) return null;
    
    Monotype[] res = new Monotype[monotypes.length];
    
    for (int i=0; i<monotypes.length; i++)
      res[i] = monotypes[i].canonify();

    return res;
  }

  /****************************************************************
   * Printing
   ****************************************************************/

  /**
     Print the monotype, using context information.
  */
  public String toString(boolean isNull, String suffix)
  {
    String res = toString();
    if (isNull)
      res = "?" + res;
    if (suffix != null)
      res = res + suffix;
    return res;
  }
}

