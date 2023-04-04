/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.ide

import org.jetbrains.kotlin.compilerRunner.toArgumentStrings
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.CreateCompilerArgumentsContext
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerArgumentsProducer
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.utils.hasAndroidPlugin

internal class IdeCompilerArgumentsResolverImpl(
    private val extension: KotlinProjectExtension
) : IdeCompilerArgumentsResolver {

    override fun resolveCompilerArguments(any: Any): List<String>? {
        if (any is KotlinCompilerArgumentsProducer) {
            return resolveCompilerArguments(any)
        }

        if (any is KotlinCompilation<*>) {
            return resolveCompilerArguments(any.compileTaskProvider.orNull ?: return null)
        }

        return null
    }

    private fun resolveCompilerArguments(producer: KotlinCompilerArgumentsProducer): List<String> {
        val compilerArguments = producer.createCompilerArguments(
            CreateCompilerArgumentsContext(
                isLenient = true,
                includeArgumentTypes = setOfNotNull(
                    KotlinCompilerArgumentsProducer.ArgumentType.Primitive,
                    KotlinCompilerArgumentsProducer.ArgumentType.Classpath
                        .takeIf { extension !is KotlinMultiplatformExtension }
                        .takeIf { producer is KotlinCompile }
                        .takeIf { !extension.project.hasAndroidPlugin }
                ),
            )
        )
        return compilerArguments.toArgumentStrings(
            shortArgumentKeys = true,
            compactArgumentValues = false
        )
    }
}