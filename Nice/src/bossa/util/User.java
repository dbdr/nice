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

// File    : User.java
// Created : Wed Jul 07 18:20:58 1999 by bonniot
//$Modified: Mon Aug 23 17:44:00 1999 by bonniot $
// Description : Messages for the user

package bossa.util;

public class User
{
  public static void error(boolean condition, Located responsible, String message)
  {
    if(condition)
      error(responsible,message);
  }

  public static void error(Located responsible, String message)
  {
    error(responsible.location()+message);
  }

  public static void error(boolean condition, String message)
  {
    if(condition)
      error(message);
  }

  public static void error(String message)
  {
    System.out.println(message);
    System.exit(1);
  }

  public static void warning(Located responsible, String message)
  {
    warning(responsible.location()+message);
  }

  public static void warning(String message)
  {
    System.out.println("[Warning] "+message);
  }

  public static void warning(boolean condition, String message)
  {
    if(condition)
      warning(message);
  }
}
