package eu.smesec.platform.utils;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public final class LocaleUtils {
  private static final Set<String> languages = Arrays.stream(Locale.getISOLanguages())
      .collect(Collectors.toSet());
  private static final Set<String> countries = Arrays.stream(Locale.getISOCountries())
      .collect(Collectors.toSet());

  private LocaleUtils() {}

  public static Locale fromString(String locale) {
    if (locale != null) {
      Locale locale1 = Locale.forLanguageTag(locale.replace('_', '-'));
      if (isLanguage(locale1.getLanguage())) {
        return locale1;
      }
    }
    return Locale.ENGLISH;
  }

  public static boolean isLanguage(String language) {
    if (language == null || language.isEmpty()) {
      return false;
    }
    return languages.contains(language.toLowerCase());
  }

  public static boolean isCountry(String country) {
    if (country == null || country.isEmpty()) {
      return false;
    }
    return countries.contains(country.toUpperCase());
  }
}
