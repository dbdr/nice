package mlsub.typing.lowlevel;

/**
 * For pretty-printing lists separated by commas or semi-colons or, etc.
 **/
public class Separator {
  private boolean first;
  private String content1;
  private String content2;

  public Separator(String content1, String content2) {
    this.first = true;
    this.content1 = content1;
    this.content2 = content2;
  }
  
  public Separator(String content2) {
    this("", content2);
  }

  public void setContent(String content) {
    this.content2 = content2;
  }
  
  public String toString() {
    if (first) {
      first = false;
      return content1;
    } else {
      return content2;
    }
  }
  public void reset() {
    this.first = true;
  }

  public void reset(String content2) {
    this.first = true;
    this.content2 = content2;
  }

  public void reset(String content1, String content2) {
    this.first = true;
    this.content1 = content1;
    this.content2 = content2;
  }
}
    
