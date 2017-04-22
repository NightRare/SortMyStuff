package nz.ac.aut.comp705.sortmystuff;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DonnaCello on 21 Apr 2017.
 */

public class Asset {
    private String name;
    private List<Asset> index;
    private List<ExtendedDetail> detailList;

    /* Private Constructor to create an Asset with just its name
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
     * @param name
     */
    private Asset(String name, List<Asset> content, List<ExtendedDetail> details){
        nameAs(name);
        index = content;
        detailList = details;
    }

    public static Asset createFromFile(String name, List<Asset> content, List<ExtendedDetail> details){
        return new Asset(name,content,details);
    }

    public static Asset createAsset(String n){
        return new Asset(n);
    }

    /*
    private  Asset rootAsset(){
        return createAsset("Root");
    }
    */

    public static Asset loadDummyAssets(){
        Asset root = createAsset("Root");
        root.addContent("Apartment");
        root.addContent("Office");
        return root;
    }

    public void nameAs(String n){
        if(n == null) {throw new NullPointerException("Asset name cannot be null");}
        if(n.length() < 1){throw new IllegalArgumentException("Please enter a valid name");}
        name = n;
    }

    public void addContent(String n){
        index.add(createAsset(n));
    }

    public void addTextDetail(String label, String fieldString){
        detailList.add(ExtendedDetail.createDetail(label,Field.createField(fieldString)));
    }

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

}
