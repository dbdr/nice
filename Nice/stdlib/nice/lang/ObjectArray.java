package nice.lang;

public final class ObjectArray extends AbstractArray
{
  ObjectArray(Object[] value)
  {
    this.value = value;
  }
  
  public static ObjectArray make(Object[] value)
  {
    if(value==null)
      return null;
    return new ObjectArray(value);
  }
  
  public static Object[] newInstance(int length)
  {
    return new Object[length];
  }
  
  public static Object[] newInstance(int length, Object sample)
  {
    return (Object[]) 
      java.lang.reflect.Array.newInstance(sample.getClass(),length);
  }
  
  /** Return a new copy with newSize elements */
  public static Object[] resizeArray(Object[] from, int newSize)
  {
    int copyLength = from.length;
    if(newSize<copyLength)
      copyLength=newSize;
    
    Object[] res = newInstance(newSize);
    java.lang.System.arraycopy(from,0,res,0,copyLength);

    return res;
  }
  
  public int getLength()
  {
    return value.length;
  }
  
  public Object get(int index)
  {
    return value[index];
  }
  
  public void set(int index, Object elem)
  {
    value[index]=elem;
  }

  public Object[] value;
}
