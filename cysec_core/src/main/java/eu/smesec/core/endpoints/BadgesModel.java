package eu.smesec.core.endpoints;

import eu.smesec.bridge.execptions.CacheException;
import eu.smesec.bridge.md.Badge;
import eu.smesec.bridge.md.MetadataUtils;
import eu.smesec.core.cache.CacheAbstractionLayer;
import eu.smesec.core.cache.LibCal;
import eu.smesec.core.messages.BadgeMsg;
import eu.smesec.core.utils.LocaleUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class BadgesModel {
    CacheAbstractionLayer cal = CacheAbstractionLayer.getInstance();

    /**
     * Generates the model for the badge overview page.
     *
     * @return badge model
     */
    public Map<String, Object> getModel() throws CacheException {
        String companyId = LibCal.getCompany();
        //TODO: Add locale string back into the model. Search for setAttribute("locale") to see usage within the code
        Locale locale = LocaleUtils.fromString(null);
        List<Badge> badges = getBadges(companyId);
        BadgeMsg msg = new BadgeMsg(locale, badges.size());
        Map<String, Object> model = new HashMap<>();
        model.put("msg", msg.getMessages());
        model.put("badges", badges);
        return model;
    }

    private List<Badge> getBadges(String companyId) throws CacheException {
        return cal.getAllMetadataOnAnswer(companyId, LibCal.FQCN_COMPANY).stream()
                .filter(md -> md.getKey().startsWith(MetadataUtils.MD_BADGES))
                .map(md -> MetadataUtils.fromMd(md, Badge.class))
                .collect(Collectors.toList());
    }
}
