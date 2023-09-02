package com.example.cmd.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class LogsDirectory


@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class NewLogFileChannel

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class IOCoroutineScope
