package com.intelliviz.movieapp3;

/**
 * Created by edm on 6/1/2016.
 */
public class QueryPreferences {
    public static final String PREF_REFRESH_LIST = "refreshlist";
/*
    public static boolean getRefreshList(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_REFRESH_LIST, false);
    }

    public static void setRefreshList(Context context, boolean refresh) {

        SharedPreferences.Editor editor =
                PreferenceManager.getDefaultSharedPreferences(context).edit();

        editor.putBoolean(PREF_REFRESH_LIST, refresh);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            editor.apply();
        }
        else {
            editor.commit();
        }
    }
    */
}
