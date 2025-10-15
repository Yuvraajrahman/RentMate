package com.rentmate.app.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class FlatDao_Impl implements FlatDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Flat> __insertionAdapterOfFlat;

  public FlatDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfFlat = new EntityInsertionAdapter<Flat>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `flats` (`key`,`name`,`address`,`dueDay`,`bkashNumber`) VALUES (?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Flat entity) {
        statement.bindLong(1, entity.getKey());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getAddress());
        statement.bindLong(4, entity.getDueDay());
        statement.bindString(5, entity.getBkashNumber());
      }
    };
  }

  @Override
  public Object upsert(final Flat flat, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfFlat.insert(flat);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object get(final Continuation<? super Flat> $completion) {
    final String _sql = "SELECT * FROM flats WHERE `key`=1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Flat>() {
      @Override
      @Nullable
      public Flat call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfKey = CursorUtil.getColumnIndexOrThrow(_cursor, "key");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "address");
          final int _cursorIndexOfDueDay = CursorUtil.getColumnIndexOrThrow(_cursor, "dueDay");
          final int _cursorIndexOfBkashNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "bkashNumber");
          final Flat _result;
          if (_cursor.moveToFirst()) {
            final int _tmpKey;
            _tmpKey = _cursor.getInt(_cursorIndexOfKey);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpAddress;
            _tmpAddress = _cursor.getString(_cursorIndexOfAddress);
            final int _tmpDueDay;
            _tmpDueDay = _cursor.getInt(_cursorIndexOfDueDay);
            final String _tmpBkashNumber;
            _tmpBkashNumber = _cursor.getString(_cursorIndexOfBkashNumber);
            _result = new Flat(_tmpKey,_tmpName,_tmpAddress,_tmpDueDay,_tmpBkashNumber);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
