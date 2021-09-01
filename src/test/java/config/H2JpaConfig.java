package config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Objects;
import java.util.Properties;

@Configuration
@PropertySource("classpath:test-persistence-properties.yml")
@EnableTransactionManagement
public class H2JpaConfig {
    private final Environment env;

    public  H2JpaConfig(Environment env) {
        this.env = env;
    }


    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource());
        emf.setPackagesToScan("diplom.blogengine.model");

        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        emf.setJpaVendorAdapter(vendorAdapter);
        emf.setJpaProperties(additionalProperties());

        return emf;
    }

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(Objects.requireNonNull(env.getProperty("datasource.driver-class-name"), "datasource driver-class-name is null"));
        dataSource.setUrl(Objects.requireNonNull(env.getProperty("datasource.url"), "datasource url is null"));
        dataSource.setUsername(Objects.requireNonNull(env.getProperty("datasource.username"), "datasource username is null"));
        dataSource.setPassword(Objects.requireNonNull(env.getProperty("datasource.password"), "datasource password is null"));

        return dataSource;
    }

    protected Properties additionalProperties() {
        Properties properties = new Properties();
        properties.setProperty("hibernate.hbm2ddl.auto", Objects.requireNonNull(env.getProperty("hibernate.ddl-auto"), "hibernate.ddl-auto is null"));
        properties.setProperty("hibernate.dialect", Objects.requireNonNull(env.getProperty("hibernate.dialect"), "hibernate.dialect is null"));
        return properties;
    }


}
