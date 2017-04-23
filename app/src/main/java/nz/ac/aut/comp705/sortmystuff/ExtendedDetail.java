package nz.ac.aut.comp705.sortmystuff;

/**
 * @author Donna
 *
 * An Extended Detail class used to format an asset's details
 */

public class ExtendedDetail {
    //Represents the detail's label
    private String detailLabel;
    //Represents the detail's information or description
    private Field detailField;


    /**
     * Creates a detail
     *
     * @param label states what information is asked
     * @param field the information or description given
     * @return
     */
    public static ExtendedDetail createTextDetail(String label, Field field){
        return new ExtendedDetail(label, field);
    }

    /**
     * Creates a label
     *
     * @param label
     */
    private void createLabel(String label){
        if(label == null) {throw new NullPointerException("Asset name cannot be null");}
        if(label.length() < 1){throw new IllegalArgumentException("Please enter a valid name");}
        detailLabel = label;
    }


    public String getDetailLabel(){
        return detailLabel;
    }

    public Field getDetailField(){
        return detailField;
    }

    private ExtendedDetail(String label, Field field){
        createLabel(label);
        detailField = field;
    }
}
