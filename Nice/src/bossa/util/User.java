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
//$Modified: Thu Jul 27 16:06:06 2000 by Daniel Bonniot $

package bossa.util;

/**
 * Messages for the user.
 *
 * When the error or warning is conditional, 
 * put it inside a 'if'.
 * This is better than passing a boolean to error(), 
 * since the latter, forcing evaluation of the string,
 * would slow down compilation.
 */
public final class User
{
  public static void error(Located responsible, Exception exn)
  {
    error(responsible, exn.toString());
  }

  public static void error(Exception exn)
  {
    error(exn.toString());
  }

  public static void error(Located responsible, String message, String dbgMsg)
  {
    if(Debug.powerUser)
      error(responsible,message+dbgMsg);
    else
      error(responsible,message);
  }

  public static void error(Located responsible, String message, Exception dbgExn)
  {
    if(Debug.powerUser)
      error(responsible,message+" ["+dbgExn+"]");
    else
      error(responsible,message);
  }

  public static void error(Located responsible, String message)
  {
    Location loc = responsible.location();
    if(loc==null)
      error(message);
    else
      error(loc+": "+message);
  }

  public static void error(String message)
  {
    if(Debug.alwaysDumpStack)
      Internal.printStackTrace();
    
    System.out.println(message);
    System.exit(2);
  }

  public static void warning(Located responsible, String message)
  {
    warning(responsible.location()+": "+message);
  }

  public static void warning(String message)
  {
    if(Debug.alwaysDumpStack)
      Internal.printStackTrace();
    
    System.out.println("[Warning]: "+message);
  }
}
