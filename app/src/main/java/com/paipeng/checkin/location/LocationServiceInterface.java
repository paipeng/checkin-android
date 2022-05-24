package com.paipeng.checkin.location;

public interface LocationServiceInterface {
    void setLanguage(GoogleLocationService.Language language);
    void start();
    void stop();
    void searching();
    void started();
    void stopped();
}
