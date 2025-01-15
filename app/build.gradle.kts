import io.github.andreabrighi.gradle.gitsemver.conventionalcommit.ConventionalCommit

plugins {
    id("org.danilopianini.git-sensitive-semantic-versioning") version "0.1.0"
    // Apply the Node.js plugin
    id("com.github.node-gradle.node") version "7.1.0"
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        // Add the plugin to the classpath
        classpath("io.github.andreabrighi:conventional-commit-strategy-for-git-sensitive-semantic-versioning-gradle-plugin:1.0.0")
    }
}

gitSemVer {
    maxVersionLength.set(20)
    commitNameBasedUpdateStrategy(ConventionalCommit::semanticVersionUpdate)
}

node {
    // Download a local Node.js distribution (instead of using a global one)
    download.set(true)

    // Pick whichever version of Node.js you prefer
    version.set("18.17.1")

    // If you have a specific version of npm to use, uncomment and set it:
    // npmVersion.set("9.6.6")

    // This is the directory where the plugin will look for package.json
    nodeProjectDir.set(file(project.projectDir))
}

tasks.register<com.github.gradle.node.npm.task.NpmTask>("installDependencies") {
    // The command is "npm install"
    args.set(listOf("install"))
}

// If you want to run "npm run dev" via Gradle
tasks.register<com.github.gradle.node.npm.task.NpmTask>("runDev") {
    dependsOn("installDependencies")
    args.set(listOf("run", "dev"))
}

// If you also want to run "npm start" or anything else, add more tasks:
tasks.register<com.github.gradle.node.npm.task.NpmTask>("startApp") {
    dependsOn("installDependencies")
    args.set(listOf("start"))
}

tasks.register("printVersion") {
    doLast {
        println("Project version: ${project.version}")
    }
}