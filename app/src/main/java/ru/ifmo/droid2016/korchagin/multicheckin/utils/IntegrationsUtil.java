package ru.ifmo.droid2016.korchagin.multicheckin.utils;

import java.util.Vector;

import ru.ifmo.droid2016.korchagin.multicheckin.integration.FacebookIntegration;
import ru.ifmo.droid2016.korchagin.multicheckin.integration.SocialIntegration;
import ru.ifmo.droid2016.korchagin.multicheckin.integration.TwitterIntegration;
import ru.ifmo.droid2016.korchagin.multicheckin.integration.VKIntegration;

public class IntegrationsUtil {

    public static Vector<SocialIntegration> getAllIntegrations() {
        Vector<SocialIntegration> vector = new Vector<>();

        vector.addElement(FacebookIntegration.getInstance());
        vector.addElement(VKIntegration.getInstance());
        vector.addElement(TwitterIntegration.getInstance());
        // TODO  добавить сюда все Integration-ы

        return vector;
    }


}
