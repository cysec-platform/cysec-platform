package eu.smesec.cysec.platform.core.endpoints;

import eu.smesec.bridge.execptions.CacheException;
import eu.smesec.bridge.md.Badge;
import eu.smesec.bridge.md.MetadataUtils;
import eu.smesec.cysec.platform.core.cache.CacheAbstractionLayer;
import eu.smesec.cysec.platform.core.cache.LibCal;
import eu.smesec.cysec.platform.core.messages.BadgeMsg;
import eu.smesec.cysec.platform.core.utils.LocaleUtils;
import eu.smesec.cysec.platform.core.auth.Secured;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.security.DenyAll;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.mvc.Viewable;

@Secured
@DenyAll
@Path("rest/badges")
public class Badges {
  private static final Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);

  @Inject private CacheAbstractionLayer cal;
  @Context ServletContext context;

  /**
   * Renders the badge overview page.
   *
   * @return rendered badge page
   */
  @GET
  @Path("/render")
  @Produces(MediaType.TEXT_HTML)
  public Response renderAllBadges() {
    String companyId = context.getAttribute("company").toString();
    Locale locale = LocaleUtils.fromString(context.getAttribute("locale").toString());
    try {
      List<Badge> badges =
          cal.getAllMetadataOnAnswer(companyId, LibCal.FQCN_COMPANY).stream()
              .filter(md -> md.getKey().startsWith(MetadataUtils.MD_BADGES))
              .map(md -> MetadataUtils.fromMd(md, Badge.class))
              .collect(Collectors.toList());
      BadgeMsg msg = new BadgeMsg(locale, badges.size());
      Map<String, Object> model = new HashMap<>();
      model.put("msg", msg.getMessages());
      model.put("badges", badges);
      return Response.status(200).entity(new Viewable("/all_badges", model)).build();
    } catch (CacheException ce) {
      logger.warning(ce.getMessage());
      return Response.status(400).build();
    } catch (Exception e) {
      logger.severe(e.getMessage());
    }
    return Response.status(500).build();
  }
}
