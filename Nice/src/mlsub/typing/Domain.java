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

// File    : Domain.java
// Created : Fri Jun 02 16:59:06 2000 by Daniel Bonniot

package mlsub.typing;

/**
   Ex V. K. \theta
   
   @version $Date$
   @author Daniel Bonniot
 */
public class Domain
{
  public Domain(Constraint constraint, Monotype[] monotypes)
  {
    this.constraint = constraint;
    this.monotypes = monotypes;
  }

  /**
     The domain Ex T. True. T
  */
  public final static Domain bot = null;
  
  public Constraint getConstraint()
  {
    return constraint;
  }
  
  public Monotype[] getMonotypes()
  {
    return monotypes;
  }

  /****************************************************************
   * Misc
   ****************************************************************/

  public String toString()
  {
    return (constraint == null ? "" : "Ex " + constraint) +
      java.util.Arrays.asList(monotypes).toString();
  }

  private Constraint constraint;
  private Monotype[] monotypes;
}
