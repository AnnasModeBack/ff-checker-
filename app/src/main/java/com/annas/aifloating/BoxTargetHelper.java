package com.annas.aifloating;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

public class BoxTargetHelper {

    public static String resolveTargetContent(Context context, int boxX, int boxY, int boxW, int boxH) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        String clipText = "";
        if (clipboard != null && clipboard.hasPrimaryClip()) {
            ClipData clipData = clipboard.getPrimaryClip();
            if (clipData != null && clipData.getItemCount() > 0) {
                CharSequence cs = clipData.getItemAt(0).coerceToText(context);
                if (cs != null) {
                    clipText = cs.toString().trim();
                }
            }
        }

        if (!clipText.isEmpty()) {
            return "Area Kotak Ditandai [x=" + boxX + ", y=" + boxY + ", " + boxW + "x" + boxH + "px]:\n" + clipText;
        } else {
            return "Area layar yang ditandai kotak AI berada di posisi X=" + boxX + ", Y=" + boxY +
                    " dengan ukuran " + boxW + "x" + boxH + " piksel. Tolong bantu jawab atau jelaskan soal/tugas yang ada di area tersebut.";
        }
    }
}
