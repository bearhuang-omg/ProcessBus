// IService.aidl
package com.bear.processbus.service;

parcelable Request;
parcelable Response;

interface IService {
    Response call(in Request request);
}