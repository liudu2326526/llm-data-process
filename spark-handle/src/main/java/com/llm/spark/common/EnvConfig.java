package com.llm.spark.common;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.StringUtils;

public class EnvConfig extends PropertiesConfiguration {

  private static final String CONFIG_FILE = "/%s.properties";
  private static final String DEFAULT_ENV = "dev";

  public EnvConfig(InputStream is) {
    try {
      load(is);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static EnvConfig loadConfig(String env) {
    if (StringUtils.isEmpty(env)) {
      env = DEFAULT_ENV;
    }

    try (InputStream input =
        EnvConfig.class.getResourceAsStream(String.format(CONFIG_FILE, env.toLowerCase()))) {
      return new EnvConfig(input);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
