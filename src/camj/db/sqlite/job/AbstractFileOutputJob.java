/**
 * 
 */
package camj.db.sqlite.job;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import camj.db.sqlite.EnvProperties;

/**
 * �t�@�C���o�͂��钊�ۃN���X�ł��B
 * 
 * @author kohno
 */
public abstract class AbstractFileOutputJob extends AbstractCamjDBSqliteJob
        implements CamjDBSqliteJob {

    /** �o�͕����� */
    private StringBuilder stringBuilder;

    public AbstractFileOutputJob() {
        stringBuilder = new StringBuilder();
    }

    /**
     * �o�͂���t�@�C������Ԃ��܂��B
     * 
     * @return �o�͂���t�@�C����
     */
    abstract protected String getOutputFileName();

    /**
     * �t�@�C���ɏo�͂��镶�����ǉ����܂��B
     * 
     * @param s �ǉ����镶����
     */
    protected void append(String s) {
        stringBuilder.append(s + "\n");
    }

    /**
     * �t�@�C�����o�͂��܂��B
     */
    protected void outputFile() {
        try {
            // // FileWriter�N���X�̃I�u�W�F�N�g�𐶐�����
            // FileWriter file = new FileWriter(EnvProperties.WOKR_DIR + "\\" +
            // getOutputFileName());
            // // PrintWriter�N���X�̃I�u�W�F�N�g�𐶐�����
            // PrintWriter pw = new PrintWriter(new BufferedWriter(file));
            //
            // pw.println(stringBuilder.toString());
            //
            // // �t�@�C�������
            // pw.close();

            // FileWriter�N���X�̃I�u�W�F�N�g�𐶐�����
            OutputStreamWriter osw = new OutputStreamWriter(
                    new FileOutputStream(
                            EnvProperties.getWorkDir() + "\\work\\sql\\" + getOutputFileName()),
                    "UTF-8");
            // PrintWriter�N���X�̃I�u�W�F�N�g�𐶐�����
            BufferedWriter bw = new BufferedWriter(osw);

            bw.write(stringBuilder.toString());

            // �t�@�C�������
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
