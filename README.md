############################################################
#           PROYECTO: MiEscuela360 - INSTRUCCIONES         #
############################################################

Este documento describe cómo configurar y levantar el sistema MiEscuela360 en un entorno local Windows 10.

############################################################
#               REQUISITOS DEL SISTEMA                     #
############################################################

- Windows 10 (64 bits)
- Java JDK 17 o superior (recomendado: Adoptium)
- Apache Maven 3.9.x
- Microsoft SQL Server (2017 o superior)
- SQL Server Management Studio (SSMS)
- IntelliJ IDEA o Spring Tool Suite (STS)

############################################################
#                 1. INSTALAR JAVA JDK 17                  #
############################################################

➤ Descargar desde: https://adoptium.net/en-GB/temurin/releases/?version=17

➤ Configurar variables de entorno:
    - JAVA_HOME → ruta del JDK
    - Agregar %JAVA_HOME%\bin al PATH

➤ Verificar instalación:
    > java -version

############################################################
#                 2. INSTALAR MAVEN                        #
############################################################

➤ Descargar desde: https://maven.apache.org/download.cgi

➤ Descomprimir y configurar variables:
    - MAVEN_HOME → ruta descomprimida
    - Agregar %MAVEN_HOME%\bin al PATH

➤ Verificar instalación:
    > mvn -version

############################################################
#            3. INSTALAR Y CONFIGURAR SQL SERVER           #
############################################################

➤ Asegúrate de conocer tu instancia de SQL Server:
    Ejemplo: localhost\SQLEXPRESS

➤ Habilitar autenticación mixta (SQL Server y Windows)

➤ Crear usuario:
    Usuario: sa
    Contraseña: n3f3rt1t3

############################################################
#              4. RESTAURAR LA BASE DE DATOS              #
############################################################

➤ Abrir SSMS
➤ Crear base de datos: MiEscuela360
➤ Restaurar desde el archivo .BAK proporcionado

############################################################
#            5. CONFIGURAR EL ARCHIVO DE PROYECTO         #
############################################################

➤ Abrir: src/main/resources/application-prod.properties

➤ Reemplazar el contenido:
spring.datasource.url=jdbc:sqlserver://<TU_PC>\<INSTANCIA>;databaseName=MiEscuela360;encrypt=true;trustServerCertificate=true
spring.datasource.username=
spring.datasource.password=
spring.profiles.active=prod
server.port=8085

✱ Reemplaza <TU_PC> y <INSTANCIA> según tu configuración local.

############################################################
#             6. COMPILAR Y LEVANTAR EL PROYECTO          #
############################################################

➤ Desde consola:
    > mvn clean install
    > mvn spring-boot:run

➤ Desde IntelliJ:
    - Abrir proyecto
    - Esperar carga de dependencias
    - Ejecutar clase: MiEscuela360Application.java

############################################################
#              7. ACCEDER A LA APLICACIÓN                 #
############################################################

➤ Navegar a:
    http://localhost:8085

✱ Asegúrate de que el puerto 8085 no esté en uso.

############################################################
#                 OBSERVACIONES FINALES                   #
############################################################

- Verifica conexión exitosa a la base de datos.
- Asegúrate de que el servicio SQL esté activo.
- Si hay errores de login, valida que la contraseña del usuario `sa` sea correcta.
- El proyecto usa Spring Boot, Thymeleaf y Hibernate.
