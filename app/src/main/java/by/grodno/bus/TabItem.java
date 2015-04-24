package by.grodno.bus;

public enum TabItem {
    BUSES( R.drawable.bus), //R.string.buses
    TROLL( R.drawable.trolleybus),
    STOPS( R.drawable.stop),
    FAVOURITIES( R.drawable.star_full),;

    private int itemIcon;
    TabItem( int icon) {
        itemIcon = icon;
    }

   // public int getText(){
    //    return itemCaption;
  //  }

    public int getIcon(){
        return itemIcon;
    }
}
