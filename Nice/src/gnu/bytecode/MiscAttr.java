// Copyright (c) 1997  Per M.A. Bothner.
// This is free software;  for terms and warranty disclaimer see ./COPYING.

package gnu.bytecode;
import java.io.*;

/* Represents a generic uninterpreted or unknown attribute.
 * @author      Per Bothner
 */

public class MiscAttr extends Attribute
{
  public byte[] data;
  int offset;
  int length;

  public MiscAttr(String name, byte[] data, int offset, int length)
  {
    super(name);
    this.data = data;
    this.offset = offset;
    this.length = length;
  }

  public MiscAttr(String name, byte[] data)
  {
    this(name, data, 0, data.length);
  }

  public MiscAttr(String name)
  {
    this(name, nobytes, 0, 0);
  }

  private static final byte[] nobytes = new byte[0];

  public static Attribute synthetic()
  {
    return new MiscAttr("Synthetic");
  }

  public int getLength() { return length; }

  public void write (DataOutputStream dstr)
    throws java.io.IOException
  {
    dstr.write(data, offset, length);
  }

}

