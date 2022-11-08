/*
 * Copyright (C) 2016 AriaLyy(https://github.com/AriaLyy/Aria)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arialyy.aria.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * aria 服务加载器
 * 参考{@link ServiceLoader}
 */
public class AriaServiceLoader<S> {
  private static final String TAG = "AriaServiceLoader";
  private static final String PREFIX = "META-INF/services/";

  // The class or interface representing the service being loaded
  private final Class<S> service;

  // The class loader used to locate, load, and instantiate providers
  private final ClassLoader loader;

  // The access control context taken when the ServiceLoader is created
  // Android-changed: do not use legacy security code.
  // private final AccessControlContext acc;

  private LazyLoader lazyLoader;

  public void reload() {
    lazyLoader = new LazyLoader(service, loader);
  }

  private AriaServiceLoader(Class<S> svc, ClassLoader cl) {
    service = svc;
    loader = (cl == null) ? ClassLoader.getSystemClassLoader() : cl;
    // Android-changed: Do not use legacy security code.
    // On Android, System.getSecurityManager() is always null.
    // acc = (System.getSecurityManager() != null) ? AccessController.getContext() : null;
    reload();
  }

  public static <S> AriaServiceLoader<S> load(Class<S> service, ClassLoader loader) {
    return new AriaServiceLoader<>(service, loader);
  }

  public static <S> AriaServiceLoader<S> load(Class<S> service) {
    Thread.currentThread().setContextClassLoader(AriaServiceLoader.class.getClassLoader());
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    return AriaServiceLoader.load(service, cl);
  }

  public S getService(String serviceName) {
    return lazyLoader.loadService(serviceName);
  }

  private class LazyLoader {
    private Class<S> service;
    private ClassLoader loader;
    private Enumeration<URL> configs = null;
    private List<String> pending = null;

    private LazyLoader(Class<S> service, ClassLoader loader) {
      this.service = service;
      this.loader = loader;
      parseConfig();
    }

    // Parse the content of the given URL as a provider-configuration file.
    //
    // @param  service
    //         The service type for which providers are being sought;
    //         used to construct error detail strings
    //
    // @param  u
    //         The URL naming the configuration file to be parsed
    //
    // @return A (possibly empty) iterator that will yield the provider-class
    //         names in the given configuration file that are not yet members
    //         of the returned set
    //
    // @throws ServiceConfigurationError
    //         If an I/O error occurs while reading from the given URL, or
    //         if a configuration-file format error is detected
    //
    private List<String> parse(Class<?> service, URL u) throws ServiceConfigurationError {
      InputStream in = null;
      BufferedReader r = null;
      ArrayList<String> names = new ArrayList<>();
      try {
        in = u.openStream();
        r = new BufferedReader(new InputStreamReader(in, "utf-8"));
        int lc = 1;
        while ((lc = parseLine(service, u, r, lc, names)) >= 0) ;
      } catch (IOException x) {
        fail(service, "Error reading configuration file", x);
      } finally {
        try {
          if (r != null) r.close();
          if (in != null) in.close();
        } catch (IOException y) {
          fail(service, "Error closing configuration file", y);
        }
      }
      return names;
    }

    private void fail(Class<?> service, String msg, Throwable cause)
        throws ServiceConfigurationError {
      throw new ServiceConfigurationError(service.getName() + ": " + msg,
          cause);
    }

    private void fail(Class<?> service, String msg)
        throws ServiceConfigurationError {
      throw new ServiceConfigurationError(service.getName() + ": " + msg);
    }

    private void fail(Class<?> service, URL u, int line, String msg)
        throws ServiceConfigurationError {
      fail(service, u + ":" + line + ": " + msg);
    }

    // Parse a single line from the given configuration file, adding the name
    // on the line to the names list.
    //
    private int parseLine(Class<?> service, URL u, BufferedReader r, int lc,
        List<String> names)
        throws IOException, ServiceConfigurationError {
      String ln = r.readLine();
      if (ln == null) {
        return -1;
      }
      int ci = ln.indexOf('#');
      if (ci >= 0) ln = ln.substring(0, ci);
      ln = ln.trim();
      int n = ln.length();
      if (n != 0) {
        if ((ln.indexOf(' ') >= 0) || (ln.indexOf('\t') >= 0)) {
          fail(service, u, lc, "Illegal configuration-file syntax");
        }
        int cp = ln.codePointAt(0);
        if (!Character.isJavaIdentifierStart(cp)) {
          fail(service, u, lc, "Illegal provider-class name: " + ln);
        }
        for (int i = Character.charCount(cp); i < n; i += Character.charCount(cp)) {
          cp = ln.codePointAt(i);
          if (!Character.isJavaIdentifierPart(cp) && (cp != '.')) {
            fail(service, u, lc, "Illegal provider-class name: " + ln);
          }
        }
        if (!names.contains(ln)) {
          names.add(ln);
        }
      }
      return lc + 1;
    }

    private S loadService(String serviceName) {
      if (pending == null){
        ALog.e(TAG, "pending为空");
        return null;
      }
      for (String s : pending) {
        if (s.equals(serviceName)) {
          Class<?> c = null;
          try {
            c = Class.forName(serviceName, false, loader);
          } catch (ClassNotFoundException x) {
            fail(service,
                // Android-changed: Let the ServiceConfigurationError have a cause.
                "Provider " + serviceName + " not found", x);
            // "Provider " + cn + " not found");
          }
          if (!service.isAssignableFrom(c)) {
            // Android-changed: Let the ServiceConfigurationError have a cause.
            ClassCastException cce = new ClassCastException(
                service.getCanonicalName() + " is not assignable from " + c.getCanonicalName());
            fail(service,
                "Provider " + serviceName + " not a subtype", cce);
            // fail(service,
            //        "Provider " + cn  + " not a subtype");
          }
          try {
            return service.cast(c.newInstance());
          } catch (Throwable x) {
            fail(service, "Provider " + serviceName + " could not be instantiated", x);
          }
        }
      }

      throw new Error();          // This cannot happen
    }

    /**
     * 解析配置文件
     */
    private void parseConfig() {
      if (configs == null) {
        try {
          String fullName = PREFIX + service.getName();
          if (loader == null) {
            configs = ClassLoader.getSystemResources(fullName);
          } else {
            configs = loader.getResources(fullName);
          }
        } catch (IOException x) {
          fail(service, "Error locating configuration files", x);
        }
      }
      while ((pending == null) || pending.isEmpty()) {
        if (!configs.hasMoreElements()) {
          return;
        }
        pending = parse(service, configs.nextElement());
      }
    }
  }
}
