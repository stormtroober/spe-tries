import io.github.andreabrighi.gradle.gitsemver.conventionalcommit.ConventionalCommit

plugins {
    alias(libs.plugins.kotlin.jvm)
    id("org.danilopianini.git-sensitive-semantic-versioning") version "0.3.0"
    id("com.github.node-gradle.node") version "7.0.1"
    application
}

node {
    version.set("20.11.1")
    download.set(true)
    npmVersion.set("10.2.4")

    nodeProjectDir.set(file("${project.projectDir}/src/web"))

}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        // Add the plugin to the classpath
        classpath("io.github.andreabrighi:conventional-commit-strategy-for-git-sensitive-semantic-versioning-gradle-plugin:1.0.15")
    }
}


repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Use the Kotlin JUnit 5 integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    // Use the JUnit 5 integration.
    testImplementation(libs.junit.jupiter.engine)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // This dependency is used by the application.
    implementation(libs.guava)
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    // Define the main class for the application.
    mainClass = "org.example.AppKt"
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}

gitSemVer {
    maxVersionLength.set(20)
    commitNameBasedUpdateStrategy(ConventionalCommit::semanticVersionUpdate)
}


tasks.register<com.github.gradle.node.npm.task.NpmTask>("npmBuild") {
    workingDir.set(file("src/web"))
    dependsOn("npmInstall")
    args.set(listOf("run", "build"))
}

tasks.named("build") {
    dependsOn("npmBuild")
}

tasks.register<com.github.gradle.node.npm.task.NpmTask>("npmDev") {
    workingDir.set(file("src/web"))
    args.set(listOf("run", "dev"))
}

tasks.register("devEnvironment") {
    dependsOn("run", "npmDev")
}

tasks.named("run") {
    // Ensure Kotlin runs in parallel with npm
    mustRunAfter("npmDev")
}

tasks.register("printVersion") {
    doLast {
        println("Project version: ${project.version}")
    }
}


