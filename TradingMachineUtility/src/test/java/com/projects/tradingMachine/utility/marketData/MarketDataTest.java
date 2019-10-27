package com.projects.tradingMachine.utility.marketData;

import com.projects.tradingMachine.utility.marketData.MarketData;
import java.util.Date;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;

public class MarketDataTest {
  @Rule
  public final Timeout globalTimeout = new Timeout(10000);

  @Rule
  public final ExpectedException thrown = ExpectedException.none();


  @Test
  public void getSymbolOutputNotNull() {
    final MarketData thisObj = new MarketData("ABC", 1.0, 1.0, 1, 1);
    final String actual = thisObj.getSymbol();
    Assert.assertEquals("ABC", actual);
  }

  @Test
  public void getBidOutputPositive() {
    final MarketData thisObj = new MarketData("ABC", 1.0, 1.0, 1, 1);
    final double actual = thisObj.getBid();
    Assert.assertEquals(1.0, actual, 0.0);
  }

  @Test
  public void getAskOutputPositive() {
    final MarketData thisObj = new MarketData("ABC", 1.0, 1.0, 1, 1);
    final double actual = thisObj.getAsk();
    Assert.assertEquals(1.0, actual, 0.0);
  }

  @Test
  public void getBidSizeOutputPositive() {
    final MarketData thisObj = new MarketData("ABC", 1.0, 1.0, 1, 1);
    final int actual = thisObj.getBidSize();
    Assert.assertEquals(1, actual);
  }

  @Test
  public void testConstructor() {
    final String arg0 = "ABC";
    final String arg1 = "ABC";
    final double arg2 = 1.0;
    final double arg3 = 1.0;
    final int arg4 = 1;
    final int arg5 = 1;
    final Date arg6 = new Date(1L);
    final MarketData actual = new MarketData(arg0, arg1, arg2, arg3, arg4, arg5, arg6);
    Assert.assertNotNull(actual);
    Assert.assertEquals(1, actual.getAskSize());
    Assert.assertEquals(1.0, actual.getBid(), 0.0);
    Assert.assertEquals(1.0, actual.getAsk(), 0.0);
    Assert.assertEquals(1, actual.getBidSize());
    Assert.assertNotNull(actual.getQuoteTime());
    Assert.assertEquals("ABC", actual.getSymbol());
  }

  @Test
  public void getAskSize() {
    final MarketData thisObj = new MarketData("ABC", 1.0, 1.0, 1, 1);
    final int actual = thisObj.getAskSize();
    Assert.assertEquals(1, actual);
  }

  @Test
  public void testConstructor2() {
    final String arg0 = "ABC";
    final double arg1 = 1.0;
    final double arg2 = 1.0;
    final int arg3 = 1;
    final int arg4 = 1;
    final MarketData actual = new MarketData(arg0, arg1, arg2, arg3, arg4);
    Assert.assertNotNull(actual);
    Assert.assertEquals(1, actual.getAskSize());
    Assert.assertEquals(1.0, actual.getBid(), 0.0);
    Assert.assertEquals(1.0, actual.getAsk(), 0.0);
    Assert.assertEquals(1, actual.getBidSize());
    Assert.assertNotNull(actual.getQuoteTime());
    Assert.assertEquals("ABC", actual.getSymbol());
  }

  @Test
  public void getQuoteTimeOutputNotNull() {
    final MarketData thisObj = new MarketData("ABC", 1.0, 1.0, 1, 1);
    final Date actual = thisObj.getQuoteTime();
    Assert.assertNotNull(actual);
  }

}