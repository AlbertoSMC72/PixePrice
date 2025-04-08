package com.example.pixelprice.core.navigation

object Routes {
    const val LOGIN = "Login"
    const val REGISTER = "Register"

    const val PROJECT_LIST = "ProjectList"
    const val CREATE_PROJECT = "CreateProject"

    const val ARG_PROJECT_ID = "projectId"

    const val PROJECT_DETAIL_BASE = "ProjectDetail"
    fun projectDetail(projectId: Int) = "$PROJECT_DETAIL_BASE/$projectId"
    const val PROJECT_DETAIL_ROUTE = "$PROJECT_DETAIL_BASE/{$ARG_PROJECT_ID}"

    const val PROCESSING_BASE = "Processing"
    fun processing(projectId: Int) = "$PROCESSING_BASE/$projectId"
    const val PROCESSING_ROUTE = "$PROCESSING_BASE/{$ARG_PROJECT_ID}"

    const val PROFILE = "Profile"
}