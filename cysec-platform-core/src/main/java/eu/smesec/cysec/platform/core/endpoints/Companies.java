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
package eu.smesec.cysec.platform.core.endpoints;

import com.google.gson.Gson;

import eu.smesec.cysec.platform.bridge.FQCN;
import eu.smesec.cysec.platform.bridge.execptions.CacheException;
import eu.smesec.cysec.platform.bridge.generated.Audit;
import eu.smesec.cysec.platform.bridge.generated.Metadata;
import eu.smesec.cysec.platform.bridge.md.MetadataUtils;
import eu.smesec.cysec.platform.bridge.md.Rating;
import eu.smesec.cysec.platform.core.cache.CacheAbstractionLayer;
import eu.smesec.cysec.platform.core.auth.Secured;

import java.awt.geom.Rectangle2D;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.security.DenyAll;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.mvc.Viewable;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.graphics2d.svg.SVGGraphics2D;

@Secured
@DenyAll
@Path("rest/companies")
public class Companies {
  static Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);
  @Context ServletContext context;
  @Inject private CacheAbstractionLayer cal;
  // Needed to wrap String in JSON
  private final Gson gson = new Gson();

  /**
   * Return the Company for which the logged in user is registered.
   *
   * @return A String containing the company ID.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getCompanyForUser() {
    if (context.getAttribute("company") == null) {
      logger.warning("Invalid request, no company id in context");
      return Response.status(500).build();
    } else {
      String user = (String) context.getAttribute("user");
      logger.info(String.format("Fetching company for user %s", user));
    }
    String company = (String) context.getAttribute("company");
    logger.info(String.format("Found company %s for user", company));

    return Response.status(200).entity(gson.toJson(company)).build();
  }

  /**
   * Fetch the last couple of actions from the audit file.
   *
   * @param qid The coach id to filter the audit logs. Not used at the moment.
   * @return The <code>User</code> representation of the requested user.
   */
  @GET
  @Path("{qid}/widget")
  @Produces(MediaType.TEXT_HTML)
  public Response getWidget(@PathParam("qid") String qid) {
    if (context.getAttribute("company") == null) {
      logger.warning("Invalid request, no company id in context");
      return Response.status(500).build();
    }
    try {
      String company = (String) context.getAttribute("company");
      // retrieve last audit actions
      List<Audit> audits = cal.getAllAuditLogs(company);
      Map<String, Long> auditChartData = new TreeMap<>();
      if (audits != null) {
        // count activities per day
        auditChartData =
            audits.stream()
                .map(Audit::getTime)
                .map(date -> LocalDate.of(date.getYear(), date.getMonth(), date.getDay()))
                // group by day entry and map values with sum
                .collect(
                    Collectors.groupingBy(
                        LocalDate::toString,
                        Collectors.mapping(Function.identity(), Collectors.counting())));
      }
      int maxEntries = 7;
      // Change group name
      auditChartData =
          auditChartData.entrySet().stream()
              // only display last 7 entries
              .skip(auditChartData.size() - maxEntries)
              // cut year (yyyy-) e.g 5 chars from date string
              .collect(Collectors.toMap(e -> e.getKey().substring(5), Map.Entry::getValue));
      // Safely collect the grade and score from Metadata
      Metadata md = cal.getMetadataOnAnswer(company, FQCN.fromString(qid), MetadataUtils.MD_RATING);
      // handle case there is no grade and score
      Map<String, Object> model = new HashMap<>();
      if (md != null) {
        Rating rating = MetadataUtils.fromMd(md, Rating.class);
        String grade = rating.getGrade();
        model.put("grade", grade != null ? grade : "n/a");
        model.put("score", Double.toString(rating.getScore()));
      }

      String svgLineChart;
      if (auditChartData == null) {
        svgLineChart = "No data available";
      } else {
        // call helper methods to create the LineChart and export SvgCode
        JFreeChart chart = createLineChart(auditChartData, "CySec Activity", "Days", "Count");
        svgLineChart = getSvgXml(chart, 1200, 600, 0, 0);
      }
      model.put("audits", svgLineChart);

      return Response.status(200).entity(new Viewable("/companies/widget", model)).build();
    } catch (CacheException ce) {
      logger.warning(ce.getMessage());
    }
    return Response.status(500).build();
  }

  /**
   * Helper method to create a LineChart using JFree library.
   *
   * @param dataMap A map containing String, Long pairs.
   * @return a JFree line chart object.
   */
  private JFreeChart createLineChart(
      Map<String, Long> dataMap, String title, String labelxAxis, String labelyAxis) {

    final DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
    dataMap.forEach((key, value) -> dataSet.addValue(value, "days", key));

    JFreeChart lineChart =
        ChartFactory.createLineChart(
            title, labelxAxis, labelyAxis, dataSet, PlotOrientation.VERTICAL, true, true, false);

    ChartPanel chartPanel = new ChartPanel(lineChart);
    chartPanel.setPreferredSize(new java.awt.Dimension(560, 367));
    return lineChart;
  }

  /**
   * Extracts the XML Code for an SVG from a JFreeChart.
   *
   * @param chart A JFree Chart.
   * @param width the width of the chart.
   * @param height the height of the chart.
   * @param x the x coordinate of the draw area.
   * @param y the y coordinate of the draw area.
   * @return a string representation of the SVG image based on the JFree Chart.
   */
  private String getSvgXml(JFreeChart chart, int width, int height, int x, int y) {
    final int widthOfSvg = width;
    final int heightOfSvg = height;
    final SVGGraphics2D svg2d = new SVGGraphics2D(widthOfSvg, heightOfSvg);

    chart.draw(svg2d, new Rectangle2D.Double(x, y, widthOfSvg, heightOfSvg));

    final String svgElement = svg2d.getSVGElement();
    return svgElement;
  }
}
