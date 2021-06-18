package eu.smesec.core.endpoints;

import eu.smesec.bridge.generated.Metadata;
import eu.smesec.bridge.md.MetadataUtils;
import eu.smesec.bridge.md.Rating;
import java.util.HashMap;
import java.util.Map;


public class CompaniesModel {

    public static Map<String, Object> getCompaniesModel(Metadata md) {

        Map<String, Object> model = new HashMap<>();
        model.put("grade", "n/a");
        model.put("score", "0.0");
        model.put("audits", "No data available");

        if (md != null) {
            Rating rating = MetadataUtils.fromMd(md, Rating.class);
            String grade = rating.getGrade();
            if (grade != null) {
                model.put("grade", grade);
                model.put("score", Double.toString(rating.getScore()));
            }
        }
        return model;
    }
}
