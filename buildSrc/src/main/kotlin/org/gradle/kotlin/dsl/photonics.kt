package org.gradle.kotlin.dsl

import net.fabricmc.loom.api.LoomGradleExtensionAPI
import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.accessors.runtime.addConfiguredDependencyTo
import org.gradle.kotlin.dsl.accessors.runtime.addDependencyTo
import org.gradle.language.jvm.tasks.ProcessResources

var DependencyHandlerScope._fabricLoader: Any? by Extensions

/** MC 26.x and newer ship unobfuscated: no mappings, no remapping, no mod* configurations. */
fun Project.isUnobfuscated(): Boolean {
    val versionDir = if (name == "common" || name == "fabric") parent!!.projectDir else projectDir
    return (versionDir.name.substringBefore('_').toIntOrNull() ?: 0) >= 26
}

interface PhotonicsExtension {
    val _dependencyBlock: Property<Action<PhotonicsCommonDependenciesScope>>

    val minecraft: Property<String>

    val javaVersion: Property<JavaVersion>

    fun commonDependencies(action: Action<PhotonicsCommonDependenciesScope>) {
        this._dependencyBlock = action;
    }
}

@JvmInline
value class PhotonicsCommonDependenciesScope(
    private val backing: DependencyHandlerScope
) {
    val project: Project
        get() = backing.ext.get("proj") as Project

    val loom: LoomGradleExtensionAPI
        get() = project.loom

    private fun add(configuration: String, dependencyNotation: Any?, action: Action<ExternalModuleDependency>) {
        if (dependencyNotation is Provider<*>) {
            addConfiguredDependencyTo(backing.dependencies, configuration, dependencyNotation, action)
        } else if (dependencyNotation is org.gradle.api.file.FileCollection) {
            // local jars (files(...)) are not module dependencies, so the action must not be applied
            backing.dependencies.add(configuration, dependencyNotation)
        } else {
            addDependencyTo(backing.dependencies, configuration, dependencyNotation!!, action)
        }
    }

    // workaround for these being internal in version build file
    fun mappings(dependencyNotation: Any?, configureAction: Action<ExternalModuleDependency> = Action { }) {
        if (!project.isUnobfuscated()) add("mappings", dependencyNotation, configureAction)
    }
    private fun modConf(name: String) = if (project.isUnobfuscated()) name.removePrefix("mod").replaceFirstChar { it.lowercase() } else name

    fun shadow(dependencyNotation: Any?, configureAction: Action<ExternalModuleDependency> = Action { }) =
        add("shadow", dependencyNotation!!, configureAction)

    fun fabricLoader(dependencyNotation: Any?) {
        backing._fabricLoader = dependencyNotation
    }

    fun api(dependencyNotation: Any?, configureAction: Action<ExternalModuleDependency> = Action { }) =
        add("api", dependencyNotation!!, configureAction)
    fun compileOnly(dependencyNotation: Any?, configureAction: Action<ExternalModuleDependency> = Action { }) =
        add("compileOnly", dependencyNotation!!, configureAction)
    fun implementation(dependencyNotation: Any?, configureAction: Action<ExternalModuleDependency> = Action { }) =
        add("implementation", dependencyNotation!!, configureAction)
    fun include(dependencyNotation: Any?, configureAction: Action<ExternalModuleDependency> = Action { }) =
        add("include", dependencyNotation!!, configureAction)
    fun runtimeOnly(dependencyNotation: Any?, configureAction: Action<ExternalModuleDependency> = Action { }) =
        add("runtimeOnly", dependencyNotation!!, configureAction)


    fun modApi(dependencyNotation: Any?, configureAction: Action<ExternalModuleDependency> = Action { }) =
        add(modConf("modApi"), dependencyNotation!!, configureAction)
    fun modCompileOnly(dependencyNotation: Any?, configureAction: Action<ExternalModuleDependency> = Action { }) =
        add(modConf("modCompileOnly"), dependencyNotation!!, configureAction)
    fun modImplementation(dependencyNotation: Any?, configureAction: Action<ExternalModuleDependency> = Action { }) =
        add(modConf("modImplementation"), dependencyNotation!!, configureAction)
    fun modRuntimeOnly(dependencyNotation: Any?, configureAction: Action<ExternalModuleDependency> = Action { }) =
        add(modConf("modRuntimeOnly"), dependencyNotation!!, configureAction)


    fun testCompileOnly(dependencyNotation: Any?, configureAction: Action<ExternalModuleDependency> = Action { }) =
        add("testCompileOnly", dependencyNotation!!, configureAction)
    fun testImplementation(dependencyNotation: Any?, configureAction: Action<ExternalModuleDependency> = Action { }) =
        add("testImplementation", dependencyNotation!!, configureAction)
    fun testRuntimeOnly(dependencyNotation: Any?, configureAction: Action<ExternalModuleDependency> = Action { }) =
        add("testRuntimeOnly", dependencyNotation!!, configureAction)
}