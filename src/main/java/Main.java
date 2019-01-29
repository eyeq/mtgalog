import javax.swing.*;
import java.io.*;
import java.nio.Buffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
    public static void main(String[] args) throws IOException {
        final JDialog dialog = new JDialog();
        try {
            dialog.setAlwaysOnTop(true);

            String target;
            boolean isDialog = false;
            try (BufferedReader br = Files.newBufferedReader(Paths.get("properties.txt"), StandardCharsets.UTF_8)) {
                Properties properties = new Properties();
                properties.load(br);

                target = properties.getProperty("target");
                isDialog = Boolean.parseBoolean(properties.getProperty("dialog"));
            } catch (Exception e) {
                try (BufferedWriter bw = Files.newBufferedWriter(Paths.get("output.html"), StandardCharsets.UTF_8)) {
                    log(bw, e);
                }
                if (isDialog) {
                    JOptionPane.showMessageDialog(dialog, "error has occurred.", "mtgalog", JOptionPane.ERROR_MESSAGE);
                }
                return;
            }
            if (target == null) {
                if (isDialog) {
                    JOptionPane.showMessageDialog(dialog, "could not found target.", "mtgalog", JOptionPane.ERROR_MESSAGE);
                }
                return;
            }

            try (FileChannel fc = FileChannel.open(Paths.get("mtgalog.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                 FileLock lock = fc.tryLock()) {
                if (lock == null) {
                    if (isDialog) {
                        JOptionPane.showMessageDialog(dialog, "has already been launched.", "mtgalog", JOptionPane.WARNING_MESSAGE);
                    }
                    return;
                }

                Parser parser = new Parser();
                try {
                    parser.loadRecord(Files.newInputStream(Paths.get("data.txt")));
                    parser.loadCardLibrary(new FileReader("cardlist.json"));

                    parser.readLog(new FileReader(target));
                } catch (Exception e) {
                    try (BufferedWriter bw = Files.newBufferedWriter(Paths.get("output.html"), StandardCharsets.UTF_8)) {
                        log(bw, e);
                    }
                    if (isDialog) {
                        JOptionPane.showMessageDialog(dialog, "error has occurred.", "mtgalog", JOptionPane.ERROR_MESSAGE);
                    }
                    return;
                }

                try (BufferedWriter bw = Files.newBufferedWriter(Paths.get("output.html"), StandardCharsets.UTF_8)) {
                    output(bw, parser);
                    parser.saveRecord(new FileOutputStream(new File("data.txt")));
                } catch (Exception e) {
                    try (BufferedWriter bw = Files.newBufferedWriter(Paths.get("output.html"), StandardCharsets.UTF_8)) {
                        log(bw, e);
                    }
                    if (isDialog) {
                        JOptionPane.showMessageDialog(dialog, "error has occurred.", "mtgalog", JOptionPane.ERROR_MESSAGE);
                    }
                    return;
                }

                if (isDialog) {
                    JOptionPane.showMessageDialog(dialog, "process has completed.", "mtgalog", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } finally {
            dialog.dispose();
        }
    }


    private static String h2(String title) {
        return "<h2>" + title + "</h2>";
    }

    private static String h3(String title) {
        return "<h3>" + title + "</h3>";
    }

    private static String h4(String title) {
        return "<h4 style='margin-bottom: 0;'>" + title + "</h4>";
    }

    private static List<String> td(int columns, int c, String... tds) {
        return IntStream.range(0, columns).mapToObj(j -> tds[c * columns + j]).collect(Collectors.toList());
    }

    private static List<String> tr(int columns, String... tds) {
        return IntStream.range(0, tds.length / columns).mapToObj(i -> "<td>" + String.join("</td><td>", td(columns, i, tds)) + "</td>").collect(Collectors.toList());
    }

    private static String table(int columns, String col,  String... tds) {
        String[] t = Arrays.stream(tds).map(d -> "<u>" + ("NaN%".equals(d) ? "--.-%" : d) + "</u>").toArray(String[]::new);
        return "<table>" + col + "<tr>" + String.join("</tr><tr>", tr(columns, t)) + "</tr></table>";
    }

    private static void log(BufferedWriter bw, Exception e) throws IOException {
        bw.write(e.getMessage());
    }

    private static void output(BufferedWriter bw, Parser parser) throws IOException {
        List<Record> allRecords = parser.getRecords();
        Parser.RankInfo rankInfo = parser.getRankInfo();
        Parser.PlayerInventory inventory = parser.getPlayerInventory();

        bw.write(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        bw.newLine();

        bw.write(h2("Player Info"));
        bw.newLine();

        bw.write(h3("Inventory"));
        List<Map.Entry<Parser.Card, Integer>> collectible = inventory.cards.entrySet().stream().filter(d -> d.getKey().collectible).collect(Collectors.toList());
        {
            int nextM = 17 - inventory.wcTrackPosition;
            if (nextM <= 0) {
                nextM += 30;
            }
            int nextR = 6 - ((inventory.wcTrackPosition + 1) % 6);
            if (nextR == nextM) {
                nextR += 6;
            }

            int collectedCardCount = collectible.stream().mapToInt(d -> d.getValue()).sum();
            double p = 100.0 * collectedCardCount / (collectible.size() * 4);
            bw.write(table(3,
                    "<col width='420'><col width='200'><col width='200'>",
                    "", "<b>Retention</b>", "<b>(Change)</b>",
                    "GOLD:", Integer.toString(inventory.gold), String.format("(%+d gold)", parser.getDiffGold()),
                    "GEMS:", Integer.toString(inventory.gems), String.format("(%+d gems)", parser.getDiffGems()),
                    "VAULT:", String.format("%3.1f%%", inventory.vaultProgress), String.format("(%+3.1f%%)", parser.getDiffVaultProgress()),
                    "NEXT WC Rare:", Integer.toString(nextR), "",
                    "NEXT WC Mythic Rare:", Integer.toString(nextM), "",
                    "Card Variety:", collectible.stream().filter(d -> d.getValue() != 0).count() + " / " + collectible.size(), "",
                    "Card Collection:", String.format("%3.1f%%", p), String.format("(%+d card)", (collectedCardCount - parser.getPreCollectedCardCount()))
            ));
            bw.newLine();
        }
        bw.write("<details><div style='padding-left: 40px;'>");
        bw.newLine();
        {
            for (String set : new String[]{"Ixalan", "Dominaria", "Magic 2019", "M19 Gift Pack", "Guilds of Ravnica", "Ravnica Allegiance", "Mythic Edition"}) {
                bw.write(h4(set));

                List<Map.Entry<Parser.Card, Integer>> setList = collectible.stream().filter(d -> set.equals(d.getKey().set)).collect(Collectors.toList());
                List<Map.Entry<Parser.Card, Integer>> common = setList.stream().filter(d -> "common".equals(d.getKey().rarity)).collect(Collectors.toList());
                List<Map.Entry<Parser.Card, Integer>> uncommon = setList.stream().filter(d -> "uncommon".equals(d.getKey().rarity)).collect(Collectors.toList());
                List<Map.Entry<Parser.Card, Integer>> rare = setList.stream().filter(d -> "rare".equals(d.getKey().rarity)).collect(Collectors.toList());
                List<Map.Entry<Parser.Card, Integer>> mythic = setList.stream().filter(d -> "mythic".equals(d.getKey().rarity)).collect(Collectors.toList());
                double setP = 100.0 * setList.stream().mapToInt(d -> d.getValue()).sum() / (setList.size() * 4);
                double commonP = 100.0 * common.stream().mapToInt(d -> d.getValue()).sum() / (common.size() * 4);
                double uncommonP = 100.0 * uncommon.stream().mapToInt(d -> d.getValue()).sum() / (uncommon.size() * 4);
                double rareP = 100.0 * rare.stream().mapToInt(d -> d.getValue()).sum() / (rare.size() * 4);
                double mythicP = 100.0 * mythic.stream().mapToInt(d -> d.getValue()).sum() / (mythic.size() * 4);
                bw.write(table(2,
                        "<col width='380'><col width='200'>",
                        "Card Variety:", setList.stream().filter(d -> d.getValue() != 0).count() + " / " + setList.size(),
                        "Card Collection:", String.format("%3.1f%%", setP),
                        "Common Card Collection:", String.format("%3.1f%%", commonP),
                        "Uncommon Card Collection:", String.format("%3.1f%%", uncommonP),
                        "Rare Card Collection:", String.format("%3.1f%%", rareP),
                        "Mythic Rare Card Collection:", String.format("%3.1f%%", mythicP)
                ));
            }
        }
        bw.write("</div></details>");
        bw.newLine();

        {
            bw.write(h3("---CONSTRUCTED---"));

            double constructedWinRatio = 100.0 * rankInfo.constructedMatchesWon / (rankInfo.constructedMatchesWon + rankInfo.constructedMatchesLost);
            bw.write(table(2,
                    "<col width='420'><col width='200'>",
                    "WIN:", Integer.toString(rankInfo.constructedMatchesWon),
                    "LOSE:", Integer.toString(rankInfo.constructedMatchesLost),
                    // "DRAW:", Integer.toString(constructedMatchesDrawn)
                    "Winning Percentage:", String.format("%3.1f%%", constructedWinRatio)
            ));
            bw.newLine();
        }
        {
            bw.write(h3("---LIMITED---"));
            double limitedWinRatio = 100.0 * rankInfo.limitedMatchesWon / (rankInfo.limitedMatchesWon + rankInfo.limitedMatchesLost);
            bw.write(table(2,
                    "<col width='420'><col width='200'>",
                    "WIN:", Integer.toString(rankInfo.limitedMatchesWon),
                    "LOSE:", Integer.toString(rankInfo.limitedMatchesLost),
                    // "DRAW:", Integer.toString(limitedMatchesDrawn)
                    "Winning Percentage:", String.format("%3.1f%%", limitedWinRatio)
            ));
            bw.newLine();
        }

        List<Parser.Deck> deckList = allRecords.stream().map(d -> d.getDeckId()).distinct()
                .map(deckId -> {
                    List<Record> deckRecord = allRecords.stream().filter(d -> deckId.equals(d.getDeckId())).collect(Collectors.toList());
                    String deckName = deckRecord.stream().sorted(Comparator.comparing(Record::getTimestamp).reversed()).findFirst().get().getDeckName(); //最新のデッキ名
                    long win = deckRecord.stream().filter(d -> d.getWin()).count();
                    long lose = deckRecord.stream().filter(d -> !d.getWin()).count();

                    return new Parser.Deck(deckId, deckName, win, lose);
                }).sorted(Comparator.comparing(Parser.Deck::getWinrate).reversed()).collect(Collectors.toList()); //全体の勝率順

        bw.write(h2("Deck Info"));
        bw.newLine();
        for (String event : allRecords.stream().map(d -> d.getEventName()).distinct().collect(Collectors.toList())) {
            bw.write(h3("---" + event + "---"));
            bw.newLine();
            bw.write("<details><div style='padding-left: 40px;'>");
            bw.newLine();

            List<Record> eventRecords = allRecords.stream().filter(d -> event.equals(d.getEventName())).collect(Collectors.toList());
            for (Parser.Deck deck : deckList) {
                List<Record> records = eventRecords.stream().filter(d -> deck.getDeckId().equals(d.getDeckId())).collect(Collectors.toList());
                if (records.isEmpty()) {
                    continue;
                }

                bw.write(h4(deck.getDeckName()));
                bw.newLine();
                {
                    //Total
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

                    //Mythic
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

                    bw.write(table(3,
                            "<col width='380'><col width='100'><col width='100'>",
                            "", "<b>Total</b>", "<b>vsMythic</b>",
                            "WIN:", Long.toString(win), Long.toString(winM),
                            "LOSE:", Long.toString(lose), Long.toString(loseM),
                            // "DRAW:", Long.toString((records.stream().count() - win - lose))
                            "Winning Percentage:", String.format("%3.1f%%", winRatio), String.format("%3.1f%%", winRatioM),
                            "Winning Percentage(play first):", String.format("%3.1f%%", winRatioF), String.format("%3.1f%%", winRatioMF),
                            "Winning Percentage(draw first):", String.format("%3.1f%%", winRatioA), String.format("%3.1f%%", winRatioMA)
                    ));
                    bw.newLine();
                }

                bw.write("<details><div style='padding-left: 40px;'>");
                {
                    //Total
                    List<Record> mulliganRecords = records.stream().filter(d -> d.getMulliganedCount() != 0).collect(Collectors.toList());
                    double mulliganRatio = 100.0 * mulliganRecords.size() / records.size();
                    double mulliganCount = mulliganRecords.isEmpty() ? Double.NaN : mulliganRecords.stream().mapToInt(d -> d.getMulliganedCount()).average().getAsDouble();

                    double turn = records.stream().mapToInt(d -> d.getTurnCount()).average().getAsDouble();

                    int duration = (int) records.stream().mapToInt(d -> d.getSecondsCount()).average().getAsDouble();
                    int hour = duration / 3600;
                    int minute = (duration % 3600) / 60;
                    int second = (duration % 3600) % 60;

                    List<Integer> lands = records.stream().mapToInt(d -> d.getMaxLands()).sorted().boxed().collect(Collectors.toList());
                    List<Integer> creatures = records.stream().mapToInt(d -> d.getMaxCreatures()).sorted().boxed().collect(Collectors.toList());
                    List<Integer> permanents = records.stream().mapToInt(d -> d.getMaxArtifactsAndEnchantments()).sorted().boxed().collect(Collectors.toList());
                    List<Integer> casts = records.stream().mapToInt(d -> d.getSpellCastCount()).sorted().boxed().collect(Collectors.toList());

                    //Win
                    double turnW = 0;
                    int durationW = 0;
                    int hourW = 0;
                    int minuteW = 0;
                    int secondW = 0;
                    List<Integer> landsW = Arrays.asList(0);
                    List<Integer> creaturesW = Arrays.asList(0);
                    List<Integer> permanentsW = Arrays.asList(0);
                    List<Integer> castsW = Arrays.asList(0);

                    List<Record> winRecords = records.stream().filter(d -> d.getWin()).collect(Collectors.toList());
                    if(!winRecords.isEmpty()) {
                        turnW = winRecords.stream().mapToInt(d -> d.getTurnCount()).average().getAsDouble();

                        durationW = (int) winRecords.stream().mapToInt(d -> d.getSecondsCount()).average().getAsDouble();
                        hourW = durationW / 3600;
                        minuteW = (durationW % 3600) / 60;
                        secondW = (durationW % 3600) % 60;

                        landsW = winRecords.stream().mapToInt(d -> d.getMaxLands()).sorted().boxed().collect(Collectors.toList());
                        creaturesW = winRecords.stream().mapToInt(d -> d.getMaxCreatures()).sorted().boxed().collect(Collectors.toList());
                        permanentsW = winRecords.stream().mapToInt(d -> d.getMaxArtifactsAndEnchantments()).sorted().boxed().collect(Collectors.toList());
                        castsW = winRecords.stream().mapToInt(d -> d.getSpellCastCount()).sorted().boxed().collect(Collectors.toList());
                    }

                    //Lose
                    double turnL = 0;
                    int durationL = 0;
                    int hourL = 0;
                    int minuteL = 0;
                    int secondL = 0;
                    List<Integer> landsL = Arrays.asList(0);
                    List<Integer> creaturesL = Arrays.asList(0);
                    List<Integer> permanentsL = Arrays.asList(0);
                    List<Integer> castsL = Arrays.asList(0);

                    List<Record> loseRecords = records.stream().filter(d -> !d.getWin()).collect(Collectors.toList());
                    if(!loseRecords.isEmpty()) {
                        turnL = loseRecords.stream().mapToInt(d -> d.getTurnCount()).average().getAsDouble();

                        durationL = (int) loseRecords.stream().mapToInt(d -> d.getSecondsCount()).average().getAsDouble();
                        hourL = durationL / 3600;
                        minuteL = (durationL % 3600) / 60;
                        secondL = (durationL % 3600) % 60;

                        landsL = loseRecords.stream().mapToInt(d -> d.getMaxLands()).sorted().boxed().collect(Collectors.toList());
                        creaturesL = loseRecords.stream().mapToInt(d -> d.getMaxCreatures()).sorted().boxed().collect(Collectors.toList());
                        permanentsL = loseRecords.stream().mapToInt(d -> d.getMaxArtifactsAndEnchantments()).sorted().boxed().collect(Collectors.toList());
                        castsL = loseRecords.stream().mapToInt(d -> d.getSpellCastCount()).sorted().boxed().collect(Collectors.toList());
                    }

                    bw.write(table(4,
                            "<col width='340'><col width='100'><col width='100'><col width='100'>",
                            "", "<b>Total</b>", "<b>withWin</b>", "<b>withLose</b>",
                            "Mulligan Percentage:",
                                String.format("%3.1f%%", mulliganRatio), "", "",
                            "Average Mulligan Count when to Mulligan:",
                                String.format("%3.1f", mulliganCount), "", "",

                            "Average Turn:",
                                String.format("%3.1f", turn),
                                String.format("%3.1f", turnW),
                                String.format("%3.1f", turnL),
                            "Average Duration:",
                                String.format("%02d:%02d:%02d", hour, minute, second),
                                String.format("%02d:%02d:%02d", hourW, minuteW, secondW),
                                String.format("%02d:%02d:%02d", hourL, minuteL, secondL),

                            "Average Max Lands:",
                                String.format("%3.1f", lands.stream().mapToInt(d -> d).average().getAsDouble()),
                                String.format("%3.1f", landsW.stream().mapToInt(d -> d).average().getAsDouble()),
                                String.format("%3.1f", landsL.stream().mapToInt(d -> d).average().getAsDouble()),
                            "Average Max Creatures:",
                                String.format("%3.1f", creatures.stream().mapToInt(d -> d).average().getAsDouble()),
                                String.format("%3.1f", creaturesW.stream().mapToInt(d -> d).average().getAsDouble()),
                                String.format("%3.1f", creaturesL.stream().mapToInt(d -> d).average().getAsDouble()),
                            "Average Max Artifacts and Enchantments:",
                                String.format("%3.1f", permanents.stream().mapToInt(d -> d).average().getAsDouble()),
                                String.format("%3.1f", permanentsW.stream().mapToInt(d -> d).average().getAsDouble()),
                                String.format("%3.1f", permanentsL.stream().mapToInt(d -> d).average().getAsDouble()),
                            "Average Spell Cast Count:",
                                String.format("%3.1f", casts.stream().mapToInt(d -> d).average().getAsDouble()),
                                String.format("%3.1f", castsW.stream().mapToInt(d -> d).average().getAsDouble()),
                                String.format("%3.1f", castsL.stream().mapToInt(d -> d).average().getAsDouble()),

                            "Median Max Lands:",
                                Integer.toString(lands.get(lands.size() / 2)),
                                Integer.toString(landsW.get(landsW.size() / 2)),
                                Integer.toString(landsL.get(landsL.size() / 2)),
                            "Median Max Creatures:",
                                Integer.toString(creatures.get(creatures.size() / 2)),
                                Integer.toString(creaturesW.get(creaturesW.size() / 2)),
                                Integer.toString(creaturesL.get(creaturesL.size() / 2)),
                            "Median Max Artifacts and Enchantments:",
                                Integer.toString(permanents.get(permanents.size() / 2)),
                                Integer.toString(permanentsW.get(permanentsW.size() / 2)),
                                Integer.toString(permanentsL.get(permanentsL.size() / 2)),
                            "Median Spell Cast Count:",
                                Integer.toString(casts.get(casts.size() / 2)),
                                Integer.toString(castsW.get(castsW.size() / 2)),
                                Integer.toString(castsL.get(castsL.size() / 2))
                    ));
                    bw.newLine();
                }
                bw.write("</div></details>");
                bw.newLine();
            }
            bw.write("</div></details>");
            bw.newLine();
        }
    }
}
