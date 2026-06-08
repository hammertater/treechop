# Toolchain image for building the mod. Source is bind-mounted at run time
# (see compose.yaml); nothing is copied into the image.
# Gradle version matches gradle/wrapper/gradle-wrapper.properties.
FROM gradle:8.9-jdk21

ENV GRADLE_USER_HOME=/gradle-home
WORKDIR /workspace
