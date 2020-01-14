/**
 * 
 */
package camj.db.sqlite.job.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import camj.db.sqlite.EnvProperties;
import camj.db.sqlite.job.AbstractCamjDBSqliteJob;

/**
 * 
 * 
 * @author kohno
 */
public class ConvertSekkeiDocToItemsFileJob extends AbstractCamjDBSqliteJob {

    private static final String TARGET_DOC_KEYWOOD = "機能詳細";

    private static final int EXCEL_MAX_COL = 128;

    private List<String> ignoreList;

    private int count;

    /** テーブルリスト */
    private List<String> tableList;
    /** カラムリスト */
    private List<String> columnList;

    /**
     * コンストラクタです。
     */
    public ConvertSekkeiDocToItemsFileJob() {
        count = 0;

        this.ignoreList = new ArrayList<>();
        ignoreList.add("シラバス入力_機能詳細設計書.xlsm");

        this.tableList = getDataList("table_list");
        this.columnList = getDataList("column_list");
    }

    private String getTargetPath() {
        return EnvProperties.getWorkDir() + "\\work\\documents\\sekkei";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() {

        final File file = new File(getTargetPath());

        for (File top : file.listFiles()) {
            final String topName = top.getName();
            if (topName.indexOf("設計書") != -1) {
                searchFilesRec(top);
            }
        }
    }

    /**
     * 再帰的にフォルダツリー内のファイルを探します。
     * 
     * @param file ファイルオブジェクト
     */
    private void searchFilesRec(File file) {
        if (file.isFile()) {
            final String name = file.getName();
            String src = null;
            if (name.indexOf(TARGET_DOC_KEYWOOD) != -1 && !ignoreList.contains(name)) {
                if (".txt".equals(getExtensions(name))) {
                    src = openText(file);
                } else {
                    src = openExcel(file);
                }
            }

            if (src != null) {
                debug(name);
                count++;

                final List<String> itemList = convertItems(src);
                checkSrc(itemList);

                final StringBuilder sb = new StringBuilder();
                String l = "";
                for (String s : itemList) {
                    l += s;
                    if (l.length() < 50) {
                        l += ", ";
                    } else {
                        sb.append(l + ",\n");
                        l = "";
                    }
                }
                outputFile(String.format("%03d_%s.txt", count, name.split("\\.")[0]),
                        sb.toString());
            }
        } else {
            for (File folder : file.listFiles()) {
                searchFilesRec(folder);
            }
        }
    }

    /**
     * ファイルを出力します。
     */
    protected void outputFile(String fileName, String value) {
        try {
            // FileWriterクラスのオブジェクトを生成する
            OutputStreamWriter osw = new OutputStreamWriter(
                    new FileOutputStream(
                            EnvProperties.getWorkDir() + "\\work\\data\\sekkei\\" + fileName),
                    "UTF-8");
            // PrintWriterクラスのオブジェクトを生成する
            BufferedWriter bw = new BufferedWriter(osw);

            bw.write(value);

            // ファイルを閉じる
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkSrc(List<String> itemList) {

        boolean existTbl = false;
        for (String tbl : tableList) {
            if (itemList.contains(tbl)) {
                debug("  |--【table】" + tbl);

                existTbl = true;
            }
        }
        if (existTbl) {
            for (String col : columnList) {
                if (itemList.contains(col)) {
                    debug("  |--【column】" + col);
                }
            }
        }
    }

    private List<String> convertItems(String base) {
        final String validityList = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_";

        final List<String> itemList = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < base.length(); i++) {
            boolean check = false;
            for (int j = 0; j < validityList.length(); j++) {
                if (base.charAt(i) == validityList.charAt(j)) {
                    check = true;
                    break;
                }
            }
            if (check) {
                sb.append(base.charAt(i));
            } else {
                if (sb.length() >= 1) {
                    if (!itemList.contains(sb.toString())) {
                        itemList.add(sb.toString());
                    }
                    sb = new StringBuilder();
                }
            }
        }

        if (sb.length() >= 1) {
            if (!itemList.contains(sb.toString())) {
                itemList.add(sb.toString());
            }
        }

        return itemList;
    }

    private String openText(File file) {
        StringBuilder sb = new StringBuilder();
        final Path path = Paths.get(file.getAbsolutePath());
        try {
            List<String> list = Files.readAllLines(path, Charset.forName("MS932"));

            for (String s : list) {
                if (!"".equals(s)) {
                    sb.append(s.trim() + "\n");
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return sb.toString();
    }

    private String openExcel(File file) {
        final Workbook workbook = readWorkbook(file);
        if (workbook == null) {
            return null;
        }

        final StringBuilder sb = new StringBuilder();
        for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
            final Sheet sheet = workbook.getSheetAt(s);

            // if (sheet.getSheetName().indexOf("機能詳細") == -1) {
            // continue;
            // }

            int i = 0;
            int emptyCnt = 0;
            while (true) {
                final StringBuilder lb = new StringBuilder();
                /* 1レコード分のデータを格納 */
                boolean empty = true;
                for (int j = 0; j < EXCEL_MAX_COL; j++) {
                    final String value = getCellValue(sheet, i, j);
                    if (!"".equals(value) && value.length() >= 5) {
                        empty = false;
                        lb.append(value + " ");
                    }
                }
                /* 1レコード全てが空白ならbreakする */
                if (empty) {
                    if (emptyCnt > 100) {
                        break;
                    }
                    emptyCnt++;
                } else {
                    emptyCnt = 0;
                    sb.append(lb.toString().trim() + "\n");
                }

                i++;
            }
        }

        try {
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    protected Cell getCell(Sheet sheet, int rowNo, int cellNo) {
        Row row = sheet.getRow(rowNo);
        if (row == null) {
            return null;
        }
        return row.getCell(cellNo);
    }

    protected String getCellValue(Sheet sheet, int rowNo, int cellNo) {
        final Cell cell = getCell(sheet, rowNo, cellNo);
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
        case STRING:
            return cell.getStringCellValue();
        default:
            return "";
        }
    }

    /**
     * ファイルよりワークブックのオブジェクトを取得します。
     * 
     * @param file ファイル
     * @return ワークブック
     */
    protected Workbook readWorkbook(File file) {
        String extension = getExtensions(file.getName());
        FileInputStream filein = null;
        try {
            filein = new FileInputStream(file.getAbsolutePath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Workbook wb = null;

        try {
            // .xlsか.xlsxでファイル形式を変更
            if (extension.equals(".xls")) {
                wb = new HSSFWorkbook(filein);
            } else if (extension.equals(".xlsx") || extension.equals(".xlsm")) {
                wb = new XSSFWorkbook(filein);
            }

        } catch (Exception e) {
            // e.printStackTrace();
            debug("××××××××××××××××××××");
        }
        return wb;
    }

    /**
     * ファイル名の拡張子を返します。
     * 
     * @param fileName ファイル名
     * @return 拡張子
     */
    private String getExtensions(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }
}
