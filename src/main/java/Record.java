import java.io.Serializable;

public class Record implements Serializable {
    private static final long serialVersionUID = 1L;

    private String eventName;
    private String deckId;
    private String deckName;
    private String deckFormat;

    private String opponentName;
    private String opponentRankClass;
    private int opponentRankTier;
    private double opponentRankPercentile;

    private String timestamp;
    private boolean playFirst;
    private boolean win;
    private int mulliganedCount;
    private int turnCount;
    private int secondsCount;

    private int maxCreatures;
    private int maxLands;
    private int maxArtifactsAndEnchantments;
    private int spellCastCount;

    public Record() {
    }

    public Record(String eventName, String deckId, String deckName, String deckFormat,
                  String opponentName, String opponentRankClass, int opponentRankTier, double opponentRankPercentile,
                  String timestamp, boolean playFirst, boolean win, int mulliganedCount, int turnCount, int secondsCount,
                  int maxCreatures, int maxLands, int maxArtifactsAndEnchantments, int spellCastCount) {

        this.eventName = eventName;
        this.deckId = deckId;
        this.deckName = deckName;
        this.deckFormat = deckFormat;

        this.opponentName = opponentName;
        this.opponentRankClass = opponentRankClass;
        this.opponentRankTier = opponentRankTier;
        this.opponentRankPercentile = opponentRankPercentile;

        this.timestamp = timestamp;
        this.playFirst = playFirst;
        this.win = win;
        this.mulliganedCount = mulliganedCount;
        this.turnCount = turnCount;
        this.secondsCount = secondsCount;

        this.maxCreatures = maxCreatures;
        this.maxLands = maxLands;
        this.maxArtifactsAndEnchantments = maxArtifactsAndEnchantments;
        this.spellCastCount = spellCastCount;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getDeckId() {
        return deckId;
    }

    public void setDeckId(String deckId) {
        this.deckId = deckId;
    }

    public String getDeckName() {
        return deckName;
    }

    public void setDeckName(String deckName) {
        this.deckName = deckName;
    }

    public String getDeckFormat() {
        return deckFormat;
    }

    public void setDeckFormat(String deckFormat) {
        this.deckFormat = deckFormat;
    }

    public String getOpponentName() {
        return opponentName;
    }

    public void setOpponentName(String opponentName) {
        this.opponentName = opponentName;
    }

    public String getOpponentRankClass() {
        return opponentRankClass;
    }

    public void setOpponentRankClass(String opponentRankClass) {
        this.opponentRankClass = opponentRankClass;
    }

    public int getOpponentRankTier() {
        return opponentRankTier;
    }

    public void setOpponentRankTier(int opponentRankTier) {
        this.opponentRankTier = opponentRankTier;
    }

    public double getOpponentRankPercentile() {
        return opponentRankPercentile;
    }

    public void setOpponentRankPercentile(double opponentRankPercentile) {
        this.opponentRankPercentile = opponentRankPercentile;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean getPlayFirst() {
        return playFirst;
    }

    public void setPlayFirst(boolean playFirst) {
        this.playFirst = playFirst;
    }

    public boolean getWin() {
        return win;
    }

    public void setWin(boolean win) {
        this.win = win;
    }

    public int getMulliganedCount() {
        return mulliganedCount;
    }

    public void setMulliganedCount(int mulliganedCount) {
        this.mulliganedCount = mulliganedCount;
    }

    public int getTurnCount() {
        return turnCount;
    }

    public void setTurnCount(int turnCount) {
        this.turnCount = turnCount;
    }

    public int getSecondsCount() {
        return secondsCount;
    }

    public void setSecondsCount(int secondsCount) {
        this.secondsCount = secondsCount;
    }

    public int getMaxCreatures() {
        return maxCreatures;
    }

    public void setMaxCreatures(int maxCreatures) {
        this.maxCreatures = maxCreatures;
    }

    public int getMaxLands() {
        return maxLands;
    }

    public void setMaxLands(int maxLands) {
        this.maxLands = maxLands;
    }

    public int getMaxArtifactsAndEnchantments() {
        return maxArtifactsAndEnchantments;
    }

    public void setMaxArtifactsAndEnchantments(int maxArtifactsAndEnchantments) {
        this.maxArtifactsAndEnchantments = maxArtifactsAndEnchantments;
    }

    public int getSpellCastCount() {
        return spellCastCount;
    }

    public void setSpellCastCount(int spellCastCount) {
        this.spellCastCount = spellCastCount;
    }
}