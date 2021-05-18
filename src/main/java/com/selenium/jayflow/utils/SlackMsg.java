package com.selenium.jayflow.utils;

import java.io.IOException;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;

public class SlackMsg {
  Slack slack = Slack.getInstance();
  String token = "xoxb-286015376337-947762741636-0Ty4C0BA4hdOKdAHQphFfp66";
  MethodsClient methods = slack.methods(token);

  public void send(String text) {
    ChatPostMessageRequest request = ChatPostMessageRequest.builder().channel("경매이징-시스템알림").text(text).build();
    try {
      ChatPostMessageResponse response = methods.chatPostMessage(request);
      System.out.println(response);
    } catch (IOException | SlackApiException e) {
      e.printStackTrace();
    }
  }
}
