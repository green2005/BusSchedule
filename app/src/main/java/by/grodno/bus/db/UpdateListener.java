package by.grodno.bus.db;

public interface UpdateListener {
    public void onError(String error);
    public void onError(int stringResId);
    public void onSuccess(String updatedDate);
}
