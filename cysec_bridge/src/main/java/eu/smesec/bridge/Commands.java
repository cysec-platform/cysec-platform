package eu.smesec.bridge;

public enum Commands {
  LOAD_BLOCK,
  LOAD_QUESTION,
  ADD_RECOMMENDATION,
  REMOVE_RECOMMENDATION,
  UPDATE_ACTIVE_BLOCKS,
  UPDATE_AVAILABLE_BLOCKS,
  UPDATE_ACTIVE_QUESTIONS,
  SET_NEXT;

  @Override
  public String toString() {
    String[] parts = name().split("_");
    StringBuilder sb = new StringBuilder();

    // loop through all parts of the name
    for (int i = 0; i < parts.length; i++) {
      String word = parts[i];
      // use a lowercase letter for the first word
      if (i == 0) {
        sb.append(word.toLowerCase());
      // follow camel case pattern (first letter capital)
      } else {
        sb.append(String.valueOf(word.charAt(0)));
        sb.append(word.substring(1, word.length()).toLowerCase());
      }
    }
    return sb.toString();
  }
}
