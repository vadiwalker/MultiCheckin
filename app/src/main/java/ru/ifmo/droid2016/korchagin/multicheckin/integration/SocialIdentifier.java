package ru.ifmo.droid2016.korchagin.multicheckin.integration;

import android.support.annotation.IntDef;

/**
 * Created by ME on 19.12.2016.
 */

@IntDef(value = {
        SocialIdentifier.VK_LOGIN,
        SocialIdentifier.TWITTER_AUTH
})
/**
 * Если нужно указать где-нибудь какой-нибудь ID, резервируем его здесь, блоками по 100.
 * Номера 0-100 не трогаем
 *  Пока что воообще никак не используется, если что удалим
 */
public @interface SocialIdentifier {
    int VK_LOGIN = 1001; // если что, возьму под VK блок 1000-1099
    int TWITTER_AUTH = 1101; // Twitter 1100-1199
}