/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.lucene.analysis.tokenattributes;

import org.apache.lucene.util.Attribute;

/**
 * Determines the position of this token relative to the previous Token in a TokenStream, used in
 * phrase searching.
 *
 * <p>The default value is one.
 *
 * <p>Some common uses for this are:
 *
 * <ul>
 *   <li>Set it to zero to put multiple terms in the same position. This is useful if, e.g., a word
 *       has multiple stems. Searches for phrases including either stem will match. In this case,
 *       all but (除了) the first stem's increment should be set to zero: the increment of the first
 *       instance should be one. Repeating a token with an increment of zero can also be used to
 *       boost the scores of matches on that token.
 *   <li>Set it to values greater than one to inhibit (禁止，抑制) exact phrase matches. If, for example, one
 *       does not want phrases to match across removed stop words, then one could build a stop word
 *       filter that removes stop words and also sets the increment to the number of stop words
 *       removed before each non-stop word. Then exact phrase queries will only match when the terms
 *       occur with no intervening(介于中间的) stop words.
 * </ul>
 * 确定此标记相对于 TokenStream 中前一个标记的位置，用于短语搜索。
 * 默认值为一。
 * 其一些常见用途是：
 * 将其设置为零可将多个项放在同一位置。例如，如果一个单词有多个词干，这很有用。搜索包含任一词干的短语将匹配。
 * 在这种情况下，除了第一个主干的增量之外的所有增量都应设置为零：第一个实例的增量应为 1。以零增量重复标记也可用于提高该标记的匹配分数。
 *
 * 将其设置为大于 1 的值以禁止精确的短语匹配。
 * 例如，如果不希望短语与已删除的停用词进行匹配，则可以构建一个停用词过滤器来删除停用词，
 * 并将增量设置为每个非停用词之前删除的停用词的数量。
 * 然后，仅当术语出现且中间没有停用词时，精确短语查询才会匹配。
 *
 * @see org.apache.lucene.index.PostingsEnum
 */
public interface PositionIncrementAttribute extends Attribute {
  /**
   * Set the position increment. The default value is one.
   *
   * @param positionIncrement the distance from the prior term 与前一项的举例，比如 a b c d，默认情况下
   *                          d与前一项的距离为1，但是如果b，c是停止词，则d与前一项(a)的距离是3
   * @throws IllegalArgumentException if <code>positionIncrement</code> is negative.
   * @see #getPositionIncrement()
   */
  public void setPositionIncrement(int positionIncrement);

  /**
   * Returns the position increment of this Token.
   *
   * @see #setPositionIncrement(int)
   */
  public int getPositionIncrement();
}
