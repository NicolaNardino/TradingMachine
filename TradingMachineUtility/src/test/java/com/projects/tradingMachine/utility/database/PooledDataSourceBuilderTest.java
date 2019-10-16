package com.projects.tradingMachine.utility.database;

import com.projects.tradingMachine.utility.database.PooledDataSourceBuilder;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;

public class PooledDataSourceBuilderTest {
  @Rule
  public final Timeout globalTimeout = new Timeout(10000);

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void testConstructor() {
    final PooledDataSourceBuilder actual = new PooledDataSourceBuilder();
    Assert.assertNotNull(actual);
  }
}