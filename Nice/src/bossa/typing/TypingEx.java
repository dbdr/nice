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

// File    : TypingEx.java
// Created : Tue Jul 20 12:06:53 1999 by bonniot
//$Modified: Tue Jul 27 11:06:10 1999 by bonniot $
// Description : Generic type exception

package bossa.typing;

import bossa.util.*;
import bossa.syntax.*;

public class TypingEx extends Exception
{
  TypingEx(String msg)
  {
    super(msg);
  }
}

