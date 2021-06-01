package eu.smesec.core.cache;

import eu.smesec.bridge.execptions.CacheException;

public interface ICommand<T, R> {
  R execute(T t) throws CacheException;
}
