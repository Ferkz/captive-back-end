# Estágio 1: Build da aplicação com Maven
FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app
COPY . .
# Limpa dependências antigas e baixa novamente, depois empacota
RUN mvn clean package -DskipTests

# Estágio 2: Criação da imagem final para rodar a aplicação
FROM openjdk:17-jdk-slim AS final

WORKDIR /app

# Defina o nome do arquivo do certificado como um ARG para flexibilidade
ARG UNIFI_CERT_FILE=unifi.crt
ARG UNIFI_CERT_ALIAS=unifissl

# Copia o certificado do contexto do build para a imagem
COPY ${UNIFI_CERT_FILE} /app/${UNIFI_CERT_FILE}

# Importa o certificado para o truststore da JVM
# O caminho para cacerts em openjdk:17-jdk-slim é geralmente $JAVA_HOME/lib/security/cacerts
# A variável JAVA_HOME deve estar definida na imagem base.
# A senha padrão para cacerts é "changeit"
RUN keytool -importcert -noprompt \
    -alias ${UNIFI_CERT_ALIAS} \
    -file /app/${UNIFI_CERT_FILE} \
    -keystore ${JAVA_HOME}/lib/security/cacerts \
    -storepass changeit \
    && rm /app/${UNIFI_CERT_FILE} # Remove o arquivo do certificado após a importação para limpar

# Copia o JAR construído do estágio de build
COPY --from=build /app/target/Captive-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# Define o comando para rodar a aplicação
ENTRYPOINT ["java", "-jar", "/app/app.jar"]