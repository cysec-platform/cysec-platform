/*-
 * #%L
 * CYSEC Platform Bridge
 * %%
 * Copyright (C) 2020 - 2024 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
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
package eu.smesec.cysec.platform.bridge.md;

import eu.smesec.cysec.platform.bridge.generated.Metadata;
import eu.smesec.cysec.platform.bridge.md.annotations.MdNamespace;
import eu.smesec.cysec.platform.bridge.md.annotations.MvKey;

import java.util.Arrays;
import java.util.Map;

@MdNamespace(MetadataUtils.MD_RATING)
public class Rating {
  @MvKey(MetadataUtils.MV_MICRO_SCORE)
  protected double score;
  @MvKey(MetadataUtils.MV_MICRO_GRADE)
  protected String grade;

  public Rating() {}

  public Rating(double score, String grade) {
    this.score = score;
    this.grade = grade;
  }

  public double getScore() {
    return score;
  }

  public String getGrade() {
    return grade;
  }

  @Deprecated
  public static Metadata toMd(Rating rating)  {
    return MetadataUtils.createMetadata(MetadataUtils.MD_RATING,
          Arrays.asList(
                MetadataUtils.createMvalueStr(MetadataUtils.MV_MICRO_SCORE, Double.toString(rating.score)),
                MetadataUtils.createMvalueStr(MetadataUtils.MV_MICRO_GRADE, rating.grade)
          ));
  }

  @Deprecated
  public static Rating fromMd(Metadata md) {
    String key = md.getKey();
    Map<String, MetadataUtils.SimpleMvalue> values = MetadataUtils.parseMvalues(md.getMvalue());
    if (!key.equals(MetadataUtils.MD_RATING)) {
      throw new IllegalArgumentException("Metadata key has wrong namespace for recommendation");
    }
    return new Rating(Double.parseDouble(values.get(MetadataUtils.MV_MICRO_SCORE).getValue()),
          values.get(MetadataUtils.MV_MICRO_GRADE).getValue());
  }
}
