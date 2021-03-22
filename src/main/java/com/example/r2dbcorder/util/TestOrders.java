package com.example.r2dbcorder.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public class TestOrders {

    private static final AtomicInteger sequence = new AtomicInteger();

    public static String generateOrderNumber() {
        return getTodayStr() + getLeadingZeroSeq();
    }

    private static String getTodayStr() {
        return LocalDate.now()
                .format(DateTimeFormatter.BASIC_ISO_DATE);
    }

    private static String getLeadingZeroSeq() {
        int generatedSeq = sequence.incrementAndGet();
        return String.format("%05d", generatedSeq);
    }


}
