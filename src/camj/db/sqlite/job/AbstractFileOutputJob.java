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
 * ファイル出力する抽象クラスです。
 * 
 * @author kohno
 */
public abstract class AbstractFileOutputJob extends AbstractCamjDBSqliteJob
        implements CamjDBSqliteJob {

    /** 出力文字列 */
    private StringBuilder stringBuilder;

    public AbstractFileOutputJob() {
        stringBuilder = new StringBuilder();
    }

    /**
     * 出力するファイル名を返します。
     * 
     * @return 出力するファイル名
     */
    abstract protected String getOutputFileName();

    /**
     * ファイルに出力する文字列を追加します。
     * 
     * @param s 追加する文字列
     */
    protected void append(String s) {
        stringBuilder.append(s + "\n");
    }

    /**
     * ファイルを出力します。
     */
    protected void outputFile() {
        try {
            // // FileWriterクラスのオブジェクトを生成する
            // FileWriter file = new FileWriter(EnvProperties.WOKR_DIR + "\\" +
            // getOutputFileName());
            // // PrintWriterクラスのオブジェクトを生成する
            // PrintWriter pw = new PrintWriter(new BufferedWriter(file));
            //
            // pw.println(stringBuilder.toString());
            //
            // // ファイルを閉じる
            // pw.close();

            // FileWriterクラスのオブジェクトを生成する
            OutputStreamWriter osw = new OutputStreamWriter(
                    new FileOutputStream(
                            EnvProperties.getWorkDir() + "\\work\\sql\\" + getOutputFileName()),
                    "UTF-8");
            // PrintWriterクラスのオブジェクトを生成する
            BufferedWriter bw = new BufferedWriter(osw);

            bw.write(stringBuilder.toString());

            // ファイルを閉じる
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
