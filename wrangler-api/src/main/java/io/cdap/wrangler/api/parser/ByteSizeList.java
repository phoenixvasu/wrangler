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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.cdap.wrangler.api.annotations.PublicEvolving;

import java.util.ArrayList;
import java.util.List;

/**
 * The ByteSize List class wraps the list of ByteSize type {@code Long} in a
 * object.
 * An object of type {@code ByteSizeList} contains the value as a {@code List}
 * of
 * ByteSize type
 * {@code Long}. Along with the list of {@code Long} type, this object
 * also contains
 * the value that represents the type of this object as {@code TokenType}.
 *
 * <p>
 * In addition, this class provides two methods one to extract the
 * value held by this wrapper object, and the second for extracting the
 * type of the token.
 * </p>
 *
 * @see TimeDuration
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
 */
@PublicEvolving
public class ByteSizeList implements Token {
  /**
   * The {@code List<Long>} object that represents the value held by the token.
   */
  private List<ByteSize> values;

  /**
   * Allocates a {@code List<ByteSize>} object representing the {@code value}
   * argument.
   * 
   * @param values
   */
  public ByteSizeList(List<String> values) {
    this.values = new ArrayList<>();
    for (String value : values) {
      this.values.add(new ByteSize(value));
    }
  }

  /**
   * Returns the value of this {@code ByteSizeList} object as a list of ByteSize.
   *
   * @return the list of {@code ByteSize} {@code values} of this object.
   */
  @Override
  public List<ByteSize> value() {
    return values;
  }

  /**
   * Returns the type of this {@code ByteSizeList} object as a {@code TokenType}
   * enum.
   *
   * @return the enumerated {@code TokenType} of this object.
   */
  @Override
  public TokenType type() {
    return TokenType.BYTESIZE_LIST;
  }

  /**
   * Returns the members of this {@code ByteSizeList} object as a
   * {@code JsonElement}.
   *
   * @return Json representation of this {@code ByteSizeList} object as
   *         {@code JsonElement}
   */
  @Override
  public JsonElement toJson() {
    JsonObject object = new JsonObject();
    object.addProperty("type", TokenType.BYTESIZE_LIST.name());
    JsonArray array = new JsonArray();
    for (ByteSize value : values) {
      array.add(new JsonPrimitive(value.value()));
    }
    object.add("value", array);
    return object;
  }
}
