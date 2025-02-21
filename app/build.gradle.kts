import io.github.andreabrighi.gradle.gitsemver.conventionalcommit.ConventionalCommit

plugins {
    alias(libs.plugins.git.sensitive.semantic.versioning)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    id("org.jetbrains.dokka") version "2.0.0"
    application
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("io.github.andreabrighi:conventional-commit-strategy-for-git-sensitive-semantic-versioning-gradle-plugin:1.0.15")
    }
}

gitSemVer {
    maxVersionLength.set(20)
    commitNameBasedUpdateStrategy(ConventionalCommit::semanticVersionUpdate)
}

repositories {
    mavenCentral()
}

dependencies {
    // Dependencies for testing
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.konsist)

    // Dependencies for runtime
    testRuntimeOnly(libs.junit.platform.launcher)

    // Dependencies for the application
    implementation(libs.bundles.ktor)
    implementation(libs.logback.classic)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.dotenv.kotlin)
    implementation(libs.kmongo.coroutine.serialization)
    implementation(libs.auth0.jwt)
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "it.unibo.MainKt"
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom("config/detekt/detekt.yaml")
}

tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {
    // Disable configuration cache for this task
    notCompatibleWithConfigurationCache("DokkaTask is not compatible with configuration cache")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.named("build") {
    dependsOn("ktlintFormat", "detekt", "test")
}

tasks.register("printVersion") {
    val version = project.version
    doLast {
        println("Project version: $version")
    }
}

tasks.jar {
    archiveFileName.set("app.jar")
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }

    // Include all runtime dependencies into the JAR file
    from(
        configurations.runtimeClasspath
            .get()
            .filter { it.name.endsWith("jar") }
            .map { zipTree(it) },
    )

    // Optionally, include your compiled classes (if not already included by default)
    from(sourceSets.main.get().output)

    // Ensure the JAR is built as a single fat JAR
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.register<Exec>("dockerBuild") {
    group = "docker"
    description = "Builds the Docker image for the application."

    workingDir = file("..")
    commandLine("docker", "build", "-f", "Dockerfile", "-t", "notification:latest", ".")
}

tasks.register<Exec>("dockerRun") {
    group = "docker"
    description = "Runs the Docker container for the application."

    dependsOn("dockerBuild")
    // Adjust the port mapping as needed
    commandLine("docker", "run", "-p", "8080:8080", "notification:latest")
}

tasks.register<Exec>("dockerClean") {
    group = "docker"
    description = "Removes dangling Docker images."

    commandLine("docker", "image", "prune", "-f")
}
