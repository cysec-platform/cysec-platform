/*-
 * #%L
 * CYSEC Platform Core
 * %%
 * Copyright (C) 2020 - 2025 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
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
package eu.smesec.cysec.platform.core.helpers.dashboard;

import eu.smesec.cysec.platform.bridge.generated.Block;

import eu.smesec.cysec.platform.bridge.md.Rating;
import eu.smesec.cysec.platform.bridge.md.Skills;
import eu.smesec.cysec.platform.bridge.md.State;
import eu.smesec.cysec.platform.core.json.CoachMetaData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CoachHelper {
  private String id;
  private String readableName;
  private String description;
  private String icon;
  private State state;
  private Skills skills;
  private Rating rating;
  private List<Block> blocks;
  private List<CoachMetaData> visibleCoachMetadata;

  /**
   * Coach visible data container constructor.
   *
   * @param id The id of the coach
   * @param readableName The name of the coach
   */
  public CoachHelper(String id, String readableName) {
    this.id = id;
    this.readableName = readableName;
  }

  public Skills getSkills() {
    return skills;
  }

  public void setSkills(Skills skills) {
    this.skills = skills;
  }

  public String getIcon() {
    return icon;
  }

  public void setIcon(String icon) {
    this.icon = icon;
  }

  public void setBlocks(Collection<Block> blocks) {
    this.blocks = (blocks==null) ? new ArrayList<>():new ArrayList<>(blocks);
  }

  public String getId() {
    return id;
  }

  public String getReadableName() {
    return readableName;
  }

  public String getDescription() {
    return description != null ? description : "";
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<Block> getBlocks() {
    return blocks;
  }

  public Rating getRating() {
    return rating;
  }

  public void setRating(Rating rating) {
    this.rating = rating;
  }

  public void setState(State state) {
    this.state = state;
  }

  public State getState() {
    return state;
  }

  public void setVisibleCoachMetadata(List<CoachMetaData> metadata) {
    this.visibleCoachMetadata = (metadata==null) ? new ArrayList<>():new ArrayList<>(metadata);
  }

  public List<CoachMetaData> getVisibleCoachMetadata() {
    return visibleCoachMetadata;
  }
}
