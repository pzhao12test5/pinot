package com.linkedin.thirdeye.detector.email.filter;

import com.linkedin.thirdeye.constant.AnomalyResultSource;
import com.linkedin.thirdeye.datalayer.dto.AnomalyFeedbackDTO;
import com.linkedin.thirdeye.datalayer.dto.MergedAnomalyResultDTO;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.linkedin.thirdeye.constant.AnomalyFeedbackType.*;
import static org.testng.Assert.*;


public class TestUserReportUtils {
  @Test(dataProvider = "provideAnomaliesWithUserReport")
  public void testIsUserReportAnomalyIsQualified(List<MergedAnomalyResultDTO> anomalyResultDTOS, MergedAnomalyResultDTO userReportAnomalyRecovered, MergedAnomalyResultDTO userReportAnomalyFailRecovered) throws Exception {
    AlertFilter alertFilter = new DummyAlertFilter();
    Assert.assertTrue(UserReportUtils.isUserReportAnomalyIsQualified(alertFilter, userReportAnomalyRecovered, anomalyResultDTOS));
    Assert.assertFalse(UserReportUtils.isUserReportAnomalyIsQualified(alertFilter, userReportAnomalyFailRecovered, anomalyResultDTOS));
  }

  @DataProvider(name = "provideAnomaliesWithUserReport")
  public Object[][] getMockMergedAnomalies() {
    List<MergedAnomalyResultDTO> anomalyResultDTOS = new ArrayList<>();
    int totalAnomalies = 7;
    AnomalyFeedbackDTO positiveFeedback = new AnomalyFeedbackDTO();
    AnomalyFeedbackDTO negativeFeedback = new AnomalyFeedbackDTO();
    positiveFeedback.setFeedbackType(ANOMALY);
    negativeFeedback.setFeedbackType(NOT_ANOMALY);
    for (int i = 0; i < totalAnomalies; i++) {
      MergedAnomalyResultDTO anomaly = new MergedAnomalyResultDTO();
      anomaly.setFeedback(null);
      anomaly.setNotified(true);
      anomaly.setStartTime(i);
      anomaly.setEndTime(i+1);
      anomalyResultDTOS.add(anomaly);
    }
    MergedAnomalyResultDTO userReportAnomalyRecovered = new MergedAnomalyResultDTO();
    userReportAnomalyRecovered.setNotified(false);
    userReportAnomalyRecovered.setFeedback(positiveFeedback);
    userReportAnomalyRecovered.setAnomalyResultSource(AnomalyResultSource.USER_LABELED_ANOMALY);
    userReportAnomalyRecovered.setStartTime(0L);
    userReportAnomalyRecovered.setEndTime(10L);

    MergedAnomalyResultDTO userReportAnomalyFailRecovered = new MergedAnomalyResultDTO();
    userReportAnomalyFailRecovered.setNotified(false);
    userReportAnomalyFailRecovered.setFeedback(positiveFeedback);
    userReportAnomalyFailRecovered.setAnomalyResultSource(AnomalyResultSource.USER_LABELED_ANOMALY);
    userReportAnomalyFailRecovered.setStartTime(12L);
    userReportAnomalyFailRecovered.setEndTime(20L);

    anomalyResultDTOS.add(userReportAnomalyRecovered);
    anomalyResultDTOS.add(userReportAnomalyFailRecovered);
    return new Object[][]{{anomalyResultDTOS, userReportAnomalyRecovered, userReportAnomalyFailRecovered}};
  }
}