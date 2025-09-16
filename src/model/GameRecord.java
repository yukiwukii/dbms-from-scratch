package model;

public class GameRecord {
    private String gameDate;
    private int teamIdHome;
    private int ptsHome;
    private float fgPctHome;
    private float ftPctHome;
    private float fg3PctHome;
    private int astHome;
    private int rebHome;
    private int homeTeamWins;

    public GameRecord(String gameDate, int teamIdHome, int ptsHome, float fgPctHome, float ftPctHome, float fg3PctHome, int astHome, int rebHome, int homeTeamWins){
        this.gameDate = gameDate;
        this.teamIdHome = teamIdHome;
        this.ptsHome = ptsHome;
        this.fgPctHome = fgPctHome;
        this.ftPctHome = ftPctHome;
        this.fg3PctHome = fg3PctHome;
        this.astHome = astHome;
        this.rebHome = rebHome;
        this.homeTeamWins = homeTeamWins;
    }

    public float getKey() {
        return this.getFtPctHome();
    }

    public String getGameDate() {
        return gameDate;
    }

    public void setGameDate(String gameDate) {
        this.gameDate = gameDate;
    }

    public int getTeamIdHome() {
        return teamIdHome;
    }

    public void setTeamIdHome(int teamIdHome) {
        this.teamIdHome = teamIdHome;
    }

    public int getPtsHome() {
        return ptsHome;
    }

    public void setPtsHome(int ptsHome) {
        this.ptsHome = ptsHome;
    }

    public float getFgPctHome() {
        return fgPctHome;
    }

    public void setFgPctHome(float fgPctHome) {
        this.fgPctHome = fgPctHome;
    }

    public float getFtPctHome() {
        return ftPctHome;
    }

    public void setFtPctHome(float ftPctHome) {
        this.ftPctHome = ftPctHome;
    }

    public float getFg3PctHome() {
        return fg3PctHome;
    }

    public void setFg3PctHome(float fg3PctHome) {
        this.fg3PctHome = fg3PctHome;
    }

    public int getAstHome() {
        return astHome;
    }

    public void setAstHome(int astHome) {
        this.astHome = astHome;
    }

    public int getRebHome() {
        return rebHome;
    }

    public void setRebHome(int rebHome) {
        this.rebHome = rebHome;
    }

    public int getHomeTeamWins() {
        return homeTeamWins;
    }

    public void setHomeTeamWins(int homeTeamWins) {
        this.homeTeamWins = homeTeamWins;
    }

    @Override
    public String toString() {
        return String.format("Date: %s | TeamID: %d | PTS: %d | FG%%: %.3f | FT%%: %.3f | 3P%%: %.3f | AST: %d | REB: %d | Wins: %d",
                gameDate, teamIdHome, ptsHome, fgPctHome, ftPctHome, fg3PctHome, astHome, rebHome, homeTeamWins);
    }
}
