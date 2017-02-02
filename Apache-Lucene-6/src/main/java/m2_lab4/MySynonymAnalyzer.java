package m2_lab4;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.standard.ClassicTokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.synonym.FlattenGraphFilter;
import org.apache.lucene.analysis.synonym.SynonymGraphFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.util.CharsRef;

/**
 *
 * @author Asif
 */
public class MySynonymAnalyzer extends Analyzer {

	// //////////////////////////////////////////////////////////////////////////
	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		Tokenizer source = new ClassicTokenizer();
		TokenStream filter = new StandardFilter(source);
		filter = new LowerCaseFilter(filter);
		filter = new SynonymGraphFilter(filter, getSynonymsMap(), false);
		filter = new FlattenGraphFilter(filter);
		return new TokenStreamComponents(source, filter);
	}

	// //////////////////////////////////////////////////////////////////////////
	private SynonymMap getSynonymsMap() {
		try {
			SynonymMap.Builder builder = new SynonymMap.Builder(true);
			builder.add(new CharsRef("finished"), new CharsRef("completed"),
					true);
			builder.add(new CharsRef("finished"), new CharsRef("over"), true);
			builder.add(new CharsRef("finished"), new CharsRef("ended"), true);
			builder.add(new CharsRef("work"), new CharsRef("job"), true);
			builder.add(new CharsRef("work"), new CharsRef("labor"), true);
			builder.add(new CharsRef("work"), new CharsRef("effort"), true);
			SynonymMap mySynonymMap = builder.build();
			return mySynonymMap;
		} catch (Exception ex) {
			return null;
		}
	}
	// //////////////////////////////////////////////////////////////////////////

}
