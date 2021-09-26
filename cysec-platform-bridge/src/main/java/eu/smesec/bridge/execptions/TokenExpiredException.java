package eu.smesec.bridge.execptions;

import eu.smesec.bridge.generated.Token;

public class TokenExpiredException extends CacheException {
  private Token token;

  public TokenExpiredException(Token token) {
    super("token (" + token.getId() + ") already expired: " + token.getExpiry().toString());
    this.token = token;
  }
}
