package com.aristidevs.cursotestingandroid.core

import com.aristidevs.cursotestingandroid.core.domain.coroutines.DispatchersProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.TestDispatcher

class TestDispatchersProvider(dispatcher: TestDispatcher) : DispatchersProvider {
    override val main: CoroutineDispatcher = dispatcher
    override val io: CoroutineDispatcher = dispatcher
    override val default: CoroutineDispatcher = dispatcher
}

