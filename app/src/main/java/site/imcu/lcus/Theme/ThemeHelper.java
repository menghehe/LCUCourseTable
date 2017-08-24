package site.imcu.lcus.theme;

import android.content.Context;
import android.content.SharedPreferences;

public class ThemeHelper {
    private static final String CURRENT_THEME = "theme_current";

    static final int CARD_SAKURA = 0x1;
    static final int CARD_HOPE = 0x2;
    static final int CARD_STORM = 0x3;
    static final int CARD_WOOD = 0x4;
    static final int CARD_LIGHT = 0x5;
    static final int CARD_THUNDER = 0x6;
    static final int CARD_SAND = 0x7;
    static final int CARD_FIREY = 0x8;
    static final int CARD_TEAL = 0x9;
    static final int CARD_LIME = 0x10;
    static final int CARD_CYAN = 0x11;

    private static SharedPreferences getSharePreference(Context context) {
        return context.getSharedPreferences("multiple_theme", Context.MODE_PRIVATE);
    }

    public static void setTheme(Context context, int themeId) {
        getSharePreference(context).edit()
                .putInt(CURRENT_THEME, themeId)
                .apply();
    }

    public static int getTheme(Context context) {
        return getSharePreference(context).getInt(CURRENT_THEME, CARD_SAKURA);
    }

     static boolean isDefaultTheme(Context context) {
        return getTheme(context) == CARD_SAKURA;
    }

    public static String getName(int currentTheme) {
        switch (currentTheme) {
            case CARD_SAKURA:
                return "THE SAKURA";
            case CARD_STORM:
                return "THE STORM";
            case CARD_WOOD:
                return "THE WOOD";
            case CARD_LIGHT:
                return "THE LIGHT";
            case CARD_HOPE:
                return "THE HOPE";
            case CARD_THUNDER:
                return "THE THUNDER";
            case CARD_SAND:
                return "THE SAND";
            case CARD_FIREY:
                return "THE FIREY";
            case CARD_TEAL:
                return "THE TEAL";
            case CARD_LIME:
                return "THE LIME";
            case CARD_CYAN:
                return "THE CYAN";
        }
        return "THE RETURN";
    }
}
