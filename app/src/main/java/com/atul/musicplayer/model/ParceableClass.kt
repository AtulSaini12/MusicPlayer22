package com.atul.musicplayer.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.File

@Parcelize
data class parceableClass(
    var file : File
): Parcelable