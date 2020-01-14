/**
 * 
 */
package camj.db.sqlite.job;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import camj.db.sqlite.EnvProperties;

/**
 * Excelファイルについて処理を行い、ファイル出力する抽象クラスです。
 * 
 * @author kohno
 */
public abstract class AbstractExcelToInsertSqlJob extends AbstractFileOutputJob
        implements CamjDBSqliteJob {

    /**
     * コンストラクタです。
     */
    public AbstractExcelToInsertSqlJob() {
    }

    /**
     * 定義書のカテゴリを返します。
     * 
     * @return 定義書のカテゴリ
     */
    abstract protected String getCategory();

    /**
     * {@inheritDoc}
     */
    protected String getOutputFileName() {
        return "exel_" + getCategory() + ".sql";
    }
    
    private String getTargetPath() {
        return EnvProperties.getWorkDir() + "\\work\\documents\\" + getCategory();
    }

    /**
     * 1Excelファイルについて処理します。
     * 
     * @param workbook ワークブック
     * @param subsysNo サブシステム連番
     */
    abstract protected void openExcelFile(Workbook workbook, String subsys);

    /**
     * {@inheritDoc}
     */
    public void execute() {
        final File root = new File(getTargetPath());
        int no = 0;
        for (File file : root.listFiles()) {
            if (file.isFile()) {
                final Workbook workbook = readWorkbook(file);
                if (workbook != null) {
                    final String subsysName = getSubSystemName(file.getName());

                    final String subsysNo = String.valueOf(no);

                    /* サブシステム区分情報のInsert文を出力 */
                    append(getSysknInsertSql(subsysName, subsysNo));
                    openExcelFile(workbook, subsysNo);
                    no++;
                }
            }
        }
        outputFile();
    }

    private String getSysknInsertSql(String subsysName, String subsysNo) {
        return String.format("insert into kubun values('%s','%s','%s');", "sys_" + getCategory(),
                subsysNo, subsysName);
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
            } else if (extension.equals(".xlsx")) {
                wb = new XSSFWorkbook(filein);
            }

        } catch (IOException e) {
            e.printStackTrace();
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

    /**
     * ファイル名からサブシステム名を取得します。
     *
     * @param fileName ファイル名
     * @return サブシステム名
     */
    private String getSubSystemName(String fileName) {
        return fileName.split("【")[1].split("】")[0];
    }

    protected List<String[]> getTableDataList(Sheet sheet, int startRow, int[] useCells) {
        final List<String[]> list = new ArrayList<String[]>();

        int i = startRow;
        while (true) {

            /* 1レコード分のデータを格納 */
            final String[] rowInfo = new String[useCells.length];
            boolean empty = true;
            for (int j = 0; j < useCells.length; j++) {
                final String value = getCellValue(sheet, i, useCells[j]);
                rowInfo[j] = value;
                if (!"".equals(value)) {
                    empty = false;
                }
            }
            /* 1レコード全てが空白ならbreakする */
            if (empty) {
                break;
            }
            list.add(rowInfo);

            i++;
        }
        return list;
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
        return cell == null ? "" : cell.getStringCellValue();
    }

}
