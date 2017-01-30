package m2_lab8;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.LabelAndValue;
//import org.apache.lucene.facet.index.params.DefaultFacetIndexingParams;
//import org.apache.lucene.facet.search.FacetsCollector;
//import org.apache.lucene.facet.search.params.CountFacetRequest;
//import org.apache.lucene.facet.search.params.FacetSearchParams;
//import org.apache.lucene.facet.search.results.FacetResult;
//import org.apache.lucene.facet.search.results.FacetResultNode;
import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.apache.lucene.facet.taxonomy.FastTaxonomyFacetCounts;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.complexPhrase.ComplexPhraseQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.MultiCollector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;


class FacetLuceneSearcher {
	private static Version LUCENE_VERSION = Version.LUCENE_40;
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
		
		IndexReader indexReader = DirectoryReader.open(FSDirectory.open(new File(indexDirectory)));
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);

		TaxonomyReader taxonomyReader = new DirectoryTaxonomyReader(FSDirectory.open(new File(taxonomyDirectory)));

//		FacetSearchParams searchParams = new FacetSearchParams(new DefaultFacetIndexingParams());
//		searchParams.addFacetRequest(new CountFacetRequest(new CategoryPath("author"), 100));
//		searchParams.addFacetRequest(new CountFacetRequest(new CategoryPath("book_category"), 100));
		
		ComplexPhraseQueryParser queryParser = new ComplexPhraseQueryParser(LUCENE_VERSION, "title", new StandardAnalyzer(LUCENE_VERSION));
		Query luceneQuery = queryParser.parse(query);

		// Collectors to get top results and facets
		TopScoreDocCollector topScoreDocCollector = TopScoreDocCollector.create(10, true);
		//FacetsCollector facetsCollector = new FacetsCollector(searchParams, indexReader, taxonomyReader);
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
					document.get("id"), document.get("title"),
					document.get("book_category"),
					document.get("authors"),
					scoreDoc.score);
		}

		System.out.println("Facets:");
//		for(FacetResult facetResult: facetsCollector.getFacetResults()) {
//			System.out.println("- " + facetResult.getFacetResultNode().getLabel());
//			for(FacetResultNode facetResultNode: facetResult.getFacetResultNode().getSubResults()) {
//				System.out.printf("    - %s (%f)\n", facetResultNode.getLabel().toString(),
//						facetResultNode.getValue());
//				for(FacetResultNode subFacetResultNode: facetResultNode.getSubResults()) {
//					System.out.printf("        - %s (%f)\n", subFacetResultNode.getLabel().toString(),
//							subFacetResultNode.getValue());
//				}
//			}
//		}
		for(FacetResult facetResult: results) {
			System.out.println("- " + facetResult);
//			for(FacetResultNode facetResultNode: facetResult.getFacetResultNode().getSubResults()) {
//				System.out.printf("    - %s (%f)\n", facetResultNode.getLabel().toString(),
//						facetResultNode.getValue());
//				for(FacetResultNode subFacetResultNode: facetResultNode.getSubResults()) {
//					System.out.printf("        - %s (%f)\n", subFacetResultNode.getLabel().toString(),
//							subFacetResultNode.getValue());
//				}
//			}
		}
		taxonomyReader.close();
		indexReader.close();
	}
}