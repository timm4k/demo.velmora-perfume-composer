package velmora.composer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import velmora.composer.pool.SimpleConnectionPool;

@Configuration
public class PoolConfig {

  @Bean(destroyMethod = "closeAll")
  public SimpleConnectionPool simpleConnectionPool(
      @Value("${spring.datasource.url}") String url,
      @Value("${spring.datasource.username}") String user,
      @Value("${spring.datasource.password}") String password
  ) {
    return new SimpleConnectionPool(url, user, password, 10, 2);
  }
}
