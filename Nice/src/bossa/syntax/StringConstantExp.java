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

// File    : StringConstantExp.java
// Created : Thu Sep 02 14:49:48 1999 by bonniot
//$Modified: Fri Feb 25 18:51:27 2000 by Daniel Bonniot $

package bossa.syntax;

import bossa.util.*;

/**
 * A String constant
 */
public class StringConstantExp extends ConstantExp
{
  public StringConstantExp(String value)
  {
    className="java.lang.String";
    this.value=unescape(value);
  }

  // cf. JLS 3.10.6
  public static String unescape(String s)
  //throws IllegalEscapeSequenceException 
  {
    StringBuffer sb = new StringBuffer();
    int n = s.length();
    for (int i = 0; i < n; i++) {
      char c = s.charAt(i);
      if (c == '\\') {
        i++;
	char c2;
        if (! (i < n)) {
          //throw new IllegalEscapeSequenceException("\\");
        }
	switch (c2 = s.charAt(i)) {
        case 'b': sb.append('\b'); break;
        case 't': sb.append('\t'); break;
	case 'n': sb.append('\n'); break;
        case 'f': sb.append('\f'); break;
	case 'r': sb.append('\r'); break;
        case '\"': sb.append('\"'); break;
        case '\'': sb.append('\''); break;
        case '\\': sb.append('\\'); break;
        default: if (c2 < '0' || c2 > '7') {
          //throw new IllegalEscapeSequenceException("\\" + c2);
        } else {
          // octal escape
          int code = c2 - '0';
          if (i+1 < n) {
            char c3 = s.charAt(i+1);
            if (c3 >= '0' && c3 <= '7') {
              code = 8*code + (c3 - '0');
              i++;
              if (c2 >= '0' && c2 <= '3' && i+1 < n) {
                // try a last number...
                char c4 = s.charAt(i+1);
                if (c4 >= '0' && c4 <= '7') {
                  code = 8*code + (c4 - '0');
                }
                i++;
              }
            }
          }
          sb.append((char)code);
        }
        }
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  public String toString()
  {
    return "\""+value+"\"";
  }
}
