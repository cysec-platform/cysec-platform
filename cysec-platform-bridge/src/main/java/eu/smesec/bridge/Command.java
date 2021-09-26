package eu.smesec.bridge;

public class Command {

  private String function;
  private String[] arguments;

  public Command(String function, String[] arguments) {
    this.function = function;
    this.arguments = arguments;
  }

  public Command(String function) {
    this.function = function;
    this.arguments = null;
  }

  public String getFunction() {
    return function;
  }

  public String[] getArguments() {
    return arguments;
  }
}
