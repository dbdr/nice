package gnu.mapping;
import gnu.text.NullReader;
import java.io.*;

/** An Inport for reading from a char array.
  * Essentially the same as an InPort wrapped around a CharArrayReader, but
  * more efficient because it uses the char array as the InPort's buffer. */

public class CharArrayInPort extends InPort
{
  public CharArrayInPort (char[] buffer, int len)
  {
    super(NullReader.nullReader, "<string>");
    try
      {
	setBuffer(buffer);
      }
    catch (java.io.IOException ex)
      {
	throw new Error(ex.toString()); // Can't happen.
      }
    limit = len;
  }

  public CharArrayInPort (char[] buffer)
  {
    this(buffer, buffer.length);
  }

  public CharArrayInPort (String string)
  {
    this(string.toCharArray());
  }

  public int read () throws java.io.IOException
  {
    if (pos >= limit)
      return -1;
    return super.read();
  }
}
