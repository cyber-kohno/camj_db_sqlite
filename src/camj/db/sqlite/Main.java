/**
 * 
 */
package camj.db.sqlite;

import camj.db.sqlite.job.CamjDBSqliteJob;
import camj.db.sqlite.job.impl.ConvertSekkeiDocToItemsFileJob;

/**
 * ���C���N���X�ł��B
 * 
 * @author kohno
 */
public class Main {

    /**
     * ���C�����\�b�h�ł��B
     * 
     * @param args �R�}���h���C������
     */
    public static void main(String[] args) {

        // EnvProperties.initialize(args[0]);
        EnvProperties.initialize("C:\\Users\\kohno\\Desktop\\camj_db_sqlite�o�b�`");

        final String jobKey = "ExcelInsertCode";
        // final String jobKey = args[1];

        CamjDBSqliteJob job = new ConvertSekkeiDocToItemsFileJob();

        // switch (jobKey) {
        // case "CreateTableRefInsert":
        // job = new ColumnKinouRelationInsertCreater();
        // break;
        // case "ExcelInsertCode":
        // job = new CodeDefInsertCreater();
        // break;
        // }

        job.execute();
    }

}
