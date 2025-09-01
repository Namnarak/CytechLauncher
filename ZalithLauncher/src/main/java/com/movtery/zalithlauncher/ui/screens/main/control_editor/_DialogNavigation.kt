package com.movtery.zalithlauncher.ui.screens.main.control_editor

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material.icons.outlined.TouchApp
import androidx.navigation3.runtime.NavKey
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.screens.content.elements.CategoryIcon
import com.movtery.zalithlauncher.ui.screens.content.elements.CategoryItem
import kotlinx.serialization.Serializable

sealed interface EditWidgetCategory : NavKey {
    /** 基本信息 */
    @Serializable data object Info : EditWidgetCategory
    /** 点击事件 */
    @Serializable data object ClickEvent : EditWidgetCategory
    /** 控件样式 */
    @Serializable data object Style : EditWidgetCategory
}

/**
 * 编辑控件标签页
 */
val editWidgetCategories = listOf(
    CategoryItem(EditWidgetCategory.Info, { CategoryIcon(Icons.Outlined.Info, R.string.control_editor_edit_category_info) }, R.string.control_editor_edit_category_info),
    CategoryItem(EditWidgetCategory.ClickEvent, { CategoryIcon(Icons.Outlined.TouchApp, R.string.control_editor_edit_category_event) }, R.string.control_editor_edit_category_event),
    CategoryItem(EditWidgetCategory.Style, { CategoryIcon(Icons.Outlined.Style, R.string.control_editor_edit_category_style) }, R.string.control_editor_edit_category_style)
)