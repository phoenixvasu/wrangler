/*
 *  Copyright © 2021 Cask Data, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */
package io.cdap.directives.parser;

import io.cdap.wrangler.TestingRig;
import io.cdap.wrangler.api.RecipeException;
import io.cdap.wrangler.api.Row;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

/**
 * Tests {@link io.cdap.directives.parser.ParseTimeDuration}
 */
public class ParseTimeDurationTest {

  @Test
  public void testTime() throws Exception {
    String[] colNames = new String[] { "col1", "col2", "col3", "col4", "col5" };
    String[] timeInputs = new String[] { "1s", "1m", "2h",
        "1d", "223400s" };
    String[] timeOutputs = new String[] { "1000ms", "60000ms", "7200000ms", "86400000ms", "223400000ms" };
    String[] directives = new String[timeInputs.length];
    Row row = new Row();
    for (int i = 0; i < timeInputs.length; i++) {
      directives[i] = String
          .format("%s :%s", ParseTimeDuration.NAME, colNames[i]);
      row.add(colNames[i], timeInputs[i]);
    }
    List<Row> rows = TestingRig.execute(directives, Collections.singletonList(row));

    Assert.assertEquals(1, rows.size());

    for (Row resultRow : rows) {
      for (int i = 0; i < timeInputs.length; i++) {
        Assert.assertEquals(timeOutputs[i],
            rows.get(0).getValue(colNames[i]));
      }
    }
  }

  @Test
  public void testTimeFormats() throws Exception {
    String[] units = new String[] { "ms", "s", "m",
        "h", "d", "ms" };
    String[] colNames = new String[] { "col1", "col2", "col3", "col4", "col5",
        "col6" };
    String[] timeInputs = new String[] { "1s", "40m", "540000s",
        "1d", "200.1h", "20.24s" };
    String[] timeOutputs = new String[] { "1000ms", "2400s", "9000m", "24h", "8.338d", "20240ms" };
    String[] directives = new String[units.length];
    Row row = new Row();
    for (int i = 0; i < timeInputs.length; i++) {
      directives[i] = String
          .format("%s :%s \"%s\"", ParseTimeDuration.NAME, colNames[i], units[i]);
      row.add(colNames[i], timeInputs[i]);
    }
    List<Row> rows = TestingRig.execute(directives,
        Collections.singletonList(row));

    Assert.assertEquals(1, rows.size());

    for (Row resultRow : rows) {
      for (int i = 0; i < timeInputs.length; i++) {
        Assert.assertEquals(timeOutputs[i],
            rows.get(0).getValue(colNames[i]));
      }
    }
  }

  @Test(expected = RecipeException.class)
  public void testInvalidUpperCaseUnit() throws Exception {
    String unit = "S";
    String colName = "col1";
    String time = "100s";
    String[] directives = new String[] {
        String.format("%s :%s \"%s\"", ParseTimeDuration.NAME, colName, unit)
    };
    Row row1 = new Row();
    row1.add(colName, time);
    TestingRig.execute(directives, Collections.singletonList(row1));
  }

  @Test(expected = RecipeException.class)
  public void testInvalidUnit() throws Exception {
    String unit = "a";
    String colName = "col1";
    String time = "100s";
    String[] directives = new String[] {
        String.format("%s :%s \"%s\"", ParseTimeDuration.NAME, colName, unit)
    };
    Row row1 = new Row();
    row1.add(colName, time);
    TestingRig.execute(directives, Collections.singletonList(row1));
  }

  @Test
  public void testTimeWithoutUnit() throws Exception {
    String colName = "col1";
    String timeInput = "100";
    String timeOutput = "100ms";
    String[] directives = new String[] {
        String.format("%s :%s", ParseTimeDuration.NAME, colName)
    };
    Row row1 = new Row();
    row1.add(colName, timeInput);
    List<Row> rows = TestingRig.execute(directives,
        Collections.singletonList(row1));

    Assert.assertEquals(1, rows.size());

    Assert.assertEquals(timeOutput,
        rows.get(0).getValue(colName));
  }

  @Test(expected = NumberFormatException.class)
  public void testInvalidTime() throws Exception {
    String colName = "col1";
    String time = "abc";
    String[] directives = new String[] {
        String.format("%s :%s", ParseTimeDuration.NAME, colName)
    };
    Row row1 = new Row();
    row1.add(colName, time);
    TestingRig.execute(directives, Collections.singletonList(row1));
  }
}
