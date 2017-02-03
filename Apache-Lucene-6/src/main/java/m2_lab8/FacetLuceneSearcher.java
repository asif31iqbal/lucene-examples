package m2_lab8;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.taxonomy.FastTaxonomyFacetCounts;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.complexPhrase.ComplexPhraseQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiCollector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;


class FacetLuceneSearcher {
	public static void main(String args[]) throws Exception {
		if (args.length != 3) {
			System.err.println("Parameters: [index directory] [taxonomy directory] [query]");
			System.exit(1);
		}
		
		String indexDirectory = args[0];
		String taxonomyDirectory = args[1];
		String query = args[2];
		
		FacetsConfig config = new FacetsConfig();
		config.setIndexFieldName("Category", "book_category");
		config.setIndexFieldName("Author", "authors");
		config.setHierarchical("Category", true);
		config.setMultiValued("Author", true);
		
		IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(indexDirectory)));
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);

		TaxonomyReader taxonomyReader = new DirectoryTaxonomyReader(FSDirectory.open(Paths.get(taxonomyDirectory)));
		
		ComplexPhraseQueryParser queryParser = new ComplexPhraseQueryParser("title", new StandardAnalyzer());
		Query luceneQuery = queryParser.parse(query);

		// Collectors to get top results and facets
		TopScoreDocCollector topScoreDocCollector = TopScoreDocCollector.create(10);
		FacetsCollector facetsCollector = new FacetsCollector();
		FacetsCollector.search(indexSearcher, luceneQuery, 10, facetsCollector);
		
		List<FacetResult> results = new ArrayList<>();
		 
		Facets author = new FastTaxonomyFacetCounts("authors", taxonomyReader, config, facetsCollector);
		results.add(author.getTopChildren(10, "Author"));
		
		Facets category = new FastTaxonomyFacetCounts("book_category", taxonomyReader, config, facetsCollector);
		results.add(category.getTopChildren(10, "Category"));
		
		indexSearcher.search(luceneQuery, MultiCollector.wrap(topScoreDocCollector, facetsCollector));
		System.out.println("Found:");
		
		for(ScoreDoc scoreDoc: topScoreDocCollector.topDocs().scoreDocs) {
			Document document = indexReader.document(scoreDoc.doc);
			System.out.printf("- book: id=%s, title=%s, book_category=%s, authors=%s, score=%f\n",
					document.get("id_stored"), document.get("title"),
					document.get("book_category"),
					document.get("authors"),
					scoreDoc.score);
		}

		System.out.println("Facets:");
		for(FacetResult facetResult: results) {
			System.out.println("- " + facetResult);
		}
		taxonomyReader.close();
		indexReader.close();
	}
}