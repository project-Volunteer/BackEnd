package project.volunteer.support;

import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Table;
import javax.persistence.metamodel.Type;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DatabaseCleaner {
    private static final String FOREIGN_KEY_CHECK_FORMAT = "SET REFERENTIAL_INTEGRITY %s";
    private static final String TRUNCATE_TABLE_FORMAT = "TRUNCATE TABLE %s";
    private List<String> tableNames;
    @PersistenceContext
    private EntityManager em;

    @PostConstruct
    public void findAllTable() {
        tableNames = em.getMetamodel()
                .getEntities()
                .stream()
                .map(Type::getJavaType)
                .map(javaType -> javaType.getAnnotation(Table.class))
                .map(Table::name)
                .collect(Collectors.toList());
    }

    @Transactional
    public void execute() {
        em.flush();
        em.createNativeQuery(String.format(FOREIGN_KEY_CHECK_FORMAT, "FALSE")).executeUpdate();

        for (String tableName : tableNames) {
            em.createNativeQuery(String.format(TRUNCATE_TABLE_FORMAT, tableName)).executeUpdate();
        }

        em.createNativeQuery(String.format(FOREIGN_KEY_CHECK_FORMAT, "TRUE")).executeUpdate();
    }
}
