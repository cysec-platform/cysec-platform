/*-
 * #%L
 * CYSEC Platform Core
 * %%
 * Copyright (C) 2020 - 2021 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
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

import eu.smesec.cysec.platform.bridge.FQCN;
import eu.smesec.cysec.platform.bridge.md.MetadataUtils;
import eu.smesec.cysec.platform.core.cache.CacheAbstractionLayer;
import eu.smesec.cysec.platform.bridge.execptions.CacheException;
import eu.smesec.cysec.platform.bridge.generated.Audit;

import eu.smesec.cysec.platform.bridge.utils.AuditUtils;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.xml.datatype.XMLGregorianCalendar;

import static org.mockito.Mockito.when;

import eu.smesec.cysec.platform.bridge.generated.Metadata;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.jsp.JspMvcFeature;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

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
    System.out.println(getClass().getClassLoader().getResource("WEB-INF/templates").toString());
    System.out.println(getClass().getClassLoader().getResource("WEB-INF/templates").toExternalForm());
    System.out.println(getClass().getClassLoader().getResource("WEB-INF/templates").getPath());

    System.out.println(getClass().getResource("WEB-INF/templates").toString());
    File file = new File(getClass().getResource("WEB-INF/templates").getPath());
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
