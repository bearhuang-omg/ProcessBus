package com.bear.processbus

import android.os.Parcel
import android.os.Parcelable

//Event的附件，可用于传输大文件
class Attachment(var content: ByteArray?):Parcelable {
    constructor(parcel: Parcel) : this(parcel.createByteArray()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByteArray(content)
    }

    fun readFromParcel(source: Parcel) {
        content = source.createByteArray()
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Attachment> {
        override fun createFromParcel(parcel: Parcel): Attachment {
            return Attachment(parcel)
        }

        override fun newArray(size: Int): Array<Attachment?> {
            return arrayOfNulls(size)
        }
    }
}