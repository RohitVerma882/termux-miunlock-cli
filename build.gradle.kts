plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation("info.picocli:picocli:4.7.4")
    implementation("commons-codec:commons-codec:1.11")
    implementation("org.apache.httpcomponents:httpclient:4.5.5")
    implementation("org.json:json:20180130")
}

sourceSets {
    main {
        kotlin.srcDir("src/main/kotlin")
        java.srcDir("src/main/kotlin")
        resources.srcDir("src/main/resources")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        archiveBaseName.set("termux-miunlock")
        archiveClassifier.set("all")
        manifest {
            attributes("Main-Class" to "dev.rohitverma882.miunlock.cli.MainKt")
        }
        exclude("META-INF/*.SF")
        exclude("META-INF/*.DSA")
        exclude("META-INF/*.RSA")
        exclude("META-INF/*.txt")
        exclude("META-INF/*.kotlin_module")
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/LICENSE")
        exclude("META-INF/NOTICE")
        exclude("META-INF/maven/**")
        exclude("META-INF/versions/**")
    }
}