package nz.ac.aut.comp705.sortmystuff;

/**
 * @author Donna
 *
 * A Field class to store field information
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
