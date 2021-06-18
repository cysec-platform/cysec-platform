package eu.smesec.core.endpoints;

import eu.smesec.bridge.execptions.CacheException;
import eu.smesec.bridge.generated.Token;
import eu.smesec.bridge.generated.User;
import eu.smesec.bridge.utils.TokenUtils;
import eu.smesec.core.auth.CryptPasswordStorage;
import eu.smesec.core.cache.CacheAbstractionLayer;
import eu.smesec.core.cache.LibCal;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.logging.Logger;


public class PasswordForgottenModel {
    private static final Logger logger = Logger.getLogger(PasswordForgottenModel.class.getName());
    private static final int tokenExpiryHours = 1;

    private final CacheAbstractionLayer cal = CacheAbstractionLayer.getInstance();

    /**
     * Creates a new token for a password reset request.
     */
    public Token createToken() {
        return TokenUtils.createToken(
                TokenUtils.TOKEN_RESET,
                TokenUtils.generateRandomHexToken(16),
                LocalDateTime.now().plusDays(tokenExpiryHours));
    }

    /**
     * Check if a given token is valid.
     *
     * @param tokenId   The token registered to a user email.
     * @param companyId the company id
     * @return The user found with the reset token
     */
    public User verifyToken(String tokenId, String companyId) throws CacheException {
        return cal.getUserByToken(companyId, tokenId);

    }

    /**
     * Updates the password. Uses default company
     * @param newPassword New user password
     * @param user User
     * @throws NoSuchAlgorithmException Thrown if CryptoPassword algo does not exist
     * @throws CacheException Thrown if error when updating the user
     */
    public void updatePassword(String newPassword, User user) throws NoSuchAlgorithmException, CacheException {
        String company = LibCal.getCompany();
        updatePassword(newPassword, user, company);
    }

    /**
     * Updates the password
     * @param newPassword New user password
     * @param user User
     * @param companyId The company id
     * @throws NoSuchAlgorithmException Thrown if CryptoPassword algo does not exist
     * @throws CacheException Thrown if error when updating the user
     */
    public void updatePassword(String newPassword, User user, String companyId) throws NoSuchAlgorithmException, CacheException {
        CryptPasswordStorage passwordStorage = new CryptPasswordStorage(newPassword, null);
        user.setPassword(passwordStorage.getPasswordStorage());
        user.getToken().removeIf(token -> token.getId().equals("reset"));
        cal.updateUser(companyId, user);
    }
}
