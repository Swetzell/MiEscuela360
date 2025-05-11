warning: in the working copy of 'src/main/java/com/sudamericana/impoexcel/controller/SunatConsultaController.java', LF will be replaced by CRLF the next time Git touches it
[1mdiff --git a/src/main/java/com/sudamericana/impoexcel/controller/SunatConsultaController.java b/src/main/java/com/sudamericana/impoexcel/controller/SunatConsultaController.java[m
[1mindex 61cf613..c8c4a39 100644[m
[1m--- a/src/main/java/com/sudamericana/impoexcel/controller/SunatConsultaController.java[m
[1m+++ b/src/main/java/com/sudamericana/impoexcel/controller/SunatConsultaController.java[m
[36m@@ -165,4 +165,4 @@[m [mpublic class SunatConsultaController {[m
                                 .body(null);[m
         }[m
     }[m
[31m-}[m
[32m+[m[32m}[m
\ No newline at end of file[m
[1mdiff --git a/src/main/resources/application-prod.properties b/src/main/resources/application-prod.properties[m
[1mindex 3120626..7e4579e 100644[m
[1m--- a/src/main/resources/application-prod.properties[m
[1m+++ b/src/main/resources/application-prod.properties[m
[36m@@ -1,15 +1,14 @@[m
 # Configuración de SQL Server[m
[32m+[m[32mspring.datasource.url=jdbc:sqlserver://SVR-TS01;databaseName=BDSU_BP;encrypt=true;trustServerCertificate=true[m
[32m+[m[32mspring.datasource.username=SA[m
[32m+[m[32mspring.datasource.password=Suda2012..[m
[32m+[m[32mspring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver[m
[32m+[m[32m#spring.jpa.hibernate.ddl-auto=update[m
[32m+[m[32m#spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.SQLServerDialect[m
[32m+[m[32m#[m
 #spring.datasource.url=jdbc:sqlserver://DESKTOP-QDS77PO\\MSSQLSERVER01;databaseName=BDSU_BP;encrypt=true;trustServerCertificate=true[m
 #spring.datasource.username=sa[m
 #spring.datasource.password=n3f3rt1t3[m
 #spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver[m
 #spring.jpa.hibernate.ddl-auto=update[m
[31m-#spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.SQLServerDialect[m
[31m-[m
[31m-#[m
[31m-spring.datasource.url=jdbc:sqlserver://DESKTOP-QDS77PO\\MSSQLSERVER01;databaseName=BDSU_BP;encrypt=true;trustServerCertificate=true[m
[31m-spring.datasource.username=sa[m
[31m-spring.datasource.password=n3f3rt1t3[m
[31m-spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver[m
[31m-spring.jpa.hibernate.ddl-auto=update[m
[31m-spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.SQLServerDialect[m
\ No newline at end of file[m
[32m+[m[32m#spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.SQLServerDialect[m
\ No newline at end of file[m
