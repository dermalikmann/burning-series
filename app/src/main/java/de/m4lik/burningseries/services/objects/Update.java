package de.m4lik.burningseries.services.objects;

import android.os.Parcel;
import android.os.Parcelable;

import org.immutables.gson.Gson;
import org.immutables.value.Value;

/**
 * Created by Malik on 28.01.2017
 *
 * @author Malik Mann
 */

@Value.Immutable
@Gson.TypeAdapters
public abstract class Update implements Parcelable {

    public static final Creator<Update> CREATOR = new Creator<Update>() {
        public Update createFromParcel(Parcel source) {
            return ImmutableUpdate.builder()
                    .buildNumber(source.readInt())
                    .versionName(source.readString())
                    .changelog(source.readString())
                    .apk(source.readString())
                    .build();
        }

        public Update[] newArray(int size) {
            return new Update[size];
        }
    };

    public abstract Integer buildNumber();

    public abstract String versionName();

    public abstract String changelog();

    public abstract String apk();

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(buildNumber());
        dest.writeString(versionName());
        dest.writeString(changelog());
        dest.writeString(apk());
    }
}
