/**
 * 
 */
package camj.db.sqlite;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 環境のプロパティ値を定義するクラスです。
 * 
 * @author kohno
 */
public class EnvProperties {

    public static final boolean DEBUG = true;

    public static final Map<String, String> map = new HashMap<>();

    public static final void loadProperties() {

        final Path path = Paths.get(EnvProperties.getWorkDir() + "\\module\\setting.txt");
        try {
            List<String> list = Files.readAllLines(path, Charset.forName("MS932"));

            for (String s : list) {
                final String[] val = s.split("=");
                map.put(val[0], val[1]);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static final String getProperty(String key){ 
        return map.get(key);
    }

    private static String workDir;

    public static void initialize(String path) {
        workDir = path;
        loadProperties();
    }

    public static String getWorkDir() {
        return workDir;
    }
}
