package eu.smesec.cysec.platform.core.cache;

import eu.smesec.cysec.platform.bridge.execptions.CacheException;

public interface ICommand<T, R> {
  R execute(T t) throws CacheException;
}
