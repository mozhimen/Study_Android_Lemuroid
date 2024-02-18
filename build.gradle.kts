import com.android.build.gradle.BaseExtension
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

buildscript {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        maven { setUrl("https://maven.aliyun.com/repository/central") }
        maven { setUrl("https://maven.aliyun.com/repository/google") }
        maven { setUrl("https://maven.aliyun.com/repository/jcenter") }
        maven { setUrl("https://maven.aliyun.com/repository/public") }
        maven { setUrl("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { setUrl("https://maven.aliyun.com/repository/grails-core") }
        maven { setUrl("https://gitee.com/liuchaoya/libcommon/raw/master/repository/") }
        maven { setUrl("https://repo.eclipse.org/content/repositories/paho-snapshots/") }
        maven { setUrl("https://developer.huawei.com/repo/") }
        maven { setUrl("https://jitpack.io") }
        maven { setUrl("https://www.jitpack.io") }
        mavenCentral()
        google()
    }
    dependencies {
        classpath(deps.plugins.android)
        classpath(deps.plugins.bugsnag)
        classpath(deps.plugins.navigationSafeArgs)
    }
}

plugins {
    id("org.jetbrains.kotlin.jvm") version deps.versions.kotlin
    id("com.github.ben-manes.versions") version "0.20.0"
    id("org.jmailen.kotlinter") version "2.1.1"
    checkstyle
}

allprojects {
    repositories {
        mavenLocal()
        maven { setUrl("https://maven.aliyun.com/repository/central") }
        maven { setUrl("https://maven.aliyun.com/repository/google") }
        maven { setUrl("https://maven.aliyun.com/repository/jcenter") }
        maven { setUrl("https://maven.aliyun.com/repository/public") }
        maven { setUrl("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { setUrl("https://maven.aliyun.com/repository/grails-core") }
        maven { setUrl("https://raw.githubusercontent.com/saki4510t/libcommon/master/repository/") }
        maven { setUrl("https://gitee.com/liuchaoya/libcommon/raw/master/repository/") }
        maven { setUrl("https://repo.eclipse.org/content/repositories/paho-snapshots/") }
        maven { setUrl("https://developer.huawei.com/repo/") }
        maven { setUrl("https://jitpack.io") }
        maven { setUrl("https://www.jitpack.io") }
        mavenCentral()
        google()
    }

    apply(plugin = "org.jmailen.kotlinter")

    kotlinter {
        // We are currently disabling tests for import ordering.
        disabledRules = arrayOf("import-ordering")
    }

    configurations.all {
        resolutionStrategy.eachDependency {
            when (requested.group) {
                "com.google.android.gms" -> useVersion(deps.versions.gms)
                "org.jetbrains.kotlin" -> {
                    if (requested.name.startsWith("kotlin-stdlib-jre")) {
                        with(requested) {
                            useTarget("$group:${name.replace("jre", "jdk")}:$version")
                        }
                    }
                    useVersion(deps.versions.kotlin)
                }
            }
        }
    }
}

subprojects {
    apply {
        plugin("checkstyle")
    }

    tasks {
        val checkstyle by creating(Checkstyle::class) {
            configFile = file("$rootDir/config/checkstyle/checkstyle.xml")
            classpath = files()
            source("src")
        }
        findByName("check")?.dependsOn(checkstyle)
    }

    extensions.configure(CheckstyleExtension::class.java) {
        isIgnoreFailures = false
        toolVersion = "8.8"
    }

    afterEvaluate {
        // BaseExtension is common parent for application, library and test modules
        extensions.configure(BaseExtension::class.java) {
            compileSdkVersion(deps.android.compileSdkVersion)
            buildToolsVersion(deps.android.buildToolsVersion)
            defaultConfig {
                minSdkVersion(deps.android.minSdkVersion)
                targetSdkVersion(deps.android.targetSdkVersion)
                multiDexEnabled = true
            }
            lintOptions {
                isAbortOnError = true
                disable("UnusedResources") // https://issuetracker.google.com/issues/63150366
                disable("InvalidPackage")
                disable("VectorPath")
                disable("TrustAllX509TrustManager")
            }
            dexOptions {
                dexInProcess = true
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_1_8
                targetCompatibility = JavaVersion.VERSION_1_8
            }
        }
    }

    configurations {
        all {
            exclude(group = "com.google.code.findbugs", module = "jsr305")
        }
    }
}

tasks {
    "clean"(Delete::class) {
        delete(buildDir)
    }

    "dependencyUpdates"(DependencyUpdatesTask::class) {
        resolutionStrategy {
            componentSelection {
                all {
                    val rejected = listOf("alpha", "beta", "rc", "cr", "m")
                            .map { qualifier -> Regex("(?i).*[.-]$qualifier[.\\d-]*") }
                            .any { it.matches(candidate.version) }
                    if (rejected) {
                        reject("Release candidate")
                    }
                }
            }
        }
    }
}
