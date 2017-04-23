package nz.ac.aut.comp705.sortmystuff;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Donna
 *
 * An Asset class which stores formatted information
 * relevant to an asset
 */

public class Asset {

    //name of the asset
    private String name;
    //list of assets this certain asset contains
    private List<Asset> index;
    //list of this asset's extended details
    private List<ExtendedDetail> detailList;


    /* Factory method prepared for creating Asset from a validly formatted file
     *
     * @param name the name of the new Asset
     * @param content list of its contents
     * @param details list of any extended details
     */
    public static Asset createFromFile(String name, ArrayList<Asset> content, ArrayList<ExtendedDetail> details){
        return new Asset(name,content,details);
    }

    /* Factory method prepared for creating an Asset within the app
     *
     * @param name the name of the new Asset
     * @return asset new asset
     */
    public static Asset createAsset(String n){
        return new Asset(n);
    }

    /* Creates a dummy asset for debugging
     *
     * @param name the name of the new Asset
     * @param content list of its contents
     * @param details list of any extended details
     * @return root the dummy asset
     */
    public static Asset loadDummyAssets(){
        Asset root = createAsset("Root");
        root.addContent("Apartment");
        root.addContent("Office");
        return root;
    }

    //**********MUTATORS**********

    /*
     * Names the asset provided that the name id valid
     *
     * @param n name
     * @return boolean checks if name input is valid
     */
    private boolean nameAs(String n){
        boolean isValid = false;
        if(n == null) {throw new NullPointerException("Asset name cannot be null");}
        else if(n.length() < 1){throw new IllegalArgumentException("Please enter a valid name");}
        else {
            name = n;
            isValid = true;
        }
        return isValid;
    }

    /*
     * Adds an asset within this asset
     *
     * @param n name
     * @return a the asset created provided name is valid
     */
    public Asset addContent(String n){
        Asset a;
        if(n == null) {throw new NullPointerException("Asset name cannot be null");}
        else if(n.length() < 1){throw new IllegalArgumentException("Please enter a valid name");}
        else {
            a = createAsset(n);
            index.add(a);
        }
        return a;
    }

    /*
     * Adds information to this asset
     *
     * @param label states what information is asked
     * @param fieldString the information or description given
     */
    public void addTextDetail(String label, String fieldString){
        detailList.add(ExtendedDetail.createTextDetail(label,Field.createField(fieldString)));
    }

    //**********ACCESSORS**********

    public String getName(){
        return name;
    }

    public ArrayList<Asset> getContent(){
        ArrayList<Asset> contents = new ArrayList<Asset>();
        contents.addAll(index);
        return contents;
    }

    public ArrayList<String> getContentNames(){
        ArrayList<String> nameList = new ArrayList<>();
        for(Asset a: getContent()){
            nameList.add(a.getName());
        }
        return nameList;
    }

    public List<ExtendedDetail> getDetail(){
        return detailList;
    }

    public String toString(){

        return name +": " + getContentNames().toString();
    }

    //**********CONSTRUCTORS**********

    /* Private Constructor to create an Asset with only its name
     *
     * @param name the name of the new Asset
     */
    private Asset(String name) {
        nameAs(name);
        index = new ArrayList<Asset>();
        detailList = new ArrayList<ExtendedDetail>();
    }

    /* Private Constructor to create an Asset with its name, content, and details, if any
     *
     * @param name the name of the new Asset
     * @param content list of its contents
     * @param details list of any extended details
     */
    private Asset(String name, ArrayList<Asset> content, ArrayList<ExtendedDetail> details){
        nameAs(name);
        index = content;
        detailList = details;
    }

}
