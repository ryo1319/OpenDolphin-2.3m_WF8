package open.dolphin.impl.rezept.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import open.dolphin.impl.rezept.RezeUtil;
import open.dolphin.infomodel.PatientModel;

/**
 * レセプト共通レコード
 * 
 * @author masuda, Masuda Naika
 */
public class RE_Model implements IRezeModel {

    public static final String ID = "RE";
    
    private int rezeType;       // レセプト種別
    private Date billDate;      // 診療年月
    private String name;        // 氏名
    private String sex;         // 性別
    private Date birthday;      // 生年月日
    private Date admitDate;     // 入院年月日
    private String patientId;   // 患者番号
    private int checkFlag;      // チェック
    
    private HO_Model hoModel;
    private List<KO_Model> koModelList;
    private KH_Model khModel;
    private List<SY_Model> syModelList;
    private List<IRezeItem> itemList;
    private List<SJ_Model> sjModelList;
    
    private PatientModel patientModel;
    
    public String getRezeType(int num) {
        return RezeUtil.getInstance().getRezeTypeDesc(num, rezeType);
    }
    public Date getBillDate() {
        return billDate;
    }
    public String getName() {
        return name;
    }
    public String getSex() {
        return sex;
    }
    public Date getBirthday() {
        return birthday;
    }
    public Date getAdmitDate() {
        return admitDate;
    }
    public String getPatientId() {
        return patientId;
    }
    public void setCheckFlag(int flag) {
        checkFlag = flag;
    }
    public int getCheckFlag() {
        return checkFlag;
    }
    
    public List<IRezeItem> getItemList() {
        return itemList;
    }
    public void addItem(IRezeItem item) {
        if (itemList == null) {
            itemList = new ArrayList<>();
        }
        itemList.add(item);
    }
    public List<SJ_Model> getSJModelList() {
        return sjModelList;
    }
    public void addSJModel(SJ_Model sjModel) {
        if (sjModelList == null) {
            sjModelList = new ArrayList<>();
        }
        sjModelList.add(sjModel);
    }
    public void setHOModel(HO_Model model) {
        hoModel = model;
    }
    public HO_Model getHOModel() {
        return hoModel;
    }
    public void addKOModel(KO_Model model) {
        if (koModelList == null) {
            koModelList = new ArrayList<>();
        }
        koModelList.add(model);
    }
    public List<KO_Model> getKOModelList() {
        return koModelList;
    }
    public void setKHModel(KH_Model model) {
        khModel = model;
    }
    public KH_Model getKHModel() {
        return khModel;
    }
    public List<SY_Model> getSYModelList() {
        return syModelList;
    }
    public void addSYModel(SY_Model model) {
        if (syModelList == null) {
            syModelList = new ArrayList<>();
        }
        syModelList.add(model);
    }
    public void setPatientModel(PatientModel pm) {
        patientModel = pm;
    }
    public PatientModel getPatientModel() {
        return patientModel;
    }
    public boolean isOpened() {
        if (patientModel != null) {
            return patientModel.isOpened();
        }
        return false;
    }
    
    @Override
    public void parseLine(String csv) {
        String[] tokens = csv.split(CAMMA);
        rezeType = Integer.parseInt(tokens[2]);
        billDate = RezeUtil.getInstance().fromYearMonth(tokens[3]);
        name = tokens[4].trim();
        sex = RezeUtil.getInstance().getSexDesc(tokens[5]);
        birthday = RezeUtil.getInstance().fromYearMonthDate(tokens[6]);
        admitDate = RezeUtil.getInstance().fromYearMonthDate(tokens[8]);
        patientId = tokens[13].trim();
    }
}