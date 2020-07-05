plugins {
    kotlin("jvm") version "1.3.61"
    application
}

group = "org.siroque"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    maven { setUrl("https://dl.bintray.com/ijabz/maven") }
}

application {
    mainClassName = "kotlin/com/github/siroque/silverraingrabber/Grabber.kt"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jsoup:jsoup:1.13.1")
    implementation("com.squareup.okhttp3:okhttp:4.7.2")
    implementation("commons-io:commons-io:2.7")
    implementation("net.jthink:jaudiotagger:2.2.3")

    testImplementation(platform("org.spockframework:spock-bom:2.0-M1-groovy-2.5"))
    testImplementation("org.spockframework:spock-core")
    testImplementation("org.hamcrest:hamcrest-core:2.2")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}