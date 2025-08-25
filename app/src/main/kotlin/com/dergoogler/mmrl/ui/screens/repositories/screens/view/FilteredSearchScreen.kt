package com.dergoogler.mmrl.ui.screens.repositories.screens.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ui.component.HtmlText
import com.dergoogler.mmrl.ui.component.Loading
import com.dergoogler.mmrl.ui.component.PageIndicator
import com.dergoogler.mmrl.ui.component.TopAppBar
import com.dergoogler.mmrl.ui.providable.LocalPanicArguments
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.ui.screens.repositories.screens.repository.ModulesList
import com.dergoogler.mmrl.viewmodel.RepositoryViewModel
import com.dergoogler.mmrl.ext.none
import com.dergoogler.mmrl.ext.panicString
import com.dergoogler.mmrl.ext.shareText
import com.dergoogler.mmrl.ext.takeTrue
import com.dergoogler.mmrl.ext.toDecodedUrl
import com.dergoogler.mmrl.ext.toEncodedUrl
import com.dergoogler.mmrl.ui.component.scaffold.Scaffold
import com.dergoogler.mmrl.ui.component.text.BBCodeText

@Composable
fun FilteredSearchScreen(
    viewModel: RepositoryViewModel,
    navController: NavHostController,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val list by viewModel.online.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val userPrefs = LocalUserPreferences.current
    val context = LocalContext.current

    val arguments = LocalPanicArguments.current

    val type = arguments.panicString("type")
    val value = arguments.panicString("value")
    val repoUrl = arguments.panicString("repoUrl")

    LaunchedEffect(list, value) {
        if (value.isNotBlank() && type.isNotBlank()) {
            viewModel.search("${type}:${value}")
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_left),
                            contentDescription = null
                        )
                    }
                },
                title = { Text(text = value.toDecodedUrl(true)) },
                actions = {
                    if (value.isNotBlank() && type.isNotBlank()) {
                        IconButton(
                            onClick = {
                                context.shareText("https://mmrl.dergoogler.com/search/${repoUrl.toEncodedUrl()}/$type/${value.toEncodedUrl()}?utm_medium=share&utm_source=${context.packageName}")
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.share),
                                contentDescription = null,
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = WindowInsets.none
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding)
        ) {
            if (viewModel.isLoading) {
                Loading()
            }

            if (list.isEmpty() && !viewModel.isLoading && value.isNotBlank()) {
                PageIndicator(
                    icon = R.drawable.cloud,
                    text = if (viewModel.isSearch) R.string.search_empty else R.string.repository_empty,
                )
            }

            this@Scaffold.ModulesList(
                before = {
                    BBCodeText(
                        modifier = Modifier.padding(vertical = 8.dp),
                        text = pluralStringResource(
                            id = R.plurals.search_results,
                            count = list.size,
                            list.size
                        )
                    )

                    userPrefs.developerMode.takeTrue {
                        Text(
                            text = "$type:${value.toEncodedUrl()}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline),
                        )
                    }
                },
                list = list,
                state = listState,
            )
        }
    }
}