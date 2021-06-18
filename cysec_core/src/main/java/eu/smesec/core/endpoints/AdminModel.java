package eu.smesec.core.endpoints;

import eu.smesec.bridge.execptions.CacheException;
import eu.smesec.bridge.generated.Audit;
import eu.smesec.bridge.generated.Company;
import eu.smesec.core.cache.CacheAbstractionLayer;
import eu.smesec.core.messages.AdminAuditsMsg;
import eu.smesec.core.messages.AdminMsg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminModel {
    CacheAbstractionLayer cal = CacheAbstractionLayer.getInstance();

    /**
     * Returns the model for the admin overview page.
     *
     * @return model for admin page.
     */
    public Map<String, Object> getAdminModel() throws CacheException {
        List<Company> companies = cal.getCompanies();
        AdminMsg msg = new AdminMsg(null, companies.size());
        Map<String, Object> model = new HashMap<>();
        model.put("msg", msg.getMessages());
        model.put("companies", companies);
        return model;
    }

    /**
     * Returns the model for the admin audits.
     *
     * @param companyId The id of the company
     * @return rendered admin audits
     */
    public Map<String, Object> getAdminAuditsModel(String companyId) throws CacheException {
        List<Audit> audits = cal.getAllAuditLogs(companyId);
        AdminAuditsMsg msg = new AdminAuditsMsg(null, audits.size());
        Map<String, Object> model = new HashMap<>();
        model.put("msg", msg.getMessages());
        model.put("companyId", companyId);
        model.put("audits", audits);
        return model;
    }
}
