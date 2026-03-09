/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package com.fakeroot.server.api;
public interface IShizukuServiceConnection extends android.os.IInterface
{
  /** Default implementation for IShizukuServiceConnection. */
  public static class Default implements com.fakeroot.server.api.IShizukuServiceConnection
  {
    @Override public void connected(android.os.IBinder binder) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements com.fakeroot.server.api.IShizukuServiceConnection
  {
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an com.fakeroot.server.api.IShizukuServiceConnection interface,
     * generating a proxy if needed.
     */
    public static com.fakeroot.server.api.IShizukuServiceConnection asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof com.fakeroot.server.api.IShizukuServiceConnection))) {
        return ((com.fakeroot.server.api.IShizukuServiceConnection)iin);
      }
      return new com.fakeroot.server.api.IShizukuServiceConnection.Stub.Proxy(obj);
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
        case TRANSACTION_connected:
        {
          android.os.IBinder _arg0;
          _arg0 = data.readStrongBinder();
          this.connected(_arg0);
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
    private static class Proxy implements com.fakeroot.server.api.IShizukuServiceConnection
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
      @Override public void connected(android.os.IBinder binder) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeStrongBinder(binder);
          boolean _status = mRemote.transact(Stub.TRANSACTION_connected, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
    }
    static final int TRANSACTION_connected = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
  }
  public static final java.lang.String DESCRIPTOR = "com.fakeroot.server.api.IShizukuServiceConnection";
  public void connected(android.os.IBinder binder) throws android.os.RemoteException;
}
