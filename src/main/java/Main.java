import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
    public static void main(String[] args) {
        Main program = new Main();

        try {
            String logPath;
            try (BufferedReader br = new BufferedReader(new FileReader(new File("config.txt")))) {
                logPath = br.readLine();
            }

            program.loadRecord(new FileInputStream(new File("data.txt")));
            program.input(new FileReader(logPath));

        } catch (Exception e) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File("output.html")))) {
                bw.write(e.getMessage());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return;
        }

        try {
            program.output(new FileWriter(new File("output.html")));
            program.saveRecord(new FileOutputStream(new File("data.txt")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private class Deck {
        private final String deckId;
        private final String deckName;
        private final double winrate;

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

    private static String h2(String title) {
        return "<h2>" + title + "</h2>";
    }

    private static String h3(String title) {
        return "<h3>" + title + "</h3>";
    }

    private static String h4(String title) {
        return "<h4>" + title + "</h4>";
    }

    private static List<String> td(int columns, int c, String... tds) {
        return IntStream.range(0, columns).mapToObj(j -> tds[c * columns + j]).collect(Collectors.toList());
    }

    private static List<String> tr(int columns, String... tds) {
        return IntStream.range(0, tds.length / columns).mapToObj(i -> "<td width='400px'>" + String.join("</td><td>", td(columns, i, tds)) + "<td>").collect(Collectors.toList());
    }

    private static String table(int columns, String... tds) {
        return "<table><tr>" + String.join("</tr><tr>", tr(columns, tds)) + "</tr></table>";
    }

    //player info
    private int constructedMatchesWon = 0;
    private int constructedMatchesLost = 0;
    private int constructedMatchesDrawn = 0;
    private int limitedMatchesWon = 0;
    private int limitedMatchesLost = 0;
    private int limitedMatchesDrawn = 0;
    //player inventory info
    private double vaultProgress = 0;

    private List<Record> log = new ArrayList<>();

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

    private void addRecord(Record record) {
        log.removeAll(log.stream().filter(d -> record.getTimestamp().equals(d.getTimestamp())).collect(Collectors.toList()));
        log.add(record);
    }

    public void input(Reader in) throws IOException {
        boolean json = false;
        String jsonText = "";

        try (BufferedReader br = new BufferedReader(in)) {
            String line;
            while ((line = br.readLine()) != null) {
                if ("EXIT".equals(line)) {
                    return;
                }

                if (json) {
                    jsonText += line;
                    if ("}".equals(line)) {
                        json = false;
                        convertJson(jsonText);
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

    private void convertJson(String jsonText) {
        JsonObject json = new Gson().fromJson(jsonText, JsonObject.class);
        if (json == null) {
            return;
        }

        if (json.has("constructedMatchesWon")) {
            constructedMatchesWon = json.getAsJsonPrimitive("constructedMatchesWon").getAsInt();
        }
        if (json.has("constructedMatchesLost")) {
            constructedMatchesLost = json.getAsJsonPrimitive("constructedMatchesLost").getAsInt();
        }
        if (json.has("constructedMatchesDrawn")) {
            constructedMatchesDrawn = json.getAsJsonPrimitive("constructedMatchesDrawn").getAsInt();
        }
        if (json.has("limitedMatchesWon")) {
            limitedMatchesWon = json.getAsJsonPrimitive("limitedMatchesWon").getAsInt();
        }
        if (json.has("limitedMatchesLost")) {
            limitedMatchesLost = json.getAsJsonPrimitive("limitedMatchesLost").getAsInt();
        }
        if (json.has("limitedMatchesDrawn")) {
            limitedMatchesDrawn = json.getAsJsonPrimitive("limitedMatchesDrawn").getAsInt();
        }

        if (json.has("vaultProgress")) {
            vaultProgress = json.getAsJsonPrimitive("vaultProgress").getAsDouble();
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

                    addRecord(new Record(eventName, deckId, deckName, deckFormat,
                            opponentName, opponentRankClass, opponentRankTier, opponentRankPercentile,
                            timestamp, teamId == startingId, teamId == winningId, mulliganedCount, turnCount, secondsCount,
                            maxCreatures, maxLands, maxArtifactsAndEnchantments, spellCastCount));
                }
            }
        }
    }

    public void output(Writer out) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(out)) {
            bw.write(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));
            bw.newLine();

            bw.write(h2("User Info"));
            bw.newLine();
            bw.write("VAULT: " + vaultProgress + "%");
            bw.newLine();

            {
                bw.write(h3("---CONSTRUCTED---"));

                double constructedWinRatio = 100.0 * constructedMatchesWon / (constructedMatchesWon + constructedMatchesLost);
                bw.write(table(2,
                        "WIN:", Integer.toString(constructedMatchesWon),
                        "LOSE:", Integer.toString(constructedMatchesLost),
                        // "DRAW:", Integer.toString(constructedMatchesDrawn)
                        "Winning Percentage:", String.format("%.1f", constructedWinRatio) + "%"
                ));
                bw.newLine();
            }
            {
                bw.write(h3("---LIMITED---"));
                double limitedWinRatio = 100.0 * limitedMatchesWon / (limitedMatchesWon + limitedMatchesLost);
                bw.write(table(2,
                        "WIN:", Integer.toString(limitedMatchesWon),
                        "LOSE:", Integer.toString(limitedMatchesLost),
                        // "DRAW:", Integer.toString(limitedMatchesDrawn)
                        "Winning Percentage:", String.format("%.1f", limitedWinRatio) + "%"
                ));
                bw.newLine();
            }

            List<Deck> deckList = log.stream().map(d -> d.getDeckId()).distinct()
                    .map(deckId -> {
                        List<Record> deckRecord = log.stream().filter(d -> deckId.equals(d.getDeckId())).collect(Collectors.toList());
                        String deckName = deckRecord.stream().sorted(Comparator.comparing(Record::getTimestamp).reversed()).findFirst().get().getDeckName(); //最新のデッキ名
                        long win = deckRecord.stream().filter(d -> d.getWin()).count();
                        long lose = deckRecord.stream().filter(d -> !d.getWin()).count();

                        return new Deck(deckId, deckName, win, lose);
                    }).sorted(Comparator.comparing(Deck::getWinrate).reversed()).collect(Collectors.toList()); //全体の勝率順

            bw.write(h2("Deck Info"));
            bw.newLine();
            for (String event : log.stream().map(d -> d.getEventName()).distinct().collect(Collectors.toList())) {
                bw.write(h3("---" + event + "---"));
                bw.newLine();

                List<Record> eventRecords = log.stream().filter(d -> event.equals(d.getEventName())).collect(Collectors.toList());
                for (Deck deck : deckList) {
                    List<Record> records = eventRecords.stream().filter(d -> deck.getDeckId().equals(d.getDeckId())).collect(Collectors.toList());
                    if (records.isEmpty()) {
                        continue;
                    }

                    bw.write(h4(deck.getDeckName()));
                    bw.newLine();
                    {
                        long win = records.stream().filter(d -> d.getWin()).count();
                        long lose = records.stream().filter(d -> !d.getWin()).count();
                        double winRatio = 100.0 * win / (win + lose);

                        List<Record> playFirstRecords = records.stream().filter(d -> d.getPlayFirst()).collect(Collectors.toList());
                        long winF = playFirstRecords.stream().filter(d -> d.getWin()).count();
                        long loseF = playFirstRecords.stream().filter(d -> !d.getWin()).count();
                        double winRatioF = 100.0 * winF / (winF + loseF);

                        List<Record> drawFirstRecords = records.stream().filter(d -> !d.getPlayFirst()).collect(Collectors.toList());
                        long winA = drawFirstRecords.stream().filter(d -> d.getWin()).count();
                        long loseA = drawFirstRecords.stream().filter(d -> !d.getWin()).count();
                        double winRatioA = 100.0 * winA / (winA + loseA);

                        List<Record> mythicRecords = records.stream().filter(d -> "Mythic".equals(d.getOpponentRankClass())).collect(Collectors.toList());
                        long winM = mythicRecords.stream().filter(d -> d.getWin()).count();
                        long loseM = mythicRecords.stream().filter(d -> !d.getWin()).count();
                        double winRatioM = 100.0 * winM / (winM + loseM);

                        List<Record> mythicPlayFirstRecords = mythicRecords.stream().filter(d -> d.getPlayFirst()).collect(Collectors.toList());
                        long winMF = mythicPlayFirstRecords.stream().filter(d -> d.getWin()).count();
                        long loseMF = mythicPlayFirstRecords.stream().filter(d -> !d.getWin()).count();
                        double winRatioMF = 100.0 * winMF / (winMF + loseMF);

                        List<Record> mythicDrawFirstRecords = mythicRecords.stream().filter(d -> !d.getPlayFirst()).collect(Collectors.toList());
                        long winMA = mythicDrawFirstRecords.stream().filter(d -> d.getWin()).count();
                        long loseMA = mythicDrawFirstRecords.stream().filter(d -> !d.getWin()).count();
                        double winRatioMA = 100.0 * winMA / (winMA + loseMA);

                        bw.write(table(2,
                                "WIN:", Long.toString(win),
                                "LOSE:", Long.toString(lose),
                                // "DRAW:", Long.toString((records.stream().count() - win - lose))
                                "Winning Percentage:", String.format("%.1f", winRatio) + "%",
                                "Winning Percentage(play first):", String.format("%.1f", winRatioF) + "%",
                                "Winning Percentage(draw first):", String.format("%.1f", winRatioA) + "%",
                                "Winning Percentage vs Mythic Rank:", String.format("%.1f", winRatioM) + "%",
                                "Winning Percentage vs Mythic Rank(play first):", String.format("%.1f", winRatioMF) + "%",
                                "Winning Percentage vs Mythic Rank(draw first):", String.format("%.1f", winRatioMA) + "%"
                        ));
                        bw.newLine();
                    }
                    bw.write("<details>");
                    {
                        List<Record> mulliganRecords = records.stream().filter(d -> d.getMulliganedCount() != 0).collect(Collectors.toList());
                        double mulliganRatio = 100.0 * mulliganRecords.size() / records.size();
                        double mulliganCount = mulliganRecords.isEmpty() ? Double.NaN : mulliganRecords.stream().mapToInt(d -> d.getMulliganedCount()).average().getAsDouble();

                        bw.write(table(2,
                                "Mulligan Percentage:", String.format("%.1f", mulliganRatio) + "%",
                                "Average Mulligan Count when to Mulligan:", String.format("%.1f", mulliganCount)
                        ));
                        bw.newLine();
                    }
                    {
                        double turn = records.stream().mapToInt(d -> d.getTurnCount()).average().getAsDouble();

                        int duration = (int) records.stream().mapToInt(d -> d.getSecondsCount()).average().getAsDouble();
                        int hour = duration / 3600;
                        int minute = (duration % 3600) / 60;
                        int second = (duration % 3600) % 60;

                        List<Integer> lands = records.stream().mapToInt(d -> d.getMaxLands()).sorted().boxed().collect(Collectors.toList());
                        List<Integer> creatures = records.stream().mapToInt(d -> d.getMaxCreatures()).sorted().boxed().collect(Collectors.toList());
                        //List<Integer> permanents = records.stream().mapToInt(d -> d.getMaxArtifactsAndEnchantments()).sorted().boxed().collect(Collectors.toList());
                        List<Integer> casts = records.stream().mapToInt(d -> d.getSpellCastCount()).sorted().boxed().collect(Collectors.toList());

                        bw.write(table(2,
                                "Average Turn:", String.format("%.1f", turn),
                                "Average Duration:", String.format("%02d:%02d:%02d", hour, minute, second),

                                "Average Max Lands:", String.format("%.1f", lands.stream().mapToInt(d -> d).average().getAsDouble()),
                                "Average Max Creatures:", String.format("%.1f", creatures.stream().mapToInt(d -> d).average().getAsDouble()),
                                //"Average Max Artifacts and Enchantments:", String.format("%.1f", permanent),
                                "Average Spell Cast Count:", String.format("%.1f", casts.stream().mapToInt(d -> d).average().getAsDouble()),

                                "Median Max Lands:", Integer.toString(lands.get(lands.size() / 2)),
                                "Median Max Creatures:", Integer.toString(creatures.get(creatures.size() / 2)),
                                //"Median Max Artifacts and Enchantments:", String.format("%.1f", permanent),
                                "Median Spell Cast Count:", Integer.toString(casts.get(casts.size() / 2))
                        ));
                        bw.newLine();
                    }

                    List<Record> winRecords = records.stream().filter(d -> d.getWin()).collect(Collectors.toList());
                    if (!winRecords.isEmpty()) {
                        double turn = winRecords.stream().mapToInt(d -> d.getTurnCount()).average().getAsDouble();

                        int duration = (int) winRecords.stream().mapToInt(d -> d.getSecondsCount()).average().getAsDouble();
                        int hour = duration / 3600;
                        int minute = (duration % 3600) / 60;
                        int second = (duration % 3600) % 60;

                        List<Integer> lands = winRecords.stream().mapToInt(d -> d.getMaxLands()).sorted().boxed().collect(Collectors.toList());
                        List<Integer> creatures = winRecords.stream().mapToInt(d -> d.getMaxCreatures()).sorted().boxed().collect(Collectors.toList());
                        //List<Integer> permanents = winRecords.stream().mapToInt(d -> d.getMaxArtifactsAndEnchantments()).sorted().boxed().collect(Collectors.toList());
                        List<Integer> casts = winRecords.stream().mapToInt(d -> d.getSpellCastCount()).sorted().boxed().collect(Collectors.toList());

                        bw.write(table(2,
                                "Average Turn(When you win):", String.format("%.1f", turn),
                                "Average Duration(When you win):", String.format("%02d:%02d:%02d", hour, minute, second),

                                "Average Max Lands(When you win):", String.format("%.1f", lands.stream().mapToInt(d -> d).average().getAsDouble()),
                                "Average Max Creatures(When you win):", String.format("%.1f", creatures.stream().mapToInt(d -> d).average().getAsDouble()),
                                //"Average Max Artifacts and Enchantments(When you win):", String.format("%.1f", permanent),
                                "Average Spell Cast Count(When you win):", String.format("%.1f", casts.stream().mapToInt(d -> d).average().getAsDouble()),

                                "Median Max Lands(When you win):", Integer.toString(lands.get(lands.size() / 2)),
                                "Median Max Creatures(When you win):", Integer.toString(creatures.get(creatures.size() / 2)),
                                //"Median Max Artifacts and Enchantments(When you win):", String.format("%.1f", permanent),
                                "Median Spell Cast Count(When you win):", Integer.toString(casts.get(casts.size() / 2))
                        ));
                        bw.newLine();
                    }

                    List<Record> loseRecords = records.stream().filter(d -> !d.getWin()).collect(Collectors.toList());
                    if (!loseRecords.isEmpty()) {
                        double turn = loseRecords.stream().mapToInt(d -> d.getTurnCount()).average().getAsDouble();

                        int duration = (int) loseRecords.stream().mapToInt(d -> d.getSecondsCount()).average().getAsDouble();
                        int hour = duration / 3600;
                        int minute = (duration % 3600) / 60;
                        int second = (duration % 3600) % 60;

                        List<Integer> lands = loseRecords.stream().mapToInt(d -> d.getMaxLands()).sorted().boxed().collect(Collectors.toList());
                        List<Integer> creatures = loseRecords.stream().mapToInt(d -> d.getMaxCreatures()).sorted().boxed().collect(Collectors.toList());
                        //List<Integer> permanents = loseRecords.stream().mapToInt(d -> d.getMaxArtifactsAndEnchantments()).sorted().boxed().collect(Collectors.toList());
                        List<Integer> casts = loseRecords.stream().mapToInt(d -> d.getSpellCastCount()).sorted().boxed().collect(Collectors.toList());

                        bw.write(table(2,
                                "Average Turn(When you lose):", String.format("%.1f", turn),
                                "Average Duration(When you lose):", String.format("%02d:%02d:%02d", hour, minute, second),

                                "Average Max Lands(When you lose):", String.format("%.1f", lands.stream().mapToInt(d -> d).average().getAsDouble()),
                                "Average Max Creatures(When you lose):", String.format("%.1f", creatures.stream().mapToInt(d -> d).average().getAsDouble()),
                                //"Average Max Artifacts and Enchantments(When you lose):", String.format("%.1f", permanent),
                                "Average Spell Cast Count(When you lose):", String.format("%.1f", casts.stream().mapToInt(d -> d).average().getAsDouble()),

                                "Median Max Lands(When you lose):", Integer.toString(lands.get(lands.size() / 2)),
                                "Median Max Creatures(When you lose):", Integer.toString(creatures.get(creatures.size() / 2)),
                                //"Median Max Artifacts and Enchantments(When you lose):", String.format("%.1f", permanent),
                                "Median Spell Cast Count(When you lose):", Integer.toString(casts.get(casts.size() / 2))
                        ));
                        bw.newLine();
                    }
                    bw.write("</details>");
                    bw.newLine();
                }
                bw.newLine();
            }
        }
    }

    public void loadRecord(InputStream in) throws IOException, ClassNotFoundException {
        try (ObjectInputStream is = new ObjectInputStream(in)) {
            log = (List<Record>) is.readObject();
        }
    }

    public void saveRecord(OutputStream out) throws IOException {
        try (ObjectOutputStream os = new ObjectOutputStream(out)) {
            os.writeObject(log);
        }
    }
}
