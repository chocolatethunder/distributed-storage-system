package app;

/**
 *This class should be used when creating entry for harm.list
 */
public class NodeAttribute {

    private String address;
    private long space;
    private boolean isAlive;

    public NodeAttribute( String address, long space, boolean alive){

        this.address = address;
        this.space = space;
        this.isAlive = alive;
    }

    public NodeAttribute(){}


    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }


    public long getSpace() {
        return space;
    }

    public void setSpace(long space) {
        this.space = space;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }
}
