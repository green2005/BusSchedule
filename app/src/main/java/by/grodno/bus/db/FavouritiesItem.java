package by.grodno.bus.db;

public class FavouritiesItem {
    private String tr;
    private String stopName;
    private String busName;
    private String directionName;

    public String getTr() {
        return tr;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public void setTr(String tr) {
        this.tr = tr;
    }

    public String getBusName() {
        return busName;
    }

    public void setBusName(String busName) {
        this.busName = busName;
    }

    public String getDirectionName() {
        return directionName;
    }

    public void setDirectionName(String directionName) {
        this.directionName = directionName;
    }
}