package com.projects.tradingMachine.utility.database.creditCheck;

import com.projects.tradingMachine.utility.database.creditCheck.CreditCheckException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;

public class CreditCheckExceptionTest {
  @Rule
  public final Timeout globalTimeout = new Timeout(10000);

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void testConstructor() {
    final Throwable arg0 = new Throwable();
    final CreditCheckException actual = new CreditCheckException(arg0);
    Assert.assertNotNull(actual);
    Assert.assertEquals("java.lang.Throwable", actual.getMessage());
  }

  @Test
  public void testConstructor2() {
    final String arg0 = "aaaaa";
    final Throwable arg1 = new Throwable();
    final CreditCheckException actual = new CreditCheckException(arg0, arg1);
    Assert.assertNotNull(actual);
    Assert.assertEquals("aaaaa", actual.getMessage());
  }

  @Test
  public void testConstructor3() {
    final String arg0 = "aaaaa";
    final CreditCheckException actual = new CreditCheckException(arg0);
    Assert.assertNotNull(actual);
    Assert.assertEquals("aaaaa", actual.getMessage());
  }

  @Test
  public void testConstructor4() {
    final CreditCheckException actual = new CreditCheckException();
    Assert.assertNotNull(actual);
    Assert.assertNull(actual.getMessage());
  }
}