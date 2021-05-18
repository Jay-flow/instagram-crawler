package com.selenium.jayflow;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

import java.sql.Timestamp;
import java.sql.Types;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.selenium.jayflow.model.MySQL;
import com.selenium.jayflow.model.SelectCallback;
import com.selenium.jayflow.utils.Delay;
import com.selenium.jayflow.utils.SlackMsg;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.sql.PreparedStatement;
import java.util.HashMap;
import com.selenium.jayflow.utils.ImageHandling;

public class Flow implements Runnable {
  private WebDriver driver;
  private WebDriverWait wait;
  private final MySQL mysql = MySQL.getinstance();
  private final Delay delay = Delay.getinstance();
  private final ImageHandling imageHandling = new ImageHandling();
  private final SlackMsg slack = new SlackMsg();
  private boolean isEnglish;
  private boolean isNotification;

  public Flow(boolean isEnglish, boolean isNotification) {
    this.isEnglish = isEnglish;
    this.isNotification = isNotification;
  }

  @Override
  public void run() {
    long startTime = System.currentTimeMillis();
    try {
      mysql.openDBConnect();
      this.driver = new ChromeDriver();
      this.wait = new WebDriverWait(driver, 10);
      sendSlackMessage(":rocket: i_cake you의 크롤링을 시작합니다.");
      driver.get("https://www.instagram.com/");
      login();
      closeLoginSaveInformation();
      closeNotificationConfirm();
      inputUserNickName();
      driver.quit();
      checkAccessibleImageURL();
    } catch (Exception e) {
      sendSlackMessage(String.format(":bug: i_cake you크롤링 중 버그가 발생하였습니다.\n```%s```", e.toString()));
    } finally {
      mysql.closeDBConnect();
      long stopTime = System.currentTimeMillis();
      long elapsedTime = stopTime - startTime;
      long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime);
      sendSlackMessage(String.format(":birthday: i_cake you의 크롤링이 완료되었습니다.(소요시간: %s분)", minutes));
    }
  }

  private void sendSlackMessage(String message) {
    if(this.isNotification) {
      slack.send(message);
    }
  }

  private void deleteCakeCompany(int cake_company_id) {
    try {
      String query = "update cake_company set deleted_at=? where id=?";
      PreparedStatement ps = mysql.getPrepareStatement(query);
      ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
      ps.setInt(2, cake_company_id);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private void findDeletedCakeCompany(int cake_company_id, Timestamp deleted_at) {
    if (deleted_at != null) {
      try {
        String query = "update cake_company set deleted_at=? where id=?";
        PreparedStatement ps = mysql.getPrepareStatement(query);
        ps.setNull(1, Types.TIMESTAMP);
        ps.setInt(2, cake_company_id);
        ps.executeUpdate();
        ps.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  private void insertUserProfileImageURL(int cake_company_id) {
    WebElement profileImage = wait.until(presenceOfElementLocated(By.cssSelector("img[data-testid='user-avatar']")));
    String profileImageURL = profileImage.getAttribute("src");
    updateUserProfileImage(profileImageURL, cake_company_id);
  }

  private void updateUserProfileImage(String url, int cake_company_id) {
    try {
      String query = "update cake_company set profile_img=? where id=?";
      PreparedStatement ps = mysql.getPrepareStatement(query);
      ps.setString(1, url);
      ps.setInt(2, cake_company_id);
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private void updateCakeImage(String imageURL, int cake_company_id) {
    String query = String.format("select * from cake_img where url='%s' and cake_company_id=%s", imageURL,
        cake_company_id);
    HashMap<String, Object> inputDelevery = new HashMap<>();
    inputDelevery.put("imageURL", imageURL);
    inputDelevery.put("cake_company_id", cake_company_id);
    mysql.select(query, inputDelevery, new SelectCallback() {
      @Override
      @SuppressWarnings("unchecked")
      public void queryResult(ResultSet rs, Object delevery) throws SQLException {
        if (!rs.next()) {
          String insertQuery = "insert into cake_img (cake_company_id, url) values (?, ?)";
          PreparedStatement ps = mysql.getPrepareStatement(insertQuery);
          HashMap<String, Object> h = (HashMap<String, Object>) delevery;
          ps.setInt(1, (int) h.get("cake_company_id"));
          ps.setString(2, (String) h.get("imageURL"));
          ps.execute();
          ps.close();
        }
      }
    });
  }

  private void insertCakeImage(int cake_company_id) {
    try {
      wait.until(presenceOfElementLocated(By.cssSelector(".v1Nh3 img")));
      List<WebElement> images = driver.findElements(By.cssSelector(".v1Nh3 img"));
      int index = 1;
      for (WebElement image : images) {
        if (index > 12) {
          break;
        }
        String imageURL = image.getAttribute("src");
        if (imageHandling.isAccessibleURL(imageURL)) {
          updateCakeImage(imageURL, cake_company_id);
          index += 1;
        }
      }
    } catch (NoSuchElementException | TimeoutException e) {
      deleteCakeCompany(cake_company_id);
    }
  }

  private boolean searchUserByNickname(String nickname, int cake_company_id, Timestamp deleted_at) {
    String searchText = isEnglish ? "Search" : "검색";
    WebElement searchElement = wait
        .until(presenceOfElementLocated(By.cssSelector(String.format("input[placeholder='%s']", searchText))));
    searchElement.sendKeys(nickname);
    try {
      WebElement nicknameElement = wait
          .until(presenceOfElementLocated(By.xpath(String.format("//div[text()='%s']", nickname))));
      nicknameElement.click();
      findDeletedCakeCompany(cake_company_id, deleted_at);
      return true;
    } catch (NoSuchElementException | TimeoutException e) {
      searchElement.clear();
      deleteCakeCompany(cake_company_id);
      return false;
    }
  }

  private void inputUserNickName() {
    mysql.select("select * from cake_company", null, new SelectCallback() {
      @Override
      public void queryResult(ResultSet rs, Object delevery) throws SQLException {
        while (rs.next()) {
          String nickname;
          nickname = rs.getString("nickname");
          int cake_company_id = rs.getInt("id");
          Timestamp deleted_at = rs.getTimestamp("deleted_at");
          delay.seconds(2);
          if (searchUserByNickname(nickname, cake_company_id, deleted_at)) {
            delay.seconds(5);
            insertUserProfileImageURL(cake_company_id);
            insertCakeImage(cake_company_id);
            System.out.println(String.format("Updated cake_company(id=%s) %s...", cake_company_id, nickname));
          }
        }
      }
    });
  }

  private void login() {
    String userName = "user@bagstation.io";
    String password = "password";
    WebElement userNameElement = wait.until(presenceOfElementLocated(By.name("username")));
    userNameElement.sendKeys(userName);
    WebElement passwordElement = wait.until(presenceOfElementLocated(By.name("password")));
    passwordElement.sendKeys(password);
    String buttonText = isEnglish ? "Log In" : "로그인";
    WebElement loginButton = wait
        .until(presenceOfElementLocated(By.xpath(String.format("//*[text()='%s']", buttonText))));
    loginButton.click();
  }

  private void closeLoginSaveInformation() {
    String notNowText = isEnglish ? "Not Now" : "나중에 하기";
    WebElement notificationConfirm = wait
        .until(presenceOfElementLocated(By.xpath(String.format("//*[text()='%s']", notNowText))));
    notificationConfirm.click();
  }

  private void closeNotificationConfirm() {
    String notNowText = isEnglish ? "Not Now" : "나중에 하기";
    WebElement notificationConfirm = wait
        .until(presenceOfElementLocated(By.xpath(String.format("//*[text()='%s']", notNowText))));
    notificationConfirm.click();
  }

  public void checkAccessibleImageURL() {
    mysql.select("select * from cake_img", null, new SelectCallback() {
      @Override
      public void queryResult(ResultSet rs, Object delivery) throws SQLException {
        System.out.println(String.format("Start Check Accessible Image"));
        while (rs.next()) {
          String url = rs.getString("url");
          int id = rs.getInt("id");
          if (!imageHandling.isAccessibleURL(url)) {
            System.out.println(String.format("Deleted: %s", url));
            PreparedStatement ps = mysql.getPrepareStatement("delete from cake_img where id = ?");
            ps.setInt(1, id);
            ps.execute();
          } else {
            System.out.println(String.format("Normal: %s", url));
          }
        }
      }
    });
    System.out.println(String.format("Image verification finished"));
  }
}
