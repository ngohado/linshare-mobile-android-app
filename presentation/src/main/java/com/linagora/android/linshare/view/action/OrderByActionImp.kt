/*
 * LinShare is an open source filesharing software, part of the LinPKI software
 * suite, developed by Linagora.
 *
 * Copyright (C) 2020 LINAGORA
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Affero General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version,
 * provided you comply with the Additional Terms applicable for LinShare software by
 * Linagora pursuant to Section 7 of the GNU Affero General Public License,
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain the
 * display in the interface of the “LinShare™” trademark/logo, the "Libre & Free" mention,
 * the words “You are using the Free and Open Source version of LinShare™, powered by
 * Linagora © 2009–2020. Contribute to Linshare R&D by subscribing to an Enterprise
 * offer!”. You must also retain the latter notice in all asynchronous messages such as
 * e-mails sent with the Program, (ii) retain all hypertext links between LinShare and
 * http://www.linshare.org, between linagora.com and Linagora, and (iii) refrain from
 * infringing Linagora intellectual property rights over its trademarks and commercial
 * brands. Other Additional Terms apply, see
 * <http://www.linshare.org/licenses/LinShare-License_AfferoGPL-v3.pdf>
 * for more details.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for
 * more details.
 * You should have received a copy of the GNU Affero General Public License and its
 * applicable Additional Terms for LinShare along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General Public License version
 *  3 and <http://www.linshare.org/licenses/LinShare-License_AfferoGPL-v3.pdf> for
 *  the Additional Terms applicable to LinShare software.
 */

package com.linagora.android.linshare.view.action

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import arrow.core.Either
import com.linagora.android.linshare.domain.usecases.sharedspace.OpenOrderByDialog
import com.linagora.android.linshare.domain.model.order.OrderListConfigurationType
import com.linagora.android.linshare.domain.usecases.sharedspace.OnOrderByRowItemClick
import com.linagora.android.linshare.model.order.OrderTypeName
import com.linagora.android.linshare.util.toOrderTypeName
import com.linagora.android.linshare.view.base.BaseViewModel

class OrderByActionImp constructor(
    val viewModel: BaseViewModel
) : OrderByAction {

    private var mutableSelectedOrderType = MutableLiveData<OrderTypeName>(OrderTypeName.Name)
    override val selectedOrderType: LiveData<OrderTypeName> = mutableSelectedOrderType

    private var mutableCurrentOrderListConfigurationType = MutableLiveData<OrderListConfigurationType>(OrderListConfigurationType.DescendingModificationDate)
    override val currentOrderListConfigurationType = mutableCurrentOrderListConfigurationType

    override fun openOrderByDialog() {
        viewModel.dispatchState(Either.right(OpenOrderByDialog))
    }

    override fun setCurrentOrderListConfigurationType(orderListConfigurationType: OrderListConfigurationType) {
        mutableCurrentOrderListConfigurationType.value = orderListConfigurationType
        mutableSelectedOrderType.value = orderListConfigurationType.toOrderTypeName()
    }

    override fun getCurrentOrderListConfigurationType(): OrderListConfigurationType {
        return mutableCurrentOrderListConfigurationType.value ?: OrderListConfigurationType.DescendingModificationDate
    }

    override fun selectOrderByRowItem(orderTypeName: OrderTypeName) {
        currentOrderListConfigurationType.value?.let {
            val newOrderListConfigurationType = orderTypeName.generateNewConfigurationType(it)
            viewModel.dispatchUIState(Either.right(OnOrderByRowItemClick(newOrderListConfigurationType)))
        }
    }
}
