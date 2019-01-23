import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

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
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File("output.txt")))) {
                bw.write(e.getMessage());
            } catch (IOException e1) {
            }
            return;
        }

        try {
            program.output(new FileWriter(new File("output.txt")));
            program.saveRecord(new FileOutputStream(new File("data.txt")));
        } catch (Exception e) {
        }
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

            bw.write("VAULT: " + vaultProgress);
            bw.newLine();
            {
                bw.write("---CONSTRUCTED---");
                bw.newLine();
                bw.write("  W: " + constructedMatchesWon);
                bw.newLine();
                bw.write("  L: " + constructedMatchesLost);
                bw.newLine();
                //bw.write("\tD: " + program.constructedMatchesDrawn);
                //bw.newLine();
                double constructedWinRatio = 100.0 * constructedMatchesWon / (constructedMatchesWon + constructedMatchesLost);
                bw.write("  Winning Percentage: " + String.format("%.1f", constructedWinRatio) + "%");
                bw.newLine();
            }
            {
                bw.write("---LIMITED---");
                bw.newLine();
                bw.write("  W: " + limitedMatchesWon);
                bw.newLine();
                bw.write("  L: " + limitedMatchesLost);
                bw.newLine();
                //bw.write("\tD: " + program.limitedMatchesDrawn);
                //bw.newLine();
                double limitedWinRatio = 100.0 * limitedMatchesWon / (limitedMatchesWon + limitedMatchesLost);
                bw.write("  Winning Percentage: " + String.format("%.1f", limitedWinRatio) + "%");
                bw.newLine();
            }
            bw.newLine();
            bw.newLine();

            for (String event : log.stream().map(d -> d.getEventName()).distinct().collect(Collectors.toList())) {
                bw.write("---" + event + "---");
                bw.newLine();

                for (String deckId : log.stream().map(d -> d.getDeckId()).distinct().collect(Collectors.toList())) {
                    List<Record> records = log.stream().filter(d -> event.equals(d.getEventName()) && deckId.equals(d.getDeckId())).collect(Collectors.toList());
                    if (records.isEmpty()) {
                        continue;
                    }

                    String deckName = records.stream().sorted(Comparator.comparing(Record::getTimestamp).reversed()).findFirst().get().getDeckName();
                    bw.write(deckName);
                    bw.newLine();
                    {
                        long win = records.stream().filter(d -> d.getWin()).count();
                        long lose = records.stream().filter(d -> !d.getWin()).count();
                        double winRatio = 100.0 * win / (win + lose);
                        bw.write("  W: " + win);
                        bw.newLine();
                        bw.write("  L: " + lose);
                        bw.newLine();
                        //bw.write("\tD: " + (records.stream().count() - win - lose));
                        //bw.newLine();
                        bw.write("  Winning Percentage: " + String.format("%.1f", winRatio) + "%");
                        bw.newLine();
                    }
                    {
                        List<Record> playFirstRecords = records.stream().filter(d -> d.getPlayFirst()).collect(Collectors.toList());
                        long winF = playFirstRecords.stream().filter(d -> d.getWin()).count();
                        long loseF = playFirstRecords.stream().filter(d -> !d.getWin()).count();
                        double winRatioF = 100.0 * winF / (winF + loseF);
                        bw.write("  Winning Percentage(play first): " + String.format("%.1f", winRatioF) + "%");
                        bw.newLine();
                    }
                    {
                        List<Record> drawFirstRecords = records.stream().filter(d -> !d.getPlayFirst()).collect(Collectors.toList());
                        long winA = drawFirstRecords.stream().filter(d -> d.getWin()).count();
                        long loseA = drawFirstRecords.stream().filter(d -> !d.getWin()).count();
                        double winRatioA = 100.0 * winA / (winA + loseA);
                        bw.write("  Winning Percentage(draw first): " + String.format("%.1f", winRatioA) + "%");
                        bw.newLine();
                    }
                    {
                        List<Record> mythicRecords = records.stream().filter(d -> "Mythic".equals(d.getOpponentRankClass())).collect(Collectors.toList());
                        long win = mythicRecords.stream().filter(d -> d.getWin()).count();
                        long lose = mythicRecords.stream().filter(d -> !d.getWin()).count();
                        double winRatio = 100.0 * win / (win + lose);
                        bw.write("  Winning Percentage vs Mythic Rank: " + String.format("%.1f", winRatio) + "%");
                        bw.newLine();

                        List<Record> playFirstRecords = mythicRecords.stream().filter(d -> d.getPlayFirst()).collect(Collectors.toList());
                        long winF = playFirstRecords.stream().filter(d -> d.getWin()).count();
                        long loseF = playFirstRecords.stream().filter(d -> !d.getWin()).count();
                        double winRatioF = 100.0 * winF / (winF + loseF);
                        bw.write("  Winning Percentage vs Mythic Rank(play first): " + String.format("%.1f", winRatioF) + "%");
                        bw.newLine();

                        List<Record> drawFirstRecords = mythicRecords.stream().filter(d -> !d.getPlayFirst()).collect(Collectors.toList());
                        long winA = drawFirstRecords.stream().filter(d -> d.getWin()).count();
                        long loseA = drawFirstRecords.stream().filter(d -> !d.getWin()).count();
                        double winRatioA = 100.0 * winA / (winA + loseA);
                        bw.write("  Winning Percentage vs Mythic Rank(draw first): " + String.format("%.1f", winRatioA) + "%");
                        bw.newLine();
                    }
                    bw.newLine();

                    {
                        List<Record> mulliganRecords = records.stream().filter(d -> d.getMulliganedCount() != 0).collect(Collectors.toList());
                        double mulliganRatio = 100.0 * mulliganRecords.size() / records.size();
                        bw.write("  Mulligan Percentage: " + String.format("%.1f", mulliganRatio) + "%");
                        bw.newLine();
                        double mulliganCount = mulliganRecords.isEmpty() ? Double.NaN : mulliganRecords.stream().mapToInt(d -> d.getMulliganedCount()).average().getAsDouble();
                        bw.write("  Average Mulligan Count when to Mulligan: " + String.format("%.1f", mulliganCount));
                        bw.newLine();
                    }
                    {
                        double turn = records.stream().mapToInt(d -> d.getTurnCount()).average().getAsDouble();
                        bw.write("  Average Turn: " + String.format("%.1f", turn));
                        bw.newLine();
                        int duration = (int) records.stream().mapToInt(d -> d.getSecondsCount()).average().getAsDouble();
                        int hour = duration / 3600;
                        int minute = (duration % 3600) / 60;
                        int second = (duration % 3600) % 60;
                        bw.write("  Average Duration: " + String.format("%02d:%02d:%02d", hour, minute, second));
                        bw.newLine();

                        double land = records.stream().mapToInt(d -> d.getMaxLands()).average().getAsDouble();
                        bw.write("  Average Max Lands: " + String.format("%.1f", land));
                        bw.newLine();
                        double creature = records.stream().mapToInt(d -> d.getMaxCreatures()).average().getAsDouble();
                        bw.write("  Average Max Creatures: " + String.format("%.1f", creature));
                        bw.newLine();
                        //double permanent = records.stream().mapToInt(d -> d.getMaxArtifactsAndEnchantments()).average().getAsDouble(); //else creature
                        //bw.write("  Average Max Artifacts and Enchantments: " + String.format("%.1f", permanent));
                        //bw.newLine();
                        double cast = records.stream().mapToInt(d -> d.getSpellCastCount()).average().getAsDouble();
                        bw.write("  Average Spell Cast Count: " + String.format("%.1f", cast));
                        bw.newLine();
                    }
                    bw.newLine();

                    List<Record> winRecords = records.stream().filter(d -> d.getWin()).collect(Collectors.toList());
                    if (!winRecords.isEmpty()) {

                        double turn = winRecords.stream().mapToInt(d -> d.getTurnCount()).average().getAsDouble();
                        bw.write("  Average Turn(when you Win): " + String.format("%.1f", turn));
                        bw.newLine();
                        int duration = (int) winRecords.stream().mapToInt(d -> d.getSecondsCount()).average().getAsDouble();
                        int hour = duration / 3600;
                        int minute = (duration % 3600) / 60;
                        int second = (duration % 3600) % 60;
                        bw.write("  Average Duration(when you Win): " + String.format("%02d:%02d:%02d", hour, minute, second));
                        bw.newLine();

                        double land = winRecords.stream().mapToInt(d -> d.getMaxLands()).average().getAsDouble();
                        bw.write("  Average Max Lands(when you Win): " + String.format("%.1f", land));
                        bw.newLine();
                        double creature = winRecords.stream().mapToInt(d -> d.getMaxCreatures()).average().getAsDouble();
                        bw.write("  Average Max Creatures(when you Win): " + String.format("%.1f", creature));
                        bw.newLine();
                        //double permanent = winRecords.stream().mapToInt(d -> d.getMaxArtifactsAndEnchantments()).average().getAsDouble(); //else creature
                        //bw.write("  Average Max Artifacts and Enchantments(when you Win): " + String.format("%.1f", permanent));
                        //bw.newLine();
                        double cast = winRecords.stream().mapToInt(d -> d.getSpellCastCount()).average().getAsDouble();
                        bw.write("  Average Spell Cast Count(when you Win): " + String.format("%.1f", cast));
                        bw.newLine();
                        bw.newLine();
                    }

                    List<Record> loseRecords = records.stream().filter(d -> !d.getWin()).collect(Collectors.toList());
                    if (!loseRecords.isEmpty()) {
                        double turn = loseRecords.stream().mapToInt(d -> d.getTurnCount()).average().getAsDouble();
                        bw.write("  Average Turn(when you Lose): " + String.format("%.1f", turn));
                        bw.newLine();
                        int duration = (int) loseRecords.stream().mapToInt(d -> d.getSecondsCount()).average().getAsDouble();
                        int hour = duration / 3600;
                        int minute = (duration % 3600) / 60;
                        int second = (duration % 3600) % 60;
                        bw.write("  Average Duration(when you Lose): " + String.format("%02d:%02d:%02d", hour, minute, second));
                        bw.newLine();

                        double land = loseRecords.stream().mapToInt(d -> d.getMaxLands()).average().getAsDouble();
                        bw.write("  Average Max Lands(when you Lose): " + String.format("%.1f", land));
                        bw.newLine();
                        double creature = loseRecords.stream().mapToInt(d -> d.getMaxCreatures()).average().getAsDouble();
                        bw.write("  Average Max Creatures(when you Lose): " + String.format("%.1f", creature));
                        bw.newLine();
                        //double permanent = list.stream().mapToInt(d -> d.getMaxArtifactsAndEnchantments()).average().getAsDouble(); //else creature
                        //bw.write("  Average Max Artifacts and Enchantments(when you Lose): " + String.format("%.1f", permanent));
                        //bw.newLine();
                        double cast = loseRecords.stream().mapToInt(d -> d.getSpellCastCount()).average().getAsDouble();
                        bw.write("  Average Spell Cast Count(when you Lose): " + String.format("%.1f", cast));
                        bw.newLine();
                        bw.newLine();
                    }
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
