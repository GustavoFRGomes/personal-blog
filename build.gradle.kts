plugins {
    kotlin("multiplatform") version "1.8.20"
    application
    java
}

group = "org.ggomes"
version = "0.1.0"

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        withJava()
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }
    js(IR) {
        browser {
            binaries.executable()
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                // common data models
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("org.commonmark:commonmark:0.18.0")
                implementation("org.commonmark:commonmark-ext-gfm-tables:0.18.0")
                implementation("org.commonmark:commonmark-ext-gfm-strikethrough:0.18.0")
                implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.8.0")
            }
        }
        val jsMain by getting {
            dependencies {
                // js-specific (if you add dynamic features)
            }
        }
    }
}

application {
    mainClass.set("org.ggomes.MainKt")
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "11"
    targetCompatibility = "11"
}


tasks.register<JavaExec>("generateSite") {
    group = "site"
    description = "Generates static site"
    dependsOn(tasks.getByName("compileKotlinJvm"))
    classpath = files(
        kotlin.targets["jvm"].compilations["main"].output.allOutputs,
        kotlin.targets["jvm"].compilations["main"].runtimeDependencyFiles
    )
    mainClass.set("org.ggomes.MainKt")
}

tasks.register("buildSite") {
    dependsOn("generateSite")
}
