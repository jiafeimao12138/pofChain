package com.example.base.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CmdArgsParser
{
    private final Map<String, String> options;
    private final List<String> args;
    private static volatile CmdArgsParser instance;

    public CmdArgsParser(String[] args) {
        this.args = new ArrayList<>();
        this.options = new HashMap<>(16);
        parse(args);
    }

    public static synchronized CmdArgsParser getInstance(String[] args)
    {
        return new CmdArgsParser(args);
    }

    public void parse(String[] args)
    {
        for (String arg : args) {
            this.args.add(arg);
            if (arg.startsWith("-")) {
                String[] items = arg.split("=");
                options.put(items[0].substring(1,items[0].length()), items[1]);
            }
        }

    }

    public List<String> getArgs() {
        return args;
    }

    public String getOption(String key)
    {
        return options.get(key);
    }

    public String getOption(String key, String defaultValue)
    {
        return options.get(key) == null ? defaultValue : options.get(key);
    }


}
