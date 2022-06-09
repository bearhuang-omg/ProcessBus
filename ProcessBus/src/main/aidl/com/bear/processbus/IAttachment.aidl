// IAttachment.aidl
package com.bear.processbus;
import android.os.Parcelable;

parcelable Attachment;

//event里面的附件
interface IAttachment {
    Attachment getAttachment();
}