package by.grodno.bus.map;

public class MarkerInfo {
    protected enum MarkerType {BUS, STOP}

    ;

    private MarkerType mMarkerType;
    private int mId;
    private String mName;
    private int mRid;

    protected MarkerInfo(MarkerType markerType, int id, String name) {
        mMarkerType = markerType;
        mId = id;
        mName = name;
    }

    protected int getId() {
        return mId;
    }

    protected MarkerType getmMarkerType() {
        return mMarkerType;
    }

    protected String getName() {
        return mName;
    }

    protected int getRid() {
        return mRid;
    }

    protected void setRid(int rid) {
        mRid = rid;
    }

}
