package com.selenium.jayflow.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class MySQL {
  private String url = "jdbc:mysql://mycomputer.iptime.org:3306";
  private static MySQL mySQLInstance;
  private Connection con;

  private MySQL() {
  }

  public static MySQL getinstance() {
    if (mySQLInstance == null) {
      mySQLInstance = new MySQL();
    }
    return mySQLInstance;
  }

  public void select(String query, Object delivery, SelectCallback callback) {
    try {
      Statement st = con.createStatement();
      ResultSet rs = st.executeQuery(query);
      callback.queryResult(rs, delivery);
      st.close();
      rs.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public PreparedStatement getPrepareStatement(String query) throws SQLException {
    return con.prepareStatement(query);
  }

  public void openDBConnect() {
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
      this.con = DriverManager.getConnection(url, "userName", "password");
    } catch (ClassNotFoundException | SQLException e) {
      e.printStackTrace();
    }
  }

  public void closeDBConnect() {
    if (con != null) {
      try {
        con.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

}