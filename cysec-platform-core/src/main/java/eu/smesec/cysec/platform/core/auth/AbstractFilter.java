/*-
 * #%L
 * CYSEC Platform Core
 * %%
 * Copyright (C) 2020 - 2022 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
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
package eu.smesec.cysec.platform.core.auth;

import eu.smesec.cysec.platform.bridge.execptions.CacheException;
import eu.smesec.cysec.platform.bridge.execptions.LockedExpetion;
import eu.smesec.cysec.platform.core.auth.strategies.AbstractAuthStrategy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.logging.LoggingFeature;

/** Subclasses must implement @Provider Annotation. */
public abstract class AbstractFilter {
  protected Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);
  protected List<AbstractAuthStrategy> authStrategies;

  public AbstractFilter() {
    this.authStrategies = new ArrayList<>();
  }

  private AbstractAuthStrategy getAuthStrategy(MultivaluedMap<String, String> headers) {
    for (AbstractAuthStrategy strategy : authStrategies) {
      if (headers.keySet().containsAll(strategy.getHeaderNames())) {
        return strategy;
      }
    }
    return null;
  }

  protected void checkReqest(ContainerRequestContext requestContext, Method method) {
    logger.info("Checking request");
    // Access allowed for all
    if (method.isAnnotationPresent(PermitAll.class)) {
      return;
    }
    // Access denied for all
    if (method.isAnnotationPresent(DenyAll.class)) {
      requestContext.abortWith(Response.status(403).build());
      return;
    }

    // Manipulate OIDC headers to start with lower case (Fix for unit test)
    MultivaluedMap<String, String> requestHeaders = requestContext.getHeaders();
    final MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
    for (String header : requestHeaders.keySet()) {
      String headerLower = header.toLowerCase();
      for (String entry : requestHeaders.get(header)) {
        headers.add(headerLower, entry);
      }
    }

    // Get defined auth strategy
    AbstractAuthStrategy strategy = getAuthStrategy(headers);
    if (strategy == null) {
      // Sometimes, authenticate header is present but empty (equals "")
      // trigger browser to reprompt for login dialog with header
      logger.log(Level.WARNING, "no auth strategy found for present headers");

      // Basic Auth should be supported so if authentication header is missing, force browser pop-up
      // throw new NotAuthorizedException("Authorization header contains no base64", "Basic
      // realm=SecuredApp");
      requestContext.abortWith(
          Response.status(401).header("WWW-Authenticate", "Basic realm=SecuredApp").build());
      return;
    }

    try {
      if (!strategy.authenticate(headers, method)) {
        logger.log(Level.WARNING, "Authentication failed");
        requestContext.abortWith(Response.status(401).build());
      }
    } catch (BadRequestException bre) {
      logger.log(Level.WARNING, "Request has invalid content", bre);
      requestContext.abortWith(Response.status(400).build());
    } catch (NotAuthorizedException nae) {
      logger.log(Level.WARNING, "Request not authorized", nae);
      requestContext.abortWith(
          Response.status(401).header("WWW-Authenticate", "Basic realm=SecuredApp").build());
    } catch (ForbiddenException fe) {
      logger.log(Level.WARNING, "Access not allowed", fe);
      requestContext.abortWith(Response.status(403).build());
    } catch (LockedExpetion le) {
      logger.log(Level.WARNING, le.getMessage(), le);
      requestContext.abortWith(Response.status(423).build());
    } catch (CacheException ce) {
      logger.log(Level.WARNING, "An error occurred accessing the cache",ce);
      requestContext.abortWith(Response.status(400).build());
    } catch (Exception e) {
      logger.log(Level.SEVERE, "An error occurred during authentication", e);
      requestContext.abortWith(Response.status(500).build());
    }
  }

  //  private static final Response.ResponseBuilder INVALID_CREDENTIALS =
  //          Response.status(Response.Status.UNAUTHORIZED).entity("Wrong Username or Password.")
  //                  .header("WWW-Authenticate", "BASIC realm=SecuredApp");
  //  private static final Response.ResponseBuilder ACCESS_DENIED =
  //          Response.status(Response.Status.UNAUTHORIZED).entity("You cannot access this
  // resource")
  //                  .header("WWW-Authenticate", "BASIC realm=SecuredApp");
  //  private static final Response.ResponseBuilder ACCESS_FORBIDDEN =
  //          Response.status(Response.Status.FORBIDDEN)
  //                  .entity("Access blocked for all users");
  //  private static final Response.ResponseBuilder ACCESS_LOCKED =
  //        Response.status(Response.status(423).build())
  //              .entity("You registration is currently  for all users");
}
