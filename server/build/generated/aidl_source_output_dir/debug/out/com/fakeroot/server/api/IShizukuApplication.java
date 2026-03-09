/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package com.fakeroot.server.api;
public interface IShizukuApplication extends android.os.IInterface
{
  /** Default implementation for IShizukuApplication. */
  public static class Default implements com.fakeroot.server.api.IShizukuApplication
  {
    @Override public void bindApplication(android.os.Bundle args) throws android.os.RemoteException
    {
    }
    @Override public void dispatchRequestPermissionResult(int requestCode, boolean granted) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements com.fakeroot.server.api.IShizukuApplication
  {
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an com.fakeroot.server.api.IShizukuApplication interface,
     * generating a proxy if needed.
     */
    public static com.fakeroot.server.api.IShizukuApplication asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof com.fakeroot.server.api.IShizukuApplication))) {
        return ((com.fakeroot.server.api.IShizukuApplication)iin);
      }
      return new com.fakeroot.server.api.IShizukuApplication.Stub.Proxy(obj);
    }
    @Override public android.os.IBinder asBinder()
    {
      return this;
    }
    @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
    {
      java.lang.String descriptor = DESCRIPTOR;
      if (code >= android.os.IBinder.FIRST_CALL_TRANSACTION && code <= android.os.IBinder.LAST_CALL_TRANSACTION) {
        data.enforceInterface(descriptor);
      }
      switch (code)
      {
        case INTERFACE_TRANSACTION:
        {
          reply.writeString(descriptor);
          return true;
        }
      }
      switch (code)
      {
        case TRANSACTION_bindApplication:
        {
          android.os.Bundle _arg0;
          _arg0 = _Parcel.readTypedObject(data, android.os.Bundle.CREATOR);
          this.bindApplication(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_dispatchRequestPermissionResult:
        {
          int _arg0;
          _arg0 = data.readInt();
          boolean _arg1;
          _arg1 = (0!=data.readInt());
          this.dispatchRequestPermissionResult(_arg0, _arg1);
          reply.writeNoException();
          break;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
      return true;
    }
    private static class Proxy implements com.fakeroot.server.api.IShizukuApplication
    {
      private android.os.IBinder mRemote;
      Proxy(android.os.IBinder remote)
      {
        mRemote = remote;
      }
      @Override public android.os.IBinder asBinder()
      {
        return mRemote;
      }
      public java.lang.String getInterfaceDescriptor()
      {
        return DESCRIPTOR;
      }
      @Override public void bindApplication(android.os.Bundle args) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _Parcel.writeTypedObject(_data, args, 0);
          boolean _status = mRemote.transact(Stub.TRANSACTION_bindApplication, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void dispatchRequestPermissionResult(int requestCode, boolean granted) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(requestCode);
          _data.writeInt(((granted)?(1):(0)));
          boolean _status = mRemote.transact(Stub.TRANSACTION_dispatchRequestPermissionResult, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
    }
    static final int TRANSACTION_bindApplication = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_dispatchRequestPermissionResult = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
  }
  public static final java.lang.String DESCRIPTOR = "com.fakeroot.server.api.IShizukuApplication";
  public void bindApplication(android.os.Bundle args) throws android.os.RemoteException;
  public void dispatchRequestPermissionResult(int requestCode, boolean granted) throws android.os.RemoteException;
  /** @hide */
  static class _Parcel {
    static private <T> T readTypedObject(
        android.os.Parcel parcel,
        android.os.Parcelable.Creator<T> c) {
      if (parcel.readInt() != 0) {
          return c.createFromParcel(parcel);
      } else {
          return null;
      }
    }
    static private <T extends android.os.Parcelable> void writeTypedObject(
        android.os.Parcel parcel, T value, int parcelableFlags) {
      if (value != null) {
        parcel.writeInt(1);
        value.writeToParcel(parcel, parcelableFlags);
      } else {
        parcel.writeInt(0);
      }
    }
  }
}
