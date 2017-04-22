package nz.ac.aut.comp705.sortmystuff;

/**
 * Created by DonnaCello on 22 Apr 2017.
 */

public class ExtendedDetail {
    private String detailLabel;
    private Field detailField;

    private ExtendedDetail(String label, Field field){
        createLabel(label);
        detailField = field;
    }

    public static ExtendedDetail createDetail(String label, Field field){
        return new ExtendedDetail(label, field);
    }

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
}
