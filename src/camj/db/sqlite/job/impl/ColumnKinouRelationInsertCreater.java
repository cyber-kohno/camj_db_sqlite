/**
 * 
 */
package camj.db.sqlite.job.impl;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import camj.db.sqlite.job.AbstractGrepToWorkspaceJob;
import camj.db.sqlite.job.CamjDBSqliteJob;

/**
 * �J�������Q�Ƃ���Ă���@�\�̊֘A�����i�[����INSERT�����o�͂���W���u�N���X�ł��B
 * 
 * @author kohno
 */
public class ColumnKinouRelationInsertCreater extends AbstractGrepToWorkspaceJob
        implements CamjDBSqliteJob {

    /** ���O����e�[�u�� */
    private static final String[] IGNORE_TABLES = { "code_tbl", "batsttbl", "kinoutbl", "knprptbl",
            "n_nycodtbl" };
    /** �o�̓t�@�C���� */
    private static final String OUTPUT_FILE = "insert_table_ref.sql";

    /** key=�e�[�u����, [key=�J������, �@�\ID�i�����j]��map */
    private Map<String, Map<String, Set<String>>> relationColKinouMap;
    /** �@�\ID�ꗗ */
    private List<String> kinouList;

    /**
     * �R���X�g���N�^�ł��B
     */
    public ColumnKinouRelationInsertCreater() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getOutputFileName() {
        return OUTPUT_FILE;
    }

    /**
     * {@inheritDoc}
     */
    protected boolean acceptProject(String fileName) {
        return fileName.indexOf("system") != -1;
    }

    /**
     * {@inheritDoc}
     */
    protected boolean acceptTopDir(String fileName) {
        return "src".equals(fileName);
    }

    /**
     * {@inheritDoc}
     */
    protected void initialize() {

        buildRelationMapBase();

        this.kinouList = getDataList("kinou_list");
    }

    /**
     * �e�[�u��#�J�������ɑ΂��Ă̎Q�Ƌ@�\���i�[����map���\�����܂��B
     */
    private void buildRelationMapBase() {

        this.relationColKinouMap = new LinkedHashMap<>();

        /* data/column_list�����[�h */
        final List<String> columnList = getDataList("column_list");

        /* ���O�ꗗ��List�Ɉڂ� */
        List<String> ignoreList = Arrays.asList(IGNORE_TABLES);

        /* �e�[�u���̕ύX�`�F�b�N�p */
        String curTable = "";
        Map<String, Set<String>> columnsInfo = null;
        for (String info : columnList) {

            if (info.split("\t").length != 2) {
                continue;
            }

            final String[] infos = info.split("\t");
            final String table = infos[0];

            if (ignoreList.contains(table)) {
                continue;
            }

            final String column = infos[1];

            if (!table.equals(curTable)) {
                columnsInfo = new LinkedHashMap<>();
                curTable = table;

                relationColKinouMap.put(table, columnsInfo);
            }

            columnsInfo.put(column, new LinkedHashSet<String>());
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void afterProcess() {
        int total = 0;
        int ref = 0;
        for (String table : this.relationColKinouMap.keySet()) {
            Map<String, Set<String>> columnsMap = relationColKinouMap.get(table);

            for (String column : columnsMap.keySet()) {
                Set<String> kinouSet = columnsMap.get(column);
                debug(String.format("%s.%s = %d��", table, column, kinouSet.size()));

                for (String kinou : kinouSet) {
                    append(String.format("insert into rclkn values('%s','%s','%s');", table, column,
                            kinou));
                }

                total++;
                if (kinouSet.size() != 0) {
                    ref++;
                }
            }
        }

        debug(String.format("������%d/%d", ref, total));
    }

    /**
     * {@inheritDoc}
     */
    protected boolean acceptOpenFile(String fileName) {
        return containsPartMatch(fileName, ".java");
    }

    /**
     * {@inheritDoc}
     */
    protected void checkSrcFile(File file) {

        String kinouId = getMatchKinou(file.getName());
        if (kinouId == null) {
            return;
        }

        final Path path = Paths.get(file.getAbsolutePath());
        try {
            List<String> list = Files.readAllLines(path, Charset.forName("MS932"));

            for (String s : list) {
                final String l = s.toLowerCase().trim();

                for (String table : this.relationColKinouMap.keySet()) {

                    final String entity = getEntityName(table);
                    final int itemTop = l.indexOf(entity);
                    if (itemTop != -1) {
                        final Map<String, Set<String>> columnMap = relationColKinouMap.get(table);
                        final String base = l.substring(itemTop);

                        final String item = getItem(base);

                        if (!containsPartMatch(item, entity + ".getcomp_id", entity + "pk",
                                entity + ".")) {
                            continue;
                        }

                        for (String columnBase : columnMap.keySet()) {

                            final String column = columnBase.replace("_", "");

                            if (!containsPartMatch(item, entity + ".getcomp_id().set" + column,
                                    entity + ".getcomp_id().get" + column,
                                    entity + "pk.get" + column, entity + "pk.set" + column,
                                    entity + ".get" + column, entity + ".set" + column,
                                    entity + "." + column, entity + ".id." + column)) {
                                continue;
                            }

                            final Set<String> colmunSet = columnMap.get(columnBase);
                            String val = String.format("%s.%s��%s", table, columnBase, kinouId);
                            if (!colmunSet.contains(val)) {
                                debug(val);
                                colmunSet.add(kinouId);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            debug("������ERROR: " + file.getName() + "\n");
        }
    }

    /**
     * �t�@�C�����̒��ɋ@�\ID���܂܂�Ă���ꍇ�A�@�\ID��Ԃ��܂��B<br>
     * �i���v����@�\ID�����݂��Ȃ��ꍇnull��Ԃ��j
     * 
     * @param fileName �t�@�C����
     * @return �@�\ID
     */
    private String getMatchKinou(String fileName) {
        for (String item : this.kinouList) {
            if (fileName.indexOf(item) != -1) {
                return item;
            }
        }
        return null;
    }

    /**
     * �x�[�X�̍\���ɂ��Đ擪����1���߂𒊏o���܂��B<br>
     * ��j�urisyu.getSsktanto(), null);�v�ł���΁A�urisyu.getSsktanto()�v�𒊏o
     * 
     * @param base �x�[�X������
     * @return ���o���ꂽ������
     */
    private String getItem(String base) {
        final int tail = getDelimitedPos(base,
                "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_.()");
        return base.substring(0, tail);
    }

    /**
     * �Ώە�����Ɍ���������Ɋ܂܂�镶����ƕ�����v���邩�𔻒肵�܂��B
     * 
     * @param target �Ώە�����
     * @param items ����������
     * @return ���茋��
     */
    private boolean containsPartMatch(String target, String... items) {
        for (String item : items) {
            if (target.indexOf(item) != -1) {
                return true;
            }
        }
        return false;
    }

    /**
     * �e�[�u����`���G���e�B�e�B���ɕϊ����ĕԂ��܂��B
     * 
     * @param tableName �e�[�u����`��
     * @return �G���e�B�e�B��
     */
    private String getEntityName(String tableName) {
        return tableName.replace("tbl", "").replace("_", "");
    }
}
