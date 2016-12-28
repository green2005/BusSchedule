package by.grodno.bus.bo;

import android.content.ContentValues;

import org.json.JSONObject;

public interface ContentValuesItem {
    public abstract void fillContentValues(JSONObject jo, ContentValues values) throws Exception;
    //public abstract ContentValues fillContentValues();
}
