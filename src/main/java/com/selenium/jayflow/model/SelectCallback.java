package com.selenium.jayflow.model;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface SelectCallback {
  void queryResult(ResultSet rs, Object delivery) throws SQLException;
}
