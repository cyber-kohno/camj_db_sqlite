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
 * 処理を行う抽象クラスです。
 * 
 * @author kohno
 */
abstract public class AbstractCamjDBSqliteJob implements CamjDBSqliteJob {

    /**
     * コンソールに出力します。
     * 
     * @param str 出力する文字列
     */
    protected void debug(String str) {
        if (EnvProperties.DEBUG) {
            System.out.println(str);
        }
    }

    /**
     * 複数行の外部ファイルデータを読み込み、リストに格納して返します。
     * 
     * @param fileName ファイル名
     * @return リスト
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
     * 変数の区切れ位置を返します。
     * 
     * @param base 元となる文字列
     * @param validityList 有効文字
     * @return 変数の区切れ位置
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
