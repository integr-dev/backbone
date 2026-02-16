package net.integr.backbone.systems.hotloader.configuration.resolver

import net.integr.backbone.Backbone
import org.apache.ivy.Ivy
import org.apache.ivy.core.LogOptions
import org.apache.ivy.core.module.descriptor.DefaultDependencyArtifactDescriptor
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor
import org.apache.ivy.core.module.id.ModuleRevisionId
import org.apache.ivy.core.resolve.ResolveOptions
import org.apache.ivy.core.settings.IvySettings
import org.apache.ivy.plugins.parser.xml.XmlModuleDescriptorWriter
import org.apache.ivy.plugins.resolver.ChainResolver
import org.apache.ivy.plugins.resolver.IBiblioResolver
import org.apache.ivy.plugins.resolver.URLResolver
import org.apache.ivy.util.AbstractMessageLogger
import org.apache.ivy.util.DefaultMessageLogger
import org.apache.ivy.util.Message
import java.io.File
import kotlin.script.experimental.api.*
import kotlin.script.experimental.dependencies.ExternalDependenciesResolver
import kotlin.script.experimental.dependencies.RepositoryCoordinates
import kotlin.script.experimental.dependencies.impl.toRepositoryUrlOrNull

/*
    This file originates from the JetBrains simple-main-kts example for kotlin scripting
    https://github.com/Kotlin/kotlin-script-examples/blob/master/jvm/simple-main-kts/simple-main-kts/src/main/kotlin/org/jetbrains/kotlin/script/examples/simpleMainKts/impl/ivy.kt
 */
class IvyResolver : ExternalDependenciesResolver {

    private fun String?.isValidParam() = this?.isNotBlank() ?: false

    override fun acceptsArtifact(artifactCoordinates: String): Boolean = with(artifactCoordinates) {
        isValidParam() && count { it == ':' }.let { it == 2 || it == 3 }
    }

    override fun acceptsRepository(repositoryCoordinates: RepositoryCoordinates): Boolean =
        repositoryCoordinates.toRepositoryUrlOrNull() != null

    override suspend fun resolve(
        artifactCoordinates: String,
        options: ExternalDependenciesResolver.Options,
        sourceCodeLocation: SourceCode.LocationWithId?
    ): ResultWithDiagnostics<List<File>> {
        logger.info("Resolving artifact with ivy: $artifactCoordinates")
        val artifactType = artifactCoordinates.substringAfterLast('@', "").trim()
        val stringCoordinates = if (artifactType.isNotEmpty()) artifactCoordinates.removeSuffix("@$artifactType") else artifactCoordinates
        return if (acceptsArtifact(stringCoordinates)) {
            val artifactId = stringCoordinates.split(':')
            try {
                val result = resolveArtifact(
                    artifactId[0], artifactId[1], artifactId[2],
                    if (artifactId.size > 3) artifactId[3] else null,
                    artifactType.ifEmpty { null }
                )
                logger.info("Resolved artifact with ivy: $artifactCoordinates")

                result
            } catch (e: Exception) {
                logger.severe("Failed to resolve artifact with ivy: $artifactCoordinates")
                makeFailureResult(e.asDiagnostics())
            }
        } else {
            makeFailureResult("Unrecognized set of arguments to ivy resolver: $stringCoordinates")
        }
    }

    private val ivyResolvers = arrayListOf<URLResolver>()

    private fun resolveArtifact(
        groupId: String,
        artifactName: String,
        revision: String,
        conf: String? = null,
        type: String? = null
    ): ResultWithDiagnostics<List<File>> {

        if (ivyResolvers.isEmpty() || ivyResolvers.none { it.name == "central" }) {
            ivyResolvers.add(
                IBiblioResolver().apply {
                    isM2compatible = true
                    isUsepoms = true
                    name = "central"
                }
            )
        }
        val ivySettings = IvySettings().apply {
            val resolver =
                if (ivyResolvers.size == 1) ivyResolvers.first()
                else ChainResolver().also {
                    it.name = "chain"
                    for (resolver in ivyResolvers) {
                        it.add(resolver)
                    }
                }
            addResolver(resolver)
            setDefaultResolver(resolver.name)
        }

        val ivy = Ivy.newInstance(ivySettings)

        val moduleDescriptor = DefaultModuleDescriptor.newDefaultInstance(
            ModuleRevisionId.newInstance(groupId, "$artifactName-caller", "working")
        )

        val depsDescriptor = DefaultDependencyDescriptor(
            moduleDescriptor,
            ModuleRevisionId.newInstance(groupId, artifactName, conf, revision),
            false, false, true
        )

        if (type != null) {
            val depArtifact = DefaultDependencyArtifactDescriptor(depsDescriptor, artifactName, type, type, null, null)
            depsDescriptor.addDependencyArtifact(conf, depArtifact)
        }

        depsDescriptor.addDependencyConfiguration("default", "master,compile")
        moduleDescriptor.addDependency(depsDescriptor)

        val resolveOptions = ResolveOptions().apply {
            setConfs(arrayOf("default"))
            setLog(LogOptions.LOG_QUIET)
            setOutputReport(false)
        }

        //creates an ivy configuration file
        val ivyFile = File.createTempFile("ivy", ".xml").apply { deleteOnExit() }
        XmlModuleDescriptorWriter.write(moduleDescriptor, ivyFile)
        val report = ivy.resolve(ivyFile.toURI().toURL(), resolveOptions)

        val diagnostics = report.allProblemMessages.map { it.asErrorDiagnostics() }

        return if (report.hasError()) makeFailureResult(diagnostics)
        else report.allArtifactsReports.map { it.localFile }.asSuccess(diagnostics)
    }

    override fun addRepository(
        repositoryCoordinates: RepositoryCoordinates,
        options: ExternalDependenciesResolver.Options,
        sourceCodeLocation: SourceCode.LocationWithId?
    ): ResultWithDiagnostics<Boolean> {
        val url = repositoryCoordinates.toRepositoryUrlOrNull()

        if (url != null) {
            ivyResolvers.add(
                IBiblioResolver().apply {
                    isM2compatible = true
                    name = url.host
                    root = url.toExternalForm()
                }
            )
            return true.asSuccess()
        } else {
            return false.asSuccess()
        }
    }


    companion object {
        private val logger = Backbone.LOGGER.derive("ivy")

        init {
            Message.setDefaultLogger(object : AbstractMessageLogger() {
                override fun doProgress() {
                    logger.info(".")
                }

                override fun doEndProgress(msg: String) {
                    logger.info(msg)
                }

                override fun log(msg: String, level: Int) {
                    if (level > Message.MSG_WARN) return
                    logger.info(msg)
                }

                override fun rawlog(msg: String, level: Int) {
                    if (level > Message.MSG_WARN) return
                    logger.info(msg)
                }
            })
        }
    }
}