package eu.smesec.platform.endpoints;

import eu.smesec.bridge.FQCN;
import eu.smesec.bridge.execptions.CacheException;
import eu.smesec.bridge.generated.Audit;
import eu.smesec.bridge.generated.Metadata;
import eu.smesec.bridge.md.MetadataUtils;
import eu.smesec.bridge.utils.AuditUtils;
import eu.smesec.core.cache.CacheAbstractionLayer;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.jsp.JspMvcFeature;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.mockito.Mockito.when;

public class CompaniesTest extends JerseyTest {
  private static String RESOURCE_PATH = "rest/companies";
  private ServletContext context;
  private CacheAbstractionLayer cal;

  @Override
  protected Application configure() {
    //Mocks to be injected
    context = Mockito.mock(ServletContext.class);
    cal = Mockito.mock(CacheAbstractionLayer.class);

    //test env resource config
    ResourceConfig config = new ResourceConfig(Companies.class);
    // add support for JSP to the Test server
    System.out.println(Objects.requireNonNull(getClass().getClassLoader().getResource("WEB-INF/templates")).toString());
    System.out.println(Objects.requireNonNull(getClass().getClassLoader().getResource("WEB-INF/templates")).toExternalForm());
    System.out.println(Objects.requireNonNull(getClass().getClassLoader().getResource("WEB-INF/templates")).getPath());

    System.out.println(Objects.requireNonNull(getClass().getResource("WEB-INF/templates")).toString());
    File file = new File(Objects.requireNonNull(getClass().getResource("WEB-INF/templates")).getPath());
    // give 500 error, but solves the right path
    config.property(JspMvcFeature.TEMPLATE_BASE_PATH, getClass().getResource("WEB-INF/templates").getPath());
//    config.property(JspMvcFeature.TEMPLATE_BASE_PATH, "WEB-INF/templates");
    config.register(org.glassfish.jersey.server.mvc.jsp.JspMvcFeature.class);
    config.register(new AbstractBinder() {
      protected void configure() {
        //inject mocks
        bind(context).to(ServletContext.class);
        bind(cal).to(CacheAbstractionLayer.class);
      }
    });

    return config;
  }

  @Before
  public void setUp() throws Exception {
    super.setUp(); //must be called to start test server
    Mockito.reset(context);
    Mockito.reset(cal);

  }

  /**
   * Ignored because the Test server from Jersey cant find the template directory.
   * not sure how this can be accomplished.
   */
  @Test
  @Ignore
  public void testGetWidget() throws CacheException {
    Metadata rating = MetadataUtils.createMetadata(MetadataUtils.MD_RATING, Arrays.asList(
          MetadataUtils.createMvalueStr(MetadataUtils.MV_MICRO_SCORE, "102"),
          MetadataUtils.createMvalueStr(MetadataUtils.MV_MICRO_GRADE, "A")
    ));

    XMLGregorianCalendar instant = AuditUtils.now();
    Audit audit1 = new Audit();
    audit1.setTime(instant);
    Audit audit2 = new Audit();
    audit2.setTime(instant);
    List<Audit> audits = Arrays.asList(audit1, audit2);
    when(context.getAttribute("company")).thenReturn("fhnw");
    when(cal.getMetadataOnAnswer("fhnw", FQCN.fromString("password"), MetadataUtils.MD_RATING)).thenReturn(rating);
    when(cal.getAllAuditLogs("fhnw")).thenReturn(audits);
    Response res = target(RESOURCE_PATH + "/{coach}/widget")
          .resolveTemplate("coach", "password")
          .request().get();
//      Viewable viewable = res.readEntity(Viewable.class);
    Assert.assertEquals(200, res);
  }
}
