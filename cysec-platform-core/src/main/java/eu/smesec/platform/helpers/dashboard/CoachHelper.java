package eu.smesec.platform.helpers.dashboard;

import eu.smesec.bridge.generated.Block;

import eu.smesec.bridge.md.Rating;
import eu.smesec.bridge.md.Skills;
import eu.smesec.bridge.md.State;
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
    this.blocks = new ArrayList<>(blocks);
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
}
