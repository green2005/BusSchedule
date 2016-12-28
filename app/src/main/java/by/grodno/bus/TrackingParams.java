package by.grodno.bus;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;


public class TrackingParams implements Parcelable {
    public static final String KEY = "TrackingParamsKey";
    public static final String TRANSPORT_TYPE_KEY = "TransportTypeKey";
    public static final String BUS_TYPE_KEY = "А";
    public static final String MINI_BUS_TYPE_KEY = "М";


    private ArrayList<String> mBusNames;
    private ArrayList<String> mBusTypes;
    private String mStopName;
    private String mRouteName;

    private TrackingParams() {
        mBusNames = new ArrayList<>();
        mBusTypes = new ArrayList<>();
    }

    public TrackingParams(String busName, String busType, String stopName, String routeName) {
        this();
        mBusNames.add(busName);
        mBusTypes.add(busType);
        mStopName = stopName;
        mRouteName = routeName;
    }

    public TrackingParams(List<String> busNames, List<String> busTypes, String stopName, String routeName) {
        this();
        mBusNames.addAll(busNames);
        mBusTypes.addAll(busTypes);
        mStopName = stopName;
        mRouteName = routeName;
    }

    protected TrackingParams(Parcel in) {
        this();
        in.readStringList(mBusNames);
        in.readStringList(mBusTypes);
        mStopName = in.readString();
        mRouteName = in.readString();
    }

    public static final Creator<TrackingParams> CREATOR = new Creator<TrackingParams>() {
        @Override
        public TrackingParams createFromParcel(Parcel in) {
            return new TrackingParams(in);
        }

        @Override
        public TrackingParams[] newArray(int size) {
            return new TrackingParams[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(mBusNames);
        dest.writeStringList(mBusTypes);
        dest.writeString(mStopName);
        dest.writeString(mRouteName);
    }

    public String getRouteName(){
        return mRouteName;
    }

    public String getStopName(){
        return mStopName;
    }

    public ArrayList<String> getBusNames(){
        return mBusNames;
    }

    public ArrayList<String> getBusTypes(){
        return mBusTypes;
    }
}
