/**************************************************************************/
/*                                N I C E                                 */
/*             A high-level object-oriented research language             */
/*                        (c) Daniel Bonniot 2003                         */
/*                                                                        */
/*  This program is free software; you can redistribute it and/or modify  */
/*  it under the terms of the GNU General Public License as published by  */
/*  the Free Software Foundation; either version 2 of the License, or     */
/*  (at your option) any later version.                                   */
/*                                                                        */
/**************************************************************************/

package gnu.bytecode;

import java.util.*;

/**
   A mapping from source files to the bytecode line numbers.
   The format is specified by the JSR 45.

   @author Daniel Bonniot (bonniot@users.sourceforge.net)
 */

class SourceMap extends Attribute
{
  SourceMap(ClassType classfile)
  {
    super("SourceDebugExtension");
    buffer = new StringBuffer("SMAP\n");
    buffer.append(classfile.getName()).append('\n');
    buffer.append("Default\n*S Default\n*F\n");
    fileMap = new HashMap();
  }

  private StringBuffer buffer;
  private StringBuffer lines = new StringBuffer("*L\n");

  private static final String trailer = "*E\n";

  public int getLength() 
  {
    return getBytes().length;
  }

  private byte[] getBytes()
  {
    // write the last part.
    writeCurrent();
    currentFile = null;

    try {
      String value = buffer.toString() + lines.toString() + trailer;
      return value.getBytes("UTF-8");
    }
    catch (java.io.UnsupportedEncodingException e) {
      // Should never happen, UTF-8 is standard.
      throw new Error(e.toString());
    }
  }

  public void write(java.io.DataOutputStream out) 
    throws java.io.IOException
  {
    byte[] bytes = getBytes();
    out.write(bytes, 0, bytes.length);
 }

  /*
    We work by remembering the last file used, and add a new File
    entry each time we change files. However, we keep the first line
    number, so that each source line from the same file is relative to
    that base. This helps keeping the output line number low.
  */

  private String currentFile = null;
  private int lastFileNumber = 0;
  private int firstLine = -1;
  private int lastLine = -1;
  private int outputBase = 1;
  private Map fileMap;

  /**
     Return a line number which is unique in this source map, 
     and which can be translated back into the given file and source
     line number.
  */
  int translate(String file, int line)
  {
    // We cannot do anything without the file name.
    if (file == null)
      return -1;

    if (! file.equals(currentFile) || line < firstLine)
      {
        writeCurrent();
        currentFile = file;
        firstLine = line;
      }

    lastLine = line;
    return outputBase + line - firstLine;
  }

  private void writeCurrent()
  {
    if (currentFile != null)
      {
        int fileNumber = writeFile(currentFile);
        int len = lastLine - firstLine + 1;
        lines.append("" + firstLine + '#' + fileNumber + 
                     ',' + len +
                     ':' + outputBase).append('\n');

        outputBase += len;
      }
  }
  
  private int writeFile(String file)
  {
    Integer fileNumber = (Integer)fileMap.get(file);
    if (fileNumber == null)
      {
        lastFileNumber++;
        fileNumber = new Integer(lastFileNumber);
        fileMap.put(file, fileNumber);
      }

    buffer.append(fileNumber.intValue()).append(' ').append(file).append('\n');
    return fileNumber.intValue();
  }

}
