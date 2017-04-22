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

    private Asset(String name) {
        nameAs(name);
        index = new ArrayList<Asset>();
        detailList = new ArrayList<ExtendedDetail>();
    }

    private Asset(String n, List<Asset> content, List<ExtendedDetail> details){
        nameAs(n);
        index = content;
        detailList = details;
    }

    public static Asset createFromFile(String n, List<Asset> content, List<ExtendedDetail> details){
        return new Asset(n,content,details);
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

    public List<Asset> getContent(){
        return index;
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
        return name;
    }

}
