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
import javax.ws.rs.core.MultivaluedMap;

public abstract class AbstractUserAuthStrategy extends AbstractAuthStrategy {
  public AbstractUserAuthStrategy(
      CacheAbstractionLayer cal, Config config, ServletContext context, boolean proxyAuth) {
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
        throw new ForbiddenException(
            "user "
                + username
                + " does not have one of the required roles ["
                + String.join(" ", rolesSet)
                + "]");
      }
    }

    // check password
    String password = credentials[2];
    if (!isProxyAuth() && !checkPassword(user, password)) {
      logger.log(Level.WARNING, "password does not match for user " + username);
      return false;
    }

    // check locale
    String locale = credentials[3];
    if (locale == null) {
      locale = user.getLocale();
      if (locale == null) {
        locale = "en";  // browser
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
   * Extracts the required credentials from the present headers.
   *
   * @param header the header map
   * @return {company name, user name / email, password, locale}
   */
  protected abstract String[] extractCredentials(MultivaluedMap<String, String> header)
      throws CacheException, ClientErrorException;
}
