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

public final class NiceUtils
{
  public static gnu.expr.Expression doInline(gnu.mapping.Procedure1 proc, gnu.expr.Expression arg1)
  {
     return nice.tools.code.Inline.inline(proc, arg1);
  }

  public static gnu.expr.Expression doInline(gnu.mapping.Procedure2 proc, gnu.expr.Expression arg1, gnu.expr.Expression arg2)
  {
     return nice.tools.code.Inline.inline(proc, arg1, arg2);
  }

}

