package bigmac.urlmodifierbackend.config;

import bigmac.urlmodifierbackend.util.SnowflakeIdGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdGeneratorConfig {
    @Bean
    public SnowflakeIdGenerator snowflakeIdGenerator()
    {
        return new SnowflakeIdGenerator(1, 1);
    }
}