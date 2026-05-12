package com.example.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DBUtil {
    private static Properties props = new Properties();

    static {
        try {
            props.load(DBUtil.class.getClassLoader().getResourceAsStream("db.properties"));
            Class.forName(props.getProperty("jdbc.driver"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(
                props.getProperty("jdbc.url"),
                props.getProperty("jdbc.username"),
                props.getProperty("jdbc.password")
        );
    }
}