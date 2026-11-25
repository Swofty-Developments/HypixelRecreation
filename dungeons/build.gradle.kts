plugins {
    java
}

group = "net.swofty"
version = "3.0"

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

dependencies {
    implementation(project(":commons"))
    testImplementation(libs.junit)
    compileOnly(libs.lombok)
    implementation(libs.gson)
}
