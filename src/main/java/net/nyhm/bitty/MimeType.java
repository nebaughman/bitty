package net.nyhm.bitty;

/**
 * Simple representation of a MIME type
 */
public class MimeType {
  private final String mPrimary;
  private final String mSecondary;

  public MimeType(String primary, String secondary) {
    mPrimary = primary;
    mSecondary = secondary;
  }

  public String export() {
    return mPrimary + "/" + mSecondary;
  }

  // TODO: public static MimeType parse(String mimeType)
}
