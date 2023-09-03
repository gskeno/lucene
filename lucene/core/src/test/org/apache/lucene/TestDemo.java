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
package org.apache.lucene;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.tests.util.LuceneTestCase;
import org.apache.lucene.util.IOUtils;

/**
 * A very simple demo used in the API documentation (src/java/overview.html).
 *
 * <p>Please try to keep src/java/overview.html up-to-date when making changes to this class.
 */
public class TestDemo extends LuceneTestCase {

  public void testPath(){
    URL resource = this.getClass().getResource("/");
    String indexDir = resource.getPath() + "testIndex";
    // /Users/ruchen/IdeaProjects/lucene/lucene/core/build/idea/classes/test/testIndex
    System.out.println(indexDir);
    Path indexPath = Path.of(indexDir);
    System.out.println(indexPath.toString());
  }

  // 引入了test-framework, 也就引入了一些codec实现类，会导致LukeMain无法识别
  public void testDemo() throws IOException, URISyntaxException {
    String longTerm =
        "longtermlongtermlongtermlongtermlongtermlongtermlongtermlong"
            + "termlongtermlongtermlongtermlongtermlongtermlongtermlongterm"
            + "longtermlongtermlongterm";
    String text = "This is the text to be indexed. " + longTerm;
    URL resource = this.getClass().getResource("/");
    // 在指定位置，创建临时目录，方便查看分析文件；且测试方法末尾不删除临时文件
    Path indexPath = Files.createTempDirectory(Paths.get(resource.toURI()), "tempIndex");
    try (Directory dir = FSDirectory.open(indexPath)) {
      Analyzer analyzer = new StandardAnalyzer();
      try (IndexWriter iw = new IndexWriter(dir, new IndexWriterConfig(analyzer))) {
        Document doc = new Document();
        doc.add(newTextField("fieldname", text, Field.Store.YES));
        iw.addDocument(doc);
      }

      // Now search the index.
      try (IndexReader reader = DirectoryReader.open(dir)) {
        IndexSearcher searcher = newSearcher(reader);

        assertEquals(1, searcher.count(new TermQuery(new Term("fieldname", longTerm))));

        Query query = new TermQuery(new Term("fieldname", "text"));
        TopDocs hits = searcher.search(query, 1);
        assertEquals(1, hits.totalHits.value);

        // Iterate through the results.
        StoredFields storedFields = searcher.storedFields();
        for (int i = 0; i < hits.scoreDocs.length; i++) {
          Document hitDoc = storedFields.document(hits.scoreDocs[i].doc);
          assertEquals(text, hitDoc.get("fieldname"));
        }

        // Test simple phrase query.
        PhraseQuery phraseQuery = new PhraseQuery("fieldname", "to", "be");
        assertEquals(1, searcher.count(phraseQuery));
      }
    }

    // IOUtils.rm(indexPath);
  }
}
