package eu.smesec.core.endpoints;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.smesec.bridge.execptions.CacheException;
import eu.smesec.bridge.generated.Locks;
import eu.smesec.bridge.generated.User;
import eu.smesec.core.auth.CryptPasswordStorage;
import eu.smesec.core.cache.CacheAbstractionLayer;
import eu.smesec.core.cache.LibCal;
import eu.smesec.core.exceptions.ValidationException;
import eu.smesec.core.json.FieldsExclusionStrategy;
import eu.smesec.core.utils.Validator;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SignUpModel {
    private static final Logger logger = Logger.getLogger(SignUpModel.class.getName());
    private static final Gson addUserGson =
            new GsonBuilder()
                    .addDeserializationExclusionStrategy(
                            new FieldsExclusionStrategy("id", "lock", "roles", "token"))
                    .create();

    private CacheAbstractionLayer cal = CacheAbstractionLayer.getInstance();

    /**
     * Returns a model containing all the company IDs
     *
     * @return Model containing all company IDs
     */

    public Map<String, Object> getUserSignUpFormModel() {
        List<String> companyIds = new ArrayList<>(cal.getCompanyIds());
        Map<String, Object> model = new HashMap<>();
        model.put("companyIds", companyIds);
        return model;
    }

    /**
     * Creates a new user. Uses default company
     *
     * @param json user data as json
     * @return User ID of the newly created user
     */
    public long createUser(String json) throws CacheException, NoSuchAlgorithmException {
        String company = LibCal.getCompany();
        return createUser(json, company);
    }

    /**
     * Creates a new user.
     *
     * @param json      user data as json
     * @param companyId company id
     * @return User ID of the newly created user
     */

    public long createUser(String json, String companyId) throws CacheException, NoSuchAlgorithmException {
        UsersModel usersModel = new UsersModel();
        return usersModel.createUser(json);
    }

    /**
     * Renders the sign-up form model.
     *
     * @return rendered sign-up form
     */
    public Map<String, Object> getCompanySignUpFormModel() {
        Map<String, Object> model = new HashMap<>();
        model.put("companyIds", null);
        return model;
    }

    /**
     * Creates a new company. Uses default company ID
     * The user will be the first company admin.
     *
     * @param json        the data of the company
     * @param companyName the name of the company
     * @return Admin ID of the newly created admin
     */
    public Long createCompany(String json, String companyName) throws NoSuchAlgorithmException, CacheException, ValidationException {
        String company = LibCal.getCompany();
        return createCompany(json, company, companyName);
    }

    /**
     * Creates a new company.
     * The user will be the first company admin.
     *
     * @param json        the data of the company
     * @param companyId   the id of the company
     * @param companyName the name of the company
     * @return Admin ID of the newly created admin
     */

    public Long createCompany(String json, String companyId, String companyName) throws NoSuchAlgorithmException, CacheException, ValidationException {
        User admin = addUserGson.fromJson(json, User.class);
        Validator.validateUser(admin);
        logger.log(Level.INFO, "Hashing and salting the password");
        String password = admin.getPassword();
        CryptPasswordStorage passwordStorage = new CryptPasswordStorage(password, null);
        admin.setPassword(passwordStorage.getPasswordStorage());
        admin.getRole().add("Admin");
        admin.setLock(Locks.NONE);
        cal.createCompany(companyId, companyName, admin);
        return admin.getId();
    }
}
