package by.grodno.bus;

public enum TabItem {
    BUSES(0, R.drawable.bus), //R.string.buses
    TROLL(R.string.troll, R.drawable.trolleybus),
    STOPS(R.string.stops, R.drawable.stop),
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
