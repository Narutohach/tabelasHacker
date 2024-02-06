import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

group = "com.hacker.tabelas"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.compose.material:material-icons-core:1.5.11")
    implementation("org.jetbrains.compose.material:material-icons-extended:1.5.11")
    implementation("org.jetbrains.compose.material3:material3-desktop:1.5.11")
    implementation("com.itextpdf:itext7-core:8.0.1")
    implementation("com.itextpdf:layout:8.0.1")
    implementation("org.apache.pdfbox:pdfbox:2.0.24")
    implementation("org.apache.poi:poi-ooxml:5.2.3")

    implementation("org.apache.logging.log4j:log4j-core:2.22.1")

}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "tabelas"
            packageVersion = "1.0.0"
        }
    }
}
