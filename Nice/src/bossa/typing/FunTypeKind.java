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

// File    : FunTypeKind.java
// Created : Wed Jul 28 17:51:02 1999 by bonniot
//$Modified: Thu Oct 28 16:06:18 1999 by bonniot $

package bossa.typing;

import bossa.util.*;
import bossa.syntax.Monotype;
import bossa.engine.*;

/**
 * 
 * 
 * @author bonniot
 */

public class FunTypeKind implements Kind
{
  public FunTypeKind(int domainArity)
  {
    this.domainArity=domainArity;
  }

  public void register(Element e)
  {
  }
  
  public void leq(Element e1, Element e2, boolean initial)
    throws Unsatisfiable
  {
    Internal.error(initial,"initial leq in FunTypeKind");
    leq(e1,e2);
  }
  
  public void leq(Element e1, Element e2)
    throws Unsatisfiable
  {
    Monotype m1=(Monotype) e1, m2=(Monotype) e2;
 
    Engine.leq(m2.domain(),m1.domain());
    Engine.leq(m1.codomain(),m2.codomain());
  }
  
  public boolean isBase()
  {
    return false;
  }
  
  /****************************************************************
   * Hastable oriented methods : usefull for the Engine to determine the right constraint
   ****************************************************************/
  
  public int hashCode()
  {
    return domainArity;
  }
  
  public boolean equals(Object o)
  {
    return (o instanceof FunTypeKind) && (((FunTypeKind) o).domainArity==domainArity);
  }

  public int domainArity;
  
}
