package nz.ac.aut.comp705.sortmystuff.data.models;

public class DetailForm<T> implements IDetail<T>{

    private IDetail<T> mEntity;
    private T mField;

    public DetailForm(IDetail<T> entity) {
        mEntity = entity;
    }

    @Override
    public String getId() {
        return mEntity.getId();
    }

    @Override
    public String getAssetId() {
        return mEntity.getAssetId();
    }

    @Override
    public DetailType getType() {
        return mEntity.getType();
    }

    @Override
    public String getLabel() {
        return mEntity.getLabel();
    }

    @Override
    public T getField() {
        return mField == null ? mEntity.getField() : mField;
    }

    @Override
    public Long getCreateTimestamp() {
        return mEntity.getCreateTimestamp();
    }

    @Override
    public Long getModifyTimestamp() {
        return mEntity.getModifyTimestamp();
    }

    @Override
    public int getPosition() {
        return mEntity.getPosition();
    }

    @Override
    public boolean isDefaultFieldValue() {
        // if mField is not null, then return true
        // else return the value of the entity.isDefaultFieldValue()
        return mField == null && mEntity.isDefaultFieldValue();
    }

    public void setField(T field) {
        mField = field;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof DetailForm) {
            DetailForm dm = (DetailForm) obj;
            return dm.getId().equals(getId());
        }
        return false;
    }
}
