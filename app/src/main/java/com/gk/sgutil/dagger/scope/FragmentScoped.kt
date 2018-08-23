package com.gk.sgutil.dagger.scope

import javax.inject.Scope

/**
 *
 */
@Scope
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.TYPE, AnnotationTarget.FUNCTION)
annotation class FragmentScoped