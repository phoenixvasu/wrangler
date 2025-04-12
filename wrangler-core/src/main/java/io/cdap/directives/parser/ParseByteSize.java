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

package io.cdap.directives.parser;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;

import edu.emory.mathcs.backport.java.util.Arrays;
import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.ErrorRowException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Optional;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.annotations.Categories;
import io.cdap.wrangler.api.lineage.Lineage;
import io.cdap.wrangler.api.lineage.Many;
import io.cdap.wrangler.api.lineage.Mutation;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.Text;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;

import java.text.ParseException;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * A Executor to parse date.
 */
@Plugin(type = Directive.TYPE)
@Name("parse-as-byte-size")
@Categories(categories = { "parser", "byte" })
@Description("Parses column values as byteSizes and " +
    "automatically convert it to required units.")
public class ParseByteSize implements Directive, Lineage {
  public static final String NAME = "parse-as-byte-size";
  private String column;
  private ByteSize.Units unit;

  @Override
  public UsageDefinition define() {
    UsageDefinition.Builder builder = UsageDefinition.builder(NAME);
    builder.define("column", TokenType.COLUMN_NAME);
    builder.define("unit", TokenType.TEXT, Optional.TRUE);
    return builder.build();
  }

  @Override
  public void initialize(Arguments args) throws DirectiveParseException {
    this.column = ((ColumnName) args.value("column")).value();
    if (args.contains("unit")) {
      Text unit = ((Text) args.value("unit"));
      try {
        this.unit = ByteSize.Units.valueOf(unit.value());
      } catch (IllegalArgumentException e) {
        throw new DirectiveParseException(NAME, String.format(
            "ByteSize unit can only be one of the following %s. '%s' is invalid.",
            Arrays.toString(ByteSize.Units.values()),
            unit.value()));
      }
    } else {
      this.unit = ByteSize.Units.B;
    }
  }

  @Override
  public void destroy() {
    // no-op
  }

  @Override
  public List<Row> execute(List<Row> rows, ExecutorContext context)
      throws DirectiveExecutionException, ErrorRowException {
    for (Row row : rows) {
      int idx = row.find(column);
      if (idx != -1) {
        Object object = row.getValue(idx);
        // If the data in the cell is null or is already of byteSize format, then
        // continue to next row.
        if (object == null || object instanceof ByteSize) {
          continue;
        }
        if (object instanceof String) {
          ByteSize byteSize = new ByteSize((String) object);
          switch (unit) {
            case TB:
              row.setValue(idx, byteSize.getTeraBytesAsString());
              break;
            case GB:
              row.setValue(idx, byteSize.getGigaBytesAsString());
              break;
            case MB:
              row.setValue(idx, byteSize.getMegaBytesAsString());
              break;
            case KB:
              row.setValue(idx, byteSize.getKiloBytesAsString());
              break;
            case B:
            default:
              row.setValue(idx, byteSize.getBytesAsString());
              break;
          }
        } else {
          throw new ErrorRowException(
              NAME, String.format("Column '%s' is of invalid type '%s'. It should be of type 'String'.",
                  column, object.getClass().getSimpleName()),
              1);
        }
      }
    }
    return rows;
  }

  @Override
  public Mutation lineage() {
    return Mutation.builder()
        .readable("Parsed column '%s' as byteSize", column)
        .all(Many.columns(column), Many.columns(column))
        .build();
  }
}
