package m2_lab5;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TermRangeQuery;

public class LuceneRangeQueryDemo {

	public static String FILES_TO_INDEX_DIRECTORY = "./documents";
	public static String INDEX_DIRECTORY = "./index";
        
	static int hitsPerPage = 10;
	// Searcher searcher;
	public static final String FIELD_PATH = "path";
	public static final String FIELD_CONTENTS = "contents";
	public static final String FIELD_LAST_MODIFIED = "lastModified";
	public static final boolean INCLUSIVE = true;
	public static final boolean EXCLUSIVE = false;
	
	
	public static void main(String[] args) throws Exception {

                utils.MyUtils.delete(new File (INDEX_DIRECTORY));      
		createIndex();
                searchIndexWithRangeQuery(FIELD_CONTENTS, new BytesRef("2012".getBytes()), new BytesRef("2013".getBytes()), INCLUSIVE, INCLUSIVE);
                searchIndexWithRangeQuery(FIELD_CONTENTS, new BytesRef("2012".getBytes()), new BytesRef("2015".getBytes()), INCLUSIVE, INCLUSIVE);
                searchIndexWithRangeQuery(FIELD_CONTENTS, new BytesRef("sa".getBytes()), new BytesRef("st".getBytes()), INCLUSIVE, INCLUSIVE);
                searchIndexWithRangeQuery(FIELD_CONTENTS, new BytesRef("a".getBytes()), new BytesRef("ar".getBytes()), EXCLUSIVE, EXCLUSIVE);

	}

	public static void createIndex() throws CorruptIndexException, LockObtainFailedException, IOException 
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

		Directory     dir = FSDirectory.open(new File(INDEX_DIRECTORY));
		Analyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_4_10_0, analyzer);
		IndexWriter Writer = new IndexWriter(dir, iwc);
		File dir1 = new File(FILES_TO_INDEX_DIRECTORY);
		File[] files = dir1.listFiles();
		for (File file : files) {
			Document document = new Document();

			String path = file.getName();
			document.add(new Field(FIELD_PATH, path, Field.Store.YES, Field.Index.NOT_ANALYZED));

			Reader reader = new FileReader(file);
			document.add(new Field(FIELD_CONTENTS, reader));
			
			String lastModified = sdf.format(new Date(file.lastModified()));
			document.add(new Field(FIELD_LAST_MODIFIED, lastModified, Field.Store.YES, Field.Index.NOT_ANALYZED));

			Writer.addDocument(document);
		}
	        Writer.close();
	}

	public static void searchIndexWithRangeQuery(String whichField, BytesRef start, BytesRef end, boolean inclusive, boolean inclusive1) throws IOException, ParseException {
		System.out.println("\nSearching for range '" + start.utf8ToString() + " [TO] " + end.utf8ToString() + "' using RangeQuery");

		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(INDEX_DIRECTORY)));
		IndexSearcher searcher = new IndexSearcher(reader);
		Query query = new TermRangeQuery(whichField,start, end, inclusive, inclusive1);
		TopDocs results = searcher.search(query, 5 * hitsPerPage);
		ScoreDoc[] hits = results.scoreDocs;
		displayHits(hits);
	}

	public static void displayHits(ScoreDoc[] hits) throws CorruptIndexException, IOException {
		System.out.println("Number of hits: " + hits.length);

		Iterator<ScoreDoc> it = Arrays.asList(hits).iterator();
		while (it.hasNext()) {
			ScoreDoc hitt = it.next();
			IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(INDEX_DIRECTORY)));
			IndexSearcher searcher = new IndexSearcher(reader);
			int docId = hitt.doc;
			Document document = searcher.doc(docId);
			String path = document.get(FIELD_PATH);
			System.out.println("Hit: " + path); 

			     
		}
	}
}