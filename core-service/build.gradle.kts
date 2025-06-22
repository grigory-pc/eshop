import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    id("java")
    id("org.springframework.boot") version "3.3.10"
    id("io.spring.dependency-management") version "1.1.4"
    id("org.openapi.generator") version "7.12.0"
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.projectlombok:lombok")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    implementation("org.hibernate.orm:hibernate-core")
    implementation("commons-fileupload:commons-fileupload:1.5")
    implementation("commons-io:commons-io")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("io.r2dbc:r2dbc-h2")
    implementation("org.postgresql:r2dbc-postgresql")
    implementation("org.springframework.data:spring-data-r2dbc")
    implementation("org.flywaydb:flyway-core")

    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")

    runtimeOnly("com.h2database:h2")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.mockito:mockito-junit-jupiter")
    testImplementation("org.skyscreamer:jsonassert")
    testImplementation("org.projectlombok:lombok")

    testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.test {
    useJUnitPlatform()
}

tasks.bootJar {
    mainClass.set("ru.yandex.practicum.eshop.core.Main")
}

tasks.withType<GenerateTask> {
    generatorName.set("spring")
    inputSpec.set("$projectDir/src/main/resources/payment-client.yaml")
    outputDir.set("$projectDir/build/generated")
    modelPackage.set("ru.yandex.practicum.eshop.core.domain")
    apiPackage.set("ru.yandex.practicum.eshop.core.api")
    configOptions.set(mapOf(
            "sourceFolder" to "src/gen/java/main/codegen",
            "interfaceOnly" to "false",
            "library" to "spring-boot",
            "useTags" to "true",
            "dateLibrary" to "java8",
            "groupId" to "ru.yandex.practicum.eshop.core",
            "artifactId" to "payment-service-request",
            "artifactVersion" to "1.0.0",
            "apiPackage" to "ru.yandex.practicum.eshop.core.api",
            "modelPackage" to "ru.yandex.practicum.eshop.core.domain",
            "invokerPackage" to "ru.yandex.practicum.eshop.core.client",
            "hideGenerationTimestamp" to "true",
            "generateApis" to "false",
            "generateModels" to "true",
            "generateSupportingFiles" to "false",
            "configPackage" to "ru.yandex.practicum.eshop.core.config",
            "delegatePattern" to "false",
            "withXml" to "false",
            "useBeanValidation" to "true",
            "useRxJava2" to "false",
            "useFeign" to "false",
            "useOkHttp" to "true",
            "reactive" to "true",
            "javaVersion" to "21"
    ))
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}