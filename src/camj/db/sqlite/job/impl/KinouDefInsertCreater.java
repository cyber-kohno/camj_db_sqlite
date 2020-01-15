package camj.db.sqlite.job.impl;

import java.util.Collections;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;

import camj.db.sqlite.job.AbstractTeigiDocToInsertSqlJob;

public class KinouDefInsertCreater extends AbstractTeigiDocToInsertSqlJob {

	/** 読み込み開始行 */
	private static final int START_ROW = 6;
	private static final int SYSYO_START_ROW = 7;

	private static final String CATEGORY = "kinou";

	/**
	 * コンストラクタ
	 */
	public KinouDefInsertCreater() {
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
		return "機能一覧表";
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
		int total = workbook.getNumberOfSheets();

		String[] prevValue = { "", "", "", "", "", "" };

		for (int i = 0; i < total; i++) {
			final List<String[]> columnList = getTableDataList(workbook.getSheetAt(i),
					getStartIndex(getCellValue(workbook.getSheetAt(i), 0, 0)),
					new int[] { 3, 4, 6, 7, 8 });

			for (String[] columnRecord : columnList) {

				for (int j = 0; j < columnRecord.length; j++) {
					columnRecord[j] = isSame(columnRecord[j], prevValue[j]).replace("\n", "");
				}

				final String kinouName = columnRecord[0];
				final String kinouId = columnRecord[1];
				final String kinouType = switchKubun(columnRecord[2]);
				final String content = columnRecord[3];
				final String version = columnRecord[4];

				final String record = String.format(
						"insert into kinou values('%s','%s','%s','%s','%s','%s');",
						subsysNo, kinouName, kinouId, kinouType, content, version);
				debug(record);
				append(record);
				prevValue = columnRecord;
			}
		}
	}

	/**
	 *
	 *
	 * @param current セルの値
	 * @return 検索開始行数
	 */
	private int getStartIndex(String cellValue) {
		if (cellValue.contains("就職")) {
			return SYSYO_START_ROW;
		}
		return START_ROW;
	}

	/**
	 * カラムが「〃」の場合、一つ前のカラム情報を返す
	 *
	 * @param current セルの値
	 * @param prev 一つ前の値
	 * @return サブシステム名
	 */
	private String isSame(String current, String prev) {
		if (current.equals("〃")) {
			return prev;
		}
		return current;
	}

	private String switchKubun(String type) {
		switch (type) {
		case "更新":
			return "U";
		case "参照":
			return "R";
		case "帳票":
			return "P";
		case "アップロード":
			return "T";
		case "ダウンロード":
			return "D";
		case "バッチ":
			return "B";
		case "ヘルプ":
			return "H";
		case "オフラインバッチ":
			return "ON";
		case "オンラインバッチ":
			return "OFF";
		default:
			return "-";
		}
	}

}
