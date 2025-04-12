/*
 * Copyright © 2017-2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.wrangler.api.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.cdap.wrangler.api.annotations.PublicEvolving;

/**
 * The ByteSize class wraps the ByteSize type {@code Long} in a object.
 * An object of type {@code Long} contains the value in ByteSize type
 * as well as the type of the token this class represents.
 *
 * <p>
 * In addition, this class provides two methods one to extract the
 * value held by this wrapper object, and the second for extracting the
 * type of the token.
 * </p>
 *
 * @see TimeDurationList
 * @see ColumnName
 * @see ColumnNameList
 * @see DirectiveName
 * @see Numeric
 * @see NumericList
 * @see Properties
 * @see Ranges
 * @see Expression
 * @see Text
 * @see TextList
 * @see Identifier
 */
@PublicEvolving
public class ByteSize implements Token {
  /**
   * The {@code Long} object that represents the value held by the token.
   */
  private Long value;

  /**
   * The {@code enum} object that represents the various units processable by the
   * token.
   */
  public enum Units {
    TB, GB, MB, KB, B
  }

  /**
   * Allocates a {@code Long} object representing the
   * {@code size} argument value.
   *
   * @param byteSize the value of the {@code String}.
   */
  public ByteSize(String byteSize) {
    Double value = Double.parseDouble(byteSize.replaceAll("[^\\.0-9]", "")); // Extract digits
    String unit = byteSize.replaceAll("[0-9]", "").replaceAll("\\.", "").trim(); // Extract units

    switch (unit) {
      case "TB":
        this.value = (long) (value * 1024 * 1024 * 1024 * 1024);
        break;
      case "GB":
        this.value = (long) (value * 1024 * 1024 * 1024);
        break;
      case "MB":
        this.value = (long) (value * 1024 * 1024);
        break;
      case "KB":
        this.value = (long) (value * 1024);
        break;
      case "B":
      default:
        this.value = value.longValue();
        break;
    }
  }

  public ByteSize(long byteSize) {
    this.value = byteSize;
  }

  public Long getBytes() {
    return value;
  }

  public Double getKiloBytes() {
    return (double) value / 1024;
  }

  public Double getMegaBytes() {
    return (double) value / 1024 / 1024;
  }

  public Double getGigaBytes() {
    return (double) value / 1024 / 1024 / 1024;
  }

  public Double getTeraBytes() {
    return (double) value / 1024 / 1024 / 1024 / 1024;
  }

  /**
   * Returns the value of this {@code Long} object as a long
   * primitive.
   *
   * @return the primitive {@code long} value of this object.
   */
  @Override
  public Long value() {
    return getBytes();
  }

  public String getBytesAsString() {
    return String.format("%s%s", stringFormatDouble(getBytes()), ByteSize.Units.B.toString());
  }

  public String getKiloBytesAsString() {
    return String.format("%s%s", stringFormatDouble(getKiloBytes()), ByteSize.Units.KB.toString());
  }

  public String getMegaBytesAsString() {
    return String.format("%s%s", stringFormatDouble(getMegaBytes()), ByteSize.Units.MB.toString());
  }

  public String getGigaBytesAsString() {
    return String.format("%s%s", stringFormatDouble(getGigaBytes()), ByteSize.Units.GB.toString());
  }

  public String getTeraBytesAsString() {
    return String.format("%s%s", stringFormatDouble(getTeraBytes()), ByteSize.Units.TB.toString());
  }

  @Override
  public String toString() {
    return getBytesAsString();
  }

  /**
   * Returns the type of this {@code Long} object as a {@code TokenType}
   * enum.
   *
   * @return the enumerated {@code TokenType} of this object.
   */
  @Override
  public TokenType type() {
    return TokenType.BYTESIZE;
  }

  /**
   * Returns the members of this {@code Long} object as a {@code JsonElement}.
   *
   * @return Json representation of this {@code Long} object as
   *         {@code JsonElement}
   */
  @Override
  public JsonElement toJson() {
    JsonObject object = new JsonObject();
    object.addProperty("type", TokenType.BYTESIZE.name());
    object.addProperty("value", value);
    return object;
  }

  protected static String stringFormatDouble(double number) {
    if (number == (long) number) {
      return String.format("%d", (long) number);
    } else {
      return String.format("%.3f", number); // Format with 2 decimal places
    }
  }
}
