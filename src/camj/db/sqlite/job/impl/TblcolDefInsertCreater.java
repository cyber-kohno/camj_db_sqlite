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
 * �e�[�u����`�̃C���T�[�g���𐶐�����W���u�N���X�ł��B
 *
 * @author kohno
 */
public class TblcolDefInsertCreater extends AbstractTeigiDocToInsertSqlJob {

	/** �e�[�u���A�� */
	private static int tableSequence = 1;

	private static final String CATEGORY = "table";

	/** �ǂݍ��݊J�n�s */
	private static final int START_ROW = 5;

	/**
	 * �R���X�g���N�^�ł��B
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
		return "�e�[�u����`��";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<String> getIgnoreCase() {
		List<String> ignoreName = new ArrayList<String>();
		ignoreName.add("�e�[�u����`��_�yWebFW�z_�|�[�^���ύX.xls");
		ignoreName.add("�e�[�u����`��_�y�|�[�^���������z_�ǉ�.xls");
		return ignoreName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void openExcelFile(Workbook workbook, String subsysNo) {
		final List<String[]> tableList = getTableDataList(workbook.getSheet("���e�[�u���ꗗ"), START_ROW,
				new int[] { 3, 6 });

		int index = 4;

		// �e�[�u����������
		for (String[] tableRecord : tableList) {
			final String tableName = normalizaTableName(tableRecord[0]);
			final String tableDefName = tableRecord[1];

			trimSheetName(workbook, index);
			Sheet columnSheet = workbook.getSheet(tableName);

			if (columnSheet == null) {
				columnSheet = workbook.getSheetAt(index + 1);
			}

			// �e�[�u�����TBL�ւ�insert��ǉ�
			final String recordTbl = String.format(
					"insert into tbldf values('%s','%s','%s','%s');",
					subsysNo, tableSequence, tableDefName, tableName);
			debug(recordTbl);
			append(recordTbl);
			tableSequence++;

			//�J������������
			final List<String[]> columnList = getTableDataList(columnSheet, START_ROW, new int[] { 3, 6, 10, 2, 5 });
			int columnSequence = 1;
			for (String[] columnRecord : columnList) {

				final String columnName = columnRecord[0];
				final String columnDefName = columnRecord[1];
				final String domain = columnRecord[2];
				final String colmunKey = columnRecord[3];
				final String colmunHissu = columnRecord[4];

				// �J�������TBL�ւ�insert��ǉ�
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
	 * �V�[�g���ɋ󔒕���������������V�[�g����ݒ肵�܂��B
	 *
	 * @param wb ���[�N�u�b�N
	 * @param index �V�[�g�C���f�b�N�X
	 */
	private String normalizaTableName(String tableName) {
		if (tableName.contains("�e�[�u��")) {
			return tableName.substring(0, tableName.indexOf("�e�[�u��"));
		}
		return tableName;
	}

	/**
	 * �V�[�g���ɋ󔒕���������������V�[�g����ݒ肵�܂��B
	 *
	 * @param workbook ���[�N�u�b�N
	 * @param index �V�[�g�C���f�b�N�X
	 */
	private void trimSheetName(Workbook workbook, int index) {
		workbook.setSheetName(index, workbook.getSheetName(index).trim());
	}

}
