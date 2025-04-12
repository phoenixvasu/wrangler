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
 * Tests {@link io.cdap.directives.parser.ParseByteSize}
 */
public class ParseByteSizeTest {

  @Test
  public void testByteSize() throws Exception {
    String[] colNames = new String[] { "col1", "col2", "col3", "col4", "col5" };
    String[] byteSizeInputs = new String[] { "1KB", "1MB", "2GB",
        "1TB", "223400KB" };
    String[] byteSizeOutputs = new String[] { "1024B", "1048576B", "2147483648B",
        "1099511627776B", "228761600B" };
    String[] directives = new String[byteSizeInputs.length];
    Row row = new Row();
    for (int i = 0; i < byteSizeInputs.length; i++) {
      directives[i] = String
          .format("%s :%s", ParseByteSize.NAME, colNames[i]);
      row.add(colNames[i], byteSizeInputs[i]);
    }
    List<Row> rows = TestingRig.execute(directives, Collections.singletonList(row));

    Assert.assertEquals(1, rows.size());

    for (Row resultRow : rows) {
      for (int i = 0; i < byteSizeInputs.length; i++) {
        Assert.assertEquals(byteSizeOutputs[i],
            rows.get(0).getValue(colNames[i]));
      }
    }
  }

  @Test
  public void testByteSizeFormats() throws Exception {
    String[] testUnits = new String[] { "B", "KB", "MB",
        "GB", "TB", "B" };
    String[] colNames = new String[] { "col1", "col2", "col3", "col4", "col5", "col6" };
    String[] byteSizeInputs = new String[] { "1KB", "40MB", "540000KB",
        "1TB", "200.1GB", "20.24KB" };
    String[] byteSizeOutputs = new String[] { "1024B", "40960KB", "527.344MB",
        "1024GB", "0.195TB", "20725B" };
    String[] directives = new String[testUnits.length];
    Row row = new Row();
    for (int i = 0; i < testUnits.length; i++) {
      directives[i] = String
          .format("%s :%s \"%s\"", ParseByteSize.NAME, colNames[i], testUnits[i]);
      row.add(colNames[i], byteSizeInputs[i]);
    }
    List<Row> rows = TestingRig.execute(directives, Collections.singletonList(row));

    Assert.assertEquals(1, rows.size());

    for (Row resultRow : rows) {
      for (int i = 0; i < testUnits.length; i++) {
        Assert.assertEquals(byteSizeOutputs[i],
            rows.get(0).getValue(colNames[i]));
      }
    }
  }

  @Test(expected = RecipeException.class)
  public void testInvalidLowerCaseUnit() throws Exception {
    String unit = "kb";
    String colName = "col1";
    String byteSize = "100KB";
    String[] directives = new String[] {
        String.format("%s :%s \"%s\"", ParseByteSize.NAME, colName, unit)
    };
    Row row1 = new Row();
    row1.add(colName, byteSize);
    TestingRig.execute(directives, Collections.singletonList(row1));
  }

  @Test(expected = RecipeException.class)
  public void testInvalidUnit() throws Exception {
    String unit = "a";
    String colName = "col1";
    String byteSize = "100B";
    String[] directives = new String[] {
        String.format("%s :%s \"%s\"", ParseByteSize.NAME, colName, unit)
    };
    Row row1 = new Row();
    row1.add(colName, byteSize);
    TestingRig.execute(directives, Collections.singletonList(row1));
  }

  @Test
  public void testByteSizeWithoutUnit() throws Exception {
    String colName = "col1";
    String byteSizeInput = "100";
    String byteSizeOutput = "100B";
    String[] directives = new String[] {
        String.format("%s :%s", ParseByteSize.NAME, colName)
    };
    Row row1 = new Row();
    row1.add(colName, byteSizeInput);
    List<Row> rows = TestingRig.execute(directives, Collections.singletonList(row1));

    Assert.assertEquals(1, rows.size());

    Assert.assertEquals(byteSizeOutput,
        rows.get(0).getValue(colName));
  }

  @Test(expected = NumberFormatException.class)
  public void testInvalidByteSize() throws Exception {
    String colName = "col1";
    String byteSize = "abc";
    String[] directives = new String[] {
        String.format("%s :%s", ParseByteSize.NAME, colName)
    };
    Row row1 = new Row();
    row1.add(colName, byteSize);
    TestingRig.execute(directives, Collections.singletonList(row1));
  }
}
