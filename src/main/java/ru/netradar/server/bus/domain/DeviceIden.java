package ru.netradar.server.bus.domain;

import ru.netradar.util.EIntCode;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Created by rfk on 16.11.2017.
 */
public final class DeviceIden {
    /**
     * ID of user
     */
    public final Integer userId;
    /* Tracking object type
   MAPNAVUSERTYPE =1;
   ARTALUSERTYPE =2;
   TR102USERTYPE =3;
    */
    public final Type userType;

    public DeviceIden(@Nonnull Integer userId,
                      @Nonnull Type userType) {
        this.userId = Objects.requireNonNull(userId,"userId");
        this.userType = Objects.requireNonNull(userType,"userType");
    }

    @Nonnull
    public Integer getUserId() {
        return userId;
    }

    @Nonnull
    public Type getUserType() {
        return userType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DeviceIden that = (DeviceIden) o;

        if (!userId.equals(that.userId)) {
            return false;
        }
        return userType == that.userType;
    }

    @Override
    public int hashCode() {
        int result = userId.hashCode();
        result = 31 * result + userType.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DeviceIden{" +
                "userId=" + userId +
                ", userType=" + userType +
                '}';
    }

    public enum Type implements EIntCode {
        mapnav(1),
        artal(2),
        tr102(3);

        private final Integer code;

        Type(Integer code) {
            this.code = code;
        }

        @Nonnull
        @Override
        public Integer getCode() {
            return code;
        }

        public byte getAsByte(){
            return code.byteValue();
        }
    }



}
