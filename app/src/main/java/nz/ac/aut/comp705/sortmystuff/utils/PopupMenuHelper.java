package nz.ac.aut.comp705.sortmystuff.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.PopupMenu;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class PopupMenuHelper {

    public static PopupMenu build(
            @NonNull Context context,
            @NonNull View anchor,
            int menuId,
            @Nullable PopupMenu.OnMenuItemClickListener listener,
            boolean displayIcons) {
        PopupMenu popupMenu = new PopupMenu(context, anchor);
        popupMenu.inflate(menuId);
        if (listener != null)
            popupMenu.setOnMenuItemClickListener(listener);

        if(!displayIcons) return popupMenu;

        // this code snippet is to show the icons in the popup menu
        // TODO: this code snippet wouldn't work with proguard. Need to add proguard configuration:
        //(Note, I am using PopupMenu from support package)
        // -keepclassmembernames class android.support.v7.widget.PopupMenu { private android.support.v7.internal.view.menu.MenuPopupHelper mPopup; }
        // -keepclassmembernames class android.support.v7.internal.view.menu.MenuPopupHelper { public void setForceShowIcon(boolean); } –
        try {
            Field[] fields = popupMenu.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper
                            .getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod(
                            "setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            Log.e("PopupMenuHelper", e.getMessage(), e);
        }

        return popupMenu;
    }
}
