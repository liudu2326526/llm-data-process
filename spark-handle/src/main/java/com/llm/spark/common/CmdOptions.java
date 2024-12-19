package com.llm.spark.common;

import com.google.common.collect.Maps;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


public class CmdOptions {

  private static CmdOptions options;

  private EnvConfig envConfig;
  private CommandLine commandLine;

  private CmdOptions(CommandLine commandLine) {
    this.commandLine = commandLine;
    envConfig = EnvConfig.loadConfig(this.getEnv());
  }

  public static CmdOptions getInstance() {
    if (options == null) {
      throw new IllegalStateException("must init first");
    }
    return options;
  }

  public static void init(String[] args) {
//    log.info("args:");
    for (String arg : args) {
//      log.info(arg);
    }
    options = new CmdOptions(parseCommandLine(args));
  }

  private static CommandLine parseCommandLine(String[] args) {
    CommandLine commandLine;
    Options options = new Options();
    options.addOption("i", "input", true, "the input data location");
    options.addOption("o", "output", true, "the output result location");
    options.addOption("d", "date", true, "the last date");
    options.addOption("c", "count", true, "the date range");
    options.addOption("e", "env", true, "the env online/staging/debug");
    options.addOption("p", "partition", true, "output partitions");
    options.addOption("q", "query", true, "the sql query");
    options.addOption("b", "partitionkey", true, "the partition key list, separated by ,");
    options.addOption("t", "delta", true, "the delta data");
    options.addOption("a", "format", true, "the input/output format");
    options.addOption("s", "bucket", true, "the input/output s3 bucket");
    options.addOption("k", "key", true, "the input/output s3 key");
    options.addOption("m", "method", true, "the partition method repartition/coalesce");
    options.addOption("f", "options", true, "the input/output options");
    options.addOption("n", "name", true, "the process name");
    try {
      commandLine = new GnuParser().parse(options, args);
    } catch (ParseException e) {
      throw new RuntimeException("wrong arguments", e);
    }
    return commandLine;
  }

  public String getInputLocation() {
    return commandLine.getOptionValue("i");
  }

  public String getOutputLocation() {
    return commandLine.getOptionValue("o");
  }

  public String getEndDate() {
    return commandLine.getOptionValue("d");
  }

  public int getCount() {
    return Integer.parseInt(commandLine.getOptionValue("c"));
  }

  public String getEnv() {
    return commandLine.getOptionValue("e");
  }

  public int getPartition() {
    return Integer.parseInt(commandLine.getOptionValue("p"));
  }

  public String getQuery() {
    return commandLine.getOptionValue("q");
  }

  public String getFormat() {
    return commandLine.getOptionValue("a", "csv");
  }

  public String getPartitionKey() {
    return commandLine.getOptionValue("b", null);
  }

  public String getName() {
    return commandLine.getOptionValue("n", null);
  }

  public String getDelta() {
    return commandLine.getOptionValue("t", null);
  }

  public String getBucket() {
    return commandLine.getOptionValue("s", null);
  }

  public String getKey() {
    return commandLine.getOptionValue("k", null);
  }

  public String getMethod() {
    return commandLine.getOptionValue("m", "repartition");
  }

  public Map<String, String> getOptions() throws ArrayIndexOutOfBoundsException {
    String optionsString = commandLine.getOptionValue("f", null);
    Map<String, String> options = Maps.newHashMap();
    if (optionsString == null || optionsString.equals("")) {
      return options;
    }

    for (String option : optionsString.split("~~")) {
      options.put(option.split("==")[0], option.split("==")[1]);
    }
    return options;
  }

  public EnvConfig getEnvConfig() {
    return envConfig;
  }
}
