/**
 * 
 */
package camj.db.sqlite.job;

import java.io.File;

import camj.db.sqlite.EnvProperties;

/**
 * ���[�N�X�y�[�X��grep�������钊�ۃN���X�ł��B
 * 
 * @author kohno
 */
abstract public class AbstractGrepToWorkspaceJob extends AbstractFileOutputJob {

    /**
     * �R���X�g���N�^�ł��B
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
            debug("��" + project);

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
     * �v���W�F�N�g�̃f�B���N�g���ɑ΂��ď�����i�߂邩�ǂ����𔻒肵�܂��B
     * 
     * @param dirName �f�B���N�g����
     * @return �f�B���N�g���ɑ΂��ď�����i�߂邩�ǂ���
     */
    abstract protected boolean acceptProject(String dirName);

    /**
     * �v���W�F�N�g�z���̃f�B���N�g���ɑ΂��ď�����i�߂邩�ǂ����𔻒肵�܂��B
     * 
     * @param dirName �f�B���N�g����
     * @return �f�B���N�g���ɑ΂��ď�����i�߂邩�ǂ���
     */
    abstract protected boolean acceptTopDir(String dirName);

    /**
     * �������������s���܂��B
     */
    abstract protected void initialize();

    /**
     * �㏈�����s���܂��B
     */
    abstract protected void afterProcess();

    /**
     * src�t�H���_���ċA�I�Ɍ������܂��B
     * 
     * @param cur �J�����g�t�@�C��
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
     * �������̃t�@�C���ɑ΂��ď�����i�߂邩�ǂ����𔻒肵�܂��B
     * 
     * @param fileName �t�@�C����
     * @return �t�@�C���ɑ΂��ď�����i�߂邩�ǂ���
     */
    abstract protected boolean acceptOpenFile(String fileName);

    /**
     * �\�[�X�t�@�C���ɂ��ď������s���܂��B
     * 
     * @param file �t�@�C��
     */
    abstract protected void checkSrcFile(File file);

}
