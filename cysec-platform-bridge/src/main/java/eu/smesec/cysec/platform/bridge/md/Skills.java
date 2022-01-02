/*-
 * #%L
 * CYSEC Platform Bridge
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
package eu.smesec.cysec.platform.bridge.md;

import eu.smesec.cysec.platform.bridge.md.annotations.MdNamespace;
import eu.smesec.cysec.platform.bridge.md.annotations.MvKey;

@MdNamespace(MetadataUtils.MD_SKILLS)
public class Skills {
  @MvKey(MetadataUtils.MV_IMAGE)
  protected String image;

  @MvKey(MetadataUtils.MV_STRENGTH)
  protected double strength;
  @MvKey(MetadataUtils.MV_STRENGTH_MAX)
  protected double strengthMax;

  @MvKey(MetadataUtils.MV_KNOW_HOW)
  protected double knowHow;
  @MvKey(MetadataUtils.MV_KNOW_HOW_MAX)
  protected double knowHowMax;

  @MvKey(MetadataUtils.MV_ENDURANCE)
  protected int endurance;

  public Skills() {}

  public Skills(String image, double strength, double strengthMax,
                double knowHow, double knowHowMax, int endurance) {
    this.image = image;
    this.strength = strength;
    this.strengthMax = strengthMax;
    this.knowHow = knowHow;
    this.knowHowMax = knowHowMax;
    this.endurance = endurance;
  }

  public String getImage() {
    return image;
  }

  public double getStrength() {
    return strength;
  }

  public double getStrengthMax() {
    return strengthMax;
  }

  public double getKnowHow() {
    return knowHow;
  }

  public double getKnowHowMax() {
    return knowHowMax;
  }

  public int getEndurance() {
    return endurance;
  }
}
