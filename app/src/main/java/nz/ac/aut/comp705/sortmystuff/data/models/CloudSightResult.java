package nz.ac.aut.comp705.sortmystuff.data.models;

public class CloudSightResult implements ICloudSightResult{

    public String token;
    public String url;
    public String ttl;
    public String status;
    public String name;

    @Override
    public String toString() {
        return (name);
    }

    @Override
    public String token() {
        return token;
    }

    @Override
    public String url() {
        return url;
    }

    @Override
    public String ttl() {
        return ttl;
    }

    @Override
    public String status() {
        return status;
    }

    @Override
    public String name() {
        return name;
    }
}
