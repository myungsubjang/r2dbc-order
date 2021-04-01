package com.example.r2dbcorder.util;

import com.example.r2dbcorder.repository.entity.OmOd;
import com.example.r2dbcorder.repository.entity.OmOdDtl;
import com.example.r2dbcorder.repository.entity.OmOdFvrDtl;
import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class TestOrderUtil {

    private static AtomicInteger orderSeq = new AtomicInteger();
    private static AtomicInteger favorSeq = new AtomicInteger();

    public static String generateOrderNumber() {
        return getTodayStr() + getLeadingZeroSeq();
    }

    private static String getTodayStr() {
        return LocalDate.now()
                .format(DateTimeFormatter.BASIC_ISO_DATE);
    }

    private static String getLeadingZeroSeq() {
        int generatedSeq = orderSeq.incrementAndGet();
        return String.format("%05d", generatedSeq);
    }

    public static String generateOrderFavorNumber() {
        return Integer.toString(favorSeq.incrementAndGet());
    }

    public static OmOd createTestOrder(String memberNo, String name) {
        //TODO 클래스 분리
        OmOd newOrder = new OmOd();
        newOrder.setMbNo(memberNo);
        newOrder.setOdrNm(name);

        List<OmOdDtl> testOrderDetails = createTestOrderDetails(randomNoBoundProductSize(), memberNo);
        newOrder.setOmOdDtlList(testOrderDetails);

//        List<OmOdFvrDtl> testOrderFavorDetails = Collections.emptyList();
        List<OmOdFvrDtl> testOrderFavorDetails = createTestOrderFavorDetails(testOrderDetails);
        newOrder.setOmOdFvrDtlList(testOrderFavorDetails);
        return newOrder;
    }

    private static int randomNoBoundProductSize() {
        return ThreadLocalRandom.current().nextInt(1, TestProduct.SIZE + 1);
    }

    private static List<OmOdDtl> createTestOrderDetails(int orderDtlSize, String memberNo) {
        Assert.state(orderDtlSize <= TestProduct.SIZE + 1, "Order detail size must less than test products size!");
        List<OmOdDtl> orderDetailList = new ArrayList<>();
        List<TestProduct> testProducts = randomOrderingProducts();
        for (int i = 0; i < orderDtlSize; i++) {
            TestProduct product = testProducts.get(i);
            OmOdDtl orderDetail = createTestOdDtl(memberNo, product);
            orderDetail.setOdSeq(i + 1);
            orderDetailList.add(orderDetail);
            //임시 취소데이터 생성
            if (orderDtlSize == 3 && i + 1 == 3) {
                OmOdDtl cancelDtl = new OmOdDtl();
                BeanUtils.copyProperties(orderDetail, cancelDtl);
                cancelDtl.setProcSeq(2);
                cancelDtl.setOrglProcSeq(1);
                cancelDtl.setOdTypCd("20");
                cancelDtl.setCnclQty(cancelDtl.getOdQty());
                orderDetailList.add(cancelDtl);
            }
        }
        return orderDetailList;
    }

    private static OmOdDtl createTestOdDtl(String memberNo, TestProduct product) {
        OmOdDtl orderDetail = new OmOdDtl();
        orderDetail.setProcSeq(1);
        orderDetail.setOdTypCd("10");
        orderDetail.setOdPrgsStepCd("01");
        orderDetail.setMbNo(memberNo);
        int randomOrderQuantity = ThreadLocalRandom.current().nextInt(1, 10);
        orderDetail.setOdQty(randomOrderQuantity);
        orderDetail.setSlPrc(randomOrderQuantity * product.getPrice());
        orderDetail.setDcAmt(0);
        orderDetail.setPdNo(product.getPdNo());
        orderDetail.setPdNm(product.getPdNm());
        return orderDetail;
    }

    private static List<TestProduct> randomOrderingProducts() {
        List<TestProduct> products = Arrays.asList(TestProduct.values());
        Collections.shuffle(products);
        return products;
    }

    private static List<OmOdFvrDtl> createTestOrderFavorDetails(List<OmOdDtl> orderDetails) {
        List<OmOdFvrDtl> favorDetails = new ArrayList<>();
        for (OmOdDtl orderDetail : orderDetails) {
            OmOdFvrDtl favorDetail = new OmOdFvrDtl();
            favorDetail.setOdSeq(orderDetail.getOdSeq());
            favorDetail.setProcSeq(orderDetail.getProcSeq());
            favorDetail.setOdFvrDvsCd("HAPN");
            favorDetail.setDcTnnoCd("1st");
            favorDetail.setAplyQty(ThreadLocalRandom.current().nextInt(1, orderDetail.getOdQty() + 1));
            favorDetail.setPrNo("1");
            favorDetail.setPrNm("임시프로모션");
            favorDetails.add(favorDetail);
            int favorAmount =  (orderDetail.getSlPrc() / orderDetail.getOdQty()) * favorDetail.getAplyQty() / 10;
            favorDetail.setFvrAmt(favorAmount);
            //임시 혜택금액 세팅
            orderDetail.setDcAmt(favorAmount);
        }
        return favorDetails;
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
