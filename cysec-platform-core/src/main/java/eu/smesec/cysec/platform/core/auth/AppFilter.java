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
package eu.smesec.cysec.platform.core.auth;

import eu.smesec.cysec.platform.core.auth.strategies.DummyAuthStrategy;
import eu.smesec.cysec.platform.core.config.Config;
import eu.smesec.cysec.platform.core.config.CysecConfig;

import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.glassfish.jersey.logging.LoggingFeature;

// Maybe can be placed by js content fetcher?
@WebFilter(
    urlPatterns = "/app/*",
    filterName = "AppFilter",
    description = "Filter all frontend URL")
public class AppFilter implements Filter {
  private static final Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);

  private static final String AUTHORIZATION_PROPERTY = "authorization";
  //  private static final String AUTHENTICATION_SCHEME = "Basic";

  /** The OIDC parameters are the headers that contain user info from keycloak. Even thoug */
  private static final String OIDC_NAME = "cysec_header_username";

  private static final String OIDC_MAIL = "cysec_header_email";
  private static final String OIDC_FIRSTNAME = "cysec_header_firstname";
  private static final String OIDC_LASTNAME = "cysec_header_lastname";
  private static final String OIDC_COMPANY = "cysec_header_company";
  private static final String OIDC_LOCALE = "cysec_header_locale";

  @Override
  public void init(FilterConfig filterConfig) {}

  /**
   * Needed to authenticate access to the /webapp folder.
   *
   * @param request The servlet request
   * @param response The servlet response
   * @param chain The filter chain
   * @throws IOException If an io error occurs
   * @throws ServletException If an io servlet occurs
   */
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    // for some reason, config equals null here, there refetching the instance
    logger.info("Checking for authentication header, to force webapp to pop up basic auth");
    HttpServletRequest httpServletRequest = (HttpServletRequest) request;
    HttpServletResponse httpServletResponse = (HttpServletResponse) response;

    // extract context from the request object
    String url =
        httpServletRequest.getScheme()
            + "://localhost:"
            + httpServletRequest.getLocalPort()
            + httpServletRequest.getContextPath()
            + "/api/rest/login";
    HttpPost httpPost = new HttpPost(url);

    Config config = CysecConfig.getDefault();
    boolean dummy = "dummy".equals(config.getStringValue(null, DummyAuthStrategy.AUTH_SCHEME).toLowerCase());
    String authorization = httpServletRequest.getHeader(AUTHORIZATION_PROPERTY);
    // Case distinguition OAuth/Basic
    logger.info(String.format("doFilter authorization header %s", authorization));
    logger.info(String.format(
            "doFilter authorization header %s",
            httpServletRequest.getHeader("authorization")));
    logger.info(httpServletRequest.getHeader(config.getStringValue(null, OIDC_NAME)));
    logger.info(httpServletRequest.getHeader(config.getStringValue(null, OIDC_MAIL)));
    logger.info(httpServletRequest.getHeader(config.getStringValue(null, OIDC_FIRSTNAME)));
    logger.info(httpServletRequest.getHeader(config.getStringValue(null, OIDC_LASTNAME)));
    logger.info(httpServletRequest.getHeader(config.getStringValue(null, OIDC_COMPANY)));

    // User name is guaranteed to contain a value
    String userName = httpServletRequest.getHeader(config.getStringValue(null, OIDC_NAME));
    if (userName != null && !userName.isEmpty()) {
      // Set headers from OIDC
      httpPost.setHeader("Authorization", authorization);
      httpPost.setHeader(
          config.getStringValue(null, OIDC_NAME),
          httpServletRequest.getHeader(config.getStringValue(null, OIDC_NAME)));
      httpPost.setHeader(
          config.getStringValue(null, OIDC_MAIL),
          httpServletRequest.getHeader(config.getStringValue(null, OIDC_MAIL)));
      httpPost.setHeader(
          config.getStringValue(null, OIDC_FIRSTNAME),
          httpServletRequest.getHeader(config.getStringValue(null, OIDC_FIRSTNAME)));
      httpPost.setHeader(
          config.getStringValue(null, OIDC_LASTNAME),
          httpServletRequest.getHeader(config.getStringValue(null, OIDC_LASTNAME)));
      httpPost.setHeader(
          config.getStringValue(null, OIDC_COMPANY),
          httpServletRequest.getHeader(config.getStringValue(null, OIDC_COMPANY)));
    } else {
      String authHeader = httpServletRequest.getHeader("Authorization");
      if (authHeader == null && ! dummy) {
        logger.info("No authorization header present, triggering log in");
        httpServletResponse.setHeader("WWW-Authenticate", "Basic realm=SecuredApp");
        httpServletResponse.sendError(401, "Please login");
      }
    }
    // call protected resource to execute filter()
    httpPost.setHeader("Authorization", authorization);
    HttpClient httpClient = HttpClients.createDefault();
    HttpResponse httpResponse = httpClient.execute(httpPost);

    int status = httpResponse.getStatusLine().getStatusCode();
    logger.info("Authentication from XML returned: " + status);
    if (status == 200 || dummy) {
      chain.doFilter(request, response);
    } else {
      // force browser pop-up
      HttpServletResponse clientResponse = (HttpServletResponse) response;
      clientResponse.setStatus(401);
      clientResponse.setHeader("WWW-Authenticate", "Basic realm=SecuredApp");
    }
  }

  @Override
  public void destroy() {}
}
