package com.mycosmeticshop.utils;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    // ====== DỊCH TỪ DJANGO settings.py ======
    private static final String URL =
        "jdbc:sqlserver://localhost:1433;"
      + "databaseName=mycosmetic_shop_db;"
      + "encrypt=false;"
      + "trustServerCertificate=true";

    private static final String USER = "sa";
    private static final String PASSWORD = "123456";

    // ====== LOAD DRIVER (thay cho ODBC Driver 17) ======
    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                "❌ Không tìm thấy SQL Server JDBC Driver", e
            );
        }
    }

    // ====== GET CONNECTION ======
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            throw new RuntimeException(
                "❌ Không kết nối được SQL Server", e
            );
        }
    }
}
