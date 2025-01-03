package com.dergoogler.mmrl.ui.screens.settings.changelogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.model.online.Changelog
import com.dergoogler.mmrl.network.runRequest
import com.dergoogler.mmrl.stub.IMMRLApiManager
import com.dergoogler.mmrl.ui.component.Loading
import com.dergoogler.mmrl.ui.component.ScaffoldDefaults
import com.dergoogler.mmrl.ui.component.SettingsScaffold
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.ui.screens.settings.changelogs.items.ChangelogItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Composable
fun ChangelogScreen() {
    val userPreferences = LocalUserPreferences.current
    var changelog by remember { mutableStateOf<List<Changelog>?>(null) }

    LaunchedEffect(Unit) {
        runRequest {
            withContext(Dispatchers.IO) {
                val api = IMMRLApiManager.build()
                return@withContext api.changelog.execute()
            }
        }.onSuccess { list ->
            changelog = list
        }.onFailure {
            Timber.e(it, "unable to get changelog")
        }

    }


    SettingsScaffold(
        modifier = ScaffoldDefaults.settingsScaffoldModifier,
        title = R.string.settings_changelog
    ) {
        AnimatedVisibility(
            visible = changelog == null, enter = fadeIn(), exit = fadeOut()
        ) {
            Loading()
        }

        AnimatedVisibility(
            visible = changelog != null, enter = fadeIn(), exit = fadeOut()
        ) {
            changelog?.let { bl ->
                LazyColumn {
                    items(items = bl, key = { it.versionCode }) { entry ->
                        if (!userPreferences.checkAppUpdatesPreReleases && entry.preRelease) return@items

                        ChangelogItem(changelog = entry)
                    }
                }
            }
        }
    }
}