package m2_lab6;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

public class BoostAtQueryImpl extends AbstractBoostType {

    public BoostAtQueryImpl(boolean printExplanation) {
        super(printExplanation);
    }

    @Override
    protected String getQueryForSearch() {
        return "itemColour:white ^2 OR itemType:suv";
    }

    @Override
    protected void boostPerType(String itemName, String itemColour,
            String itemPrice, String originOfItem, String itemType)
            throws IOException {
        Document docToAdd = new Document();
        docToAdd.add(new StringField("itemName", itemName,Field.Store.YES));

        docToAdd.add(new TextField("itemColour", itemColour, Field.Store.YES));
        docToAdd.add(new StringField("itemPrice", itemPrice, Field.Store.YES));
        docToAdd.add(new StringField("originOfItem", originOfItem, Field.Store.YES));

        TextField itemTypeField = new TextField("itemType", itemType, Field.Store.YES);
        docToAdd.add(itemTypeField);
        indexWriter.addDocument(docToAdd);
    }

    @Override
    protected String printBoostTypeInformation() {
        return "Running the example for query time boosting.";
    }

}
