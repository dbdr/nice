package nice.lang;

public abstract class AbstractArray implements nice.lang.Sequence
{
  public abstract int getLength();
  public abstract Object get(int index);
  public abstract void set(int index, Object elem);
}
