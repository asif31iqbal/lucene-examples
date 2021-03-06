package m2_lab8;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.facet.FacetField;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.Version;
import org.json.JSONArray;
import org.json.JSONObject;


class FacetLuceneIndexer {
	public static void main(String args[]) throws Exception {
		if (args.length != 3) {
			System.err.println("Parameters: [index directory] [taxonomy directory] [json file]");
			System.exit(1);
		}
		
		String indexDirectory = args[0];
		String taxonomyDirectory = args[1];
		String jsonFileName = args[2];
		
		IndexWriterConfig writerConfig = new IndexWriterConfig(new WhitespaceAnalyzer());
		writerConfig.setOpenMode(OpenMode.CREATE);
		IndexWriter indexWriter = new IndexWriter(FSDirectory.open(Paths.get(indexDirectory)), writerConfig);

		TaxonomyWriter taxonomyWriter = new DirectoryTaxonomyWriter(new MMapDirectory(Paths.get(taxonomyDirectory)), OpenMode.CREATE);
		//CategoryDocumentBuilder categoryDocumentBuilder = new CategoryDocumentBuilder(taxonomyWriter, new DefaultFacetIndexingParams());
		FacetsConfig config = new FacetsConfig();
		config.setIndexFieldName("Category", "book_category");
		config.setIndexFieldName("Author", "authors");
		config.setHierarchical("Category", true);
		config.setMultiValued("Author", true);

		String content = IOUtils.toString(new FileInputStream(jsonFileName));
		JSONArray bookArray = new JSONArray(content);
		
		Field idField = new IntPoint("id", 0);
		StoredField s = new StoredField("id_stored", 0);
		Field titleField = new TextField("title", "", Store.YES);
		Field authorsField = new TextField("authors", "", Store.YES);
		Field bookCategoryField = new TextField("book_category", "", Store.YES);

		for(int i = 0 ; i < bookArray.length() ; i++) {
			Document document = new Document();

			JSONObject book = bookArray.getJSONObject(i);
			int id = book.getInt("id");
			String title = book.getString("title");
			String bookCategory = book.getString("book_category");
			String[] catgoryParts = bookCategory.split("/");
			List<String> categoryPartList = new ArrayList<String>(); 
			List<String> authorList = new ArrayList<String>(); 
			for(String p : catgoryParts) {
				if(!p.equals("")) {
					categoryPartList.add(p);
				}
			}
			
			String authorsString = "";
			JSONArray authors = book.getJSONArray("authors");
			for(int j = 0 ; j < authors.length() ; j++) {
				String author = authors.getString(j);
				if (j > 0) {
					authorsString += ", ";
				}
				authorsString += author;
				authorList.add(author);
			}
			
			idField.setIntValue(id);
			titleField.setStringValue(title);
			authorsField.setStringValue(authorsString);
			bookCategoryField.setStringValue(bookCategory);
			
			document.add(idField);
			document.add(titleField);
			document.add(authorsField);
			document.add(bookCategoryField);
			document.add(new FacetField("Category", categoryPartList.toArray(new String[categoryPartList.size()])));
			for(String at : authorList) {
				document.add(new FacetField("Author", at));
			}
			
			indexWriter.addDocument(config.build(taxonomyWriter, document));
			
			System.out.printf("Book: id=%d, title=%s, book_category=%s, authors=%s\n",
				id, title, bookCategory, authors);
		}
		taxonomyWriter.commit();
		taxonomyWriter.close();
		
		indexWriter.commit();
		indexWriter.close();
	}
}