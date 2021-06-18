package eu.smesec.core.endpoints;

import eu.smesec.bridge.execptions.CacheException;
import eu.smesec.core.cache.CacheAbstractionLayer;
import eu.smesec.core.cache.LibCal;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Endpoint for synchronization from a external server.
 */

public class ReplicaSlaveModel {
    private static final Logger logger = Logger.getLogger(ReplicaSlaveModel.class.getName());

    private final CacheAbstractionLayer cal = CacheAbstractionLayer.getInstance();

    /**
     * Zips the company and marks it as readonly.
     *
     * @return zipped company
     */
    public byte[] zip() throws CacheException {
        String companyId = LibCal.getCompany();
        logger.log(Level.INFO, "Zipping company " + companyId);
        byte[] fd = cal.zipCompany(companyId);
        cal.setCompanyReadonly(companyId, true);
        logger.log(Level.INFO, "Downloading company " + companyId);
        return fd;
    }

    /**
     * Handles the file upload to this company.
     *
     * @param inputStream  data of the file
     * @param relativePath relative path of the file
     */

    public void upload(InputStream inputStream, String relativePath) throws CacheException {
        String companyId = LibCal.getCompany();
        logger.log(Level.INFO, "Uploading file: " + companyId + "/" + relativePath);
        cal.syncFile(companyId, Paths.get(relativePath), inputStream, false);
        logger.log(Level.INFO, "Finished uploading file: " + companyId + "/" + relativePath);
    }

    /**
     * Handles the file download from this company.
     *
     * @param relPath Relative path of the file
     * @return FileResponse File to be downloaded
     */
    public byte[] download(Path relPath) throws CacheException {
        String companyId = LibCal.getCompany();
        return cal.createFileResponse(companyId, relPath);
    }

    /**
     * Returns the Company ID
     *
     * @return String Company ID
     */
    public String getCompany() {
        return LibCal.getCompany();
    }
}
