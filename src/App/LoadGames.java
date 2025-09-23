package App;

import IO.FileManager;
import storage.HeapFile;
import storage.HeapFile.RecordId;
import model.GameRecord;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class LoadGames {

    // ---- helpers ----
    private static String norm(String s) {
        return s.trim().toUpperCase().replaceAll("[^A-Z0-9]", "");
    }
    private static int col(Map<String,Integer> idx, String... candidates) {
        for (String c : candidates) {
            Integer p = idx.get(norm(c));
            if (p != null) return p;
        }
        throw new IllegalArgumentException("Missing column. Tried " + Arrays.toString(candidates)
                + " | Have: " + idx.keySet());
    }
    private static String getOrEmpty(String[] a, int i) { return i < a.length ? a[i] : ""; }
    private static int    parseInt0 (String s) { return (s == null || s.isBlank()) ? 0  : Integer.parseInt(s); }
    private static float  parseFloat0(String s) { return (s == null || s.isBlank()) ? 0f : Float.parseFloat(s); }

    public static void main(String[] args) throws Exception {
        // Defaults for the project
        String csv = "games.txt";
        String db  = "db.data";
        boolean reset = true;   // reset every run by default

        // Optional args: [csv] [--db=path] [--keep]
        for (String a : args) {
            if (a.equals("--keep")) reset = false;            // keep existing file if passed
            else if (a.startsWith("--db=")) db = a.substring(5);
            else csv = a;                                     // positional arg = CSV path
        }

        System.out.println("Working dir = " + System.getProperty("user.dir"));
        System.out.println("DB = " + db + " | reset = " + reset + " | csv = " + csv);

        try (FileManager fm = new FileManager(db, reset);
             HeapFile.Appender app = new HeapFile.Appender(fm);
             BufferedReader br = new BufferedReader(new FileReader(csv))) {

            // header
            String header = br.readLine();
            if (header == null) throw new IllegalStateException("Empty file: " + csv);

            // auto-detect tab vs comma
            String delim = header.contains("\t") ? "\t" : ",";
            String[] cols = header.split(delim, -1);
            Map<String,Integer> idx = new HashMap<>();
            for (int i = 0; i < cols.length; i++) idx.put(norm(cols[i]), i);

            System.out.println("Detected headers: " + Arrays.toString(cols));

            // map required columns
            int I_GAME_DATE    = col(idx, "GAME_DATE_EST", "GAME_DATE", "DATE");
            int I_TEAM_ID_HOME = col(idx, "TEAM_ID_HOME", "HOME_TEAM_ID");
            int I_PTS_HOME     = col(idx, "PTS_HOME", "HOME_PTS");
            int I_FG_PCT_HOME  = col(idx, "FG_PCT_HOME", "FGPCT_HOME");
            int I_FT_PCT_HOME  = col(idx, "FT_PCT_HOME", "FTPCT_HOME");      // key
            int I_FG3_PCT_HOME = col(idx, "FG3_PCT_HOME", "FG3PCT_HOME");
            int I_AST_HOME     = col(idx, "AST_HOME", "ASSISTS_HOME");
            int I_REB_HOME     = col(idx, "REB_HOME", "REBOUNDS_HOME");
            int I_HOME_WINS    = col(idx, "HOME_TEAM_WINS", "HOMEWINS");

            // data rows
            long count = 0;
            String line;
            long lineNo = 1;
            while ((line = br.readLine()) != null) {
                lineNo++;
                if (line.isBlank()) continue;

                String[] t = line.split(delim, -1);

                try {
                    String gameDate   = getOrEmpty(t, I_GAME_DATE);
                    int    teamIdHome = parseInt0 (getOrEmpty(t, I_TEAM_ID_HOME));
                    int    ptsHome    = parseInt0 (getOrEmpty(t, I_PTS_HOME));
                    float  fgPctHome  = parseFloat0(getOrEmpty(t, I_FG_PCT_HOME));
                    float  ftPctHome  = parseFloat0(getOrEmpty(t, I_FT_PCT_HOME));
                    float  fg3PctHome = parseFloat0(getOrEmpty(t, I_FG3_PCT_HOME));
                    int    astHome    = parseInt0 (getOrEmpty(t, I_AST_HOME));
                    int    rebHome    = parseInt0 (getOrEmpty(t, I_REB_HOME));
                    int    homeWins   = parseInt0 (getOrEmpty(t, I_HOME_WINS));

                    GameRecord r = new GameRecord(
                            gameDate, teamIdHome, ptsHome, fgPctHome,
                            ftPctHome, fg3PctHome, astHome, rebHome, homeWins
                    );
                    RecordId rid = app.add(r); // buffered: one write per page
                    count++;

                } catch (Exception ex) {
                    System.err.println("Parse error on line " + lineNo + ": " + ex.getMessage());
                    System.err.println("Offending line: " + line);
                    throw ex; // change to 'continue;' if you prefer to skip bad lines
                }
            }

            // Task 1 stats
            int recordsPerBlock = (FileManager.PAGE_SIZE - HeapFile.HEADER_BYTES) / Util.FixedRecordSize.RECORD_SIZE;
            long totalPages = fm.pageCount();
            long dataPages = Math.max(0, totalPages - 1); // exclude superblock
            System.out.println("Loaded records: " + count);
            System.out.println("Record size: " + Util.FixedRecordSize.RECORD_SIZE + " bytes");
            System.out.println("Records per block: " + recordsPerBlock);
            System.out.println("Blocks used (data pages): " + dataPages);
        }
    }
}

