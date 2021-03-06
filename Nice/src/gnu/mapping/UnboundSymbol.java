package gnu.mapping;

// An undefined symbol was evaled.
public class UnboundSymbol extends RuntimeException
{
  public String symbol;

  public UnboundSymbol(String symbol)
  {
    super ("Unbound symbol " + symbol);
    this.symbol = symbol;
  }

  public UnboundSymbol(String symbol, String message)
  {
    super (message);
    this.symbol = symbol;
  }
  
}
