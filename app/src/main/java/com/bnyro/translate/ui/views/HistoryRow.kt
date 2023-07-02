/*
 * Copyright (c) 2023 Bnyro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.bnyro.translate.ui.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bnyro.translate.R
import com.bnyro.translate.db.obj.HistoryItem
import com.bnyro.translate.ui.models.TranslationModel
import com.bnyro.translate.util.Preferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryRow(
    navController: NavController,
    translationModel: TranslationModel,
    historyItem: HistoryItem,
    onDelete: () -> Unit
) {
    var showDialog by remember {
        mutableStateOf(false)
    }

    fun loadTranslation() {
        showDialog = false
        translationModel.insertedText = historyItem.insertedText
        translationModel.translateNow()
        navController.navigate("translate")
    }

    val compactHistory = Preferences.get(
        Preferences.compactHistory,
        true
    )

    val maxLines = if (compactHistory) 3 else Int.MAX_VALUE

    val state = rememberDismissState(
        confirmValueChange = {
            when (it) {
                DismissValue.DismissedToEnd -> {
                    onDelete.invoke()
                    false
                }
                else -> false
            }
        }
    )

    SwipeToDismiss(
        state = state,
        background = {},
        dismissContent = {
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (compactHistory) {
                            showDialog = true
                        } else {
                            loadTranslation()
                        }
                    },
                overlineContent = {
                    Text("${historyItem.sourceLanguageName} -> ${historyItem.targetLanguageName}")
                },
                headlineContent = {
                    Text(
                        historyItem.translatedText,
                        fontSize = 18.sp,
                        maxLines = maxLines,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    Text(
                        historyItem.insertedText,
                        fontSize = 14.sp,
                        maxLines = maxLines,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        },
        directions = setOf(DismissDirection.StartToEnd)
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
            },
            text = {
                Column {
                    Text(
                        historyItem.insertedText,
                        fontSize = 18.sp
                    )

                    Spacer(
                        modifier = Modifier
                            .height(5.dp)
                    )

                    Text(
                        historyItem.translatedText,
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        loadTranslation()
                    }
                ) {
                    Text(stringResource(R.string.open))
                }
            }
        )
    }
}
