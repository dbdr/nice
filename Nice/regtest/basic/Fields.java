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

package regtest.basic;

/**
   Class with static and non-static fields,
   to test field access.
*/

class Fields
{
  static int intS;
  int intI;

  static String stringS;
  String stringI;

  String[] strings = new String[]{"A"};
}
