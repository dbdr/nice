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

// File    : IntConstantExp.java
// Created : Mon Jul 05 17:30:56 1999 by bonniot
//$Modified: Thu Feb 03 11:27:37 2000 by Daniel Bonniot $

package bossa.syntax;

import bossa.util.*;

/**
 * An integer constant.
 */
public class IntConstantExp extends ConstantExp
{
  static boolean initialized = false;
  
  {
    className = "gnu.math.IntNum";
    // already initialized in JavaTypeConstructor.addJavaTypes
    //  if(!initialized)
//        {
//  	JavaTypeConstructor.lookup(className);
//  	initialized=true;
//        }
  }
  
  public IntConstantExp(int value)
  {
    //this.value = new Integer(value);
    this.value = new gnu.math.IntNum(value);
  }
}
