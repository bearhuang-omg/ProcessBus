package com.bear.processbus

import android.os.Parcel
import android.os.Parcelable

//Event的附件，可用于传输大文件
internal class Attachment(var attachmentContent: ByteArray) : Parcelable, IAttachment {

    constructor(attach: IAttachment) : this(attach.getContent()) {

    }

    constructor(parcel: Parcel) : this(parcel.createByteArray() ?: "".toByteArray()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByteArray(attachmentContent)
    }

    fun readFromParcel(source: Parcel) {
        attachmentContent = source.createByteArray() ?: "".toByteArray()
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

    override fun getContent(): ByteArray {
        return attachmentContent
    }
}