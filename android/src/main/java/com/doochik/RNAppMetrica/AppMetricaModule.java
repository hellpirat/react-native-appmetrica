package com.doochik.RNAppMetrica;

import android.app.Activity;
import android.app.Application;
import android.support.annotation.Nullable;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;

import java.util.Calendar;
import java.util.Date;

import com.yandex.metrica.YandexMetrica;
import com.yandex.metrica.YandexMetricaConfig;
import com.yandex.metrica.profile.UserProfile;
import com.yandex.metrica.profile.Attribute;
import com.yandex.metrica.profile.GenderAttribute;

import static com.facebook.react.bridge.ReadableType.Array;

public class AppMetricaModule extends ReactContextBaseJavaModule {
    final static String MODULE_NAME = "AppMetrica";
    final static String API_KEY = "apiKey";
    final static String NAME = "name";
    final static String AGE = "age";
    final static String BIRTH_DATE = "birthDate";
    final static String GENDER = "gender";
    final static String NOTIFICATIONS_ENABLED = "notificationsEnabled";

    public AppMetricaModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return MODULE_NAME;
    }


    @ReactMethod
    public void activateWithApiKey(String key) {
        YandexMetricaConfig.Builder configBuilder = YandexMetricaConfig.newConfigBuilder(key);
        YandexMetrica.activate(getReactApplicationContext().getApplicationContext(), configBuilder.build());
        Activity activity = getCurrentActivity();
        if (activity != null) {
            Application application = activity.getApplication();
            YandexMetrica.enableActivityAutoTracking(application);
        }
    }

    @ReactMethod
    public void activateWithConfig(ReadableMap params) {
        YandexMetricaConfig.Builder configBuilder = YandexMetricaConfig.newConfigBuilder(params.getString(API_KEY));
        if (params.hasKey("sessionTimeout")) {
            configBuilder.withSessionTimeout(params.getInt("sessionTimeout"));
        }
        if (params.hasKey("firstActivationAsUpdate")) {
            configBuilder.handleFirstActivationAsUpdate(params.getBoolean("firstActivationAsUpdate"));
        }
        YandexMetrica.activate(getReactApplicationContext().getApplicationContext(), configBuilder.build());
        Activity activity = getCurrentActivity();
        if (activity != null) {
            Application application = activity.getApplication();
            YandexMetrica.enableActivityAutoTracking(application);
        }
    }

    @ReactMethod
    public void reportError(String message) {
        try {
            Integer.valueOf("00xffWr0ng");
        } catch (Throwable error) {
            YandexMetrica.reportError(message, error);
        }
    }

    @ReactMethod
    public void reportEvent(String message, @Nullable ReadableMap params) {
        if (params != null) {
            YandexMetrica.reportEvent(message, MapUtil.toMap(params));
        } else {
            YandexMetrica.reportEvent(message);
        }
    }

    @ReactMethod
    public void setUserProfileID(String profileID) {
        YandexMetrica.setUserProfileID(profileID);
    }

    @ReactMethod
    public void reportUserProfile(ReadableMap params) {
        UserProfile.Builder userProfileBuilder = UserProfile.newBuilder();
        ReadableMapKeySetIterator iterator = params.keySetIterator();

        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();

            switch (key) {
                // predefined attributes
                case NAME:
                    userProfileBuilder.apply(
                            params.isNull(key)
                                    ? Attribute.name().withValueReset()
                                    : Attribute.name().withValue(params.getString(key))
                    );
                    break;
                case GENDER:
                    userProfileBuilder.apply(
                            params.isNull(key)
                                    ? Attribute.gender().withValueReset()
                                    : Attribute.gender().withValue(
                                    params.getString(key).equals("female")
                                            ? GenderAttribute.Gender.FEMALE
                                            : params.getString(key).equals("male")
                                            ? GenderAttribute.Gender.MALE
                                            : GenderAttribute.Gender.OTHER
                            )
                    );
                    break;
                case AGE:
                    userProfileBuilder.apply(
                            params.isNull(key)
                                    ? Attribute.birthDate().withValueReset()
                                    : Attribute.birthDate().withAge(params.getInt(key))
                    );
                    break;
                case BIRTH_DATE:
                    if (params.isNull(key)) {
                        userProfileBuilder.apply(
                                Attribute.birthDate().withValueReset()
                        );
                    } else if (params.getType(key) == Array) {
                        // an array of [ year[, month][, day] ]
                        ReadableArray date = params.getArray(key);
                        if (date.size() == 1) {
                            userProfileBuilder.apply(
                                    Attribute.birthDate().withBirthDate(
                                            date.getInt(0)
                                    )
                            );
                        } else if (date.size() == 2) {
                            userProfileBuilder.apply(
                                    Attribute.birthDate().withBirthDate(
                                            date.getInt(0),
                                            date.getInt(1)
                                    )
                            );
                        } else {
                            userProfileBuilder.apply(
                                    Attribute.birthDate().withBirthDate(
                                            date.getInt(0),
                                            date.getInt(1),
                                            date.getInt(2)
                                    )
                            );
                        }
                    } else {
                        // number of milliseconds since Unix epoch
                        Date date = new Date((long)params.getInt(key));
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(date);
                        userProfileBuilder.apply(
                                Attribute.birthDate().withBirthDate(cal)
                        );
                    }
                    break;
                case NOTIFICATIONS_ENABLED:
                    userProfileBuilder.apply(
                            params.isNull(key)
                                    ? Attribute.notificationsEnabled().withValueReset()
                                    : Attribute.notificationsEnabled().withValue(params.getBoolean(key))
                    );
                    break;
                // custom attributes
                default:
                    // TODO: come up with a syntax solution to reset custom attributes. `null` will break type checking here
                    switch (params.getType(key)) {
                        case Boolean:
                            userProfileBuilder.apply(
                                    Attribute.customBoolean(key).withValue(params.getBoolean(key))
                            );
                            break;
                        case Number:
                            userProfileBuilder.apply(
                                    Attribute.customNumber(key).withValue(params.getDouble(key))
                            );
                            break;
                        case String:
                            String value = params.getString(key);
                            if (value.startsWith("+") || value.startsWith("-")) {
                                userProfileBuilder.apply(
                                        Attribute.customCounter(key).withDelta(Double.parseDouble(value))
                                );
                            } else {
                                userProfileBuilder.apply(
                                        Attribute.customString(key).withValue(value)
                                );
                            }
                            break;
                    }
            }
        }

        YandexMetrica.reportUserProfile(userProfileBuilder.build());
    }
}
