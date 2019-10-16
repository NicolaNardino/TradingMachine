package com.projects.tradingMachine.utility.database;

import com.projects.tradingMachine.utility.database.DatabaseProperties;
import com.projects.tradingMachine.utility.database.DbUtility;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;

public class DbUtilityTest {
  @Rule
  public final Timeout globalTimeout = new Timeout(10000);

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void testConstructor() {
    final DbUtility actual = new DbUtility();
    Assert.assertNotNull(actual);
  }

  @Test
  public void getMySqlConnectionUrl() {
    final DatabaseProperties arg0 = new DatabaseProperties("aaaaa", 1, "aaaaa");
    final String actual = DbUtility.getMySqlConnectionUrl(arg0);
    Assert.assertEquals("jdbc:mysql://aaaaa/aaaaa", actual);
  }
}