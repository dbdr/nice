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
//$Modified: Tue Jan 11 19:25:12 2000 by bonniot $
// Description : Internal errors...

package bossa.util;

/**
   Internal messages are sent through this static class
 */

public class Internal
{
  public static void printStackTrace()
  {
    try{  
      throw new Exception();
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }
        
  public static void warning(Located loc, String message)
  {
    Location l = loc.location();
    if(l==null)
      warning(message);
    else
      warning(l+message);
  }
  
  public static void warning(String message)
  {
    //printStackTrace();
    System.out.println("[Internal warning] "+message);
  }

  public static void warning(boolean condition, String message)
  {
    if(condition)
      warning(message);
  }

  public static void error(boolean condition, Located loc, String message)
  {
    if(condition)
      error(loc,message);
  }

  public static void error(Located loc, String message)
  {
    Location l = loc.location();
    if(l==null)
      error(message);
    else
      error(l+message);
  }

  public static void error(String message)
  {
    System.out.println("[Internal error] "+message);
    printStackTrace();
    System.exit(1);
  }

  public static void error(boolean condition, String message)
  {
    if(condition) 
      error(message);
  }

}
