/**
 * 
 */
package camj.db.sqlite.job;

import java.io.File;

import camj.db.sqlite.EnvProperties;

/**
 * ワークスペースをgrep検索する抽象クラスです。
 * 
 * @author kohno
 */
abstract public class AbstractGrepToWorkspaceJob extends AbstractFileOutputJob {

    /**
     * コンストラクタです。
     */
    public AbstractGrepToWorkspaceJob() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() {
        final File rootDir = new File(getWorkspacePath());
        final File[] projectList = rootDir.listFiles();

        initialize();

        for (File project : projectList) {

            if (!acceptProject(project.getName())) {
                continue;
            }
            debug("■" + project);

            for (File top : project.listFiles()) {

                if (!acceptTopDir(top.getName())) {
                    continue;
                }
                searchSrcFolderRec(top);
            }
        }

        afterProcess();

        outputFile();
    }

    private String getWorkspacePath() {
        return EnvProperties.getProperty("WORKSPACE_DIR");
    }

    /**
     * プロジェクトのディレクトリに対して処理を進めるかどうかを判定します。
     * 
     * @param dirName ディレクトリ名
     * @return ディレクトリに対して処理を進めるかどうか
     */
    abstract protected boolean acceptProject(String dirName);

    /**
     * プロジェクト配下のディレクトリに対して処理を進めるかどうかを判定します。
     * 
     * @param dirName ディレクトリ名
     * @return ディレクトリに対して処理を進めるかどうか
     */
    abstract protected boolean acceptTopDir(String dirName);

    /**
     * 初期化処理を行います。
     */
    abstract protected void initialize();

    /**
     * 後処理を行います。
     */
    abstract protected void afterProcess();

    /**
     * srcフォルダを再帰的に検索します。
     * 
     * @param cur カレントファイル
     */
    protected void searchSrcFolderRec(File cur) {

        final File[] list = cur.listFiles();

        for (File file : list) {
            if (file.isFile()) {

                final String fileName = file.getName();

                if (!acceptOpenFile(fileName)) {
                    continue;
                }

                checkSrcFile(file);
            } else {
                searchSrcFolderRec(file);
            }
        }
    }

    /**
     * 走査中のファイルに対して処理を進めるかどうかを判定します。
     * 
     * @param fileName ファイル名
     * @return ファイルに対して処理を進めるかどうか
     */
    abstract protected boolean acceptOpenFile(String fileName);

    /**
     * ソースファイルについて処理を行います。
     * 
     * @param file ファイル
     */
    abstract protected void checkSrcFile(File file);

}
