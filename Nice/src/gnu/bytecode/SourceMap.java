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
      System.out.println(value);
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
  private int fileNumber = 0;
  private int firstLine = -1;
  private int lastLine = -1;
  private int outputBase = 1;

  /**
     Return a line number which is unique in this source map, 
     and which can be translated back into the given file and source
     line number.
  */
  int translate(String file, int line)
  {
    System.out.println(file + ":: " + line);
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
        fileNumber++;
        buffer.append(fileNumber).append(' ').append(currentFile)
          .append('\n');
        int len = lastLine - firstLine + 1;
        lines.append("" + firstLine + '#' + fileNumber + 
                     ',' + len +
                     ':' + outputBase).append('\n');

        outputBase += len;
      }
  }
}
