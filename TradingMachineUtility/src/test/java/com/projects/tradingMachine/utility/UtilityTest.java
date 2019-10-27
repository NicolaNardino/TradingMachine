package com.projects.tradingMachine.utility;

import com.projects.tradingMachine.utility.Utility.DestinationType;
import java.lang.reflect.Array;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;

public class UtilityTest {
  @Rule
  public final Timeout globalTimeout = new Timeout(10000);

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void values() {
    final DestinationType[] actual = DestinationType.values();
    Assert.assertArrayEquals(new DestinationType[]{DestinationType.Queue, DestinationType.Topic}, actual);
  }

  @Test
  public void valueOf() {
    final String arg0 = "Queue";
    final DestinationType actual = DestinationType.valueOf(arg0);
    Assert.assertEquals(DestinationType.Queue, actual);
  }
}