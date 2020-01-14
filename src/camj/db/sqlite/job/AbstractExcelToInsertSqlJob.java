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
 * Excel�t�@�C���ɂ��ď������s���A�t�@�C���o�͂��钊�ۃN���X�ł��B
 * 
 * @author kohno
 */
public abstract class AbstractExcelToInsertSqlJob extends AbstractFileOutputJob
        implements CamjDBSqliteJob {

    /**
     * �R���X�g���N�^�ł��B
     */
    public AbstractExcelToInsertSqlJob() {
    }

    /**
     * ��`���̃J�e�S����Ԃ��܂��B
     * 
     * @return ��`���̃J�e�S��
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
     * 1Excel�t�@�C���ɂ��ď������܂��B
     * 
     * @param workbook ���[�N�u�b�N
     * @param subsysNo �T�u�V�X�e���A��
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

                    /* �T�u�V�X�e���敪����Insert�����o�� */
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
     * �t�@�C����胏�[�N�u�b�N�̃I�u�W�F�N�g���擾���܂��B
     * 
     * @param file �t�@�C��
     * @return ���[�N�u�b�N
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
            // .xls��.xlsx�Ńt�@�C���`����ύX
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
     * �t�@�C�����̊g���q��Ԃ��܂��B
     * 
     * @param fileName �t�@�C����
     * @return �g���q
     */
    private String getExtensions(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }

    /**
     * �t�@�C��������T�u�V�X�e�������擾���܂��B
     *
     * @param fileName �t�@�C����
     * @return �T�u�V�X�e����
     */
    private String getSubSystemName(String fileName) {
        return fileName.split("�y")[1].split("�z")[0];
    }

    protected List<String[]> getTableDataList(Sheet sheet, int startRow, int[] useCells) {
        final List<String[]> list = new ArrayList<String[]>();

        int i = startRow;
        while (true) {

            /* 1���R�[�h���̃f�[�^���i�[ */
            final String[] rowInfo = new String[useCells.length];
            boolean empty = true;
            for (int j = 0; j < useCells.length; j++) {
                final String value = getCellValue(sheet, i, useCells[j]);
                rowInfo[j] = value;
                if (!"".equals(value)) {
                    empty = false;
                }
            }
            /* 1���R�[�h�S�Ă��󔒂Ȃ�break���� */
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
