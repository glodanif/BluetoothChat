package com.glodanif.bluetoothchat.di

import android.content.Context
import com.glodanif.bluetoothchat.di.component.DaggerDataSourceComponent
import com.glodanif.bluetoothchat.di.component.DataSourceComponent
import com.glodanif.bluetoothchat.di.module.DataSourceModule

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
