package kr.hhplus.be.server;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Table;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Profile("test")
@Component
public class TestContainerDatabaseCleaner {

//    private static final String TRUNCATE_FORMAT = "TRUNCATE TABLE %s";
//    private static final String ALTER_FORMAT = "ALTER TABLE %s AUTO_INCREMENT = 1";
//
//    @PersistenceContext
//    private EntityManager entityManager;
//
//    private List<String> tableNames;
//
//    @PostConstruct
//    public void findTableNames() {
//        tableNames = entityManager.getMetamodel().getEntities()
//                .stream()
//                .filter(e -> e.getJavaType().getAnnotation(Entity.class) != null)
//                .map(e -> {
//                    Table tableAnnotation = e.getJavaType().getAnnotation(Table.class);
//                    if (tableAnnotation != null && !tableAnnotation.name().isEmpty()) {
//                        return tableAnnotation.name();
//                    }
//                    return e.getName(); // @Table이 없으면 기본 엔티티 이름 사용
//                })
//                .toList();
//    }
//
//    @Transactional
//    public void execute() {
//        entityManager.flush();
//        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
//        for (String tableName : tableNames) {
//            entityManager.createNativeQuery(TRUNCATE_FORMAT.formatted(tableName)).executeUpdate();
//            entityManager.createNativeQuery(ALTER_FORMAT.formatted(tableName)).executeUpdate();
//        }
//        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
//    }
}