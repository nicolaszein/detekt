package io.gitlab.arturbosch.detekt.core

import io.gitlab.arturbosch.detekt.api.RuleSetProvider
import java.net.URL
import java.net.URLClassLoader
import java.util.ServiceLoader

/**
 * @author Artur Bosch
 */
class RuleSetLocator(val excludeDefaultRuleSets: Boolean,
					 val plugins: Array<URL>) {

	companion object {
		fun instance(settings: ProcessingSettings) = with(settings) {
			RuleSetLocator(excludeDefaultRuleSets, settings.pluginUrls)
		}
	}

	fun load(): List<RuleSetProvider> {
		val detektLoader = URLClassLoader(plugins, javaClass.classLoader)
		return ServiceLoader.load(RuleSetProvider::class.java, detektLoader).asIterable()
				.map { it.nullIfDefaultAndExcluded() }
				.filterNotNull()
				.toList()
	}

	private fun RuleSetProvider.nullIfDefaultAndExcluded() = if (excludeDefaultRuleSets && provided()) null else this

	private fun RuleSetProvider.provided() = ruleSetId in defaultRuleSetIds

	private val defaultRuleSetIds = listOf("code-smell", "comments", "complexity", "empty-blocks",
			"exceptions", "potential-bugs", "performance", "style")
}
