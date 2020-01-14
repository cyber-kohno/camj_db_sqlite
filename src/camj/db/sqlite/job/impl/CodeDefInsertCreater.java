/**
 * 
 */
package camj.db.sqlite.job.impl;

import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import camj.db.sqlite.job.AbstractExcelToInsertSqlJob;

/**
 * Excelのコード定義書より情報を取得し、Insert文を生成するジョブクラスです。
 * 
 * @author kohno
 */
public class CodeDefInsertCreater extends AbstractExcelToInsertSqlJob {

    private static final String CATEGORY = "code";

    private static final int START_ROW = 3;

    /**
     * コンストラクタです。
     */
    public CodeDefInsertCreater() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getCategory() {
        return CATEGORY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void openExcelFile(Workbook workbook, String subsysNo) {

        final int totalSheetCnt = workbook.getNumberOfSheets();

        for (int i = 0; i < totalSheetCnt; i++) {

            final Sheet sheet = workbook.getSheetAt(i);
            if (sheet.getSheetName().indexOf("コード管理情報") == -1) {
                continue;
            }
            debug(sheet.getSheetName());

            final List<String[]> codeInfoList = getTableDataList(sheet, START_ROW,
                    new int[] { 1, 2, 4, 5, 6 });

            String key = null;
            for (String[] record : codeInfoList) {
                final String codeKn = record[0];
                final String name = record[1];
                final String keywood = record[2];
                final String cdkanDef = record[3];
                final String cdkanName = record[4];

                if (!"".equals(codeKn)) {
                    final String sql = String.format("insert into coddf into('%s','%s','%s','%s');",
                            subsysNo, codeKn, name, keywood);
                    debug(sql);
                    append(sql);
                    key = codeKn;
                }

                if (!"".equals(cdkanDef)) {
                    int cdkanNo = -1;
                    try {
                        cdkanNo = Integer.parseInt(cdkanDef.split("_")[1]);
                    } catch (NumberFormatException e) {
                        continue;
                    }
                    final String sql = String.format("insert into cdkan into('%s',%d,'%s');", key,
                            cdkanNo, cdkanName);
                    debug(sql);
                    append(sql);
                }
            }
        }
    }

}
