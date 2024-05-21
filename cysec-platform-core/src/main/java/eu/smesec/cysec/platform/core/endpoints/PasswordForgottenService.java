/*-
 * #%L
 * CYSEC Platform Core
 * %%
 * Copyright (C) 2020 - 2024 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package eu.smesec.cysec.platform.core.endpoints;

import eu.smesec.cysec.platform.bridge.execptions.CacheException;
import eu.smesec.cysec.platform.bridge.execptions.TokenExpiredException;
import eu.smesec.cysec.platform.bridge.generated.Token;
import eu.smesec.cysec.platform.bridge.generated.User;
import eu.smesec.cysec.platform.bridge.utils.TokenUtils;
import eu.smesec.cysec.platform.core.cache.CacheAbstractionLayer;
import eu.smesec.cysec.platform.core.auth.CryptPasswordStorage;
import eu.smesec.cysec.platform.core.services.MailServiceImpl;

import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.logging.LoggingFeature;

@Path("rest/resetPassword")
@PermitAll
public class PasswordForgottenService {
    private static final Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);
    private static final int tokenExpiryHours = 1;

    @Context ServletContext context;
    @Inject private CacheAbstractionLayer cal;
    @Inject private MailServiceImpl mailService;

    /**
     * Creates a new token for a password reset request.
     *
     * @param email     The email to the corresponding user
     * @param companyId the id of the company
     * @return 404 If no user for the given email was found
     */
    @POST
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_HTML)
    @Path("/create")
    public Response createToken(
            @QueryParam("email") String email, @QueryParam("company") String companyId) {
        try {
            User user = cal.getUserByEmail(companyId, email);
            if (user == null) {
                logger.warning("Could not find user with email " + email + " in company " + companyId);
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            Token resetToken = TokenUtils.createToken(
                    TokenUtils.TOKEN_RESET,
                    TokenUtils.generateRandomHexToken(16),
                    LocalDateTime.now().plusDays(tokenExpiryHours));

            logger.info("Created new password reset token for email '" + email + "': " + resetToken.getId());
            cal.updateUser(companyId, user);

            final String mailContent = "A password reset token was requested for the account registered with your email address.\n\n" +
                    "To set a new password, please visit the following website and enter the token:\n" +
                    "https://wwwtest.smesec.eu/cysec/public/resetPassword/resetPassword.html\n\n" +
                    "Token:\n" + resetToken.getValue() + "\n";
            mailService.sendMail(user, null, null, "Your password reset token", mailContent);

            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (CacheException ce) {
            logger.log(Level.WARNING, ce.getMessage(), ce);
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * Check if a given token is valid.
     *
     * @param tokenId   The token registered to a user email.
     * @param password1 the first password
     * @param password2 the second password
     * @param companyId the company id
     * @return A password reset form if a token was found.
     */
    @POST
    @PermitAll
    @Path("verifyToken/{token}")
    @Produces(MediaType.TEXT_HTML)
    public Response verifyToken(
            @PathParam("token") String tokenId,
            @QueryParam("password1") String password1,
            @QueryParam("password2") String password2,
            @QueryParam("company") String companyId) {
        try {
            // Check password inputs
            if ("".equals(password1) || "".equals(password2) || !password1.equals(password2)) {
                logger.warning("Empty or non-matching passwords, returning form");
                return Response.notModified().build();
            }

            // Check for empty token
            if ("".equals(tokenId)) {
                logger.info("No token provided in request");
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            User owner = cal.getUserByToken(companyId, tokenId);
            if (owner == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            // Perform password update
            CryptPasswordStorage passwordStorage = new CryptPasswordStorage(password1, null);
            owner.setPassword(passwordStorage.getPasswordStorage());
            owner.getToken().removeIf(token -> token.getId().equals("reset"));
            cal.updateUser(companyId, owner);
            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (TokenExpiredException tee) {
            logger.log(Level.WARNING, tee.getMessage(), tee);
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } catch (CacheException ce) {
            logger.log(Level.WARNING, ce.getMessage(), ce);
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during update of password: " + e.getMessage());
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
}
