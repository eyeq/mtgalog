# mtgalog

## 概要
MTGAのログを解析して、出力する。

## できること
- ユーザー情報の出力
  - Gold・Gemsの増減
  - vaultの進捗率
  - 次のWCまでに必要なパック数
  - カードの収集率
  - 全体の勝率
- 各デッキの勝率の出力
  - Mythicランク相手との勝率
  - 先手時・後手時の勝率
- 各デッキの情報の出力
  - マリガン率
  - マリガンを行ったときのマリガン回数の平均
  - 一ゲームのターン数の平均・中央値
  - 一ゲームにかかる時間の平均・中央値
  - 一ゲームの最大土地枚数の平均・中央値
  - 一ゲームの最大クリーチャー数の平均・中央値
  - 一ゲームの最大アーティファクト・エンチャント数の平均・中央値
  - 一ゲームで呪文を唱える回数の平均・中央値

## できないこと
- サーバーを利用したデータ保存
- ゲーム中の補助
- 対戦相手のデッキ情報の収集
- 削除されたログからの情報収集
- 複垢・複数端末利用への対応

## 使い方
1. config.txtファイルを編集して、ログのパスを指定する。
1. 「Magic: The Gathering Arena」の実行前、または終了後にarena_collector.jarを実行する。
1. データが出力されるのを待つ。
1. output.htmlに統計情報が出力される。

## サンプル
2019-01-29 00:00:00
<h2>Player Info</h2>
<h3>Inventory</h3><table><col width='400'><col width='100'><col width='100'><tr><td><u></u></td><td><u><b>Retention</b></u></td><td><u><b>(Change)</b></u></td></tr><tr><td><u>GOLD:</u></td><td><u>2000</u></td><td><u>(+1000 gold)</u></td></tr><tr><td><u>GEMS:</u></td><td><u>0</u></td><td><u>(+0 gems)</u></td></tr><tr><td><u>VAULT:</u></td><td><u>0.0%</u></td><td><u>(+0.0%)</u></td></tr><tr><td><u>NEXT WC Rare:</u></td><td><u>5</u></td><td><u></u></td></tr><tr><td><u>NEXT WC Mythic Rare:</u></td><td><u>17</u></td><td><u></u></td></tr><tr><td><u>Card Variety:</u></td><td><u>0 / 1649</u></td><td><u></u></td></tr><tr><td><u>Card Collection:</u></td><td><u>0.0%</u></td><td><u>(+0 card)</u></td></tr></table>
<details><div style='padding-left: 40px;'>
<h4 style='margin-bottom: 0;'>Ixalan</h4><table><col width='360'><col width='100'><tr><td><u>Card Variety:</u></td><td><u>0 / 289</u></td></tr><tr><td><u>Card Collection:</u></td><td><u>0.0%</u></td></tr><tr><td><u>Common Card Collection:</u></td><td><u>0.0%</u></td></tr><tr><td><u>Uncommon Card Collection:</u></td><td><u>0.0%</u></td></tr><tr><td><u>Rare Card Collection:</u></td><td><u>0.0%</u></td></tr><tr><td><u>Mythic Rare Card Collection:</u></td><td><u>0.0%</u></td></tr></table><h4 style='margin-bottom: 0;'>Dominaria</h4><table><col width='360'><col width='100'><tr><td><u>Card Variety:</u></td><td><u>0 / 281</u></td></tr><tr><td><u>Card Collection:</u></td><td><u>0.0%</u></td></tr><tr><td><u>Common Card Collection:</u></td><td><u>0.0%</u></td></tr><tr><td><u>Uncommon Card Collection:</u></td><td><u>0.0%</u></td></tr><tr><td><u>Rare Card Collection:</u></td><td><u>0.0%</u></td></tr><tr><td><u>Mythic Rare Card Collection:</u></td><td><u>0.0%</u></td></tr></table><h4 style='margin-bottom: 0;'>Magic 2019</h4><table><col width='360'><col width='100'><tr><td><u>Card Variety:</u></td><td><u>0 / 314</u></td></tr><tr><td><u>Card Collection:</u></td><td><u>0.0%</u></td></tr><tr><td><u>Common Card Collection:</u></td><td><u>0.0%</u></td></tr><tr><td><u>Uncommon Card Collection:</u></td><td><u>0.0%</u></td></tr><tr><td><u>Rare Card Collection:</u></td><td><u>0.0%</u></td></tr><tr><td><u>Mythic Rare Card Collection:</u></td><td><u>0.0%</u></td></tr></table><h4 style='margin-bottom: 0;'>M19 Gift Pack</h4><table><col width='360'><col width='100'><tr><td><u>Card Variety:</u></td><td><u>0 / 5</u></td></tr><tr><td><u>Card Collection:</u></td><td><u>0.0%</u></td></tr><tr><td><u>Common Card Collection:</u></td><td><u>--.-%</u></td></tr><tr><td><u>Uncommon Card Collection:</u></td><td><u>--.-%</u></td></tr><tr><td><u>Rare Card Collection:</u></td><td><u>0.0%</u></td></tr><tr><td><u>Mythic Rare Card Collection:</u></td><td><u>--.-%</u></td></tr></table><h4 style='margin-bottom: 0;'>Guilds of Ravnica</h4><table><col width='360'><col width='100'><tr><td><u>Card Variety:</u></td><td><u>0 / 274</u></td></tr><tr><td><u>Card Collection:</u></td><td><u>0.0%</u></td></tr><tr><td><u>Common Card Collection:</u></td><td><u>0.0%</u></td></tr><tr><td><u>Uncommon Card Collection:</u></td><td><u>0.0%</u></td></tr><tr><td><u>Rare Card Collection:</u></td><td><u>0.0%</u></td></tr><tr><td><u>Mythic Rare Card Collection:</u></td><td><u>0.0%</u></td></tr></table><h4 style='margin-bottom: 0;'>Ravnica Allegiance</h4><table><col width='360'><col width='100'><tr><td><u>Card Variety:</u></td><td><u>0 / 273</u></td></tr><tr><td><u>Card Collection:</u></td><td><u>0.0%</u></td></tr><tr><td><u>Common Card Collection:</u></td><td><u>0.0%</u></td></tr><tr><td><u>Uncommon Card Collection:</u></td><td><u>0.0%</u></td></tr><tr><td><u>Rare Card Collection:</u></td><td><u>0.0%</u></td></tr><tr><td><u>Mythic Rare Card Collection:</u></td><td><u>0.0%</u></td></tr></table><h4 style='margin-bottom: 0;'>Mythic Edition</h4><table><col width='360'><col width='100'><tr><td><u>Card Variety:</u></td><td><u>0 / 3</u></td></tr><tr><td><u>Card Collection:</u></td><td><u>0.0%</u></td></tr><tr><td><u>Common Card Collection:</u></td><td><u>--.-%</u></td></tr><tr><td><u>Uncommon Card Collection:</u></td><td><u>--.-%</u></td></tr><tr><td><u>Rare Card Collection:</u></td><td><u>--.-%</u></td></tr><tr><td><u>Mythic Rare Card Collection:</u></td><td><u>0.0%</u></td></tr></table></div></details>
<h3>---CONSTRUCTED---</h3><table><col width='400'><col width='100'><tr><td><u>WIN:</u></td><td><u>100</u></td></tr><tr><td><u>LOSE:</u></td><td><u>100</u></td></tr><tr><td><u>Winning Percentage:</u></td><td><u>50.0%</u></td></tr></table>
<h3>---LIMITED---</h3><table><col width='400'><col width='100'><tr><td><u>WIN:</u></td><td><u>20</u></td></tr><tr><td><u>LOSE:</u></td><td><u>20</u></td></tr><tr><td><u>Winning Percentage:</u></td><td><u>50.0%</u></td></tr></table>
<h2>Deck Info</h2>
<h3>---Play---</h3>
<details><div style='padding-left: 40px;'>
<h4 style='margin-bottom: 0;'>Forest's Might</h4>
<table><col width='380'><col width='100'><col width='100'><tr><td><u></u></td><td><u><b>Total</b></u></td><td><u><b>vsMythic</b></u></td></tr><tr><td><u>WIN:</u></td><td><u>10</u></td><td><u>1</u></td></tr><tr><td><u>LOSE:</u></td><td><u>4</u></td><td><u>0</u></td></tr><tr><td><u>Winning Percentage:</u></td><td><u>71.4%</u></td><td><u>100.0%</u></td></tr><tr><td><u>Winning Percentage(play first):</u></td><td><u>71.4%</u></td><td><u>--.-%</u></td></tr><tr><td><u>Winning Percentage(draw first):</u></td><td><u>71.4%</u></td><td><u>100.0%</u></td></tr></table>
<details><div style='padding-left: 40px;'><table><col width='340'><col width='100'><col width='100'><col width='100'><tr><td><u></u></td><td><u><b>Total</b></u></td><td><u><b>withWin</b></u></td><td><u><b>withLose</b></u></td></tr><tr><td><u>Mulligan Percentage:</u></td><td><u>21.4%</u></td><td><u></u></td><td><u></u></td></tr><tr><td><u>Average Mulligan Count when to Mulligan:</u></td><td><u>1.3</u></td><td><u></u></td><td><u></u></td></tr><tr><td><u>Average Turn:</u></td><td><u>11.4</u></td><td><u>10.3</u></td><td><u>14.3</u></td></tr><tr><td><u>Average Duration:</u></td><td><u>00:03:25</u></td><td><u>00:02:59</u></td><td><u>00:04:29</u></td></tr><tr><td><u>Average Max Lands:</u></td><td><u>6.3</u></td><td><u>5.6</u></td><td><u>8.0</u></td></tr><tr><td><u>Average Max Creatures:</u></td><td><u>3.0</u></td><td><u>3.0</u></td><td><u>3.0</u></td></tr><tr><td><u>Average Max Artifacts and Enchantments:</u></td><td><u>0.0</u></td><td><u>0.0</u></td><td><u>0.0</u></td></tr><tr><td><u>Average Spell Cast Count:</u></td><td><u>12.0</u></td><td><u>10.3</u></td><td><u>16.3</u></td></tr><tr><td><u>Median Max Lands:</u></td><td><u>7</u></td><td><u>6</u></td><td><u>9</u></td></tr><tr><td><u>Median Max Creatures:</u></td><td><u>3</u></td><td><u>3</u></td><td><u>3</u></td></tr><tr><td><u>Median Max Artifacts and Enchantments:</u></td><td><u>0</u></td><td><u>0</u></td><td><u>0</u></td></tr><tr><td><u>Median Spell Cast Count:</u></td><td><u>12</u></td><td><u>12</u></td><td><u>19</u></td></tr></table>
</div></details>
</div></details>
