package eu.smesec.platform.auth.strategies;

import eu.smesec.bridge.execptions.CacheException;
import eu.smesec.bridge.execptions.LockedExpetion;
import eu.smesec.bridge.generated.Locks;
import eu.smesec.bridge.generated.User;
import eu.smesec.platform.auth.CryptPasswordStorage;
import eu.smesec.platform.auth.PasswordStorage;
import eu.smesec.platform.cache.CacheAbstractionLayer;
import eu.smesec.platform.config.Config;

import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.security.RolesAllowed;
import javax.servlet.ServletContext;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.MultivaluedMap;

public abstract class AbstractUserAuthStrategy extends AbstractAuthStrategy {
  public AbstractUserAuthStrategy(CacheAbstractionLayer cal, Config config,
                                  ServletContext context, boolean proxyAuth) {
    super(cal, config, context, proxyAuth);
  }

  @Override
  public boolean authenticate(MultivaluedMap<String, String> header, Method method)
      throws CacheException, ClientErrorException {
    String[] credentials = extractCredentials(header);
    if (credentials == null) {
      logger.log(Level.WARNING, "invalid credentials");
      throw new BadRequestException("invalid credentials");
    }
    String companyName = credentials[0];
    String username = credentials[1];
    String password = credentials[2];
    String locale = credentials[3];

    // get user
    User user;
    if (username.contains("@")) {
      user = cal.getUserByEmail(companyName, username);
    } else {
      user = cal.getUserByName(companyName, username);
    }
    if (user == null) {
      throw new BadRequestException("User " + username + " not found in comapny " + companyName);
    }

    // check locks
    Locks lock = user.getLock();
    if (lock == null) {
      // correct missing lock attribute
      lock = Locks.NONE;
      user.setLock(lock);
      cal.updateUser(companyName, user);
    }
    if (lock.equals(Locks.LOCKED) || lock.equals(Locks.PENDING)) {
      throw new LockedExpetion(username, lock);
    }

    // check roles
    if (method.isAnnotationPresent(RolesAllowed.class)) {
      RolesAllowed rolesAnnotation = method.getAnnotation(RolesAllowed.class);
      Set<String> rolesSet = new HashSet<>(Arrays.asList(rolesAnnotation.value()));
      if (user.getRole().stream().noneMatch(rolesSet::contains)) {
        throw new ForbiddenException("user " + username + " does not have one of the required roles ["
            + String.join(" ", rolesSet) + "]");
      }
    }

    // check password
    if (!isProxyAuth() && !checkPassword(user, password)) {
      logger.log(Level.WARNING, "password does not match for user " + username);
      return false;
    }

    // check locale
    if (locale == null) {
      locale = user.getLocale();
      if (locale == null) {
        locale = "en"; //FIXME: set browser locale
      }
    }

    // set context attributes
    context.setAttribute("company", companyName);
    context.setAttribute("user", user.getUsername());
    context.setAttribute("locale", locale);
    return true;
  }

  private boolean checkPassword(User user, String password) {
    try {
      PasswordStorage storage = new CryptPasswordStorage(user.getPassword());
      return storage.verify(password);
    } catch (NoSuchAlgorithmException nae) {
      logger.log(Level.SEVERE, "no algorithm used", nae);
    }
    return false;
  }

  /**
   * Extracts the required credentials from the present headers
   *
   * @return {company name, user name / email, password, locale}
   */
  protected abstract String[] extractCredentials(MultivaluedMap<String, String> header)
      throws CacheException, ClientErrorException;


//  private boolean isValidUserAndPassword(final String username, final String password,
//                                        final String companyName) throws CacheException {
//    boolean isValid = false;
//
//    if (companyName != null) {
//      User user = null;
//
//      if (username.contains("@")) {
//        user = cal.getUserByEmail(companyName, username);
//      } else {
//        user = cal.getUserByName(companyName, username);
//      }
//      if (user == null) {
//        logger.warning("Couldn't retrieve user");
//        return false;
//      }
//
//      if (user.getLock() == null) {
//        logger.warning("User invalid status LOCKED or PENDING ");
//        return isValid;// Make sure Lock is not set to any value
//      }
//      if (!user.getLock().equals(Locks.NONE)) {
//        logger.warning("User is in status LOCKED or PENDING ");
//        return isValid;// Make sure Lock is not set to any value
//      }
//      if (user != null) {
//        try {
//          // TODO: Throws IllegalArgumentException without salt parameter
//          PasswordStorage storage = new CryptPasswordStorage(user.getPassword());
//          if (storage.verify(password)) {
//            isValid = true;
//          }
//        } catch (NoSuchAlgorithmException e) {
//          logger.warning("Incorrectly stored password!");
//        }
//      }
//    } else if (username.equals("admin") && password.equals("password")) {
//      isValid = true;
//    } else if (username.equals("mha") && password.equals("mha")) {
//      isValid = true;
//    }
//    return isValid;
//  }
//
//  /**
//   * Verifies whether a user is in status LOCKED or PENDING.
//   *
//   * @param username    The user to examine.
//   * @param companyName The company in which the user is member.
//   * @return True if the user has Locks set to NONE.
//   */
//  protected boolean isUserUnlocked(final String username,
//                                final String companyName) {
//    try {
//      if (companyName != null) {
//        User user = null;
//        if (username.contains("@")) {
//          user = cal.getUserByEmail(companyName, username);
//        } else {
//          user = cal.getUserByName(companyName, username);
//        }
//        if (user != null) {
//          if (user.getLock() == Locks.PENDING) {
//            logger.info(String.format("User: %s is pending", username));
////          requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
////                  .entity("Your account has not been verified yet by an admin").build());
//            throw new NotAuthorizedException("Account is in state pending");
//          } else if (user.getLock() == Locks.LOCKED) {
//            logger.info(String.format("User: %s is locked", username));
////          requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
////                  .entity("Your account has been locked").build());
//            throw new NotAuthorizedException("Account is in state pending");
//          }
//          return true;
//        }
//      }
//    } catch (CacheException ce) {
//      logger.warning(ce.getMessage());
//    }
//    return false;
//  }
//
//  private boolean isUserAllowed(final String username,
//                               final Set<String> rolesSet,
//                               final String companyName) {
//    boolean isAllowed = false;
//    try {
//      if (companyName != null) {
//        User user = null;
//
//        if (username.contains("@")) {
//          user = cal.getUserByEmail(companyName, username);
//        } else {
//          user = cal.getUserByName(companyName, username);
//        }
//        // Check if the user was found and has any matching role
//        // Collections.disjoint returns true if there are no common elements in two collections
//        if (user != null && !Collections.disjoint(rolesSet, user.getRole())) {
//          isAllowed = true;
//        }
//      } else if (username.equals("admin")) {
//        // Master admin login
//        String userRole = "ADMIN";
//        if (rolesSet.contains(userRole)) {
//          isAllowed = true;
//        }
//      } else if (username.equals("mha")) {
//        // mha temporary login
//        String userRole = "mha";
//        if (rolesSet.contains(userRole)) {
//          isAllowed = true;
//        }
//      }
//    } catch (CacheException ce) {
//      logger.warning(ce.getMessage());
//    }
//    return isAllowed;
//  }
//
//  private void setLocale(User user, ContainerRequestContext requestContext, MultivaluedMap<String, String> headers) {
//    String locale = "en-US";
//    // 1. from oauth
//    // 2. from user setting
//    // 3. from browser
//    final List<String> oidcLocale = headers.get(config.getStringValue(null, OIDC_LOCALE));
//    Locale requestLocale = requestContext.getAcceptableLanguages().get(0);
//    // oidc locale empty
//    if(oidcLocale == null || oidcLocale.size() == 0) {
//      if(user.getLocale() == null) {
//        // both user and oidc locale empty
//        locale = requestLocale.getDisplayName();
//        logger.info("Setting locale from browser: " + requestLocale.getDisplayName());
//      } else {
//        // oidc missing, user locale present
//        logger.info("Setting locale from user: " + user.getLocale());
//        locale = user.getLocale();
//      }
//    } else {
//      // oidc locale present
//      logger.info("Setting locale from oidc: " + oidcLocale.get(0));
//      locale = oidcLocale.get(0);
//    }
//
//    // if both oidc and user have valid locales, overwrite with oidc
//    if(!(oidcLocale == null) && user.getLocale() != null && !oidcLocale.equals(user.getLocale())) {
//      user.setLocale(oidcLocale.get(0));
//    }
//    context.setAttribute("locale", locale);
//  }
}
