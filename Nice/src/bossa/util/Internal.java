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

// File    : Internal.java
// Created : Wed Jul 07 18:23:19 1999 by bonniot
//$Modified: Fri Jul 09 21:24:26 1999 by bonniot $
// Description : Internal errors...

package bossa.util;

/**
   Internal messages are sent through this static class
 */

public class Internal
{
  public static void warning(String message)
  {
    System.out.println("[Internal warning] "+message);
  }

  public static void warning(boolean condition, String message)
  {
    if(condition)
      warning(message);
  }

  public static void error(String message)
  {
    System.out.println("[Internal error] "+message);
    System.exit(1);
  }

  public static void error(boolean condition, String message)
  {
    if(condition) 
      error(message);
  }

}
