public final class _Array implements nice.lang.Sequence
{
  _Array(Object[] value)
  {
    this.value=value;
  }
  
  public static _Array make(Object[] value)
  {
    if(value==null)
      return null;
    return new _Array(value);
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
  
  /** 
   * This method is neccessary to dispatch 
   * between Native arrays and _Arrays
   */
  static public int getLength(Object array)
  {
    if(array instanceof _Array)
      return ((_Array) array).value.length;
    else
      return ((Object[]) array).length;
  }
  
  /** 
   * This method is neccessary to dispatch 
   * between Native arrays and _Arrays
   */
  static public Object get(Object array, int index)
  {
    if(array instanceof _Array)
      return ((_Array) array).value[index];
    else
      return ((Object[]) array)[index];
  }
  
  /** 
   * This method is neccessary to dispatch 
   * between Native arrays and _Arrays
   */
  static public void set(Object array, int index, Object elem)
  {
    if(array instanceof _Array)
      ((_Array) array).value[index]=elem;
    else
      ((Object[]) array)[index]=elem;
  }

  public Object[] value;
}
