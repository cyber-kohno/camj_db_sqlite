/**
 *
 */
package camj.db.sqlite.job.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import camj.db.sqlite.job.AbstractTeigiDocToInsertSqlJob;

/**
 * テーブル定義のインサート文を生成するジョブクラスです。
 *
 * @author kohno
 */
public class TblcolDefInsertCreater extends AbstractTeigiDocToInsertSqlJob {

	/** テーブル連番 */
	private static int tableSequence = 1;

	private static final String CATEGORY = "table";

	/** 読み込み開始行 */
	private static final int START_ROW = 5;

	/**
	 * コンストラクタです。
	 */
	public TblcolDefInsertCreater() {
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
		return "テーブル定義書";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<String> getIgnoreCase() {
		List<String> ignoreName = new ArrayList<String>();
		ignoreName.add("テーブル定義書_【WebFW】_ポータル変更.xls");
		ignoreName.add("テーブル定義書_【ポータル教務情報】_追加.xls");
		return ignoreName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void openExcelFile(Workbook workbook, String subsysNo) {
		final List<String[]> tableList = getTableDataList(workbook.getSheet("■テーブル一覧"), START_ROW,
				new int[] { 3, 6 });

		int index = 4;

		// テーブル情報を検索
		for (String[] tableRecord : tableList) {
			final String tableName = normalizaTableName(tableRecord[0]);
			final String tableDefName = tableRecord[1];

			trimSheetName(workbook, index);
			Sheet columnSheet = workbook.getSheet(tableName);

			if (columnSheet == null) {
				columnSheet = workbook.getSheetAt(index + 1);
			}

			// テーブル情報TBLへのinsertを追加
			final String recordTbl = String.format(
					"insert into tbldf values('%s','%s','%s','%s');",
					subsysNo, tableSequence, tableDefName, tableName);
			debug(recordTbl);
			append(recordTbl);
			tableSequence++;

			//カラム情報を検索
			final List<String[]> columnList = getTableDataList(columnSheet, START_ROW, new int[] { 3, 6, 10, 2, 5 });
			int columnSequence = 1;
			for (String[] columnRecord : columnList) {

				final String columnName = columnRecord[0];
				final String columnDefName = columnRecord[1];
				final String domain = columnRecord[2];
				final String colmunKey = columnRecord[3];
				final String colmunHissu = columnRecord[4];

				// カラム情報TBLへのinsertを追加
				final String recordCol = String.format(
						"insert into coldf values('%s','%d','%s','%s','%s','%s','%s');",
						tableDefName, columnSequence, columnName, columnDefName, domain, colmunKey, colmunHissu);

				debug(recordCol);
				append(recordCol);
				index = workbook.getSheetIndex(columnSheet);
				columnSequence++;
			}
		}
	}

	/**
	 * シート名に空白文字列を除去したシート名を設定します。
	 *
	 * @param wb ワークブック
	 * @param index シートインデックス
	 */
	private String normalizaTableName(String tableName) {
		if (tableName.contains("テーブル")) {
			return tableName.substring(0, tableName.indexOf("テーブル"));
		}
		return tableName;
	}

	/**
	 * シート名に空白文字列を除去したシート名を設定します。
	 *
	 * @param workbook ワークブック
	 * @param index シートインデックス
	 */
	private void trimSheetName(Workbook workbook, int index) {
		workbook.setSheetName(index, workbook.getSheetName(index).trim());
	}

}
