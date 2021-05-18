package com.selenium.jayflow.utils;

import java.util.concurrent.TimeUnit;

public class Delay {
  private static Delay delayInstance;

  private Delay() {
  }

  public static Delay getinstance() {
    if (delayInstance == null) {
      delayInstance = new Delay();
    }
    return delayInstance;
  }

  public void seconds(int seconds) {
    try {
      TimeUnit.SECONDS.sleep(seconds);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
