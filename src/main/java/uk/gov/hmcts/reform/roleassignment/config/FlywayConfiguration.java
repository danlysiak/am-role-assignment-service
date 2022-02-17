package uk.gov.hmcts.reform.roleassignment.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.roleassignment.data.migration.FlywayNoOpStrategy;

@AutoConfigureAfter({
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
@AutoConfigureBefore(FlywayAutoConfiguration.class)
@Configuration
@ConditionalOnClass(Flyway.class)
@ConditionalOnProperty(prefix = "dbMigration", name = "runOnStartup", havingValue = "false")
public class FlywayConfiguration {

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return new FlywayNoOpStrategy();
    }
}