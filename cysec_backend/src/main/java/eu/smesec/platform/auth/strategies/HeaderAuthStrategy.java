package eu.smesec.platform.auth.strategies;

import eu.smesec.bridge.execptions.CacheException;
import eu.smesec.bridge.generated.Locks;
import eu.smesec.bridge.generated.User;
import eu.smesec.platform.cache.CacheAbstractionLayer;
import eu.smesec.platform.config.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.ServletContext;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.MultivaluedMap;

public final class HeaderAuthStrategy extends AbstractUserAuthStrategy {
  /** The OIDC parameters are the headers that contain user info from keycloak. Even thoug */
  public static final String OIDC_NAME = "cysec_header_username";

  public static final String OIDC_MAIL = "cysec_header_email";
  public static final String OIDC_COMPANY = "cysec_header_company";
  public static final String OIDC_FIRSTNAME = "cysec_header_firstname";
  public static final String OIDC_LASTNAME = "cysec_header_lastname";
  public static final String OIDC_LOCALE = "cysec_header_locale";

  private final List<String> headerNames;

  /**
   * Header authentication strategy constructor.
   *
   * @param cal The cache abstraction layer
   * @param config The platform configuration
   * @param context The servlet context
   */
  public HeaderAuthStrategy(CacheAbstractionLayer cal, Config config, ServletContext context) {
    super(cal, config, context, true);
    String contextName = context.getContextPath().substring(1);
    headerNames =
        new ArrayList<>(
            Arrays.asList(
                config.getStringValue(contextName, OIDC_NAME),
                config.getStringValue(contextName, OIDC_MAIL),
                config.getStringValue(contextName, OIDC_COMPANY),
                config.getStringValue(contextName, OIDC_FIRSTNAME),
                config.getStringValue(contextName, OIDC_LASTNAME),
                config.getStringValue(contextName, OIDC_LOCALE)));
  }

  @Override
  public List<String> getHeaderNames() {
    return headerNames.subList(0, 3);
  }

  @Override
  protected String[] extractCredentials(MultivaluedMap<String, String> header)
      throws CacheException, ClientErrorException {
    logger.info("Checking Header auth");
    // verify essential headers
    String oauthName = requireNonNull(headerNames.get(0), header.getFirst(headerNames.get(0)));
    String oauthMail = requireNonNull(headerNames.get(1), header.getFirst(headerNames.get(1)));

    String oauthFirstName = header.getFirst(headerNames.get(3));
    String oauthLastName = header.getFirst(headerNames.get(4));
    String oauthLocale = header.getFirst(headerNames.get(5));

    // Prepare user object to create new domain user if necessary
    User user = new User();
    user.setUsername(oauthName);
    user.setFirstname(oauthFirstName);
    user.setLocale(oauthLocale);
    user.setEmail(oauthMail);

    String oauthCompany = requireNonNull(headerNames.get(2), header.getFirst(headerNames.get(2)));
    setupCompany(user, oauthCompany);

    return new String[] {
      oauthCompany, oauthName, null, oauthLocale,
    };
  }

  private static String requireNonNull(String field, String value) {
    if (value == null || value.isEmpty()) {
      throw new BadRequestException("missing oidc fields " + field);
    }
    return value;
  }

  /**
   * Verifies if either company or user are present. If the company is not present a new company and
   * user are created. If only the user is missing, a new user is created.
   *
   * @param user The user logging in.
   * @param companyId The users company
   */
  private void setupCompany(final User user, final String companyId) {
    logger.info("Checking oauth credentials");
    try {
      // Check if company exists...
      if (cal.existsCompany(companyId)) {
        // ... and create user if no duplicate is found
        User duplicate = cal.getUserByName(companyId, user.getUsername());
        if (duplicate == null) {
          logger.info(
              String.format(
                  "Creating new user as %s not present in company %s.",
                  user.getUsername(), companyId));
          user.setLock(Locks.NONE);
          cal.createUser(companyId, user);
        } else {
          logger.info("Found existing user: " + user.getUsername());
        }
      } else {
        logger.info(
            String.format(
                "Creating first user %s in new company %s.", user.getUsername(), companyId));

        // first user of a new company has to be admin
        user.getRole().add("Admin");
        user.setLock(Locks.NONE);
        cal.createCompany(companyId, companyId, user);
      }
      logger.info(
          String.format(
              "Returning verified Header data: user %s, company %s, email %s",
              user.getUsername(), companyId, user.getEmail()));
    } catch (CacheException ce) {
      logger.warning(ce.getMessage());
    }
  }
}
