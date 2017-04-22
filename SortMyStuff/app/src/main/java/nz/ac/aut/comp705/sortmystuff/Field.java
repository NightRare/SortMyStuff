package nz.ac.aut.comp705.sortmystuff;

/**
 * Created by DonnaCello on 22 Apr 2017.
 */

public class Field {
    private String fieldString;
    //add field for field images

    private Field(String field){
        setField(field);
    }

    public static Field createField(String field){
        return new Field(field);
    }

    //createPhotoField

    public void setField(String field){
        if(field == null){
            field = "";
        }
        fieldString = field;
    }

    public String getField(){
        return fieldString;
    }
}
