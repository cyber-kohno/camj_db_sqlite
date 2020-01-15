package camj.db.sqlite.job.impl;

import java.util.Collections;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;

import camj.db.sqlite.job.AbstractTeigiDocToInsertSqlJob;

public class KinouDefInsertCreater extends AbstractTeigiDocToInsertSqlJob {

	/** �ǂݍ��݊J�n�s */
	private static final int START_ROW = 6;
	private static final int SYSYO_START_ROW = 7;

	private static final String CATEGORY = "kinou";

	/**
	 * �R���X�g���N�^
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
		return "�@�\�ꗗ�\";
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
	 * @param current �Z���̒l
	 * @return �����J�n�s��
	 */
	private int getStartIndex(String cellValue) {
		if (cellValue.contains("�A�E")) {
			return SYSYO_START_ROW;
		}
		return START_ROW;
	}

	/**
	 * �J�������u�V�v�̏ꍇ�A��O�̃J��������Ԃ�
	 *
	 * @param current �Z���̒l
	 * @param prev ��O�̒l
	 * @return �T�u�V�X�e����
	 */
	private String isSame(String current, String prev) {
		if (current.equals("�V")) {
			return prev;
		}
		return current;
	}

	private String switchKubun(String type) {
		switch (type) {
		case "�X�V":
			return "U";
		case "�Q��":
			return "R";
		case "���[":
			return "P";
		case "�A�b�v���[�h":
			return "T";
		case "�_�E�����[�h":
			return "D";
		case "�o�b�`":
			return "B";
		case "�w���v":
			return "H";
		case "�I�t���C���o�b�`":
			return "ON";
		case "�I�����C���o�b�`":
			return "OFF";
		default:
			return "-";
		}
	}

}
