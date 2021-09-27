package eu.smesec.cysec.platform.bridge.execptions;

import eu.smesec.cysec.platform.bridge.generated.Locks;

public class LockedExpetion extends CacheException {
  protected Locks lock;

  public LockedExpetion(String username, Locks lock) {
    super("User " + username + " is currently locked: " + lock.value());
    this.lock = lock;
  }
}
