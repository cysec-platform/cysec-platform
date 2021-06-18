package eu.smesec.platform.endpoints;

import eu.smesec.bridge.FQCN;
import eu.smesec.bridge.execptions.CacheException;
import eu.smesec.bridge.generated.Audit;
import eu.smesec.bridge.generated.Metadata;
import eu.smesec.bridge.md.MetadataUtils;
import eu.smesec.core.cache.CacheAbstractionLayer;
import eu.smesec.core.endpoints.CompaniesModel;
import eu.smesec.core.utils.JsonUtils;
import eu.smesec.platform.auth.Secured;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.mvc.Viewable;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.graphics2d.svg.SVGGraphics2D;

import javax.annotation.security.DenyAll;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.awt.geom.Rectangle2D;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Secured
@DenyAll
@Path("rest/companies")
public class Companies {
    static Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);
    @Context
    ServletContext context;

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
            String user = context.getAttribute("user").toString();
            logger.info(String.format("Fetching company for user %s", user));
        }
        String company = context.getAttribute("company").toString();
        logger.info(String.format("Found company %s for user", company));

        String companyJson = JsonUtils.toJson(company);
        return Response.status(200).entity(companyJson).build();
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
        final CacheAbstractionLayer cal = CacheAbstractionLayer.getInstance();
        if (context.getAttribute("company") == null) {
            logger.warning("Invalid request, no company id in context");
            return Response.status(500).build();
        }
        String company = context.getAttribute("company").toString();


        // Collect the grade and score from Metadata.
        Metadata md;
        try {
            md = cal.getMetadataOnAnswer(company, FQCN.fromString(qid), MetadataUtils.MD_RATING);
        } catch (CacheException ce) {
            logger.warning("Error getting Metadata");
            logger.warning(ce.getMessage());
            return Response.status(500).build();
        }

        Map<String, Object> companiesModel = CompaniesModel.getCompaniesModel(md);

        List<Audit> audits = null;
        try {
            audits = cal.getAllAuditLogs(company);
        } catch (CacheException ce) {
            logger.warning("Error getting All Audit Logs");
            logger.warning(ce.getMessage());
            return Response.status(500).build();
        }
        String lineChart = getLineChart(audits);
        if (lineChart != null) companiesModel.put("audits", lineChart);
        return Response.status(200).entity(new Viewable("/companies/widget", companiesModel)).build();
    }

    /**
     * Returns the ChartData or null
     *
     * @param audits List of Audits
     * @return chartDataMap or null
     */
    public static Map<String, Long> getChartData(List<Audit> audits) {
        if (audits == null) return null;

        // count activities per day
        return audits.stream()
                .map(Audit::getTime)
                .map(date -> LocalDate.of(date.getYear(), date.getMonth(), date.getDay()))
                // group by day entry and map values with sum
                .collect(
                        Collectors.groupingBy(
                                LocalDate::toString,
                                Collectors.mapping(Function.identity(), Collectors.counting())));
    }

    /**
     * Reduces the entries in the map by the maxEntries amount and return the last N entries in the map
     *
     * @param map        Map to be reduced
     * @param maxEntries Number of last N entries to return
     * @return Last N entries of the map
     */
    public static Map<String, Long> getLastNEntries(Map<String, Long> map, int maxEntries) {
        if (map == null) return null;
        return map.entrySet().stream()
                // only display last 7 entries
                .skip(map.size() - maxEntries)
                // cut year (yyyy-) e.g 5 chars from date string
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Trims all key values of the map by the trim amount, starting at the first (index 0) char. e.g. 5 chars from date string year yyyy-mm-dd => mm-dd
     *
     * @param map        Map
     * @param trimAmount Number of chars to trim
     * @return Map with trimmed Key values
     */
    public static Map<String, Long> trimKeysValueByN(Map<String, Long> map, int trimAmount) {
        return map.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().substring(trimAmount), Map.Entry::getValue));
    }


    private String getLineChart(List<Audit> audits) {
        if (audits == null) return null;
        // Add web specific line chart to the model
        int maxEntries = 7;
        Map<String, Long> allChartData = getChartData(audits);
        Map<String, Long> lastNEntries = getLastNEntries(allChartData, maxEntries);
        int trimAmount = 5;
        Map<String, Long> auditChartData = trimKeysValueByN(lastNEntries, trimAmount);
        if (auditChartData == null) return null;
        // call helper methods to create the LineChart and export SvgCode
        JFreeChart chart = createLineChart(auditChartData);
        return getSvgXml(chart);

    }

    /**
     * Helper method to create a LineChart using JFree library.
     *
     * @param dataMap A map containing String, Long pairs.
     * @return a JFree line chart object.
     */
    private JFreeChart createLineChart(
            Map<String, Long> dataMap) {
        final DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
        dataMap.forEach((key, value) -> dataSet.addValue(value, "days", key));

        JFreeChart lineChart =
                ChartFactory.createLineChart(
                        "CySec Activity", "Days", "Count", dataSet, PlotOrientation.VERTICAL, true, true, false);
        ChartPanel chartPanel = new ChartPanel(lineChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(560, 367));
        return lineChart;
    }

    /**
     * Extracts the XML Code for an SVG from a JFreeChart.
     *
     * @param chart A JFree Chart.
     * @return a string representation of the SVG image based on the JFree Chart.
     */
    private String getSvgXml(JFreeChart chart) {
        int width = 1200;
        int height = 600;
        int pos = 0;
        final SVGGraphics2D svg2d = new SVGGraphics2D(width, height);
        chart.draw(svg2d, new Rectangle2D.Double(pos, pos, width, height));
        return svg2d.getSVGElement();
    }
}
