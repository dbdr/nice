import gnu.math.IntNum;

public final class intArray extends Array
{
  public intArray(int[] value)
  {
    this.value = value;
  }
  
  public static intArray make(int[] value)
  {
    if(value==null)
      return null;
    return new intArray(value);
  }
  
  public static int[] newInstance(int length)
  {
    return new int[length];
  }
  
  /** Return a new copy with newSize elements */
  public static int[] resizeArray(int[] from, int newSize)
  {
    int copyLength = from.length;
    if(newSize<copyLength)
      copyLength=newSize;
    
    int[] res = newInstance(newSize);
    java.lang.System.arraycopy(from,0,res,0,copyLength);

    return res;
  }
  
  public int getLength()
  {
    return value.length;
  }
  
  public Object get(int index)
  {
    return IntNum.make(value[index]);
  }
  
  public void set(int index, Object elem)
  {
    value[index] = ((Number) elem).intValue();
  }

  public int[] value;
}
