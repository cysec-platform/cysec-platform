package eu.smesec.core.utils;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public final class LocaleUtils {
  private static final Set<String> languages =
      Arrays.stream(Locale.getISOLanguages()).collect(Collectors.toSet());
  private static final Set<String> countries =
      Arrays.stream(Locale.getISOCountries()).collect(Collectors.toSet());

  private LocaleUtils() {}

  /**
   * Parses, validates and creates a locale from a string. Returns english as fallback.
   * Local ISO-693-1-code
   * @see <a href="https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes">Wikipedia List of 639-1 codes</a>
   * @param locale ISO-639-1-code
   * @return locale object
   */
  public static Locale fromString(String locale) {
    if (locale != null) {
      Locale locale1 = Locale.forLanguageTag(locale.replace('_', '-'));
      if (isLanguage(locale1.getLanguage())) {
        return locale1;
      }
    }
    return Locale.ENGLISH;
  }

  /**
   * Checks, if a language tag is valid.
   *
   * @param language language tag
   * @return <code>true</code> if the language tag is valid, or <code>false</code> otherwise
   */
  public static boolean isLanguage(String language) {
    if (language == null || language.isEmpty()) {
      return false;
    }
    return languages.contains(language.toLowerCase());
  }

  /**
   * Checks, if a country tag is valid.
   *
   * @param country country tag
   * @return <code>true</code> if the country tag is valid, or <code>false</code> otherwise
   */
  public static boolean isCountry(String country) {
    if (country == null || country.isEmpty()) {
      return false;
    }
    return countries.contains(country.toUpperCase());
  }
}
