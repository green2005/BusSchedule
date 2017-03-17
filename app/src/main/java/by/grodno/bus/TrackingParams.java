package by.grodno.bus;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
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
    private int mTrackingStopsBusId;
    private boolean mNeedTrackStops = false;

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

    public String toString() {
        String busNames = TextUtils.join(",", mBusNames);
        String busTypes = TextUtils.join(",", mBusTypes);
        String stopName = mStopName;
        String routeName = mRouteName;
        return busNames + ";" + busTypes + ";" + stopName + ";" + routeName;
    }

    public static TrackingParams fromString(String params) {
        if (TextUtils.isEmpty(params)) {
            return new TrackingParams("", "", "", "");
        } else {
            String[] strings = TextUtils.split(params, ";");
            if (strings.length < 4) {
                return new TrackingParams("", "", "", "");
            }
            String sNames = strings[0];
            String sTypes = strings[1];
            String stopName = strings[2];
            String routeName = strings[3];
            ArrayList<String> busNames = new ArrayList<>(Arrays.asList(TextUtils.split(sNames, ",")));
            ArrayList<String> busTypes = new ArrayList<>(Arrays.asList(TextUtils.split(sTypes, ",")));
            return new TrackingParams(busNames, busTypes, stopName, routeName);
        }
    }

    public void setTrackStops(boolean needTrackStops, int trackBusId) {
        mNeedTrackStops = needTrackStops;
        mTrackingStopsBusId = trackBusId;
    }

    public boolean getNeedTrackStops() {
        return mNeedTrackStops;
    }

    public int getTrackingStopsBusId() {
        return mTrackingStopsBusId;
    }

    protected TrackingParams(Parcel in) {
        this();
        in.readStringList(mBusNames);
        in.readStringList(mBusTypes);
        mStopName = in.readString();
        mRouteName = in.readString();
        mTrackingStopsBusId = in.readInt();
        setTrackStops(in.readByte() == 1, mTrackingStopsBusId);
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
        dest.writeInt(mTrackingStopsBusId);
        byte b = 0;
        if (getNeedTrackStops()) {
            b = 1;
        }
        dest.writeByte(b);
        dest.writeInt(mTrackingStopsBusId);
    }

    public String getRouteName() {
        return mRouteName;
    }

    public String getStopName() {
        return mStopName;
    }

    public ArrayList<String> getBusNames() {
        return mBusNames;
    }

    public ArrayList<String> getBusTypes() {
        return mBusTypes;
    }
}
