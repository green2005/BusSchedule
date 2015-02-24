package by.grodno.bus;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class ErrorHelper {
    public static void showErrorDialog(String errorMessage, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(errorMessage);
        builder.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    public static void showErrorDialog(int errorResId, Context context) {
        showErrorDialog(context.getString(errorResId), context);
    }
}
