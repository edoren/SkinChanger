FROM eclipse-temurin:21-jdk

WORKDIR /workspace

# Copy Gradle wrapper files first so the download layer is cached
COPY gradle/ gradle/
COPY gradlew gradlew.bat ./

# Make wrapper executable and pre-download Gradle distribution
RUN chmod +x gradlew && ./gradlew --version

# Copy the rest of the source
COPY . .

CMD ["./gradlew", "build"]
