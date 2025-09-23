package Util;

import model.GameRecord;
import java.nio.ByteBuffer;

public final class FixedRecordSize {
    private FixedRecordSize() {}

    public static final int RECORD_SIZE = 36; // 9 x 4 bytes

    // FAST parser: handles M/D/YYYY, D/M/YYYY, and YYYY-MM-DD
    public static int parseDateToInt(String s) {
        if (s == null || s.isEmpty()) return 0;

        int slash = s.indexOf('/');
        if (slash >= 0) {
            int b = s.indexOf('/', slash + 1);
            int a = Integer.parseInt(s, 0, slash, 10);
            int d1 = Integer.parseInt(s, slash + 1, b, 10);
            int c = Integer.parseInt(s, b + 1, s.length(), 10);
            int year, month, day;
            if (a > 31) {                // YYYY/M/D
                year = a; month = d1; day = c;
            } else if (c > 31) {         // M/D/YYYY or D/M/YYYY
                year = c;
                if (a > 12) { day = a; month = d1; }  // D/M/YYYY
                else          { month = a; day = d1; } // M/D/YYYY
            } else {                      // fallback assume M/D/YYYY
                year = c; month = a; day = d1;
            }
            return year * 10000 + month * 100 + day;
        }

        int dash = s.indexOf('-');
        if (dash >= 0) {                  // YYYY-MM-DD
            int b = s.indexOf('-', dash + 1);
            int y = Integer.parseInt(s, 0, dash, 10);
            int m = Integer.parseInt(s, dash + 1, b, 10);
            int d = Integer.parseInt(s, b + 1, s.length(), 10);
            return y * 10000 + m * 100 + d;
        }

        // digits-only fallback (rare)
        StringBuilder z = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch >= '0' && ch <= '9') z.append(ch);
        }
        if (z.length() >= 8) {
            int y = Integer.parseInt(z.substring(0, 4));
            int m = Integer.parseInt(z.substring(4, 6));
            int d = Integer.parseInt(z.substring(6, 8));
            return y * 10000 + m * 100 + d;
        }
        throw new IllegalArgumentException("Unrecognized date: " + s);
    }


    public static String formatDateInt(int dateInt) {
        String s = String.format("%08d", dateInt);
        return s.substring(0,4) + "-" + s.substring(4,6) + "-" + s.substring(6,8);
    }

    // Keep/write your existing write/read, but here they are for completeness:

    public static void write(model.GameRecord r, ByteBuffer buf) {
        buf.putInt(parseDateToInt(r.getGameDate()));
        buf.putInt(r.getTeamIdHome());
        buf.putInt(r.getPtsHome());
        buf.putFloat(r.getFgPctHome());
        buf.putFloat(r.getFtPctHome());   // B+ key
        buf.putFloat(r.getFg3PctHome());
        buf.putInt(r.getAstHome());
        buf.putInt(r.getRebHome());
        buf.putInt(r.getHomeTeamWins());
    }

    public static GameRecord read(ByteBuffer buf) {
        int dateInt = buf.getInt();
        int teamId  = buf.getInt();
        int pts     = buf.getInt();
        float fg    = buf.getFloat();
        float ft    = buf.getFloat();
        float fg3   = buf.getFloat();
        int ast     = buf.getInt();
        int reb     = buf.getInt();
        int wins    = buf.getInt();
        String gameDate = formatDateInt(dateInt);
        return new GameRecord(gameDate, teamId, pts, fg, ft, fg3, ast, reb, wins);
    }
}


