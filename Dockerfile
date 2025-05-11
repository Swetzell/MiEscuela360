# Base image for Tomcat 11 with JDK 19 (usando una imagen personalizada)
FROM eclipse-temurin:19-jdk-jammy as builder

# Descargar y extraer Tomcat 11.0.4
RUN apt-get update && apt-get install -y wget
WORKDIR /tmp
RUN wget https://archive.apache.org/dist/tomcat/tomcat-11/v11.0.4/bin/apache-tomcat-11.0.4.tar.gz && \
    tar xzf apache-tomcat-11.0.4.tar.gz && \
    mv apache-tomcat-11.0.4 /opt/tomcat && \
    rm apache-tomcat-11.0.4.tar.gz

# Crear la imagen final
FROM eclipse-temurin:19-jdk-jammy

# Definir variable de zona horaria
ENV TZ=America/Lima

# Configurar zona horaria
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Crear grupo y usuario no privilegiado
RUN groupadd -r sudauser && useradd -r -g sudauser sudauser

# Copiar Tomcat desde la imagen de builder
COPY --from=builder /opt/tomcat /opt/tomcat

# Eliminar la aplicación por defecto de Tomcat
RUN rm -rf /opt/tomcat/webapps/ROOT

# Copiar el archivo WAR generado con el nombre adecuado para despliegue en /sudamericana
COPY target/ROOT.war /opt/tomcat/webapps/ROOT.war

# Agregar el controlador JDBC de Microsoft SQL Server compatible con JDK 19
ADD https://repo1.maven.org/maven2/com/microsoft/sqlserver/mssql-jdbc/12.4.2.jre11/mssql-jdbc-12.4.2.jre11.jar /opt/tomcat/lib/

# Crear directorio para archivos de configuración externos
RUN mkdir -p /opt/tomcat/external-config

# Configurar permisos para que Tomcat pueda escribir en sus logs y directorios de trabajo
RUN chown -R sudauser:sudauser /opt/tomcat

# Puerto en el que Tomcat escuchará - usar 9090 como en tu entorno que funciona
EXPOSE 9090

# Configurar el puerto en server.xml
RUN sed -i 's/port="8080"/port="9090"/g' /opt/tomcat/conf/server.xml

# Configurar opciones de la JVM para rendimiento optimizado en contenedores
ENV JAVA_OPTS="-Djava.awt.headless=true -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Duser.timezone=America/Lima"

# Variable CATALINA_HOME para Tomcat
ENV CATALINA_HOME=/opt/tomcat

# Usar el usuario no privilegiado
USER sudauser

# Comando para iniciar Tomcat con las opciones configuradas
CMD ["/opt/tomcat/bin/catalina.sh", "run"]