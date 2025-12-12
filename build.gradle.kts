import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java

    alias(libs.plugins.shadow)
    alias(libs.plugins.properties)
}

group = "net.skullian"
version = "1.1.0"

repositories {
    mavenCentral()
    mavenLocal()

    maven {
        name = "spigotmc-repo"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
    maven {
        name = "codemc"
        url = uri("https://repo.codemc.org/repository/maven-public/")
    }
    maven {
        name = "loohp-repo"
        url = uri("https://repo.loohpjames.com/repository")
    }
}

dependencies {
    compileOnly(libs.spigot)

    compileOnly(libs.interactivechat)
    compileOnly(libs.packetevents)
    compileOnly(libs.bytebuddy)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    compileOnly(libs.bundles.adventure)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks {
    withType<ShadowJar>().configureEach {
        from(rootProject.projectDir.resolve("LICENSE")) {
            rename("LICENSE", "META-INF/LICENSE_${rootProject.name}")
        }

        destinationDirectory.set(file("$rootDir/libs"))
        archiveFileName.set("${rootProject.name}-$version.jar")
    }

    processResources {
        filteringCharset = "UTF-8"
        filesMatching(listOf("*.yml")) {
            expand(project.properties)
        }
    }

    withType<JavaCompile> {
        options.compilerArgs.add("-parameters")
        options.isFork = true
        options.encoding = "UTF-8"
        options.release = 17
    }
}

gitProperties {
    gitPropertiesName = "version.properties"
    dotGitDirectory = project.rootProject.layout.projectDirectory.dir(".git")
    keys = listOf("git.branch", "git.build.version", "git.commit.id.abbrev")
}