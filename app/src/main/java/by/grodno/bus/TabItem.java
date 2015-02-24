package by.grodno.bus;

public enum TabItem {
    ROUTES(R.string.routes, R.drawable.routes), STOPS(R.string.stops, R.drawable.stops),
    FAVOURITIES(R.string.favorities, R.drawable.favorities),;

    private int itemCaption;
    private int itemIcon;
    TabItem(int caption, int icon) {
        itemCaption = caption;
        itemIcon = icon;
    }

    public int getText(){
        return itemCaption;
    }

    public int getIcon(){
        return itemIcon;
    }
}
