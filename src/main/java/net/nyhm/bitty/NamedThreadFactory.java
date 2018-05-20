package net.nyhm.bitty;

import java.util.concurrent.ThreadFactory;

/**
 * A thread factory that names the threads.
 */
final class NamedThreadFactory implements ThreadFactory
{
  private final String mName;

  NamedThreadFactory(String name) {
    mName = name;
  }

  @Override
  public Thread newThread(Runnable r) {
    return new Thread(r, mName);
  }
}
