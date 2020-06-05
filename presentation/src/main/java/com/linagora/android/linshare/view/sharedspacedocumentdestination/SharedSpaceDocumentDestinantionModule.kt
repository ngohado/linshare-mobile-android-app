package com.linagora.android.linshare.view.sharedspacedocumentdestination

import androidx.lifecycle.ViewModel
import com.linagora.android.linshare.inject.annotation.FragmentScoped
import com.linagora.android.linshare.inject.annotation.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
internal abstract class SharedSpaceDocumentDestinantionModule {
    @FragmentScoped
    @ContributesAndroidInjector
    internal abstract fun contributeSharedSpaceDocumentDestinationFragment(): SharedSpaceDocumentDestinationFragment

    @Binds
    @IntoMap
    @ViewModelKey(SharedSpaceDocumentDestinationViewModel::class)
    abstract fun bindSharedSpaceDocumentDestinationViewModel(sharedSpaceDocumentDestinationViewModel: SharedSpaceDocumentDestinationViewModel): ViewModel
}
