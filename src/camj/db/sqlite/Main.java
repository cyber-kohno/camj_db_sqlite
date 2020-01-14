/**
 * 
 */
package camj.db.sqlite;

import camj.db.sqlite.job.CamjDBSqliteJob;
import camj.db.sqlite.job.impl.ConvertSekkeiDocToItemsFileJob;

/**
 * メインクラスです。
 * 
 * @author kohno
 */
public class Main {

    /**
     * メインメソッドです。
     * 
     * @param args コマンドライン引数
     */
    public static void main(String[] args) {

        // EnvProperties.initialize(args[0]);
        EnvProperties.initialize("C:\\Users\\kohno\\Desktop\\camj_db_sqliteバッチ");

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
