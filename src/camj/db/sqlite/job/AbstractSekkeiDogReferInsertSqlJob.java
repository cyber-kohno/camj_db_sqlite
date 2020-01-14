/**
 * 
 */
package camj.db.sqlite.job;

import java.io.File;

import org.apache.poi.ss.usermodel.Workbook;

import camj.db.sqlite.EnvProperties;

/**
 * 
 * 
 * @author kohno
 */
abstract public class AbstractSekkeiDogReferInsertSqlJob extends AbstractFileOutputJob {

    /**
     * コンストラクタです。
     */
    public AbstractSekkeiDogReferInsertSqlJob() {

    }

    /**
     * 参照検索のカテゴリを返します。
     *
     * @return 参照検索のカテゴリのカテゴリ
     */
    abstract protected String getCategory();

    /**
     * {@inheritDoc}
     */
    protected String getOutputFileName() {
        return "refer_" + getCategory() + ".sql";
    }

    private String getTargetPath() {
        return EnvProperties.getWorkDir() + "\\work\\documents\\sekkei";
    }

    /**
     * {@inheritDoc}
     */
    public void execute() {
        final File root = new File(getTargetPath());
        
        outputFile();
    }

    private String getSysknInsertSql(String subsysName, String subsysNo) {
        return String.format("insert into kubun values('%s','%s','%s');", "sys_" + getCategory(),
                subsysNo, subsysName);
    }
}
