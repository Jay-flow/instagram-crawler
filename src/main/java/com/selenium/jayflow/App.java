package com.selenium.jayflow;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class App {
    public static void main(String[] args) {
        boolean isRunImmediately = false;
        boolean isEnglish = false;
        boolean isNotification = true;
        if (!isRunImmediately) {
            System.out.println("i_cake you 크롤러 새벽 6시에 실행 대기중...");
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
            ZonedDateTime nextRun = now.withHour(6).withMinute(0).withSecond(0);
            if (now.compareTo(nextRun) > 0) {
                nextRun = nextRun.plusDays(1);
            }
            Duration duration = Duration.between(now, nextRun);
            long initialDelay = duration.getSeconds();

            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(new Flow(isEnglish, isNotification), initialDelay, TimeUnit.DAYS.toSeconds(1),
                    TimeUnit.SECONDS);
        } else {
            new Flow(isEnglish, isNotification).run();
        }
    }
}
