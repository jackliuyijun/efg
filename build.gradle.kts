plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.2.1"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
    maven {
        url = uri("file:///D:/maven_repository/repository")
    }
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        create(providers.gradleProperty("platformType").get(), providers.gradleProperty("platformVersion").get())
        pluginVerifier()
        zipSigner()
        instrumentationTools()
    }

    implementation(platform("com.mcst:easyfk-dependencies:3.2.12"))
    implementation("com.mcst:easyfk-generator:3.2.12")
    implementation("com.mysql:mysql-connector-j")
    implementation("org.postgresql:postgresql")
}

tasks {
    wrapper {
        gradleVersion = "8.14"
    }

    compileJava {
        options.encoding = "UTF-8"
    }

    buildSearchableOptions {
        enabled = false
    }
}

intellijPlatform {
    pluginConfiguration {
        name = providers.gradleProperty("pluginName")
        version = providers.gradleProperty("pluginVersion")
        ideaVersion {
            sinceBuild = "243"
            untilBuild = provider { null }
        }
    }
}
