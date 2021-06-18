package eu.smesec.core.endpoints;

import eu.smesec.bridge.execptions.ConfigException;
import eu.smesec.core.cache.CacheAbstractionLayer;
import eu.smesec.core.config.Config;

import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;

public class ReplicaMasterModel {
    private static final Logger logger = Logger.getLogger(ReplicaMasterModel.class.getName());
    public static final String REPLICA_HOST = "cysec_replica_host";
    public static final String REPLICA_TOKEN = "cysec_replica_token";

    private final CacheAbstractionLayer cal = CacheAbstractionLayer.getInstance();

    public String getCompanyToken(Config config, String context, String companyId) throws ConfigException {
        Optional<String> companyToken =
                Arrays.stream(getReplicaEntry(config, context, REPLICA_TOKEN).split(" "))
                        .filter(entry -> entry.startsWith(companyId + "/"))
                        .findFirst();
        if (!companyToken.isPresent()) {
            throw new ConfigException("No replica token found for company " + companyId);
        }
        return companyToken.get();
    }

    public String getReplicaEntry(Config config, String context, String key) throws ConfigException {
        String entry = config.getStringValue(context, key);
        if (entry == null) {
            throw new ConfigException("Cannot find remote host entry " + key);
        }
        return entry;
    }
}
