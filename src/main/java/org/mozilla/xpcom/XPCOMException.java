package org.mozilla.xpcom;

public class XPCOMException extends RuntimeException
{
  public long errorcode;
  private static final long serialVersionUID = 198521829884000593L;

  public XPCOMException()
  {
    this(2147500037L, "Unspecified internal XPCOM error");
  }

  public XPCOMException(String paramString)
  {
    this(2147500037L, paramString);
  }

  public XPCOMException(long paramLong)
  {
    this(paramLong, "Internal XPCOM error");
  }

  public XPCOMException(long paramLong, String paramString)
  {
    super(paramString + "  (0x" + Long.toHexString(paramLong) + ")");
    this.errorcode = paramLong;
  }
}