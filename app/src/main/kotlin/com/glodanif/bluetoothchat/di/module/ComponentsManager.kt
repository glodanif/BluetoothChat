package com.glodanif.bluetoothchat.di.module

import android.content.Context

class ComponentsManager {

    companion object {

        private lateinit var dsComponent: DataSourceComponent

        fun getDataSourceComponent(): DataSourceComponent = dsComponent

        fun initialize(context: Context) {
            dsComponent = DaggerDataSourceComponent.builder()
                    .dataSourceModule(DataSourceModule(context))
                    .build()
        }
    }
}