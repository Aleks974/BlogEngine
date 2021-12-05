package unit;

import config.H2JpaConfig;
import diplom.blogengine.Application;
import diplom.blogengine.model.Role;
import diplom.blogengine.model.User;
import diplom.blogengine.repository.RoleRepository;
import diplom.blogengine.repository.UserRepository;
import diplom.blogengine.service.schedule.ScheduledTasksHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import util.TestDataGenerator;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = {Application.class, H2JpaConfig.class},
                webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Sql(scripts = {"classpath:testdbsql/V1_0__create_db_schema_tables.sql",
        "classpath:testdbsql/V1_1__add_foreign_keys.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:testdbsql/delete_tables.sql"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

    private TestDataGenerator testDataGenerator = new TestDataGenerator();

    @Autowired
    private ScheduledTasksHandler scheduler;

    @BeforeEach
    public void setUp() {
        scheduler.shutdown();
    }

    @Test
    public void givenUserAndRole_WhenSaveAndFindById_thenOk() {
        // given
        Role role = testDataGenerator.generateRole();
        role = roleRepository.saveAndFlush(role);
        User user = testDataGenerator.generateUser();
        Set<Role> roles = Collections.singleton(role);
        user.setRoles(roles);
        user = userRepository.saveAndFlush(user);

        EntityGraph<User> graph = entityManager.createEntityGraph(User.class);
        graph.addAttributeNodes("roles");
        Map<String, Object> hints = new HashMap<>();
        hints.put("javax.persistence.fetchgraph", graph);

        // when
        User foundUser = entityManager.find(User.class, user.getId(), hints);

        // then
        assertNotNull(foundUser);
        assertEquals(user.getName(), foundUser.getName());

        assertEquals(roles, foundUser.getRoles());
    }

    @Test
    public void givenUser_WhenSaveAndFindByEmail_thenOk() {
        // given
        Role role = testDataGenerator.generateRole();
        role = roleRepository.saveAndFlush(role);
        User user = testDataGenerator.generateUser();
        Set<Role> roles = Collections.singleton(role);
        user.setRoles(roles);
        user = userRepository.saveAndFlush(user);

        // when
        long actualUserId = userRepository.findUserIdByEmail(user.getEmail()).get();

        // then
        assertEquals(user.getId(), actualUserId);
    }

    @Test
    public void givenUser_WhenSaveAndFindByName_thenOk() {
        // given
        Role role = testDataGenerator.generateRole();
        role = roleRepository.saveAndFlush(role);
        User user = testDataGenerator.generateUser();
        Set<Role> roles = Collections.singleton(role);
        user.setRoles(roles);
        user = userRepository.saveAndFlush(user);

        // when
        long actualUserId = userRepository.findUserIdByName(user.getName()).get();

        // then
        assertEquals(user.getId(), actualUserId);
    }



}
