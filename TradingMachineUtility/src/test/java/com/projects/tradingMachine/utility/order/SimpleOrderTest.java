package com.projects.tradingMachine.utility.order;

import com.projects.tradingMachine.utility.order.SimpleOrder;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;

public class SimpleOrderTest {
  @Rule
  public final Timeout globalTimeout = new Timeout(10000);

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void testConstructor() {
    final SimpleOrder actual = new SimpleOrder();
    Assert.assertNotNull(actual);
  }

  @Test
  public void testConstructor2() {
    final String arg0 = "";
    final SimpleOrder actual = new SimpleOrder(arg0);
    Assert.assertNotNull(actual);
  }
}