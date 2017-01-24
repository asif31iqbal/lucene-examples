package m2_lab6;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

public class BoostAtIndexingImpl extends AbstractBoostType {

    public BoostAtIndexingImpl(boolean printExplanation) {
        super(printExplanation);
    }

    @Override
    protected void boostPerType(String itemName, String itemColour,
            String itemPrice, String originOfItem, String itemType)
            throws IOException {
        Document docToAdd = new Document();
        docToAdd.add(new StringField("itemName", itemName, Field.Store.YES));

        docToAdd.add(new StringField("itemColour", itemColour, Field.Store.YES));
        docToAdd.add(new StringField("itemPrice", itemPrice, Field.Store.YES));
        docToAdd.add(new StringField("originOfItem", originOfItem, Field.Store.YES));

        TextField itemTypeField = new TextField("itemType", itemType, Field.Store.YES);
        docToAdd.add(itemTypeField);
        //Boost items made in India
        if ("India".equalsIgnoreCase(originOfItem)) {
            itemTypeField.setBoost(2.0f);
        }
        indexWriter.addDocument(docToAdd);
    }

    @Override
    protected String printBoostTypeInformation() {
        System.out.println("----------");
        return "Running the example for Index time boosting.";
    }

    @Override
    protected String getQueryForSearch() {
        return "itemType:suv";
    }

}
