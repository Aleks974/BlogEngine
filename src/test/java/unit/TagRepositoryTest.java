package unit;

import config.H2JpaConfig;
import diplom.blogengine.Application;
import diplom.blogengine.model.Tag;
import diplom.blogengine.repository.TagRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = {Application.class, H2JpaConfig.class},
                webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Sql(scripts = {"classpath:testdbsql/V1_0__create_db_schema_tables.sql",
        "classpath:testdbsql/V1_1__add_foreign_keys.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:testdbsql/delete_tables.sql"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class TagRepositoryTest {

    @Autowired
    TagRepository tagRepository;

    @Test
    public void givenTag_WhenSaveAndRetrieveTag_thenOk() {
        // given
        Tag tag = generateTag();
        tag = tagRepository.save(tag);
        tagRepository.flush();

        // when
        Tag foundTag = tagRepository.findById(tag.getId()).get();

        // then
        assertNotNull(foundTag);
        assertEquals(tag.getName(), foundTag.getName());
    }

    private Tag generateTag(){
        Tag tag = new Tag();
        tag.setName("Тэг1");

        return tag;
    }

}
