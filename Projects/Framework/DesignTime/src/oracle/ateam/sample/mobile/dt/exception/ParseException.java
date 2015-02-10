package oracle.ateam.sample.mobile.dt.exception;

public class ParseException
  extends RuntimeException
{
  public ParseException(String string, Throwable throwable, boolean b, boolean b1)
  {
    super(string, throwable, b, b1);
  }

  public ParseException(Throwable throwable)
  {
    super(throwable);
  }

  public ParseException(String string, Throwable throwable)
  {
    super(string, throwable);
  }

  public ParseException(String string)
  {
    super(string);
  }

  public ParseException()
  {
    super();
  }
}
