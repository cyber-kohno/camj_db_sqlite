/**
 * 
 */
package camj.db.sqlite.job;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import camj.db.sqlite.EnvProperties;

/**
 * �������s�����ۃN���X�ł��B
 * 
 * @author kohno
 */
abstract public class AbstractCamjDBSqliteJob implements CamjDBSqliteJob {

    /**
     * �R���\�[���ɏo�͂��܂��B
     * 
     * @param str �o�͂��镶����
     */
    protected void debug(String str) {
        if (EnvProperties.DEBUG) {
            System.out.println(str);
        }
    }

    /**
     * �����s�̊O���t�@�C���f�[�^��ǂݍ��݁A���X�g�Ɋi�[���ĕԂ��܂��B
     * 
     * @param fileName �t�@�C����
     * @return ���X�g
     */
    protected List<String> getDataList(String fileName) {

        final Path path = Paths
                .get(String.format("%s/work/data/%s", EnvProperties.getWorkDir(), fileName));
        List<String> list = null;
        try {
            list = Files.readAllLines(path, Charset.forName("MS932"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * �ϐ��̋�؂�ʒu��Ԃ��܂��B
     * 
     * @param base ���ƂȂ镶����
     * @param validityList �L������
     * @return �ϐ��̋�؂�ʒu
     */
    protected int getDelimitedPos(String base, String validityList) {
        for (int i = 0; i < base.length(); i++) {
            boolean check = false;
            for (int j = 0; j < validityList.length(); j++) {
                if (base.charAt(i) == validityList.charAt(j)) {
                    check = true;
                    break;
                }
            }
            if (!check) {
                return i;
            }
        }
        return base.length();
    }
}
