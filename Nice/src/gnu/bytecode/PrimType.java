package gnu.bytecode;
import java.io.*;

public class PrimType extends Type {

  public PrimType (String nam, String sig, int siz, Class reflectClass) {
    super(nam, sig);
    size = siz;
    this.reflectClass = reflectClass;
    Type.registerTypeForClass(reflectClass, this);
  }

  protected PrimType(PrimType type)
  {
    this(type.this_name, type.signature, type.size, type.reflectClass);
  }

  public Object coerceFromObject (Object obj)
  {
    if (obj.getClass() == reflectClass)
      return obj;
    char sig1 = (signature == null || signature.length() != 1) ? ' '
      : signature.charAt(0);
    switch (sig1)
      {
      case 'B':	return new Byte(((Number) obj).byteValue());
      case 'S':	return new Short(((Number) obj).shortValue());
      case 'I':	return new Integer(intValue(obj));
      case 'J':	return new Long(longValue(obj));
      case 'F':	return new Float(((Number) obj).floatValue());
      case 'D':	return new Double(((Number) obj).doubleValue());
      }
    throw new ClassCastException("don't know how to coerce "
				 + obj.getClass().getName() + " to "
				 + getName());
  }

  /** Coerce value to an int.
   * Only defined if getSignature() is "I". */
  public int intValue (Object value)
  {
    try
      {
	return ((Number) value).intValue();
      }
    catch(ClassCastException e)
      {
	return ((Character) value).charValue();
      }
  }

  /** Coerce value to a long.
   * Only defined if getSignature() is "L". */
  public long longValue (Object value)
  {
    try
      {
	return ((Number) value).longValue();
      }
    catch(ClassCastException e)
      {
	return ((Character) value).charValue();
      }
  }

  /** Coerce value to a char.
   * Only defined if getSignature() is "C". */
  public char charValue (Object value)
  {
    return ((Character) value).charValue();
  }

  /** Coerce value to a boolean.
   * Only defined if getSignature() is "Z". */
  public static boolean booleanValue (Object value)
  {
    return ! (value instanceof Boolean) || ((Boolean) value).booleanValue();
  }

  public void emitCoerceFromObject (CodeAttr code)
  {
    char sig1 = (signature == null || signature.length() != 1) ? ' '
      : signature.charAt(0);
    if (sig1 == 'Z')  // boolean
      {
	code.emitCheckcast(boolean_ctype);
	code.emitInvokeVirtual(booleanValue_method);
      }
    else if (sig1 == 'V')
      code.emitPop(1);
    else if (sig1 == 'C')
      {
	code.emitCheckcast(character_type);
	code.emitInvokeVirtual(charValue_method);
      }
    else
      {
	code.emitCheckcast(number_type);
	if (sig1 == 'I' || sig1 == 'S' || sig1 == 'B')
	  code.emitInvokeVirtual(intValue_method);
	else if (sig1 == 'J')
	  code.emitInvokeVirtual(longValue_method);
	else if (sig1 == 'D')
	  code.emitInvokeVirtual(doubleValue_method);
	else if (sig1 == 'F')
	  code.emitInvokeVirtual(floatValue_method);
	else
	  super.emitCoerceFromObject(code);
      }
  }

  private static Field trueBoolean;
  private static Field falseBoolean;
  private static Method int_init, long_init, char_init;
  private static Method float_init, double_init;
  
  public void emitCoerceToObject (CodeAttr code)
  {
    char sig1 = (signature == null || signature.length() != 1) ? ' '
      : signature.charAt(0);
    switch (sig1)
      {
      case 'Z':  // boolean
	if (trueBoolean == null)
	  {
	    trueBoolean = boolean_ctype.getDeclaredField("TRUE");
	    falseBoolean = boolean_ctype.getDeclaredField("FALSE");
	  }
	
	code.emitIfIntNotZero();
	code.emitGetStatic(trueBoolean);
	code.emitElse();
	code.emitGetStatic(falseBoolean);
	code.emitFi();
	return;
      case 'I': case 'S': case 'B': // int, short, byte
	if (int_init == null)
	  {
	    int_init = int_ctype.getDeclaredMethod("<init>", 
						   new Type[]{int_type});
	  }
	
	code.emitNew(int_ctype);
	code.emitDupX();
	code.emitSwap();
	code.emitInvokeSpecial(int_init);
	return;
      case 'J': // long
	if (long_init == null)
	  {
	    long_init = long_ctype.getDeclaredMethod("<init>", 
						     new Type[]{this});
	  }
	
	code.emitNew(long_ctype);
	code.emitDupX();
	code.emitDupX();
	code.emitPop(1);
	code.emitInvokeSpecial(long_init);
	return;
      case 'F': // float
	if (float_init == null)
	  {
	    float_init = float_ctype.getDeclaredMethod("<init>", 
						       new Type[]{float_type});
	  }
	
	code.emitNew(float_ctype);
	code.emitDupX();
	code.emitSwap();
	code.emitInvokeSpecial(float_init);
	return;
      case 'D': // double
	if (double_init == null)
	  {
	    double_init = double_ctype.getDeclaredMethod("<init>", 
							 new Type[]{double_type});
	  }
	
	code.emitNew(double_ctype);
	code.emitDupX();
	code.emitDupX();
	code.emitPop(1);
	code.emitInvokeSpecial(double_init);
	return;
      case 'C': // char
	if (char_init == null)
	  {
	    char_init = char_ctype.getDeclaredMethod("<init>", 
						     new Type[]{this});
	  }
	
	code.emitNew(char_ctype);
	code.emitDupX();
	code.emitSwap();
	code.emitInvokeSpecial(char_init);
	return;
      }
    
    throw new Error ("unimplemented emitCoerceToObject for " + this);
  }

  public static int compare(PrimType type1, PrimType type2)
  {
    char sig1 = type1.signature.charAt(0);
    char sig2 = type2.signature.charAt(0);

    if (sig1 == sig2)
      return 0;

    // Anything can be converted to void, but not vice versa.
    if (sig1 == 'V')
      return 1;
    if (sig2 == 'V')
      return -1;

    // In Java, no other type can be converted to/from boolean.
    // Other languages, including C and Scheme are different:
    // "everything" can be converted to a boolean.
    if (sig1 == 'Z' || sig2 == 'Z')
      return -3;

    if (sig1 == 'C')
      return type2.size > 2 ? -1 : -3;
    if (sig2 == 'C')
      return type1.size > 2 ? 1 : -3;

    if (sig1 == 'D')
      return 1;
    if (sig2 == 'D')
      return -1;
    if (sig1 == 'F')
      return 1;
    if (sig2 == 'F')
      return -1;
    if (sig1 == 'J')
      return 1;
    if (sig2 == 'J')
      return -1;
    if (sig1 == 'I')
      return 1;
    if (sig2 == 'I')
      return -1;
    if (sig1 == 'S')
      return 1;
    if (sig2 == 'S')
      return -1;
    // Can we get here?
    return -3;
  }

  public int compare(Type other)
  {
    if (other instanceof PrimType)
      return compare(this, (PrimType) other);
    if (! (other instanceof ClassType))
      return -3;
    char sig1 = signature.charAt(0);
    String otherName = other.getName();
    // This is very incomplete!  FIXME.
    switch (sig1)
      {
      case 'V':
        return 1;
      case 'D':
        if (otherName.equals("java.lang.Double")
            || otherName.equals("gnu.math.DFloat"))
          return 0; // Or maybe 1?
        break;
      case 'I':
        if (otherName.equals("java.lang.Integer"))
          return 0; // Or maybe 1?
        if (otherName.equals("gnu.math.IntNum"))
          return -1;
        break;
      }
    if (otherName.equals("java.lang.Object"))
      return -1;
    return -2;
  }

  /** @return true if values of this type can be assigned to other
      <b>without widening nor conversion</b>.
  */
  public boolean isAssignableTo(Type other)
  {
    return this == other;
  }
}
