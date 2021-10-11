package diplom.blogengine.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsService userDetailsService;

    public WebSecurityConfig(UserDetailsService userDetailsService) {
        super();
        this.userDetailsService = userDetailsService;
    }


    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        //PasswordEncoder passwordEncoder = passwordEncoder();
       /* System.out.println(passwordEncoder.encode("password1"));
        System.out.println(passwordEncoder.encode("password2"));
        System.out.println(passwordEncoder.encode("password3"));*/

        /*auth.inMemoryAuthentication()
            .withUser("user1")
                .password(passwordEncoder.encode("pass1"))
                .roles("USER")
            .and()
            .withUser("admin").password(passwordEncoder.encode("pass2")).roles("ADMIN");*/
        //auth.userDetailsService(userDetailsService);
    }

    @Bean("authenticationManager")
    public AuthenticationManager myAuthenticationManager(final AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService);
        return auth.build();
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        /*http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/admin.html").hasRole("ADMIN")
                .antMatchers("/user.html").hasRole("USER")
                .antMatchers("/login.html").anonymous()
                .antMatchers("/logout.html").authenticated()
                .anyRequest().permitAll()
                .and()
                .formLogin()
                .loginPage("/login_page.html")
                .loginProcessingUrl("/login_perfom.html")
                .defaultSuccessUrl("/", false)
                .and()
                .logout()
                .logoutUrl("/logout_page.html")
                .deleteCookies("JSESSIONID");*/
        http
            .csrf().disable()
            .authorizeRequests()
            .anyRequest().permitAll()
            .and()
            .logout()
            .deleteCookies("JSESSIONID");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}