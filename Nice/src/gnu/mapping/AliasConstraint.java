// Copyright (c) 2000  Per M.A. Bothner.
// This is free software;  for terms and warranty disclaimer see ./COPYING.

package gnu.mapping;

/** The value field of a AliasConstraint points to another Binding. */

public class AliasConstraint extends Constraint
{
  public Object get (Binding binding)
  {
    return ((Location) binding.value).get();
  }

  public void set (Binding binding, Object value)
  {
    ((Location) binding.value).set(value);
  }

  public boolean isBound (Binding binding)
  {
    return ((Location) binding.value).isBound();
  }

  public static void define (Binding binding, Location location)
  {
    synchronized (binding)
      {
	binding.value = location;
	binding.constraint = new AliasConstraint();  // FIXME share?
      }
  }
}
