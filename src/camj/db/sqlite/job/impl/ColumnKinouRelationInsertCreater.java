/**
 * 
 */
package camj.db.sqlite.job.impl;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import camj.db.sqlite.job.AbstractGrepToWorkspaceJob;
import camj.db.sqlite.job.CamjDBSqliteJob;

/**
 * カラムが参照されている機能の関連情報を格納するINSERT文を出力するジョブクラスです。
 * 
 * @author kohno
 */
public class ColumnKinouRelationInsertCreater extends AbstractGrepToWorkspaceJob
        implements CamjDBSqliteJob {

    /** 除外するテーブル */
    private static final String[] IGNORE_TABLES = { "code_tbl", "batsttbl", "kinoutbl", "knprptbl",
            "n_nycodtbl" };
    /** 出力ファイル名 */
    private static final String OUTPUT_FILE = "insert_table_ref.sql";

    /** key=テーブル名, [key=カラム名, 機能ID（複数）]のmap */
    private Map<String, Map<String, Set<String>>> relationColKinouMap;
    /** 機能ID一覧 */
    private List<String> kinouList;

    /**
     * コンストラクタです。
     */
    public ColumnKinouRelationInsertCreater() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getOutputFileName() {
        return OUTPUT_FILE;
    }

    /**
     * {@inheritDoc}
     */
    protected boolean acceptProject(String fileName) {
        return fileName.indexOf("system") != -1;
    }

    /**
     * {@inheritDoc}
     */
    protected boolean acceptTopDir(String fileName) {
        return "src".equals(fileName);
    }

    /**
     * {@inheritDoc}
     */
    protected void initialize() {

        buildRelationMapBase();

        this.kinouList = getDataList("kinou_list");
    }

    /**
     * テーブル#カラム情報に対しての参照機能を格納するmapを構成します。
     */
    private void buildRelationMapBase() {

        this.relationColKinouMap = new LinkedHashMap<>();

        /* data/column_listをロード */
        final List<String> columnList = getDataList("column_list");

        /* 除外一覧をListに移す */
        List<String> ignoreList = Arrays.asList(IGNORE_TABLES);

        /* テーブルの変更チェック用 */
        String curTable = "";
        Map<String, Set<String>> columnsInfo = null;
        for (String info : columnList) {

            if (info.split("\t").length != 2) {
                continue;
            }

            final String[] infos = info.split("\t");
            final String table = infos[0];

            if (ignoreList.contains(table)) {
                continue;
            }

            final String column = infos[1];

            if (!table.equals(curTable)) {
                columnsInfo = new LinkedHashMap<>();
                curTable = table;

                relationColKinouMap.put(table, columnsInfo);
            }

            columnsInfo.put(column, new LinkedHashSet<String>());
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void afterProcess() {
        int total = 0;
        int ref = 0;
        for (String table : this.relationColKinouMap.keySet()) {
            Map<String, Set<String>> columnsMap = relationColKinouMap.get(table);

            for (String column : columnsMap.keySet()) {
                Set<String> kinouSet = columnsMap.get(column);
                debug(String.format("%s.%s = %d件", table, column, kinouSet.size()));

                for (String kinou : kinouSet) {
                    append(String.format("insert into rclkn values('%s','%s','%s');", table, column,
                            kinou));
                }

                total++;
                if (kinouSet.size() != 0) {
                    ref++;
                }
            }
        }

        debug(String.format("★★★%d/%d", ref, total));
    }

    /**
     * {@inheritDoc}
     */
    protected boolean acceptOpenFile(String fileName) {
        return containsPartMatch(fileName, ".java");
    }

    /**
     * {@inheritDoc}
     */
    protected void checkSrcFile(File file) {

        String kinouId = getMatchKinou(file.getName());
        if (kinouId == null) {
            return;
        }

        final Path path = Paths.get(file.getAbsolutePath());
        try {
            List<String> list = Files.readAllLines(path, Charset.forName("MS932"));

            for (String s : list) {
                final String l = s.toLowerCase().trim();

                for (String table : this.relationColKinouMap.keySet()) {

                    final String entity = getEntityName(table);
                    final int itemTop = l.indexOf(entity);
                    if (itemTop != -1) {
                        final Map<String, Set<String>> columnMap = relationColKinouMap.get(table);
                        final String base = l.substring(itemTop);

                        final String item = getItem(base);

                        if (!containsPartMatch(item, entity + ".getcomp_id", entity + "pk",
                                entity + ".")) {
                            continue;
                        }

                        for (String columnBase : columnMap.keySet()) {

                            final String column = columnBase.replace("_", "");

                            if (!containsPartMatch(item, entity + ".getcomp_id().set" + column,
                                    entity + ".getcomp_id().get" + column,
                                    entity + "pk.get" + column, entity + "pk.set" + column,
                                    entity + ".get" + column, entity + ".set" + column,
                                    entity + "." + column, entity + ".id." + column)) {
                                continue;
                            }

                            final Set<String> colmunSet = columnMap.get(columnBase);
                            String val = String.format("%s.%s★%s", table, columnBase, kinouId);
                            if (!colmunSet.contains(val)) {
                                debug(val);
                                colmunSet.add(kinouId);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            debug("■■■ERROR: " + file.getName() + "\n");
        }
    }

    /**
     * ファイル名の中に機能IDが含まれている場合、機能IDを返します。<br>
     * （合致する機能IDが存在しない場合nullを返す）
     * 
     * @param fileName ファイル名
     * @return 機能ID
     */
    private String getMatchKinou(String fileName) {
        for (String item : this.kinouList) {
            if (fileName.indexOf(item) != -1) {
                return item;
            }
        }
        return null;
    }

    /**
     * ベースの構文について先頭から1命令を抽出します。<br>
     * 例）「risyu.getSsktanto(), null);」であれば、「risyu.getSsktanto()」を抽出
     * 
     * @param base ベース文字列
     * @return 抽出された文字列
     */
    private String getItem(String base) {
        final int tail = getDelimitedPos(base,
                "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_.()");
        return base.substring(0, tail);
    }

    /**
     * 対象文字列に検索文字列に含まれる文字列と部分一致するかを判定します。
     * 
     * @param target 対象文字列
     * @param items 検索文字列
     * @return 判定結果
     */
    private boolean containsPartMatch(String target, String... items) {
        for (String item : items) {
            if (target.indexOf(item) != -1) {
                return true;
            }
        }
        return false;
    }

    /**
     * テーブル定義をエンティティ名に変換して返します。
     * 
     * @param tableName テーブル定義名
     * @return エンティティ名
     */
    private String getEntityName(String tableName) {
        return tableName.replace("tbl", "").replace("_", "");
    }
}
