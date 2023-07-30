plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.20"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("info.picocli:picocli:4.7.4")
    implementation("com.google.guava:guava:29.0-jre")
    implementation("commons-codec:commons-codec:1.11")
    implementation("commons-io:commons-io:2.6")
    implementation("commons-logging:commons-logging:1.2")
    implementation("org.apache.commons:commons-compress:1.17")
    implementation("org.apache.commons:commons-lang3:3.7")
    implementation("org.apache.httpcomponents:httpclient:4.5.5")
    implementation("org.json:json:20180130")
}

//testing {
//    suites {
//        val test by getting(JvmTestSuite::class) {
//            useKotlinTest("1.8.20")
//        }
//    }
//}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    mainClass.set("dev.rohitverma882.miunlockv2.cli.MainKt")
    applicationName = "Termux-MiUnlockV2"
}
