package com.projects.tradingMachine.utility.database;

import com.projects.tradingMachine.utility.database.DatabaseProperties;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;

public class DatabasePropertiesTest {
  @Rule
  public final Timeout globalTimeout = new Timeout(10000);

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void getPortOutputZero() {
    final DatabaseProperties databaseProperties = new DatabaseProperties("foo", 0, "foo");
    Assert.assertEquals(0, databaseProperties.getPort());
  }

  @Test
  public void getUserNameOutputVoid() {
    final DatabaseProperties thisObj = new DatabaseProperties("aaaaa", 1, "aaaaa");
    final String actual = thisObj.getUserName();
  }

  @Test
  public void getUserNameOutputNull() {
    final DatabaseProperties databaseProperties = new DatabaseProperties("foo", 0, "foo");
    Assert.assertNull(databaseProperties.getUserName());
  }

  @Test
  public void getPasswordOutputVoid() {
    final DatabaseProperties thisObj = new DatabaseProperties("aaaaa", 1, "aaaaa");
    final String actual = thisObj.getPassword();
  }

  @Test
  public void getPasswordOutputNull() {
    final DatabaseProperties databaseProperties = new DatabaseProperties("foo", 0, "foo");
    Assert.assertNull(databaseProperties.getPassword());
  }

  @Test
  public void testConstructor() {
    final String arg0 = "aaaaa";
    final int arg1 = 1;
    final String arg2 = "aaaaa";
    final DatabaseProperties actual = new DatabaseProperties(arg0, arg1, arg2);
    Assert.assertNotNull(actual);
    Assert.assertEquals("aaaaa", actual.getHost());
    Assert.assertEquals("aaaaa", actual.getDatabaseName());
    Assert.assertEquals(1, actual.getPort());
    Assert.assertNull(actual.getPassword());
    Assert.assertNull(actual.getUserName());
  }

  @Test
  public void testConstructor2() {
    final String arg0 = "aaaaa";
    final int arg1 = 1;
    final String arg2 = "aaaaa";
    final String arg3 = "aaaaa";
    final String arg4 = "aaaaa";
    final DatabaseProperties actual = new DatabaseProperties(arg0, arg1, arg2, arg3, arg4);
    Assert.assertNotNull(actual);
    Assert.assertEquals("aaaaa", actual.getHost());
    Assert.assertEquals("aaaaa", actual.getDatabaseName());
    Assert.assertEquals(1, actual.getPort());
    Assert.assertEquals("aaaaa", actual.getPassword());
    Assert.assertEquals("aaaaa", actual.getUserName());
  }

  @Test
  public void getHostOutputNotNull() {
    final DatabaseProperties thisObj = new DatabaseProperties("aaaaa", 1, "aaaaa");
    final String actual = thisObj.getHost();
    Assert.assertEquals("aaaaa", actual);
  }

  @Test
  public void getHostOutputNotNull2() {
    final DatabaseProperties databaseProperties = new DatabaseProperties("foo", 0, "foo");
    Assert.assertEquals("foo", databaseProperties.getHost());
  }

  @Test
  public void getDatabaseNameOutputNotNull1() {
    final DatabaseProperties thisObj = new DatabaseProperties("aaaaa", 1, "aaaaa");
    final String actual = thisObj.getDatabaseName();
    Assert.assertEquals("aaaaa", actual);
  }

  @Test
  public void getPortOutputPositive() {
    final DatabaseProperties thisObj = new DatabaseProperties("aaaaa", 1, "aaaaa");
    final int actual = thisObj.getPort();
    Assert.assertEquals(1, actual);
  }

  @Test
  public void getDatabaseNameOutputNotNull() {
    final DatabaseProperties databaseProperties = new DatabaseProperties("foo", 0, "foo");
    Assert.assertEquals("foo", databaseProperties.getDatabaseName());
  }
}