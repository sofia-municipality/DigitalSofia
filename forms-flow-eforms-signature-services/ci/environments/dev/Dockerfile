# Build Stage
FROM maven
WORKDIR /usr/src/app
COPY . .
RUN mkdir /root/.m2/; \
    echo \
    "<settings> \
         <profiles> \
             <profile> \
                 <id>env-dev</id> \
                 <repositories> \
                     <repository> \
                         <id>dss</id> \
                         <name>Digital Signature Service</name> \
                         <url>http://10.191.35.200:8081/repository/maven-releases</url> \
                     </repository> \
                 </repositories> \
             </profile> \
         </profiles> \
         <activeProfiles> \
             <activeProfile>env-dev</activeProfile> \
         </activeProfiles> \
         <mirrors> \
             <mirror> \
                 <id>dss-mirror</id> \
                 <mirrorOf>dss</mirrorOf> \
                 <name></name> \
                 <url>http://10.191.35.200:8081/repository/maven-releases</url> \
                 <blocked>false</blocked> \
             </mirror> \
         </mirrors> \
    </settings>" \
    > /root/.m2/settings.xml
RUN mvn -Dmaven.test.skip=true clean package

# Package stage
FROM openjdk:11
COPY --from=0 /usr/src/app/target/*.jar ./eforms-signature-services.jar
COPY src/main/resources/self_signed_tsa.p12 ./self_signed_tsa.p12
RUN \
   if [ -f selfsigned.crt ]; then \
    keytool -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -noprompt -trustcacerts -importcert -alias selfsignedcert -file selfsigned.crt;\
   fi
ENV ADMIN_USER badmin
ENV ADMIN_PASSWORD b@dm!n

EXPOSE 8010

ENTRYPOINT ["java","-jar","./eforms-signature-services.jar"]
