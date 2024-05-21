/*-
 * #%L
 * CYSEC Platform Core
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
package eu.smesec.cysec.platform.core.utils;

import eu.smesec.cysec.platform.bridge.generated.User;

import java.util.regex.Pattern;

public final class Validator {
  private static final Pattern regexWord = Pattern.compile("\\w*");
  private static final Pattern regexWordSpace = Pattern.compile("[\\w ]*");
  private static final Pattern regexEmail = Pattern.compile("\\w+(\\.\\w+)*\\@\\w+(\\.\\w+)+");
  private static final Pattern regexAnswer = Pattern.compile("[^\\/\\>\\<\\;\\?\\*\\!\\&\\{\\}]+");

  private Validator() {}

  /**
   * Checks if the input contains only word character: a-zA-Z_0-9.
   *
   * @param word input to check
   * @return <code>true</code> if the input contains only word character, or <code>false</code>
   *     otherwise
   */
  public static boolean validateWord(String word) {
    return word != null && regexWord.matcher(word).matches();
  }

  /**
   * Checks if the input contains only word character: a-zA-Z_ 0-9.
   *
   * @param word input to check
   * @return <code>true</code> if the input contains only word character, or <code>false</code>
   *     otherwise
   */
  public static boolean validateWordSpace(String word) {
    return word != null && regexWordSpace.matcher(word).matches();
  }

  /**
   * Checks if the mail address is valid.
   *
   * @param email email address to check
   * @return <code>true</code> if the email address is valid, or <code>false</code> otherwise
   */
  public static boolean validateEmail(String email) {
    return email != null && regexEmail.matcher(email).matches();
  }

  /**
   * Checks if the input contains non of the characters: /&gt;&lt;;?*!&amp;{} .
   *
   * @param input input to check
   * @return <code>true</code> if the input contains non of the specified characters, or <code>false
   *     </code> otherwise
   */
  public static boolean validateAnswer(String input) {
    return input != null && regexAnswer.matcher(input).matches();
  }

  /**
   * Checks if an user object is valid.
   *
   * @param user user object
   * @return <code>true</code> if the user is valid, or <code>false</code> otherwise
   */
  public static boolean validateUser(User user) {
    return validateWord(user.getUsername())
        && validateEmail(user.getEmail())
        && validateWordSpace(user.getFirstname())
        && validateWordSpace(user.getSurname())
        && user.getRole().stream().allMatch(Validator::validateWord);
  }
}
