package ap.be.backend.service;

import java.util.Map;

public interface OneRosterClient {
    boolean testVerbinding(String domein, String clientId, String clientSecret);

    Map<String, Object> haalLiveSchoolDataOp(String domein, String clientId, String clientSecret);
}
