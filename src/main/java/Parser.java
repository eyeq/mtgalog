import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.*;
import java.util.*;

public class Parser {

    public static class Card {
        public final String name;
        public final String set;
        public final String cost;
        public final int cmc;
        public final String rarity;
        public final boolean collectible;
        public final int dfcId;
        public final int rank;

        public Card(String name, String set, String cost, int cmc, String rarity, boolean collectible, int dfcId, int rank) {
            this.name = name;
            this.set = set;
            this.cost = cost;
            this.cmc = cmc;
            this.rarity = rarity;
            this.collectible = collectible;
            this.dfcId = dfcId;
            this.rank = rank;
        }
    }

    public static class Deck {
        public final String deckId;
        public final String deckName;
        public final double winrate;

        public Deck(String deckId, String deckName, long win, long lose) {
            this.deckId = deckId;
            this.deckName = deckName;
            this.winrate = win == 0 ? 0 : win / (double) (win + lose);
        }

        public String getDeckId() {
            return deckId;
        }

        public String getDeckName() {
            return deckName;
        }

        public double getWinrate() {
            return winrate;
        }
    }

    public class RankInfo {
        public int constructedMatchesWon = 0;
        public int constructedMatchesLost = 0;
        public int constructedMatchesDrawn = 0;
        public int limitedMatchesWon = 0;
        public int limitedMatchesLost = 0;
        public int limitedMatchesDrawn = 0;
    }

    public class PlayerInventory {
        public int wcTrackPosition = 0;
        public double vaultProgress = 0;
        public final Map<Card, Integer> cards = new HashMap<>();
    }

    private final Map<Integer, Card> cardLibrary = new HashMap<>();

    private final Map<String, Record> records = new HashMap<>();

    private final RankInfo rankInfo = new RankInfo();
    private final PlayerInventory playerInventory = new PlayerInventory();

    //temp
    private String eventName;
    private String deckId;
    private String deckName;
    private String deckFormat;

    private String opponentName;
    private String opponentRankClass;
    private int opponentRankTier;
    private double opponentRankPercentile;

    private String timestamp;

    private int teamId;
    private int startingId;
    private int winningId;
    private int mulliganedCount;
    private int turnCount;
    private int secondsCount;

    private int maxCreatures;
    private int maxLands;
    private int maxArtifactsAndEnchantments;
    private int spellCastCount;

    public ArrayList<Record> getRecords() {
        return new ArrayList<>(records.values());
    }

    private void addRecords(Iterable<Record> iterable) {
        for (Record record : iterable) {
            records.put(record.getTimestamp(), record);
        }
    }

    public RankInfo getRankInfo() {
        return rankInfo;
    }

    public PlayerInventory getPlayerInventory() {
         return playerInventory;
    }

    public void loadRecord(InputStream in) throws IOException, ClassNotFoundException {
        try (ObjectInputStream is = new ObjectInputStream(in)) {
            addRecords((List<Record>) is.readObject());
        }
    }

    public void saveRecord(OutputStream out) throws IOException {
        try (ObjectOutputStream os = new ObjectOutputStream(out)) {
            os.writeObject(getRecords());
        }
    }

    public void loadCardLibrary(Reader in) {
        JsonObject json = new Gson().fromJson(in, JsonObject.class);
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            int cardId = Integer.parseInt(entry.getKey());
            JsonObject card = entry.getValue().getAsJsonObject();

            String name = card.getAsJsonPrimitive("name").getAsString();
            String set = card.getAsJsonPrimitive("set").getAsString();
            String cost = card.getAsJsonArray("cost").toString();
            int cmc = card.getAsJsonPrimitive("cmc").getAsInt();
            String rarity = card.getAsJsonPrimitive("rarity").getAsString();
            boolean collectible = card.getAsJsonPrimitive("collectible").getAsBoolean();
            int dfcId = card.getAsJsonPrimitive("dfcId").getAsInt();
            int rank = card.getAsJsonPrimitive("rank").getAsInt();

            Card c = new Card(name, set, cost, cmc, rarity, collectible, dfcId, rank);
            cardLibrary.put(cardId, c);
            playerInventory.cards.put(c, 0);
        }
    }

    public void readLog(Reader in) throws IOException {
        boolean json = false;
        boolean playerCards = false;
        String jsonText = "";

        try (BufferedReader br = new BufferedReader(in)) {
            String line;
            while ((line = br.readLine()) != null) {
                if ("EXIT".equals(line)) {
                    return;
                }
                if (line.startsWith("<== PlayerInventory.GetPlayerCards")) {
                    playerCards = true;
                    continue;
                }

                if (json) {
                    jsonText += line;
                    if ("}".equals(line)) {
                        json = false;
                        if (playerCards) {
                            playerCards = false;
                            parsePlayerCards(jsonText);
                        } else {
                            parseJson(jsonText);
                        }
                    }
                    if ("{".equals(line) || "(-1) Incoming Event.MatchCreated {".equals(line)) {
                        System.err.println("e");
                    }
                } else {
                    if ("{".equals(line) || "(-1) Incoming Event.MatchCreated {".equals(line)) {
                        json = true;
                        jsonText = "{";
                    }
                }
            }
        }
    }

    private void parsePlayerCards(String jsonText) {
        JsonObject json = new Gson().fromJson(jsonText, JsonObject.class);
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            int cardId = Integer.parseInt(entry.getKey());
            if(cardLibrary.containsKey(cardId)) {
                int quantity = entry.getValue().getAsInt();
                playerInventory.cards.put(cardLibrary.get(cardId), quantity);
            }
        }
    }

    private void parseJson(String jsonText) {
        JsonObject json = new Gson().fromJson(jsonText, JsonObject.class);
        if (json == null) {
            return;
        }

        if (json.has("constructedMatchesWon")) {
            rankInfo.constructedMatchesWon = json.getAsJsonPrimitive("constructedMatchesWon").getAsInt();
        }
        if (json.has("constructedMatchesLost")) {
            rankInfo.constructedMatchesLost = json.getAsJsonPrimitive("constructedMatchesLost").getAsInt();
        }
        if (json.has("constructedMatchesDrawn")) {
            rankInfo.constructedMatchesDrawn = json.getAsJsonPrimitive("constructedMatchesDrawn").getAsInt();
        }
        if (json.has("limitedMatchesWon")) {
            rankInfo.limitedMatchesWon = json.getAsJsonPrimitive("limitedMatchesWon").getAsInt();
        }
        if (json.has("limitedMatchesLost")) {
            rankInfo.limitedMatchesLost = json.getAsJsonPrimitive("limitedMatchesLost").getAsInt();
        }
        if (json.has("limitedMatchesDrawn")) {
            rankInfo.limitedMatchesDrawn = json.getAsJsonPrimitive("limitedMatchesDrawn").getAsInt();
        }

        if (json.has("wcTrackPosition")) {
            playerInventory.wcTrackPosition = json.getAsJsonPrimitive("wcTrackPosition").getAsInt();
        }
        if (json.has("vaultProgress")) {
            playerInventory.vaultProgress = json.getAsJsonPrimitive("vaultProgress").getAsDouble();
        }

        if (json.has("InternalEventName")) {
            eventName = json.getAsJsonPrimitive("InternalEventName").getAsString();
            if (json.has("CourseDeck") && !json.get("CourseDeck").isJsonNull()) {
                JsonObject deck = json.getAsJsonObject("CourseDeck");
                deckId = deck.getAsJsonPrimitive("id").getAsString();
                deckName = deck.getAsJsonPrimitive("name").getAsString();
                deckFormat = deck.getAsJsonPrimitive("format").getAsString();
            }
        }

        if (json.has("opponentScreenName")) {
            opponentName = json.getAsJsonPrimitive("opponentScreenName").getAsString();
        }
        if (json.has("opponentRankingClass")) {
            opponentRankClass = json.getAsJsonPrimitive("opponentRankingClass").getAsString();
        }
        if (json.has("opponentRankingTier")) {
            opponentRankTier = json.getAsJsonPrimitive("opponentRankingTier").getAsInt();
        }
        if (json.has("opponentMythicPercentile")) {
            opponentRankPercentile = json.getAsJsonPrimitive("opponentMythicPercentile").getAsDouble();
        }

        if (json.has("params")) {
            JsonObject params = json.getAsJsonObject("params");
            if (params.has("payloadObject") && !params.get("payloadObject").isJsonNull()) {
                JsonObject payloadObject = params.getAsJsonObject("payloadObject");

                //Log.Info
                if (payloadObject.has("timestamp")) {
                    timestamp = payloadObject.getAsJsonPrimitive("timestamp").getAsString();
                }

                // DuelScene.GameStop
                if (payloadObject.has("winningTeamId")) {
                    teamId = payloadObject.getAsJsonPrimitive("teamId").getAsInt();
                    startingId = payloadObject.getAsJsonPrimitive("startingTeamId").getAsInt();
                    winningId = payloadObject.getAsJsonPrimitive("winningTeamId").getAsInt();
                    mulliganedCount = payloadObject.getAsJsonArray("mulliganedHands").size();
                    turnCount = payloadObject.getAsJsonPrimitive("turnCount").getAsInt();
                    secondsCount = payloadObject.getAsJsonPrimitive("secondsCount").getAsInt();
                }

                //"DuelScene.EndOfMatchReport"
                if (payloadObject.has("maxCreatures")) {
                    maxCreatures = payloadObject.getAsJsonPrimitive("maxCreatures").getAsInt();
                    maxLands = payloadObject.getAsJsonPrimitive("maxLands").getAsInt();
                    maxArtifactsAndEnchantments = payloadObject.getAsJsonPrimitive("maxArtifactsAndEnchantments").getAsInt();

                    int spellsCastWithAutoPayCount = payloadObject.getAsJsonPrimitive("spellsCastWithAutoPayCount").getAsInt();
                    int spellsCastWithManualManaCount = payloadObject.getAsJsonPrimitive("spellsCastWithManualManaCount").getAsInt();
                    int spellsCastWithMixedPayManaCount = payloadObject.getAsJsonPrimitive("spellsCastWithMixedPayManaCount").getAsInt();
                    spellCastCount = spellsCastWithAutoPayCount + spellsCastWithManualManaCount + spellsCastWithMixedPayManaCount;

                    records.put(timestamp, new Record(eventName, deckId, deckName, deckFormat,
                            opponentName, opponentRankClass, opponentRankTier, opponentRankPercentile,
                            timestamp, teamId == startingId, teamId == winningId, mulliganedCount, turnCount, secondsCount,
                            maxCreatures, maxLands, maxArtifactsAndEnchantments, spellCastCount));
                }
            }
        }
    }
}
