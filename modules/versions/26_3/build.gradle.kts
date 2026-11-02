val mainLibs = libs263

photonics {
    minecraft = mainLibs.versions.minecraft.get()
    javaVersion = JavaVersion.VERSION_25

    commonDependencies {

         //Use by fabric (for obvious reasons) & common for mixin dependencies
        fabricLoader(mainLibs.fabric.loader)

        shadow(sharedLibs.semver)
        shadow(sharedLibs.fastutil.concurrent.wrapper) {
            isTransitive = false
        }

        runtimeOnly(mainLibs.antlr4.runtime)
        implementation(mainLibs.glsl.transformer)
        implementation(mainLibs.jcpp)

        // Real release jars, committed under libs/ (no guessing Modrinth maven version names)
        implementation(project.rootProject.files("modules/versions/26_3/libs/sodium-0.9.2+mc26.3.jar"))
        implementation(project.rootProject.files("modules/versions/26_3/libs/iris-1.11.7+mc26.3.jar"))
    }
}