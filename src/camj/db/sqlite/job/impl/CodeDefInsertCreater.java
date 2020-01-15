/**
 *
 */
package camj.db.sqlite.job.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import camj.db.sqlite.job.AbstractTeigiDocToInsertSqlJob;

/**
 * Excelのコード定義書より情報を取得し、Insert文を生成するジョブクラスです。
 *
 * @author kohno
 */
public class CodeDefInsertCreater extends AbstractTeigiDocToInsertSqlJob {

	private static final String CATEGORY = "code";

	private static final int START_ROW = 3;

	private static List<String> existCodeKn = new ArrayList<String>();
	private static List<String> existCodeKanriNo = new ArrayList<String>();

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
	protected String getFilePattern() {
		return "コード定義書";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<String> getIgnoreCase() {
		return Collections.emptyList();
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
					String sql = String.format("insert into coddf values('%s','%s','%s','%s');",
							subsysNo, codeKn, name, keywood);
					if (existCodeKn.contains(codeKn)) {
						sql = "-- " + sql;
					}
					debug(sql);
					append(sql);
					key = codeKn;
				}

				if (!"".equals(cdkanDef)) {
					final String cdkanNo = cdkanDef.split("_")[1];
					String sql = String.format("insert into cdkan values('%s','%s','%s');", key,
							cdkanNo, cdkanName);
					if (existCodeKn.contains(key) && existCodeKanriNo.contains(cdkanNo)) {
						sql = "-- " + sql;
					}
					existCodeKanriNo.add(cdkanNo);
					debug(sql);
					append(sql);
				}

				existCodeKn.add(key);
			}
		}
	}
}
