package eu.smesec.core.auth.strategies;

import eu.smesec.bridge.execptions.CacheException;
import eu.smesec.core.cache.CacheAbstractionLayer;
import eu.smesec.core.config.Config;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletContext;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.MultivaluedMap;

public class ReplicaAuthStrategy extends AbstractAuthStrategy {
  public static final String REPLICA_TOKEN_HEADER = "x-cysec-replica-token";

  private static Pattern authRegex = Pattern.compile("^(\\w+)/(.+)$");

  public ReplicaAuthStrategy(CacheAbstractionLayer cal, Config config,
                             ServletContext context) {
    super(cal, config, context, false);
  }

  @Override
  public List<String> getHeaderNames() {
    return Arrays.asList(REPLICA_TOKEN_HEADER);
  }

  @Override
  public boolean authenticate(MultivaluedMap<String, String> headers, Method method)
      throws CacheException, ClientErrorException {
    String replica = headers.getFirst(REPLICA_TOKEN_HEADER);
    if (replica == null) {
      throw new BadRequestException("invalid auth header");
    }
    Matcher auth = authRegex.matcher(replica);
    if (!auth.matches()) {
      throw new BadRequestException("company/token pattern does not match");
    }
    String companyId = auth.group(1);
    String companyToken = auth.group(2);
    // verify token
    String token = cal.getCompanyReplicaToken(companyId);
    if (token == null || token.isEmpty()) {
      logger.log(Level.WARNING, "company " + companyId + " does not have an replica token");
      return false;
    }
    if (!token.equals(companyToken)) {
      return false;
    }
    // set attributes
    context.setAttribute("company", companyId);
    return true;
  }
}
