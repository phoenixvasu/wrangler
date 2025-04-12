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

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Optional;
import io.cdap.wrangler.api.Pair;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.TransientVariableScope;
import io.cdap.wrangler.api.annotations.Categories;
import io.cdap.wrangler.api.lineage.Lineage;
import io.cdap.wrangler.api.lineage.Mutation;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.Text;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A directive for managing date formats.
 */
@Plugin(type = Directive.TYPE)
@Name("aggregate-byte-time")
@Categories(categories = { "aggregate", "byte", "time" })
@Description("Aggregates byte sizes and time durations from specified columns.")
public class AggregateByteAndTime implements Directive, Lineage {
  public static final String NAME = "aggregate-byte-time";
  private String sizeColumnName;
  private String timeColumnName;
  private ByteSize.Units outputSizeUnit;
  private TimeDuration.Units outputTimeUnit;
  private AggregationEnum aggregationType;

  /**
   * The {@code enum} object that represents the various aggregate types
   * processable by the directive.
   */
  public enum AggregationEnum {
    total, average
  }

  private static final String TOTAL_SIZE_KEY_NAME = "total_size";
  private static final String TOTAL_TIME_KEY_NAME = "total_time";
  private static final String TOTAL_ROWS_KEY_NAME = "total_rows";

  public static final String RESULT_SIZE_COLUMN_NAME = "result_size";
  public static final String RESULT_TIME_COLUMN_NAME = "result_time";

  @Override
  public UsageDefinition define() {
    UsageDefinition.Builder builder = UsageDefinition.builder(NAME);
    builder.define("sizeColumn", TokenType.COLUMN_NAME);
    builder.define("timeColumn", TokenType.COLUMN_NAME);
    builder.define("aggregationType", TokenType.TEXT, Optional.TRUE);
    builder.define("outputSizeUnit", TokenType.TEXT, Optional.TRUE);
    builder.define("outputTimeUnit", TokenType.TEXT, Optional.TRUE);
    return builder.build();
  }

  @Override
  public void initialize(Arguments args) throws DirectiveParseException {
    this.sizeColumnName = ((ColumnName) args.value("sizeColumn")).value();
    this.timeColumnName = ((ColumnName) args.value("timeColumn")).value();

    if (args.contains("outputSizeUnit")) {
      Text outputSizeUnitInput = ((Text) args.value("outputSizeUnit"));
      try {
        this.outputSizeUnit = ByteSize.Units.valueOf(outputSizeUnitInput.value());
      } catch (IllegalArgumentException e) {
        throw new DirectiveParseException(NAME, String.format(
            "outputSizeUnit can only be one of the following %s. '%s' is invalid.",
            Arrays.toString(ByteSize.Units.values()),
            outputSizeUnitInput.value()));
      }
    } else {
      this.outputSizeUnit = ByteSize.Units.B;
    }

    if (args.contains("outputTimeUnit")) {
      Text outputTimeUnitInput = ((Text) args.value("outputTimeUnit"));
      try {
        this.outputTimeUnit = TimeDuration.Units.valueOf(outputTimeUnitInput.value());
      } catch (IllegalArgumentException e) {
        throw new DirectiveParseException(NAME, String.format(
            "outputTimeUnit can only be one of the following %s. '%s' is invalid.",
            Arrays.toString(TimeDuration.Units.values()),
            outputTimeUnitInput.value()));
      }
    } else {
      this.outputTimeUnit = TimeDuration.Units.ms;
    }

    if (args.contains("aggregationType")) {
      Text aggregationTypeInput = ((Text) args.value("aggregationType"));
      try {
        this.aggregationType = AggregationEnum.valueOf(aggregationTypeInput.value());
      } catch (IllegalArgumentException e) {
        throw new DirectiveParseException(NAME, String.format(
            "aggregationType can only be one of the following %s. '%s' is invalid.",
            Arrays.toString(AggregationEnum.values()),
            aggregationTypeInput.value()));
      }
    } else {
      this.aggregationType = AggregationEnum.total;
    }
  }

  @Override
  public void destroy() {
    // no-op
  }

  @Override
  public List<Row> execute(List<Row> rows, ExecutorContext context) throws DirectiveExecutionException {
    for (Row row : rows) {
      Row dt = new Row(row);
      int sizeColumnNameIdx = dt.find(sizeColumnName);
      int timeColumnNameIdx = dt.find(timeColumnName);

      context.getTransientStore().increment(TransientVariableScope.GLOBAL, TOTAL_ROWS_KEY_NAME, 1);

      if (sizeColumnNameIdx == -1) {
        throw new DirectiveExecutionException(NAME, String.format("Column '%s' does not exist.", sizeColumnName));
      }
      if (timeColumnNameIdx == -1) {
        throw new DirectiveExecutionException(NAME, String.format("Column '%s' does not exist.", timeColumnName));
      }

      Object sizeValue = row.getValue(sizeColumnNameIdx);
      Object timeValue = row.getValue(timeColumnNameIdx);

      if (sizeValue != null) {
        // Assuming the column contains strings parsed by ByteSize in a previous step
        ByteSize byteSize;
        if (sizeValue instanceof ByteSize) {
          byteSize = ((ByteSize) sizeValue);
        } else if (sizeValue instanceof String) {
          byteSize = new ByteSize((String) sizeValue);
        } else {
          throw new DirectiveExecutionException(
              NAME, String.format("Column '%s' has invalid type '%s'.",
                  sizeColumnName, sizeValue.getClass().getSimpleName()));
        }

        context.getTransientStore().increment(TransientVariableScope.GLOBAL, TOTAL_SIZE_KEY_NAME, byteSize.value());
      }

      if (timeValue != null) {
        // Assuming the column contains strings parsed by TimeDuration previously
        TimeDuration time;
        if (timeValue instanceof TimeDuration) {
          time = ((TimeDuration) timeValue);
        } else if (timeValue instanceof String) {
          time = new TimeDuration((String) timeValue);
        } else {
          throw new DirectiveExecutionException(
              NAME, String.format("Column '%s' has invalid type '%s'.",
                  timeColumnName, timeValue.getClass().getSimpleName()));
        }

        context.getTransientStore().increment(TransientVariableScope.GLOBAL, TOTAL_TIME_KEY_NAME, time.value());
      }
    }

    if ((Integer) context.getTransientStore().get("rowsLeft") == 0) {
      long resultByteSize = context.getTransientStore().get(TOTAL_SIZE_KEY_NAME);
      long resultTime = context.getTransientStore().get(TOTAL_TIME_KEY_NAME);
      long rowsSize = context.getTransientStore().get(TOTAL_ROWS_KEY_NAME);

      if (aggregationType == AggregationEnum.average) {
        resultByteSize /= rowsSize;
        resultTime /= rowsSize;
      }

      Row result = new Row();
      switch (outputSizeUnit) {
        case TB:
          result.add(RESULT_SIZE_COLUMN_NAME, new ByteSize(resultByteSize).getTeraBytesAsString());
          break;
        case GB:
          result.add(RESULT_SIZE_COLUMN_NAME, new ByteSize(resultByteSize).getGigaBytesAsString());
          break;
        case MB:
          result.add(RESULT_SIZE_COLUMN_NAME, new ByteSize(resultByteSize).getMegaBytesAsString());
          break;
        case KB:
          result.add(RESULT_SIZE_COLUMN_NAME, new ByteSize(resultByteSize).getKiloBytesAsString());
          break;
        case B:
        default:
          result.add(RESULT_SIZE_COLUMN_NAME, new ByteSize(resultByteSize).getBytesAsString());
          break;
      }
      switch (outputTimeUnit) {
        case d:
          result.add(RESULT_TIME_COLUMN_NAME, new TimeDuration(resultTime).getTimeInDaysAsString());
          break;
        case h:
          result.add(RESULT_TIME_COLUMN_NAME, new TimeDuration(resultTime).getTimeInHoursAsString());
          break;
        case m:
          result.add(RESULT_TIME_COLUMN_NAME, new TimeDuration(resultTime).getTimeInMinutesAsString());
          break;
        case s:
          result.add(RESULT_TIME_COLUMN_NAME, new TimeDuration(resultTime).getTimeInSecondsAsString());
          break;
        case ms:
        default:
          result.add(RESULT_TIME_COLUMN_NAME, new TimeDuration(resultTime).getTimeInMilisecondsAsString());
          break;
      }
      return Collections.singletonList(result);
    }
    return Collections.emptyList();
  }

  @Override
  public Mutation lineage() {
    return Mutation.builder()
        .readable("Formatted ByteSize in column '%s' and TimeInterval in column '%s'", sizeColumnName, timeColumnName)
        .relation(sizeColumnName, sizeColumnName)
        .relation(timeColumnName, timeColumnName)
        .build();
  }
}
