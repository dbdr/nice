import gnu.text.Char;

public final class charArray extends Array
{
  public charArray(char[] value)
  {
    this.value = value;
  }
  
  public static charArray make(char[] value)
  {
    if(value==null)
      return null;
    return new charArray(value);
  }
  
  public static char[] newInstance(int length)
  {
    return new char[length];
  }
  
  /** Return a new copy with newSize elements */
  public static char[] resizeArray(char[] from, int newSize)
  {
    int copyLength = from.length;
    if(newSize<copyLength)
      copyLength=newSize;
    
    char[] res = newInstance(newSize);
    java.lang.System.arraycopy(from,0,res,0,copyLength);

    return res;
  }
  
  public int getLength()
  {
    return value.length;
  }
  
  public Object get(int index)
  {
    return Char.make(value[index]);
  }
  
  public void set(int index, Object elem)
  {
    value[index] = ((Char) elem).charValue();
  }

  public char[] value;
}
