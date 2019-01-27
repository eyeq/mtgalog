import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
    public static void main(String[] args) {
        Parser parser = new Parser();

        try {
            String logPath;
            try (BufferedReader br = new BufferedReader(new FileReader(new File("config.txt")))) {
                logPath = br.readLine();
            }

            parser.loadRecord(new FileInputStream(new File("data.txt")));
            parser.loadCardLibrary(new FileReader("cardlist.json"));

            parser.readLog(new FileReader(logPath));
        } catch (Exception e) {
            e.printStackTrace();

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File("output.html")))) {
                bw.write(e.getMessage());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return;
        }

        try {
            output(new FileWriter(new File("output.html")), parser);
            parser.saveRecord(new FileOutputStream(new File("data.txt")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("finished");
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
        return IntStream.range(0, tds.length / columns).mapToObj(i -> "<td width='400px'>" + String.join("</td><td>", td(columns, i, tds)) + "</td>").collect(Collectors.toList());
    }

    private static String table(int columns, String... tds) {
        String[] t = Arrays.stream(tds).map(d -> "<u>" + ("NaN%".equals(d) ? "--.-%" : d) + "</u>").toArray(String[]::new);
        return "<table><tr>" + String.join("</tr><tr>", tr(columns, t)) + "</tr></table>";
    }

    public static void output(Writer out, Parser parser) throws IOException {
        List<Record> allRecords = parser.getRecords();
        Parser.RankInfo rankInfo = parser.getRankInfo();
        Parser.PlayerInventory inventory = parser.getPlayerInventory();

        try (BufferedWriter bw = new BufferedWriter(out)) {
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

                int collectedCardCount =  collectible.stream().mapToInt(d -> d.getValue()).sum();
                double p = 100.0 * collectedCardCount / (collectible.size() * 4);
                bw.write(table(3,
                        "GOLD:", Integer.toString(inventory.gold), String.format("(%+d gold)", parser.getDiffGold()),
                        "GEMS:", Integer.toString(inventory.gems), String.format("(%+d gems)", parser.getDiffGems()),
                        "VAULT:", String.format("%3.1f", inventory.vaultProgress) + "%", String.format("(%+3.1f)", parser.getDiffVaultProgress()) + "%",
                        "NEXT WC Rare:", Integer.toString(nextR), "",
                        "NEXT WC Mythic Rare:", Integer.toString(nextM), "",
                        "Card Variety:", collectible.stream().filter(d -> d.getValue() != 0).count() + " / " + collectible.size(), "",
                        "Card Collection:", String.format("%3.1f", p) + "%", String.format("(%+d card)", (collectedCardCount - parser.getPreCollectedCardCount()))
                ));
                bw.newLine();
            }
            bw.write("<details>");
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
                            "Card Variety:", setList.stream().filter(d -> d.getValue() != 0).count() + " / " + setList.size(),
                            "Card Collection:", String.format("%3.1f", setP) + "%",
                            "Common Card Collection:", String.format("%3.1f", commonP) + "%",
                            "Uncommon Card Collection:", String.format("%3.1f", uncommonP) + "%",
                            "Rare Card Collection:", String.format("%3.1f", rareP) + "%",
                            "Mythic Rare Card Collection:", String.format("%3.1f", mythicP) + "%"
                    ));
                }
            }
            bw.write("</details>");
            bw.newLine();

            {
                bw.write(h3("---CONSTRUCTED---"));

                double constructedWinRatio = 100.0 * rankInfo.constructedMatchesWon / (rankInfo.constructedMatchesWon + rankInfo.constructedMatchesLost);
                bw.write(table(2,
                        "WIN:", Integer.toString(rankInfo.constructedMatchesWon),
                        "LOSE:", Integer.toString(rankInfo.constructedMatchesLost),
                        // "DRAW:", Integer.toString(constructedMatchesDrawn)
                        "Winning Percentage:", String.format("%3.1f", constructedWinRatio) + "%"
                ));
                bw.newLine();
            }
            {
                bw.write(h3("---LIMITED---"));
                double limitedWinRatio = 100.0 * rankInfo.limitedMatchesWon / (rankInfo.limitedMatchesWon + rankInfo.limitedMatchesLost);
                bw.write(table(2,
                        "WIN:", Integer.toString(rankInfo.limitedMatchesWon),
                        "LOSE:", Integer.toString(rankInfo.limitedMatchesLost),
                        // "DRAW:", Integer.toString(limitedMatchesDrawn)
                        "Winning Percentage:", String.format("%3.1f", limitedWinRatio) + "%"
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
                bw.write("<details>");
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
                                "Winning Percentage:", String.format("%3.1f", winRatio) + "%",
                                "Winning Percentage(play first):", String.format("%3.1f", winRatioF) + "%",
                                "Winning Percentage(draw first):", String.format("%3.1f", winRatioA) + "%",
                                "Winning Percentage vs Mythic Rank:", String.format("%3.1f", winRatioM) + "%",
                                "Winning Percentage vs Mythic Rank(play first):", String.format("%3.1f", winRatioMF) + "%",
                                "Winning Percentage vs Mythic Rank(draw first):", String.format("%3.1f", winRatioMA) + "%"
                        ));
                        bw.newLine();
                    }
                    bw.write("<details>");
                    bw.newLine();
                    {
                        List<Record> mulliganRecords = records.stream().filter(d -> d.getMulliganedCount() != 0).collect(Collectors.toList());
                        double mulliganRatio = 100.0 * mulliganRecords.size() / records.size();
                        double mulliganCount = mulliganRecords.isEmpty() ? Double.NaN : mulliganRecords.stream().mapToInt(d -> d.getMulliganedCount()).average().getAsDouble();

                        bw.write(table(2,
                                "Mulligan Percentage:", String.format("%3.1f", mulliganRatio) + "%",
                                "Average Mulligan Count when to Mulligan:", String.format("%3.1f", mulliganCount)
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

                        bw.write("<br>");
                        bw.newLine();
                        bw.write(table(2,
                                "Average Turn:", String.format("%3.1f", turn),
                                "Average Duration:", String.format("%02d:%02d:%02d", hour, minute, second),

                                "Average Max Lands:", String.format("%3.1f", lands.stream().mapToInt(d -> d).average().getAsDouble()),
                                "Average Max Creatures:", String.format("%3.1f", creatures.stream().mapToInt(d -> d).average().getAsDouble()),
                                //"Average Max Artifacts and Enchantments:", String.format("%3.1f", permanent),
                                "Average Spell Cast Count:", String.format("%3.1f", casts.stream().mapToInt(d -> d).average().getAsDouble()),

                                "Median Max Lands:", Integer.toString(lands.get(lands.size() / 2)),
                                "Median Max Creatures:", Integer.toString(creatures.get(creatures.size() / 2)),
                                //"Median Max Artifacts and Enchantments:", String.format("%3.1f", permanent),
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

                        bw.write("<br>");
                        bw.newLine();
                        bw.write(table(2,
                                "Average Turn(When you win):", String.format("%3.1f", turn),
                                "Average Duration(When you win):", String.format("%02d:%02d:%02d", hour, minute, second),

                                "Average Max Lands(When you win):", String.format("%3.1f", lands.stream().mapToInt(d -> d).average().getAsDouble()),
                                "Average Max Creatures(When you win):", String.format("%3.1f", creatures.stream().mapToInt(d -> d).average().getAsDouble()),
                                //"Average Max Artifacts and Enchantments(When you win):", String.format("%3.1f", permanent),
                                "Average Spell Cast Count(When you win):", String.format("%3.1f", casts.stream().mapToInt(d -> d).average().getAsDouble()),

                                "Median Max Lands(When you win):", Integer.toString(lands.get(lands.size() / 2)),
                                "Median Max Creatures(When you win):", Integer.toString(creatures.get(creatures.size() / 2)),
                                //"Median Max Artifacts and Enchantments(When you win):", String.format("%3.1f", permanent),
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

                        bw.write("<br>");
                        bw.newLine();
                        bw.write(table(2,
                                "Average Turn(When you lose):", String.format("%3.1f", turn),
                                "Average Duration(When you lose):", String.format("%02d:%02d:%02d", hour, minute, second),

                                "Average Max Lands(When you lose):", String.format("%3.1f", lands.stream().mapToInt(d -> d).average().getAsDouble()),
                                "Average Max Creatures(When you lose):", String.format("%3.1f", creatures.stream().mapToInt(d -> d).average().getAsDouble()),
                                //"Average Max Artifacts and Enchantments(When you lose):", String.format("%3.1f", permanent),
                                "Average Spell Cast Count(When you lose):", String.format("%3.1f", casts.stream().mapToInt(d -> d).average().getAsDouble()),

                                "Median Max Lands(When you lose):", Integer.toString(lands.get(lands.size() / 2)),
                                "Median Max Creatures(When you lose):", Integer.toString(creatures.get(creatures.size() / 2)),
                                //"Median Max Artifacts and Enchantments(When you lose):", String.format("%3.1f", permanent),
                                "Median Spell Cast Count(When you lose):", Integer.toString(casts.get(casts.size() / 2))
                        ));
                        bw.newLine();
                    }
                    bw.write("</details>");
                    bw.newLine();
                }
                bw.write("</details>");
                bw.newLine();
            }
        }
    }
}
