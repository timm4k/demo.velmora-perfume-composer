package velmora.composer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class AppConfig {

    static String DB_URL;
    static String DB_USERNAME;
    static String DB_PASSWORD;
    static String MAIL_USERNAME;
    static String MAIL_PASSWORD;

    @Value("${DB_URL}")
    private void setDbUrl(String v) { DB_URL = v; }

    @Value("${DB_USERNAME}")
    private void setDbUsername(String v) { DB_USERNAME = v; }

    @Value("${DB_PASSWORD}")
    private void setDbPassword(String v) { DB_PASSWORD = v; }

    @Value("${MAIL_USERNAME}")
    private void setMailUsername(String v) { MAIL_USERNAME = v; }

    @Value("${MAIL_PASSWORD}")
    private void setMailPassword(String v) { MAIL_PASSWORD = v; }
}
