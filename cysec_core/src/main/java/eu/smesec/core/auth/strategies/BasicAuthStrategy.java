package eu.smesec.core.auth.strategies;

import eu.smesec.bridge.execptions.CacheException;
import eu.smesec.core.cache.CacheAbstractionLayer;
import eu.smesec.core.config.Config;
import eu.smesec.core.utils.Validator;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletContext;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.MultivaluedMap;
import org.glassfish.jersey.internal.util.Base64;

public class BasicAuthStrategy extends AbstractUserAuthStrategy {
  public static final String AUTHORIZATION_PROPERTY = "authorization";
  public static final Pattern regexBasic = Pattern.compile("^Basic (.+)$");
  public static final Pattern regexAuth = Pattern.compile("^(.+)/(.+):(.+)$");

  public BasicAuthStrategy(CacheAbstractionLayer cal, Config config, ServletContext context) {
    super(cal, config, context, false);
  }

  @Override
  public List<String> getHeaderNames() {
    return Collections.singletonList(AUTHORIZATION_PROPERTY);
  }

  @Override
  protected String[] extractCredentials(MultivaluedMap<String, String> header)
      throws CacheException, ClientErrorException {
    logger.info("Checking Basic auth");
    String basicAuth = header.getFirst(AUTHORIZATION_PROPERTY);
    if (basicAuth != null && !basicAuth.isEmpty()) {
      Matcher basicMatcher = regexBasic.matcher(basicAuth);
      if (basicMatcher.matches()) {
        String auth = Base64.decodeAsString(basicMatcher.group(1));
        Matcher authMatcher = regexAuth.matcher(auth);
        if (!authMatcher.matches()) {
          logger.log(Level.WARNING, "invalid auth format: " + auth);
          throw new BadRequestException("invalid auth format: " + auth);
        }
        String companyName = authMatcher.group(1);
        if (!Validator.validateWord(companyName)) {
          throw new BadRequestException("Company pattern does not match");
        }
        String username = authMatcher.group(2);
        if (!Validator.validateWord(username) && !Validator.validateEmail(username)) {
          throw new BadRequestException("Username pattern does not match");
        }
        String password = authMatcher.group(3);
        if (password == null || password.isEmpty()) {
          throw new BadRequestException("Password is null or empty");
        }
        return new String[] {companyName, username, password, null};
      }
    }
    logger.log(Level.WARNING, "invalid auth header");
    throw new BadRequestException("invalid auth header");
  }
}
