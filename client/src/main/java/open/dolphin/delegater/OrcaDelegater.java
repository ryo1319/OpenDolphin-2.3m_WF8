package open.dolphin.delegater;

import open.dolphin.client.ClaimMessageEvent;
import open.dolphin.client.KarteSenderResult;
import open.dolphin.impl.claim.ClaimSender;
import open.dolphin.impl.claim.DiagnosisSender;
import open.dolphin.infomodel.ClaimMessageModel;
import open.dolphin.infomodel.OrcaSqlModel;
import open.dolphin.project.Project;
import org.jboss.resteasy.client.ClientResponse;

/**
 * OrcaDelegater
 * @author masuda, Masuda Naika
 */
public class OrcaDelegater extends BusinessDelegater {
    
    private static final String NO_ERROR = "00";
    private static final String SERVER_CLAIM = "SERVER CLAIM";
    
    private static OrcaDelegater instance;
    
    static {
        instance = new OrcaDelegater();
    }
    
    public static OrcaDelegater getInstance() {
        return instance;
    }
    
    private OrcaDelegater() {
    }
    
    public OrcaSqlModel executeQuery(OrcaSqlModel sqlModel) {
        
        try {
            String json = getConverter().toJson(sqlModel);

            String path = "orca/query";
            ClientResponse response = getClientRequest(path, null)
                    .accept(MEDIATYPE_JSON_UTF8)
                    .body(MEDIATYPE_JSON_UTF8, json)
                    .post(ClientResponse.class);

            int status = response.getStatus();
            String entityStr = (String) response.getEntity(String.class);

            debug(status, entityStr);

            if (status != HTTP200) {
                return null;
            }
            
            sqlModel = (OrcaSqlModel) 
                    getConverter().fromJson(entityStr, OrcaSqlModel.class);
            
            return sqlModel;
        } catch (Exception ex) {
            return null;
        }
    }
    
    public void sendClaim(ClaimMessageEvent evt) {
        
        try {
            Object evtSource = evt.getSource();
            
            ClaimMessageModel model = toClaimMessageModel(evt);
            
            String path = "orca/claim";
            String json = getConverter().toJson(model);
            ClientResponse response = getClientRequest(path, null)
                    .accept(MEDIATYPE_JSON_UTF8)
                    .body(MEDIATYPE_JSON_UTF8, json)
                    .post(ClientResponse.class);
            
            int status = response.getStatus();
            String entityStr = (String) response.getEntity(String.class);
            
            debug(status, entityStr);
            
            if (status != HTTP200) {
                return;
            }
            
            ClaimMessageModel resModel = (ClaimMessageModel)
                    getConverter().fromJson(entityStr, ClaimMessageModel.class);
            
            String errMsg = resModel.getErrorMsg();
            boolean noError = NO_ERROR.equals(resModel.getErrorCode());

            if (evtSource instanceof ClaimSender) {
                ClaimSender sender = (ClaimSender) evtSource;
                KarteSenderResult result = !noError
                        ? new KarteSenderResult(SERVER_CLAIM, KarteSenderResult.ERROR, errMsg, sender)
                        : new KarteSenderResult(SERVER_CLAIM, KarteSenderResult.NO_ERROR, null, sender);
                sender.fireResult(result);
            } else if (evtSource instanceof DiagnosisSender) {
                DiagnosisSender sender = (DiagnosisSender) evtSource;
                KarteSenderResult result = !noError
                        ? new KarteSenderResult(SERVER_CLAIM, KarteSenderResult.ERROR, errMsg, sender)
                        : new KarteSenderResult(SERVER_CLAIM, KarteSenderResult.NO_ERROR, null, sender);
                sender.fireResult(result);
            }
        } catch (Exception ex) {
        }
    }
    
    private ClaimMessageModel toClaimMessageModel(ClaimMessageEvent evt) {
        ClaimMessageModel model = new ClaimMessageModel();
        model.setAddress(Project.getString(Project.CLAIM_ADDRESS));
        model.setPort(Project.getInt(Project.CLAIM_PORT));
        model.setEncoding(Project.getString(Project.CLAIM_ENCODING));
        model.setContent(evt.getClaimInsutance());
        return model;
    }
}