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

/**
   A Generic Java parameterized type.

   @version $Date$
   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */

public class ParameterizedType extends ObjectType
{
  public ParameterizedType(ClassType main, Type[] parameters)
  {
    this.main = main;
    this.parameters = parameters;
  }

  public ClassType main;
  public Type[] parameters;
}
