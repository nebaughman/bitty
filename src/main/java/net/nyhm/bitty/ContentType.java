package net.nyhm.bitty;

import java.nio.charset.Charset;

/**
 * Simple representation of a Content-Type HTTP header
 */
public class ContentType {
  private final MimeType mMimeType;
  private final Charset mCharset;

  public ContentType(MimeType mimeType, Charset charset) {
    mMimeType = mimeType;
    mCharset = charset;
  }

  /**
   * This produces the full name:value HTTP header
   */
  public String toFullHeader() {
    return "Content-Type: " + toHeaderValue();
  }

  /**
   * HTTP headers are name:value pairs; this produces the value part
   */
  public String toHeaderValue() {
    return mMimeType.export() + "; charset=" + mCharset.name().toLowerCase();
  }

  /**
   * For logging purposes only
   */
  public String toString() {
    return getClass().getSimpleName() + "{" + toFullHeader() + "}";
  }
}
