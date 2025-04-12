/*
 *  Copyright © 2017-2019 Cask Data, Inc.
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

package io.cdap.directives.aggregates;

import io.cdap.wrangler.TestingRig;
import io.cdap.wrangler.api.Row;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests {@link io.cdap.directives.aggregates.AggregateByteAndTime}
 */
public class AggregateByteAndTimeTest {

  @Test
  public void testBasicAggregateTotal() throws Exception {
    String[] colNames = new String[] { "col1", "col2" };
    String[] byteSizeInputs = new String[] { "1KB", "1MB", "2GB",
        "1TB" };
    String[] timeInputs = new String[] { "1s", "1m", "2h",
        "1d" };
    String[] outputs = new String[] { "1101660161024B", "93661000ms" };
    String[] directives = { String.format("%s :%s :%s", AggregateByteAndTime.NAME, colNames[0], colNames[1]) };
    List<Row> rows = new ArrayList<>();
    for (int i = 0; i < byteSizeInputs.length; i++) {
      Row row = new Row().add(colNames[0], byteSizeInputs[i]).add(colNames[1], timeInputs[i]);
      rows.add(row);
    }
    List<Row> resultRows = TestingRig.execute(directives, rows);

    Assert.assertEquals(1, resultRows.size());

    Assert.assertEquals(outputs[0],
        resultRows.get(0).getValue(AggregateByteAndTime.RESULT_SIZE_COLUMN_NAME));
    Assert.assertEquals(outputs[1],
        resultRows.get(0).getValue(AggregateByteAndTime.RESULT_TIME_COLUMN_NAME));
  }

  @Test
  public void testAggregateTotalWithOutputSizeUnit() throws Exception {
    String aggregationType = AggregateByteAndTime.AggregationEnum.total.toString();
    String[] colNames = new String[] { "col1", "col2" };
    String[] units = new String[] { "MB" };
    String[] byteSizeInputs = new String[] { "1KB", "1MB", "2GB",
        "1TB" };
    String[] timeInputs = new String[] { "1s", "1m", "2h",
        "1d" };
    String[] outputs = new String[] { "1050625.001MB", "93661000ms" };
    String[] directives = {
        String.format("%s :%s :%s \"%s\" \"%s\"", AggregateByteAndTime.NAME, colNames[0], colNames[1], aggregationType,
            units[0]) };
    List<Row> rows = new ArrayList<>();
    for (int i = 0; i < byteSizeInputs.length; i++) {
      Row row = new Row().add(colNames[0], byteSizeInputs[i]).add(colNames[1], timeInputs[i]);
      rows.add(row);
    }
    List<Row> resultRows = TestingRig.execute(directives, rows);

    Assert.assertEquals(1, resultRows.size());

    Assert.assertEquals(outputs[0],
        resultRows.get(0).getValue(AggregateByteAndTime.RESULT_SIZE_COLUMN_NAME));
    Assert.assertEquals(outputs[1],
        resultRows.get(0).getValue(AggregateByteAndTime.RESULT_TIME_COLUMN_NAME));
  }

  @Test
  public void testAggregateTotalWithOutputSizeUnitAndTimeUnit() throws Exception {
    String aggregationType = AggregateByteAndTime.AggregationEnum.total.toString();
    String[] colNames = new String[] { "col1", "col2" };
    String[] units = new String[] { "MB", "h" };
    String[] byteSizeInputs = new String[] { "1KB", "1MB", "2GB",
        "1TB" };
    String[] timeInputs = new String[] { "1s", "1m", "2h",
        "1d" };
    String[] outputs = new String[] { "1050625.001MB", "26.017h" };
    String[] directives = {
        String.format("%s :%s :%s \"%s\" \"%s\" \"%s\"", AggregateByteAndTime.NAME, colNames[0], colNames[1],
            aggregationType, units[0],
            units[1]) };
    List<Row> rows = new ArrayList<>();
    for (int i = 0; i < byteSizeInputs.length; i++) {
      Row row = new Row().add(colNames[0], byteSizeInputs[i]).add(colNames[1], timeInputs[i]);
      rows.add(row);
    }
    List<Row> resultRows = TestingRig.execute(directives, rows);

    Assert.assertEquals(1, resultRows.size());

    Assert.assertEquals(outputs[0],
        resultRows.get(0).getValue(AggregateByteAndTime.RESULT_SIZE_COLUMN_NAME));
    Assert.assertEquals(outputs[1],
        resultRows.get(0).getValue(AggregateByteAndTime.RESULT_TIME_COLUMN_NAME));
  }

  @Test
  public void testBasicAggregateAverage() throws Exception {
    String aggregationType = AggregateByteAndTime.AggregationEnum.average.toString();
    String[] colNames = new String[] { "col1", "col2" };
    String[] byteSizeInputs = new String[] { "1KB", "1MB", "2GB",
        "1TB" };
    String[] timeInputs = new String[] { "1s", "1m", "2h",
        "1d" };
    String[] outputs = new String[] { "275415040256B", "23415250ms" };
    String[] directives = {
        String.format("%s :%s :%s \"%s\"", AggregateByteAndTime.NAME, colNames[0], colNames[1], aggregationType) };
    List<Row> rows = new ArrayList<>();
    for (int i = 0; i < byteSizeInputs.length; i++) {
      Row row = new Row().add(colNames[0], byteSizeInputs[i]).add(colNames[1], timeInputs[i]);
      rows.add(row);
    }
    List<Row> resultRows = TestingRig.execute(directives, rows);

    Assert.assertEquals(1, resultRows.size());

    Assert.assertEquals(outputs[0],
        resultRows.get(0).getValue(AggregateByteAndTime.RESULT_SIZE_COLUMN_NAME));
    Assert.assertEquals(outputs[1],
        resultRows.get(0).getValue(AggregateByteAndTime.RESULT_TIME_COLUMN_NAME));
  }

  @Test
  public void testAggregateAverageWithOutputSizeUnit() throws Exception {
    String aggregationType = AggregateByteAndTime.AggregationEnum.average.toString();
    String[] colNames = new String[] { "col1", "col2" };
    String[] units = new String[] { "MB" };
    String[] byteSizeInputs = new String[] { "1KB", "1MB", "2GB",
        "1TB" };
    String[] timeInputs = new String[] { "1s", "1m", "2h",
        "1d" };
    String[] outputs = new String[] { "262656.250MB", "23415250ms" };
    String[] directives = {
        String.format("%s :%s :%s \"%s\" \"%s\"", AggregateByteAndTime.NAME, colNames[0], colNames[1], aggregationType,
            units[0]) };
    List<Row> rows = new ArrayList<>();
    for (int i = 0; i < byteSizeInputs.length; i++) {
      Row row = new Row().add(colNames[0], byteSizeInputs[i]).add(colNames[1], timeInputs[i]);
      rows.add(row);
    }
    List<Row> resultRows = TestingRig.execute(directives, rows);

    Assert.assertEquals(1, resultRows.size());

    Assert.assertEquals(outputs[0],
        resultRows.get(0).getValue(AggregateByteAndTime.RESULT_SIZE_COLUMN_NAME));
    Assert.assertEquals(outputs[1],
        resultRows.get(0).getValue(AggregateByteAndTime.RESULT_TIME_COLUMN_NAME));
  }

  @Test
  public void testAggregateAverageWithOutputSizeUnitAndTimeUnit() throws Exception {
    String aggregationType = AggregateByteAndTime.AggregationEnum.average.toString();
    String[] colNames = new String[] { "col1", "col2" };
    String[] units = new String[] { "MB", "h" };
    String[] byteSizeInputs = new String[] { "1KB", "1MB", "2GB",
        "1TB" };
    String[] timeInputs = new String[] { "1s", "1m", "2h",
        "1d" };
    String[] outputs = new String[] { "262656.250MB", "6.504h" };
    String[] directives = {
        String.format("%s :%s :%s \"%s\" \"%s\" \"%s\"", AggregateByteAndTime.NAME, colNames[0], colNames[1],
            aggregationType, units[0],
            units[1]) };
    List<Row> rows = new ArrayList<>();
    for (int i = 0; i < byteSizeInputs.length; i++) {
      Row row = new Row().add(colNames[0], byteSizeInputs[i]).add(colNames[1], timeInputs[i]);
      rows.add(row);
    }
    List<Row> resultRows = TestingRig.execute(directives, rows);

    Assert.assertEquals(1, resultRows.size());

    Assert.assertEquals(outputs[0],
        resultRows.get(0).getValue(AggregateByteAndTime.RESULT_SIZE_COLUMN_NAME));
    Assert.assertEquals(outputs[1],
        resultRows.get(0).getValue(AggregateByteAndTime.RESULT_TIME_COLUMN_NAME));
  }
}
