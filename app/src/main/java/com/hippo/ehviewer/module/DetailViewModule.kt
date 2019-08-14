package com.hippo.ehviewer.module

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.axlecho.api.MHApi
import com.axlecho.api.MHApiSource
import com.axlecho.api.MHComicInfo
import com.hippo.ehviewer.client.data.GalleryDetail
import com.hippo.ehviewer.client.data.GalleryInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class DetailViewModule : ViewModel() {
    val baseInfo: MutableLiveData<GalleryInfo> = MutableLiveData()
    val detail: MutableLiveData<GalleryDetail> = MutableLiveData()

    fun detail(): Disposable? {
        val info = baseInfo.value ?: return null
        return MHApi.INSTANCE.get(info.source).info(info.gid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    detail.setValue(GalleryDetail(it))
                }
    }

    fun switchSource(target: MHApiSource): Disposable? {
        val info = baseInfo.value ?: return null
        return MHApi.INSTANCE.switchSource(MHComicInfo("", info.title, "", "", 0, ",", "", 0.0f, false, info.source), target)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    baseInfo.value = GalleryInfo(it)
                    detail()
                }
    }

    fun setInfo(info: GalleryInfo?) {
        baseInfo.value = info
    }

    fun getInfo(): GalleryInfo? {
        return baseInfo.value
    }

    fun getGalleryDetailUrl(): String {
        val info = baseInfo.value ?: return ""
        return MHApi.INSTANCE.get(info.source).pageUrl(info.gid)
    }

    fun getGid(): String {
        val info = baseInfo.value ?: return ""
        return info.gid
    }
}