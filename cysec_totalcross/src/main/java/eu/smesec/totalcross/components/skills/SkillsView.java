package eu.smesec.totalcross.components.skills;

import eu.smesec.totalcross.util.Colors;
import totalcross.io.IOException;
import totalcross.ui.*;
import totalcross.ui.font.Font;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;
import totalcross.util.UnitsConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class SkillsView extends ScrollContainer {

    int gap = UnitsConverter.toPixels(DP + 8);
    ArrayList<MockSkill> mockSkills;
    ArrayList<MockAchievedLevel> mockAchievedLevels;

    public SkillsView() {
        mockSkills = new ArrayList<>();
        mockSkills.add(new MockSkill("strength", 1, 1.0));
        mockSkills.add(new MockSkill("know-how", 1, 3.0));
        mockSkills.add(new MockSkill("fitness", 1, 5.0));
        mockAchievedLevels = new ArrayList<>();
        mockAchievedLevels.add(new MockAchievedLevel("Company", "C(3.0)"));
        mockAchievedLevels.add(new MockAchievedLevel("Company", "C(3.0)"));
        mockAchievedLevels.add(new MockAchievedLevel("Company", "C(3.0)"));
        mockAchievedLevels.add(new MockAchievedLevel("Company", "C(3.0)"));
        mockAchievedLevels.add(new MockAchievedLevel("Company", "C(3.0)"));
        mockAchievedLevels.add(new MockAchievedLevel("Company", "C(3.0)"));
    }

    @Override
    public void initUI() {
        setInsets(gap, gap, gap, gap);

//       Title
        Label skillsTitleLbl = new Label("Company Skills");
        skillsTitleLbl.setFont(Font.getFont("Lato Medium", false, skillsTitleLbl.getFont().size + 6));
        add(skillsTitleLbl, LEFT, TOP);

//        Image

//        Skills
        for (MockSkill mockSkill : mockSkills) {
            Container skillContainer = getSkillContainer(mockSkill);
            add(skillContainer, LEFT, AFTER + 2 * gap, FILL, PREFERRED);
            resizeHeight();
        }

//        Levels Achieved
        Label achievedLvlLbl = new Label("Levels Achieved");
        achievedLvlLbl.setFont(Font.getFont("Lato Medium", false, achievedLvlLbl.getFont().size + 6));
        add(achievedLvlLbl, LEFT, AFTER + 3 * gap);

        for (MockAchievedLevel achievedLevel : mockAchievedLevels) {
            Container achievedLevelsContainer = getAchievedLevelsContainer(achievedLevel);
            add(achievedLevelsContainer, LEFT, AFTER + 2 * gap, FILL, PREFERRED);
            resizeHeight();
        }

//        Latest achievements
        Label latestAchievementsLbl = new Label("Latest Achievements");
        latestAchievementsLbl.setFont(Font.getFont("Lato Medium", false, latestAchievementsLbl.getFont().size + 6));
        add(latestAchievementsLbl, LEFT, AFTER + 3 * gap);
        Container latestAchievementsContainer = getLatestAchievementsContainer(new ArrayList<>(Arrays.asList("images/recommendation_bulb.png", "images/recommendation_bulb.png", "images/recommendation_bulb.png", "images/recommendation_bulb.png", "images/recommendation_bulb.png", "images/recommendation_bulb.png", "images/recommendation_bulb.png", "images/recommendation_bulb.png", "images/recommendation_bulb.png", "images/recommendation_bulb.png", "images/recommendation_bulb.png", "images/recommendation_bulb.png", "images/recommendation_bulb.png", "images/recommendation_bulb.png", "images/recommendation_bulb.png", "images/recommendation_bulb.png", "images/recommendation_bulb.png", "images/recommendation_bulb.png", "images/recommendation_bulb.png", "images/recommendation_bulb.png", "images/recommendation_bulb.png", "images/recommendation_bulb.png")));
        add(latestAchievementsContainer, LEFT, AFTER + 2 * gap, FILL, PARENTSIZEMIN);
//
        resize();
    }

    private Container getLatestAchievementsContainer(ArrayList<String> achievementImagePaths) {

        return new Container() {
            Container achievementContainer;
            final int badgesPerRow = 8;
            Image badgeImg;

            @Override
            public void initUI() {
                for (int i = 0; i < achievementImagePaths.size(); i++) {
                    if (i % badgesPerRow == 0) {
                        achievementContainer = new Container();
                        add(achievementContainer, CENTER, i == 0 ? AFTER : AFTER + gap, PARENTSIZEMAX, PREFERRED);
                    }
                    try {
                        badgeImg = new Image(achievementImagePaths.get(i));
                        badgeImg.scaledBy(2, 2);
                        badgeImg.applyChanges();
                    } catch (ImageException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (badgeImg != null) {
                        ImageControl imageControl = new ImageControl(badgeImg);
                        imageControl.scaleToFit = true;
                        imageControl.centerImage = true;
                        achievementContainer.add(imageControl, AFTER + gap, TOP, PREFERRED, PREFERRED + gap);
                        achievementContainer.resizeHeight();
                    }
                }
                resize();
            }
        };

    }

    private Container getAchievedLevelsContainer(MockAchievedLevel achievedLevel) {
        final int margin = UnitsConverter.toPixels(DP + 16);

        return new Container() {
            @Override
            public void initUI() {
                setBackColor(0xd3d6de);
                setInsets(margin, margin, margin, margin);

                Label achievedLvlTitleLbl = new Label(achievedLevel.getTitle());
                achievedLvlTitleLbl.setFont(Font.getFont("Lato Medium", false, achievedLvlTitleLbl.getFont().size + 2));

                Label achievedLvlLbl = new Label(achievedLevel.getLevel());
                achievedLvlLbl.setFont(Font.getFont("Lato Medium", false, achievedLvlLbl.getFont().size + 3));
                add(achievedLvlTitleLbl, LEFT, CENTER);
                add(achievedLvlLbl, RIGHT, CENTER);
                resizeHeight();
            }
        };
    }

    private Container getSkillContainer(MockSkill mockSkill) {
        return new Container() {
            @Override
            public void initUI() {

                Label levelLbl = new Label("Level " + mockSkill.getLevel());
                Label skillTitle = new Label(mockSkill.getTitle().toUpperCase(Locale.ROOT));
                ProgressBar progressBar = new ProgressBar();
                progressBar.max = 5;
                progressBar.highlight = true;
                progressBar.setValue((int) mockSkill.getScore());
                progressBar.suffix = mockSkill.score + " / " + (float) progressBar.max;
                progressBar.setBackForeColors(Colors.GRAY, 0x329bb8);
                progressBar.drawValue = false;
                progressBar.drawText = true;
                add(levelLbl, LEFT, AFTER, PREFERRED, PREFERRED);
                add(skillTitle, CENTER, SAME, PREFERRED, PREFERRED);
                add(progressBar, LEFT, AFTER + gap, FILL, PREFERRED);
                resizeHeight();
            }
        };
    }

    private static class MockSkill {
        private final String title;
        private final int level;
        private final double score;

        public MockSkill(String title, int level, double score) {
            this.title = title;
            this.level = level;
            this.score = score;
        }

        public String getTitle() {
            return title;
        }

        public int getLevel() {
            return level;
        }

        public double getScore() {
            return score;
        }
    }

    private static class MockAchievedLevel {
        private final String title;
        private final String level;

        public MockAchievedLevel(String title, String level) {
            this.title = title;
            this.level = level;
        }

        public String getTitle() {
            return title;
        }

        public String getLevel() {
            return level;
        }
    }
}


