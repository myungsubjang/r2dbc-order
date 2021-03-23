package com.example.r2dbcorder.util;

import com.example.r2dbcorder.repository.entity.OmOd;
import com.example.r2dbcorder.repository.entity.OmOdDtl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
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

    public static OmOd createTestOrder(String memberNo, String nameWhoOrder) {
        OmOd newOrder = new OmOd();
        newOrder.setMbNo(memberNo);
        newOrder.setOdrNm(nameWhoOrder);
        newOrder.setOmOdDtlList(createTestOrderDetail(ThreadLocalRandom.current().nextInt(1, 10), memberNo));
        return newOrder;
    }

    private static List<OmOdDtl> createTestOrderDetail(int size, String memberNo) {
        List<OmOdDtl> orderDetails = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            OmOdDtl orderDetail = new OmOdDtl();
            orderDetail.setMbNo(memberNo);
            TestProduct product = TestProduct.randomProduct();
            int randomOrderQuantity = ThreadLocalRandom.current().nextInt(1, 10);
            orderDetail.setOdQty(randomOrderQuantity);
            orderDetail.setSlPrc(randomOrderQuantity * product.getPrice());
            orderDetail.setDcAmt(2000);
            orderDetail.setPdNo(product.getPdNo());
            orderDetail.setPdNm(product.getPdNm());
            orderDetails.add(orderDetail);
        }
        return orderDetails;
    }

    private enum TestProduct {
        EARPHONE("이어폰", "1", 35710),
        ICE_CREAM("아이스크림", "2", 17900),
        SNEAKERS("스니커즈", "3", 38710);

        private final String pdNm;
        private final String pdNo;
        private final int price;

        private static final List<TestProduct> TEST_PRODUCTS = Collections.unmodifiableList(Arrays.asList(values()));
        private static final int SIZE = TEST_PRODUCTS.size();
        private static final ThreadLocalRandom random = ThreadLocalRandom.current();

        TestProduct(String pdNm, String pdNo, int price) {
            this.pdNm = pdNm;
            this.pdNo = pdNo;
            this.price = price;
        }

        public String getPdNm() {
            return this.pdNm;
        }
        public String getPdNo() {
            return this.pdNo;
        }
        public int getPrice() {
            return this.price;
        }

        public static TestProduct randomProduct() {
            return TEST_PRODUCTS.get(random.nextInt(SIZE));
        }
    }

}
