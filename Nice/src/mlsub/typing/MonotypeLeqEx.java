/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2001                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package mlsub.typing;

/**
   Error when asserting inequality between monotypes.
 */
public class MonotypeLeqEx extends TypingEx
{
  MonotypeLeqEx(Monotype m1, Monotype m2, 
		mlsub.typing.lowlevel.Unsatisfiable origin)
  {
    super("");
    this.origin = origin;
    this.m1 = m1;
    this.m2 = m2;
  }

  mlsub.typing.lowlevel.Unsatisfiable origin;
  public Monotype m1, m2;  

  public String getMessage()
  {
    return m1 + " <: " + m2 + " because of " + origin;
  }

  public Monotype getM1()
  { return m1; }

  public Monotype getM2()
  { return m2; }
}

