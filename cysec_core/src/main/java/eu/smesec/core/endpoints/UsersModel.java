package eu.smesec.core.endpoints;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.smesec.bridge.execptions.CacheException;
import eu.smesec.bridge.execptions.ValidationException;
import eu.smesec.bridge.generated.Company;
import eu.smesec.bridge.generated.Locks;
import eu.smesec.bridge.generated.User;
import eu.smesec.core.auth.CryptPasswordStorage;
import eu.smesec.core.cache.CacheAbstractionLayer;
import eu.smesec.core.cache.LibCal;
import eu.smesec.core.json.FieldsExclusionStrategy;
import eu.smesec.core.messages.UsersMsg;
import eu.smesec.core.utils.LocaleUtils;
import eu.smesec.core.utils.Validator;

import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class UsersModel {
    private static final Logger logger = Logger.getLogger(UsersModel.class.getName());
    private static final Gson addUserGson =
            new GsonBuilder()
                    .addDeserializationExclusionStrategy(new FieldsExclusionStrategy("id", "token"))
                    .create();
    private static final Gson getUserGson =
            new GsonBuilder()
                    .addSerializationExclusionStrategy(new FieldsExclusionStrategy("password"))
                    .create();
    private static final Gson updateUserGson = new GsonBuilder().create();
    private final CacheAbstractionLayer cal = CacheAbstractionLayer.getInstance();

    /**
     * Creates the user model. Uses default locale
     *
     * @return User model
     * @throws CacheException Thrown during cache operations
     */
    public Map<String, Object> getUserModel() throws CacheException {
        Locale locale = LocaleUtils.fromString(null);
        return getUserModel(locale);
    }

    /**
     * Creates the user model
     *
     * @param locale
     * @return User model
     * @throws CacheException Thrown during cache operations
     */
    public Map<String, Object> getUserModel(Locale locale) throws CacheException {
        String companyId = LibCal.getCompany();

        String replicaToken = cal.getCompanyReplicaToken(companyId);
        Company company = cal.getCompany(companyId);
        List<User> users = company.getUser();
        UsersMsg msg = new UsersMsg(locale, users.size());
        Map<String, Object> model = new HashMap<>();
        model.put("msg", msg.getMessages());
        model.put("users", users);
        model.put("replica", replicaToken);
        model.put("locales", Arrays.asList("en", "de"));
        model.put("locks", Locks.values());
        return model;
    }

    /**
     * Creates a new user.
     *
     * @param json user data
     * @return ID of the newly created user
     */
    public Long createUser(String json) throws NoSuchAlgorithmException, CacheException, ValidationException {

        String companyId = LibCal.getCompany();
        User newUser = addUserGson.fromJson(json, User.class);
        Validator.validateUser(newUser);

        logger.log(Level.INFO, "Hashing and salting the password");
        String password = newUser.getPassword();
        CryptPasswordStorage passwordStorage = new CryptPasswordStorage(password, null);
        newUser.setPassword(passwordStorage.getPasswordStorage());
        newUser.setLock(Locks.PENDING);
        cal.createUser(companyId, newUser);
        return newUser.getId();
    }

    /**
     * Returns a user from their ID as json.
     *
     * @param userId The id of the user
     * @return User object as json string or null
     */

    public String getUser(long userId) throws CacheException {
        String companyId = LibCal.getCompany();
        User user = cal.getUser(companyId, userId);
        if (user == null) return null;
        return getUserGson.toJson(user);
    }

    /**
     * Updates an existing user.
     *
     * @param userId The id of the user
     * @param json   The new user data, the data must contain unchanged data as well!
     * @throws CacheException Thrown if user could not be updated.
     */
    public void updateUser(long userId, String json) throws ValidationException, CacheException {
        String companyId = LibCal.getCompany();
        User updatedUser = updateUserGson.fromJson(json, User.class);
        updatedUser.setId(userId);
        Validator.validateUser(updatedUser);

        //      if (newUser.getPassword() != null) {
        //        logger.log(Level.INFO, "Hashing and salting the password");
        //        String password = newUser.getPassword();
        //        CryptPasswordStorage passwordStorage = new CryptPasswordStorage(password, null);
        //        newUser.setPassword(passwordStorage.getPasswordStorage());
        //      }
        updateUser(companyId, updatedUser);
    }

    /**
     * Updates an existing user
     *
     * @param companyId The company Id
     * @param user      The user object
     * @throws CacheException Thrown if user could not be updated.
     */
    public void updateUser(String companyId, User user) throws CacheException {
        cal.updateUser(companyId, user);
    }

    /**
     * Gets a user by their email address. Uses default company
     *
     * @param email Email address of the user
     * @return User
     * @throws CacheException Thrown if no user could not be retrieved
     */
    public User getUserByEmail(String email) throws CacheException {
        String company = LibCal.getCompany();
        return getUserByEmail(email, company);
    }

    /**
     * Gets a user by their email address.
     *
     * @param email Email address of the user
     * @return User
     * @throws CacheException Thrown if no user could not be retrieved
     */
    public User getUserByEmail(String email, String companyId) throws CacheException {
        return cal.getUserByEmail(companyId, email);
    }

    /**
     * Deletes an existing user.
     *
     * @param userId The id of the user
     * @throws CacheException Thrown if user could not be deleted
     */
    public void deleteUser(long userId) throws CacheException {
        String companyId = LibCal.getCompany();
        cal.removeUser(companyId, userId);

    }
}
