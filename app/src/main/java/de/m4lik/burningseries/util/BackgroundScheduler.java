package de.m4lik.burningseries.util;

import rx.Scheduler;
import rx.schedulers.Schedulers;

/**
 * Created by Malik on 28.01.2017
 *
 * @author Malik Mann
 */

public class BackgroundScheduler {
    public static Scheduler instance() {
        return Schedulers.io();
    }
}