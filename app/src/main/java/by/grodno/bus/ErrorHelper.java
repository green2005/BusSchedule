package by.grodno.bus;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

public class ErrorHelper {
    public static void showErrorDialog(String errorMessage, final Context context, final View.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(errorMessage);
        builder.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (listener != null) {
                    listener.onClick(null);
                }
            }
        });
        builder.create().show();
    }

    public static void showErrorDialog(int errorResId, Context context, final View.OnClickListener listener) {
        showErrorDialog(context.getString(errorResId), context, listener);
    }
}
