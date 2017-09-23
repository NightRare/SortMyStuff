package nz.ac.aut.comp705.sortmystuff.data.models;

public interface IDetail<T> {

    String getId();

    String getAssetId();

    DetailType getType();

    String getLabel();

    T getField();

    Long getCreateTimestamp();

    Long getModifyTimestamp();
}
