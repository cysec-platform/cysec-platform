package eu.smesec.cysec.platform.bridge.execptions;

import eu.smesec.cysec.platform.bridge.generated.Token;

public class TokenExpiredException extends CacheException {
  private Token token;

  public TokenExpiredException(Token token) {
    super("token (" + token.getId() + ") already expired: " + token.getExpiry().toString());
    this.token = token;
  }
}
