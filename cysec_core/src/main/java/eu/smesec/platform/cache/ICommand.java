package eu.smesec.platform.cache;

import eu.smesec.bridge.execptions.CacheException;

public interface ICommand<T, R> {
  R execute(T t) throws CacheException;
}
