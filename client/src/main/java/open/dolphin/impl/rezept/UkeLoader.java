package open.dolphin.impl.rezept;

import open.dolphin.impl.rezept.model.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import open.dolphin.dao.SqlMiscDao;
import open.dolphin.delegater.PatientDelegater;
import open.dolphin.infomodel.DiseaseEntry;
import open.dolphin.infomodel.PatientModel;
import open.dolphin.infomodel.TensuMaster;

/**
 * UkeLoader
 * 
 * @author masuda, Masuda Naika
 */
public class UkeLoader {
    
    private static final String UNCODED_DIAG_SRYCD = "0000999";
    private static final String MODIFIER_PREFIX = "ZZZ";
    private static final String ENCODING = "SJIS";
    
    private List<IR_Model> irModelList;
    private IR_Model currentModel;
    private int totalTen;
    
    public List<IR_Model> loadUke(String pathStr) {
        
        irModelList = new ArrayList<>();
        
        FileSystem fs = FileSystems.getDefault();
        Path path = fs.getPath(pathStr);
        try (BufferedReader br = Files.newBufferedReader(path, Charset.forName(ENCODING))) {
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                parseLine(line);
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }

        irModelList.add(currentModel);
        
        // patient id順にソート
        sortByPatientId(irModelList);
        
        // 傷病名・点数マスタを参照して名称をセットする
        processSYModel();
        processIRezeItem();
        
        // PatientModelをセットする
        try {
            setPatientModel();
        } catch (Exception ex) {
        }

        return irModelList;
    }
    
    public List<IR_Model> loadFromOrca(String ym) {
        
        irModelList = new ArrayList<>();
        
        // 1:社保, 2:国保, 3:後期高齢者
        int[] teisyutusakiArray = {1, 2, 6};
        for (int teisyutusaki : teisyutusakiArray) {
            
            totalTen = 0;
            currentModel = new IR_Model();
            currentModel.setShinsaKikan(teisyutusaki);
            currentModel.setTenTable(1);     // 医科

            // tbl_recedenを参照する
            SqlMiscDao dao = SqlMiscDao.getInstance();
            List<String> list = dao.getRecedenCsv(ym, teisyutusaki);
            if (list.isEmpty()) {
                return null;
            }

            for (String line : list) {
                parseLine(line);
            }
            
            // tbl_recedenにGOは記録されていないので作成する
            GO_Model goModel = new GO_Model();
            goModel.setTotalTen(totalTen);
            goModel.setTotalCount(currentModel.getReModelList().size());
            currentModel.setGOModel(goModel);
            
            irModelList.add(currentModel);
        }
        
        // patient id順にソート
        sortByPatientId(irModelList);
        
        // 傷病名・点数マスタを参照して名称をセットする
        processSYModel();
        processIRezeItem();
        
        // PatientModelをセットする
        try {
            setPatientModel();
        } catch (Exception ex) {
        }
        
        return irModelList;
    }
    
    private void parseLine(String line) {
        
        String id = line.substring(0, 2);
        
        switch (id) {
            case "IR":
                currentModel = new IR_Model();
                currentModel.parseLine(line);
                totalTen = 0;
            case "RE":
                RE_Model reModel = new RE_Model();
                reModel.parseLine(line);
                currentModel.addReModel(reModel);
                break;
            case "HO":
                HO_Model hoModel = new HO_Model();
                hoModel.parseLine(line);
                currentModel.getCurrentREModel().setHOModel(hoModel);
                totalTen += hoModel.getTen();
                break;
            case "KO":
                KO_Model koModel = new KO_Model();
                koModel.parseLine(line);
                currentModel.getCurrentREModel().addKOModel(koModel);
                totalTen += koModel.getTen();
                break;
            case "KH":
                KH_Model khModel = new KH_Model();
                khModel.parseLine(line);
                currentModel.getCurrentREModel().setKHModel(khModel);
                break;
            case "SY":
                SY_Model syModel = new SY_Model();
                syModel.parseLine(line);
                currentModel.getCurrentREModel().addSYModel(syModel);
                break;
            case "SI":
                SI_Model siModel = new SI_Model();
                siModel.parseLine(line);
                currentModel.getCurrentREModel().addItem(siModel);
                break;
            case "IY":    
                IY_Model iyModel = new IY_Model();
                iyModel.parseLine(line);
                currentModel.getCurrentREModel().addItem(iyModel);
                break;
            case "TO":
                TO_Model toModel = new TO_Model();
                toModel.parseLine(line);
                currentModel.getCurrentREModel().addItem(toModel);
                break;
            case "CO":
                CO_Model coModel = new CO_Model();
                coModel.parseLine(line);
                currentModel.getCurrentREModel().addItem(coModel);
                break;
            case "SJ":
                SJ_Model sjModel = new SJ_Model();
                sjModel.parseLine(line);
                currentModel.getCurrentREModel().addSJModel(sjModel);
                break;
            case "GO":
                GO_Model goModel = new GO_Model();
                goModel.parseLine(line);
                currentModel.setGOModel(goModel);
                break;
            default:
                break;
        }
    }
    
    // チャート状態同期のためにPatientModelを設定する
    private void setPatientModel() throws Exception {
        
        Set<String> idSet = new HashSet<>();
        for (IR_Model irModel : irModelList) {
            for (RE_Model reModel : irModel.getReModelList()) {
                String patientId = reModel.getPatientId();
                idSet.add(patientId);
            }
        }

        List<PatientModel> pmList = PatientDelegater.getInstance().getPatientList(idSet);
        HashMap<String, PatientModel> pmMap = new HashMap<>();
        for (PatientModel pm : pmList) {
            pmMap.put(pm.getPatientId(), pm);
        }
        
        for (IR_Model irModel : irModelList) {
            for (RE_Model reModel : irModel.getReModelList()) {
                PatientModel pm = pmMap.get(reModel.getPatientId());
                if (pm != null) {
                    reModel.setPatientModel(pm);
                }
            }
        }
        idSet.clear();
        pmMap.clear();
    }
    
    private void sortByPatientId(List<IR_Model> list) {
        for (IR_Model irModel : list) {
            List<RE_Model> reModels = irModel.getReModelList();
            Collections.sort(reModels, new RE_ModelComparator());
        }
    }
    
    // マスターを参照して項目名を設定する
    private void processSYModel() {
        
        SqlMiscDao dao = SqlMiscDao.getInstance();
        
        // 病名コードを列挙しORCAから取得
        Set<String> srycds = new HashSet<>();
        for (IR_Model irModel : irModelList) {
            for (RE_Model reModel : irModel.getReModelList()) {
                for (SY_Model syModel : reModel.getSYModelList()) {
                    String srycd = String.valueOf(syModel.getSrycd());
                    // 未コード化病名の場合はスキップ
                    if (UNCODED_DIAG_SRYCD.equals(srycd)) {
                        continue;
                    }
                    srycds.add(srycd);
                    // 修飾語を処理する
                    String modifier = syModel.getModifier();
                    if (modifier != null && !modifier.isEmpty()) {
                        // modifierは４ケタ数字の連続
                        for (int i = 0; i < modifier.length(); i += 4) {
                            String str = modifier.substring(i, i + 4);
                            // ORCAではZZZxxxxと記録されている
                            srycds.add(MODIFIER_PREFIX + str);
                        }
                    }
                }
            }
        }
        
        // いったんHashMapに登録する
        List<DiseaseEntry> list = dao.getDiseaseEntries(srycds);
        Map<String, DiseaseEntry> map = new HashMap<>();
        for (DiseaseEntry de : list) {
            map.put(de.getCode(), de);
        }
        
        // 傷病名を構築する
        for (IR_Model irModel : irModelList) {
            for (RE_Model reModel : irModel.getReModelList()) {
                for (SY_Model syModel : reModel.getSYModelList()) {
                    String srycd = String.valueOf(syModel.getSrycd());
                    // 未コード化病名はスキップ
                    if (UNCODED_DIAG_SRYCD.equals(srycd)) {
                        continue;
                    }
                    DiseaseEntry de = map.get(srycd);
                    // 修飾語がある場合に再構築する
                    String modifier = syModel.getModifier();
                    if (modifier != null && !modifier.isEmpty()) {
                        boolean pre = true;
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < modifier.length(); i += 4) {
                            String str = modifier.substring(i, i + 4);
                            // 後置修飾語は8から始まる
                            if (pre && str.startsWith("8")) {
                                sb.append(de.getName());
                                pre = false;
                            }
                            DiseaseEntry dem = map.get("ZZZ" + str);
                            sb.append(dem.getName());
                        }
                        syModel.setDiagName(sb.toString());
                    } else {
                        syModel.setDiagName(de.getName());
                    }
                    syModel.setByoKanrenKbn(de.getByoKanrenKbn());
                }
            }
        }
        list.clear();
        srycds.clear();
        map.clear();
    }

    // マスターを参照して項目名を設定する
    private void processIRezeItem() {

        // 診療行為・薬剤など
        SqlMiscDao dao = SqlMiscDao.getInstance();
        Set<String> srycds = new HashSet<>();

        // 診療行為コードを列挙しORCAから取得
        for (IR_Model irModel : irModelList) {
            for (RE_Model reModel : irModel.getReModelList()) {
                for (IRezeItem item : reModel.getItemList()) {
                    String srycd = String.valueOf(item.getSrycd());
                    srycds.add(srycd);
                }
            }
        }
        
        // いったんHashMapに登録する
        List<TensuMaster> tmList = dao.getTensuMasterList(srycds);
        Map<String, TensuMaster> map = new HashMap<>();
        for (TensuMaster tm : tmList) {
            map.put(tm.getSrycd(), tm);
        }

        // 診療行為名をセットする
        for (IR_Model irModel : irModelList) {
            for (RE_Model reModel : irModel.getReModelList()) {
                for (IRezeItem item : reModel.getItemList()) {
                    String srycd = item.getSrycd();
                    TensuMaster tm = map.get(srycd);
                    // コメントコード
                    if (item instanceof CO_Model) {
                        CO_Model coModel = (CO_Model) item;
                        String desc = reconstructComment(tm.getName(), coModel.getComment());
                        coModel.setDescription(desc);
                    } else {
                        item.setDescription(tm.getName());
                    }
                }
            }
        }
        
        tmList.clear();
        srycds.clear();
        map.clear();
    }
    
    // コメントコード内容を再構築
    private String reconstructComment(String tmName, String comment) {
        
        if (tmName.isEmpty()) {
            return comment;
        }
        if (comment.isEmpty()) {
            return tmName;
        }
        if (!tmName.contains(" ") && !tmName.contains("　") ){
            StringBuilder sb = new StringBuilder();
            sb.append(tmName).append(" ").append(comment);
            return sb.toString();
        }
        
        // 数値が入る空欄をカウントする。
        StringBuilder sb = new StringBuilder();
        int nameLen = tmName.length();
        int count = 0;
        boolean isSpc = false;
        for (int i = 0; i < nameLen; ++i) {
            char c = tmName.charAt(i);
            if (c == ' ' || c == '　') {
                if (!isSpc) {
                    sb.append(' ');
                    count++;
                    isSpc = true;
                }
            } else {
                sb.append(c);
                isSpc = false;
            }
        }
        String data = sb.toString();
        nameLen = data.length();
        
        // コメントデータをカウント数で分割する、むりやり実装ｗ
        List<String> strList = new ArrayList<>();
        int commentLen = comment.length();
        int tokenLength = commentLen / count;
        for (int i = 0; i < commentLen; i += tokenLength) {
            strList.add(comment.substring(i, i + tokenLength));
        }

        // 空欄をコメントデータで置換する
        sb = new StringBuilder();
        int cnt = 0;
        for (int i = 0; i < nameLen; ++i) {
            char c = data.charAt(i);
            sb.append(c);
            if (c == ' ') {
                sb.append(strList.get(cnt));
                cnt++;
            }
        }
        
        return sb.toString();
    }
    
    // patient id順のcomparator
    private static class RE_ModelComparator implements Comparator {

        @Override
        public int compare(Object o1, Object o2) {
            String pId1 = ((RE_Model) o1).getPatientId();
            String pId2 = ((RE_Model) o2).getPatientId();
            return pId1.compareTo(pId2);
        }
    }

}