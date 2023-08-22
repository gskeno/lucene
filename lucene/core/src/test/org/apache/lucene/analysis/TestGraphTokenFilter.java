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

package org.apache.lucene.analysis;

import java.io.IOException;
import java.io.StringReader;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.tests.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.tests.analysis.CannedTokenStream;
import org.apache.lucene.tests.analysis.Token;

public class TestGraphTokenFilter extends BaseTokenStreamTestCase {

  static class TestFilter extends GraphTokenFilter {

    public TestFilter(TokenStream input) {
      super(input);
    }

    @Override
    public final boolean incrementToken() throws IOException {
      return incrementBaseToken();
    }
  }

  public void testGraphTokenStream() throws IOException {

    TestGraphTokenizers.GraphTokenizer tok = new TestGraphTokenizers.GraphTokenizer();
    GraphTokenFilter graph = new TestFilter(tok);

    CharTermAttribute termAtt = graph.addAttribute(CharTermAttribute.class);
    PositionIncrementAttribute posIncAtt = graph.addAttribute(PositionIncrementAttribute.class);

    tok.setReader(new StringReader("a b/c d e/f:3 g/h i j k"));
    tok.reset();

    assertFalse(graph.incrementGraph()); // 根节点还未设置，无法获得下一个路由
    assertEquals(0, graph.cachedTokenCount());

    assertTrue(graph.incrementBaseToken()); // 路由根节点向前移动一步，走到a
    assertEquals("a", termAtt.toString());
    assertEquals(1, posIncAtt.getPositionIncrement()); //字母a占1个位置增量
    assertTrue(graph.incrementGraphToken()); // 当前路由上移动到下一个token处，即b位置处
    assertEquals("b", termAtt.toString());
    assertEquals(1, posIncAtt.getPositionIncrement()); // 字母b占1个位置增量
    // 内部会遍历c和d，因为c与b占用的位置一样，c的positionIncrement为0，所以这里会最终走到d。
    // b和c的position是一致的，posLen也是一样的，都是1，但是b拥有位置增量，c没有，因为c与b在同一位置处，
    // 若c也位置增量，则b到d，跨度是2，不符合预期
    assertTrue(graph.incrementGraphToken());

    assertEquals("d", termAtt.toString());
    assertEquals(4, graph.cachedTokenCount()); // 已经遍历了a,b,c,d

    assertTrue(graph.incrementGraph());  // 新增一个图表(走下一个路由), abd路由已经走过，这次走a,c,d路由；另外，内部会遍历到字符e
    assertEquals(5, graph.cachedTokenCount());
    assertEquals("a", termAtt.toString());
    assertTrue(graph.incrementGraphToken()); // graphPos++
    assertEquals("c", termAtt.toString());
    assertTrue(graph.incrementGraphToken());
    assertEquals("d", termAtt.toString());

    assertFalse(graph.incrementGraph()); // a->d的两条路由已经遍历，没有新的路由了
    assertEquals(5, graph.cachedTokenCount());

    // tok.setReader(new StringReader("a b/c d e/f:3 g/h i j k"));
    assertTrue(graph.incrementBaseToken()); // 路由根节点向前移动一步，走到b
    assertEquals("b", termAtt.toString());
    assertTrue(graph.incrementGraphToken());
    assertEquals("d", termAtt.toString());
    assertTrue(graph.incrementGraphToken());
    assertEquals("e", termAtt.toString());
    assertTrue(graph.incrementGraph()); // 寻找b--->e的下一个路由，即b,d,f，因为e,f所处位置一致
    assertEquals("b", termAtt.toString());
    assertTrue(graph.incrementGraphToken());
    assertEquals("d", termAtt.toString());
    assertTrue(graph.incrementGraphToken());
    assertEquals("f", termAtt.toString());
    assertFalse(graph.incrementGraph()); // b--->f，不再有其他路由了
    assertEquals(6, graph.cachedTokenCount());

    assertTrue(graph.incrementBaseToken()); // 路由根节点向前移动一步，走到c
    assertEquals("c", termAtt.toString());
    assertEquals(0, posIncAtt.getPositionIncrement()); // c与b占用同一个位置，b在前，c在后，c不拥有位置增量
    assertTrue(graph.incrementGraphToken());
    assertEquals("d", termAtt.toString());
    assertFalse(graph.incrementGraph()); // c--->d只有一条路由
    assertEquals(6, graph.cachedTokenCount());

    // tok.setReader(new StringReader("a b/c d e/f:3 g/h i j k"));
    assertTrue(graph.incrementBaseToken()); // 路由根节点向前移动一步，走到d
    assertEquals("d", termAtt.toString());
    assertTrue(graph.incrementGraphToken());
    assertEquals("e", termAtt.toString());
    assertTrue(graph.incrementGraphToken());
    assertEquals("g", termAtt.toString());
    assertTrue(graph.incrementGraph()); // 寻找d--->g的另一条路由，即d,e,h
    assertEquals("d", termAtt.toString());
    assertTrue(graph.incrementGraphToken());
    assertEquals("e", termAtt.toString());
    assertTrue(graph.incrementGraphToken());
    assertEquals("h", termAtt.toString());
    assertTrue(graph.incrementGraph()); // 寻找下一条路由，包含起点在内的3个字符，即d,f,j
    assertEquals("d", termAtt.toString());
    assertTrue(graph.incrementGraphToken());
    assertEquals("f", termAtt.toString());
    assertTrue(graph.incrementGraphToken());
    assertEquals("j", termAtt.toString());
    assertFalse(graph.incrementGraph());
    assertEquals(8, graph.cachedTokenCount());

    // tok.setReader(new StringReader("a b/c d e/f:3 g/h i j k"));

    assertTrue(graph.incrementBaseToken()); // 路由根节点向前移动一步，走到e
    assertEquals("e", termAtt.toString());
    assertTrue(graph.incrementGraphToken());
    assertEquals("g", termAtt.toString());
    assertTrue(graph.incrementGraphToken());
    assertEquals("i", termAtt.toString());
    assertTrue(graph.incrementGraphToken());
    assertEquals("j", termAtt.toString());
    assertTrue(graph.incrementGraph()); // 寻找下一个路由
    assertEquals("e", termAtt.toString());
    assertTrue(graph.incrementGraphToken());
    assertEquals("h", termAtt.toString());
    assertFalse(graph.incrementGraph()); // e,h 已走过，没有e开始的其他路径了
    assertEquals(8, graph.cachedTokenCount());

    assertTrue(graph.incrementBaseToken()); // 路由根节点向前移动一步，走到f
    assertEquals("f", termAtt.toString());
    assertTrue(graph.incrementGraphToken());
    assertEquals("j", termAtt.toString());
    assertTrue(graph.incrementGraphToken());
    assertEquals("k", termAtt.toString());
    assertFalse(graph.incrementGraphToken()); // k后没有其他字符了
    assertFalse(graph.incrementGraph()); // f到k也没有下一个路由了
    assertEquals(8, graph.cachedTokenCount());

    assertTrue(graph.incrementBaseToken()); // 路由根节点向前移动一步，走到g
    assertEquals("g", termAtt.toString());
    assertTrue(graph.incrementGraphToken());
    assertEquals("i", termAtt.toString());
    assertFalse(graph.incrementGraph()); // g到i，也没有下一个路由了
    assertEquals(8, graph.cachedTokenCount());

    assertTrue(graph.incrementBaseToken());  // 路由根节点向前移动一步，走到h
    assertEquals("h", termAtt.toString());
    assertFalse(graph.incrementGraph()); // h到h，也没有下一个路由了
    assertEquals(8, graph.cachedTokenCount());

    // tok.setReader(new StringReader("a b/c d e/f:3 g/h i j k"));

    assertTrue(graph.incrementBaseToken()); // 路由根节点向前移动一步，走到i
    assertTrue(graph.incrementBaseToken()); // 路由根节点向前移动一步，走到j
    assertTrue(graph.incrementBaseToken()); // 路由根节点向前移动一步，走到k
    assertEquals("k", termAtt.toString());
    assertFalse(graph.incrementGraphToken()); // k后没有字符了
    assertEquals(0, graph.getTrailingPositions());
    assertFalse(graph.incrementGraph()); // k-k，没有下一个路由了
    assertFalse(graph.incrementBaseToken()); // k后没有字符了，返回false
    assertEquals(8, graph.cachedTokenCount());
  }

  public void testTrailingPositions() throws IOException {

    // a/b:2 c _
    CannedTokenStream cts =
        new CannedTokenStream(
            1, 5, new Token("a", 0, 1), new Token("b", 0, 0, 1, 2), new Token("c", 1, 2, 3));

    GraphTokenFilter gts = new TestFilter(cts);
    assertFalse(gts.incrementGraph()); // 未设置根节点，无路由
    assertTrue(gts.incrementBaseToken()); // 根节点设置为a
    assertTrue(gts.incrementGraphToken()); // 走到 c
    assertFalse(gts.incrementGraphToken()); // c后无节点，无路可走
    assertEquals(1, gts.getTrailingPositions());


    assertFalse(gts.incrementGraph()); // 从a开始没有其他路由
    assertTrue(gts.incrementBaseToken()); // 根节点设置为b
    assertFalse(gts.incrementGraphToken()); // b后无节点，因为b占用宽度为2，c不在其路径上
    assertEquals(1, gts.getTrailingPositions());
    assertFalse(gts.incrementGraph());
  }

  public void testMaximumGraphCacheSize() throws IOException {

    Token[] tokens = new Token[GraphTokenFilter.MAX_TOKEN_CACHE_SIZE + 5];
    for (int i = 0; i < GraphTokenFilter.MAX_TOKEN_CACHE_SIZE + 5; i++) {
      tokens[i] = new Token("a", 1, i * 2, i * 2 + 1);
    }

    GraphTokenFilter gts = new TestFilter(new CannedTokenStream(tokens));
    Exception e =
        expectThrows(
            IllegalStateException.class,
            () -> {
              gts.reset();
              gts.incrementBaseToken();
              while (true) {
                gts.incrementGraphToken();
              }
            });
    assertEquals("Too many cached tokens (> 100)", e.getMessage());

    gts.reset();
    // after reset, the cache should be cleared and so we can read ahead once more
    gts.incrementBaseToken();
    gts.incrementGraphToken();
  }

  public void testGraphPathCountLimits() {

    Token[] tokens = new Token[50];
    tokens[0] = new Token("term", 1, 0, 1);
    tokens[1] = new Token("term1", 1, 2, 3);
    for (int i = 2; i < 50; i++) {
      tokens[i] = new Token("term" + i, i % 2, 2, 3);
    }

    Exception e =
        expectThrows(
            IllegalStateException.class,
            () -> {
              GraphTokenFilter graph = new TestFilter(new CannedTokenStream(tokens));
              graph.reset();
              graph.incrementBaseToken();
              for (int i = 0; i < 10; i++) {
                graph.incrementGraphToken();
              }
              while (graph.incrementGraph()) {
                for (int i = 0; i < 10; i++) {
                  graph.incrementGraphToken();
                }
              }
            });
    assertEquals("Too many graph paths (> 1000)", e.getMessage());
  }
}
