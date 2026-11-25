package pe.erp.sofas.service;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;



@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(classes = ServiciosTestConfigPMMD.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(properties = {
        "app.db.dir=target/test-data",
        "spring.datasource.url=jdbc:sqlite:target/test-sofas-${random.uuid}.db",
        "spring.jpa.hibernate.ddl-auto=create",
        "app.seeder.enabled=true"})
public @interface IntegracionTestPMMD {

}
