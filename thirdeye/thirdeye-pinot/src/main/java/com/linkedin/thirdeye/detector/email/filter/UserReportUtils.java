package com.linkedin.thirdeye.detector.email.filter;

import com.linkedin.thirdeye.constant.AnomalyResultSource;
import com.linkedin.thirdeye.datalayer.dto.MergedAnomalyResultDTO;
import com.linkedin.thirdeye.datalayer.pojo.MergedAnomalyResultBean;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class UserReportUtils {
  /**
   * Evaluate user report anomaly is qualified given alert filter, user report anomaly, as well as total anomaly set
   * Runs through total anomaly set, find out if total qualified region for system anomalies can reach more than 50% of user report region,
   * return user report anomaly as qualified, otherwise return false
   * @param alertFilter alert filter to evaluate system detected anoamlies isQualified
   * @param userReportAnomaly
   * @param totalAnomalies
   * @return
   */
  public static Boolean isUserReportAnomalyIsQualified(AlertFilter alertFilter,
      MergedAnomalyResultDTO userReportAnomaly, List<MergedAnomalyResultDTO> totalAnomalies) {
    long startTM = userReportAnomaly.getStartTime();
    long endTM = userReportAnomaly.getEndTime();
    long qualifiedRegion = 0;
    Collections.sort(totalAnomalies, Comparator.comparing(MergedAnomalyResultBean::getStartTime));
    for (MergedAnomalyResultDTO anomalyResult : totalAnomalies) {
      if (anomalyResult.getAnomalyResultSource().equals(AnomalyResultSource.DEFAULT_ANOMALY_DETECTION)
          && anomalyResult.getEndTime() >= startTM && anomalyResult.getStartTime() <= endTM) {
        if (alertFilter.isQualified(anomalyResult)) {
          qualifiedRegion += anomalyResult.getEndTime() - anomalyResult.getStartTime();
        }
      }
    }
    return qualifiedRegion >= (endTM - startTM) * 0.5;
  }
}
